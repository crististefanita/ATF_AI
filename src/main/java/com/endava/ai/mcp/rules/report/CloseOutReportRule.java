package com.endava.ai.mcp.rules.report;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@McpContract(id = "report.close-out.state-decision",
        category = McpRuleCategory.REPORT,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class CloseOutReportRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }
        String html = context.files().read(report, result);
        McpRuleSupport.requireText(html, report, result, "report.run-audit", "Missing run audit evidence",
                "Run Audit", "Run Evidence", "run_01", "run-01");
        McpRuleSupport.requireText(html, report, result, "report.close-out", "Missing closure truth",
                "Close-Out", "Close Out", "fresh ROOT_RUN", "same-package");
        requireRunAuditDistinctBusinessClaims(html, report, result);
        requireCloseOutConcreteNextBusinessObject(html, report, result);
        requireReportStateSync(context, html, report, result);
        requireDecisionTrace(context, html, report, result);
        ReportRuleSupport.requireArtifactFreshness(context, report, result);
        ReportRuleSupport.requireUnverifiableLanguageAbsent(html, report, result);
        ReportRuleSupport.requireForbiddenWordsAbsent(html, report, result);
    }

    private void requireRunAuditDistinctBusinessClaims(String html, Path report, McpPackageValidationResult result) {
        String runAudit = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Run Audit");
        Set<String> claims = new HashSet<>();
        Matcher matcher = Pattern.compile("(?is)<li\\b[^>]*>(.*?)</li>").matcher(runAudit);
        while (matcher.find()) {
            String claim = matcher.group(1).replaceAll("(?is)<[^>]+>", " ")
                    .replaceAll("\\s+", " ")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            claim = claim.replaceAll("^run[_-]?\\d+\\s*:\\s*", "");
            if (!claim.isBlank()) {
                claims.add(claim);
            }
        }
        if (claims.size() < 3) {
            result.add(McpPackageViolation.blocker("report.run-audit.claims-too-repetitive",
                    "Run Audit should expose distinct business claims, not repeated run bookkeeping. Distinct claims found: "
                            + claims.size(),
                    report));
        }
    }

    private void requireCloseOutConcreteNextBusinessObject(String html, Path report, McpPackageValidationResult result) {
        String closeOut = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Close-Out").toLowerCase(Locale.ROOT);
        boolean namesNextPressure = McpRuleSupport.containsAny(closeOut,
                "next", "remaining", "fresh", "not owned", "blocked", "open");
        boolean namesBusinessObjectShape = McpRuleSupport.containsAny(closeOut,
                "object", "entity", "item", "record", "field", "value", "id", "row",
                "state", "status", "message", "history", "screen", "element", "flow");
        if (!namesNextPressure || !namesBusinessObjectShape) {
            result.add(McpPackageViolation.blocker("report.close-out.next-object-missing",
                    "Close-Out should name a concrete next business object, not only generic continuation language",
                    report));
        }
    }

    private void requireReportStateSync(McpPackageContext context,
                                        String html,
                                        Path report,
                                        McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.extractIntValue(state, "overall");
        Integer runsCompleted = McpRuleSupport.extractIntValue(state, "runs_completed");
        Integer testsProduced = McpRuleSupport.extractIntValue(state, "tests_produced");
        String status = McpRuleSupport.extractStringValue(state, "status");
        String readmeText = context.files().readIfRegular(context.paths().readme(), result);
        String closeOutText = context.files().readIfRegular(context.paths().packageCloseOut(), result);
        String combined = (html + "\n" + readmeText + "\n" + closeOutText).toLowerCase(Locale.ROOT);
        if (overall != null && !combined.contains(String.valueOf(overall))) {
            result.add(McpPackageViolation.blocker("report-state.overall-missing",
                    "README/report/close-out must expose the package-state Overall value: " + overall,
                    packageState));
        }
        if (runsCompleted != null && !combined.contains(String.valueOf(runsCompleted))) {
            result.add(McpPackageViolation.blocker("report-state.runs-missing",
                    "README/report/close-out must expose runs_completed from package-state.json: " + runsCompleted,
                    packageState));
        }
        if (testsProduced != null && !combined.contains(String.valueOf(testsProduced))) {
            result.add(McpPackageViolation.blocker("report-state.tests-missing",
                    "README/report/close-out must expose tests_produced from package-state.json: " + testsProduced,
                    packageState));
        }
        if (status != null && !combined.contains(status.toLowerCase(Locale.ROOT))) {
            result.add(McpPackageViolation.warning("report-state.status-not-visible",
                    "README/report/close-out should expose package-state status: " + status,
                    packageState));
        }
    }

    private void requireDecisionTrace(McpPackageContext context, String html, Path report, McpPackageValidationResult result) {
        for (Path path : List.of(context.paths().executionGateCard(),
                context.paths().packageCloseOut(),
                context.packageRoot().resolve("review/next-run-eligibility-card.md"))) {
            String text = context.files().readIfRegular(path, result).toLowerCase(Locale.ROOT);
            if (text.isBlank()) {
                continue;
            }
            if (!McpRuleSupport.containsAny(text, "same-package", "same package", "fresh root", "fresh_root",
                    "continue", "stop", "blocked", "blocker", "next-run", "next run")) {
                result.add(McpPackageViolation.blocker("decision-trace.missing",
                        "Decision artifact must justify stop/continue/fresh-root/blocked state", path));
            }
        }
        String closeOut = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Close-Out").toLowerCase(Locale.ROOT);
        if (!McpRuleSupport.containsAny(closeOut, "same-package", "same package", "fresh root", "fresh_root",
                "continue", "stop", "blocked", "blocker", "next")) {
            result.add(McpPackageViolation.blocker("decision-trace.report-missing",
                    "Final report Close-Out must publish stop/continue/fresh-root decision truth", report));
        }
    }
}
