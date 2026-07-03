package com.ai.skills.mcp.rules.candidate.html;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.html.HtmlDetailsParser;
import com.ai.skills.mcp.html.HtmlSectionFinder;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@McpContract(id = "candidate.business-evidence.trace",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "state -> diff -> test -> proof -> graph -> score")
public final class HtmlExampleCandidateEvidenceTraceRule implements McpValidationRule {
    private static final HtmlSectionFinder SECTIONS = new HtmlSectionFinder();
    private static final HtmlDetailsParser DETAILS = new HtmlDetailsParser();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }

        String state = context.files().read(packageState, result);
        String businessObject = McpRuleSupport.firstStringValue(state,
                "business_object", "owned_business_object", "business_object_owned");
        Set<String> tokens = meaningfulTokens(businessObject);
        if (tokens.isEmpty()) {
            result.add(McpPackageViolation.blocker("candidate.business-object.missing",
                    "html-ex candidate must publish a concrete business_object in package-state.json",
                    packageState));
            return;
        }

        requireReportTrace(context, result, tokens, packageState);
        HtmlExampleCandidateProfile profile = HtmlExampleCandidateProfile.from(context);
        requireRunArtifactTrace(context, result, tokens, McpRunArtifacts.STATE,
                profile.minimumTraceRunStates(),
                "candidate.business-object.run-state-trace-too-thin",
                "run-state files");
        requireRunArtifactTrace(context, result, tokens, McpRunArtifacts.DIFF,
                profile.minimumTraceRunDiffs(),
                "candidate.business-object.run-diff-trace-too-thin",
                "run diff files");
    }

    private void requireReportTrace(McpPackageContext context,
                                    McpPackageValidationResult result,
                                    Set<String> tokens,
                                    Path source) {
        String reportHtml = context.files().readIfRegular(context.paths().report(), result);
        String report = normalize(reportHtml);
        String businessGraph = normalize(SECTIONS.detailsBlockBody(reportHtml, "Business Flow Graph"));
        String proofMapping = SECTIONS.detailsBlockBody(reportHtml, "Proof Mapping");
        String runAudit = normalize(SECTIONS.detailsBlockBody(reportHtml, "Run Audit"));

        boolean reportHasToken = tokens.stream().anyMatch(report::contains);
        boolean reportHasProofMapping = report.contains("proof mapping");
        boolean reportHasRunAudit = report.contains("run audit");

        if (!reportHasToken || !reportHasProofMapping || !reportHasRunAudit) {
            result.add(McpPackageViolation.blocker("candidate.business-object.report-trace-incomplete",
                    "html-ex candidate business_object must appear in final report with Proof Mapping and Run Audit context. Tokens: "
                            + tokens,
                    source));
        }
        if (businessGraph.isBlank() || tokens.stream().noneMatch(businessGraph::contains)) {
            result.add(McpPackageViolation.blocker("candidate.business-object.graph-trace-missing",
                    "html-ex candidate business_object must appear in the Business Flow Graph, not only in prose. Tokens: "
                            + tokens,
                    context.paths().report()));
        }
        if (runAudit.isBlank() || tokens.stream().noneMatch(runAudit::contains)) {
            result.add(McpPackageViolation.blocker("candidate.business-object.run-audit-trace-missing",
                    "html-ex candidate business_object must appear in Run Audit so the reader can reopen which run carried it. Tokens: "
                            + tokens,
                    context.paths().report()));
        }
        requireProofMappingRow(context, result, proofMapping, tokens);
    }

    private void requireProofMappingRow(McpPackageContext context,
                                        McpPackageValidationResult result,
                                        String proofMapping,
                                        Set<String> tokens) {
        boolean hasCandidateRow = DETAILS.tableRowsOrListItems(proofMapping).stream()
                .map(this::visibleRow)
                .filter(row -> row.length() >= 20)
                .filter(row -> !row.contains("<th"))
                .anyMatch(row -> tokens.stream().anyMatch(row::contains)
                        && hasDiffEvidence(row)
                        && hasStateEvidence(row)
                        && hasExecutionEvidence(row)
                        && hasColdVerdict(row));

        if (!hasCandidateRow) {
            result.add(McpPackageViolation.blocker("candidate.business-object.proof-row-missing",
                    "html-ex candidate business_object must have a Proof Mapping row with business token + diff + state + execution proof + cold verdict. Tokens: "
                            + tokens,
                    context.paths().report()));
        }
    }

    private void requireRunArtifactTrace(McpPackageContext context,
                                         McpPackageValidationResult result,
                                         Set<String> tokens,
                                         String relativeArtifact,
                                         int minimumMatches,
                                         String code,
                                         String label) {
        long matches = context.files().runDirectories(context.paths().runsRoot(), result).stream()
                .map(runDirectory -> normalize(context.files().readIfRegular(runDirectory.resolve(relativeArtifact), result)))
                .filter(text -> tokens.stream().anyMatch(text::contains))
                .count();

        if (matches < minimumMatches) {
            result.add(McpPackageViolation.blocker(code,
                    "html-ex candidate business_object must be traceable in at least " + minimumMatches
                            + " " + label + "; actual=" + matches + "; tokens=" + tokens,
                    context.paths().runsRoot()));
        }
    }

    private Set<String> meaningfulTokens(String value) {
        if (value == null) {
            return Set.of();
        }
        Set<String> stopWords = Set.of("state", "object", "business", "frontier", "memory", "owned", "real");
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 5)
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private String visibleRow(String row) {
        return normalize(SECTIONS.stripTags(row));
    }

    private boolean hasDiffEvidence(String row) {
        return McpRuleSupport.containsAny(row, "thin-no-index.diff", "root-source-thin-no-index.diff");
    }

    private boolean hasStateEvidence(String row) {
        return McpRuleSupport.containsAny(row, "run-state.json", "package-state.json");
    }

    private boolean hasExecutionEvidence(String row) {
        return McpRuleSupport.containsAny(row,
                "extentreport",
                "validation/reports",
                "testng-results.xml",
                "command-result.md",
                "test-");
    }

    private boolean hasColdVerdict(String row) {
        return McpRuleSupport.containsAny(row, "verdict", "owned", "countable", "partial", "blocked", "capped");
    }
}
