package com.ai.skills.mcp.rules.packageaudit.closeout;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class McpCloseoutReportDisclosure {
    private McpCloseoutReportDisclosure() {
    }

    static void validateSummary(McpPackageContext context,
                                McpPackageValidationResult result,
                                String gateText) {
        McpCloseoutCounts counts = McpCloseoutCounts.fromGate(gateText);
        if (!counts.complete()) {
            return;
        }
        Path finalReport = context.paths().report();
        if (!Files.isRegularFile(finalReport)) {
            result.add(McpPackageViolation.blocker("report.mcp-closeout-summary.missing",
                    "Final report must publish the MCP package-closeout profile and pass counts before close-out",
                    finalReport));
            return;
        }
        String lower = safeRead(finalReport).toLowerCase(Locale.ROOT);
        if (!hasVisibleCloseoutSummary(lower, counts)) {
            result.add(McpPackageViolation.blocker("report.mcp-closeout-summary.missing",
                    McpArtifactPaths.FINAL_REPORT + " must state the final MCP subcollection "
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName() + "/"
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.id()
                            + " and publish total/passed plus 0 failures/0 errors",
                    finalReport));
        }
        validateAuditedMavenCommand(context, result, finalReport);
    }

    static void validateSkippedSummary(McpPackageContext context,
                                       McpPackageValidationResult result,
                                       Integer skipped) {
        Path finalReport = context.paths().report();
        if (!Files.isRegularFile(finalReport)) {
            result.add(McpPackageViolation.blocker("report.mcp-skipped-explanation.missing",
                    "Final report must explain accepted MCP skipped tests when "
                            + McpArtifactPaths.MCP_EXTENT_REPORT + " contains skips",
                    finalReport));
            return;
        }
        String lower = safeRead(finalReport).toLowerCase(Locale.ROOT);
        boolean namesMcpSkips = lower.contains("mcp")
                && (lower.contains("skipped") || lower.contains("skip"))
                && (skipped == null || lower.contains(String.valueOf(skipped)));
        boolean explainsReason = McpRuleSupport.containsAny(lower,
                "reason",
                "profile/precondition",
                "precondition",
                "profile-gated",
                "profile gated",
                "skipped profile");
        boolean publishesAcceptance = lower.contains("accepted=true")
                || lower.contains("accepted = true")
                || (lower.contains("accepted") && lower.contains("close-out"));
        boolean publishesCap = McpRuleSupport.containsAny(lower, "cap", "capped", "score_cap", "plafon");
        boolean linksLedger = lower.contains("full-mcp-method-ledger.md");
        if (!namesMcpSkips || !explainsReason || !publishesAcceptance || !publishesCap || !linksLedger) {
            result.add(McpPackageViolation.blocker("report.mcp-skipped-explanation.missing",
                    McpArtifactPaths.FINAL_REPORT
                            + " must publish MCP skipped count, reason, accepted=true, score cap and "
                            + fileName(McpArtifactPaths.MCP_METHOD_LEDGER)
                            + " link when final MCP evidence has skipped tests",
                    finalReport));
        }
    }

    private static boolean hasVisibleCloseoutSummary(String lower, McpCloseoutCounts counts) {
        boolean namesCloseoutProfile = lower.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName())
                || lower.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.id())
                || lower.contains("package close-out");
        boolean publishesTotal = counts.total == null || lower.contains(String.valueOf(counts.total));
        boolean publishesPassed = counts.passed == null || lower.contains(String.valueOf(counts.passed));
        boolean publishesAllPassed = lower.contains("all passed")
                || lower.contains("all tests")
                || lower.contains(counts.passed + "/" + counts.total)
                || lower.contains(counts.passed + " / " + counts.total)
                || lower.contains(counts.total + "/" + counts.total)
                || lower.contains(counts.total + " / " + counts.total);
        boolean publishesNoFailuresOrErrors = McpRuleSupport.containsAny(lower,
                "zero failures/errors",
                "zero failures and errors",
                "0 failures/errors",
                "0 failures and errors",
                "failures=0, errors=0",
                "failures=0 errors=0",
                "failures: 0, errors: 0");
        boolean publishesZeroFailures = counts.failures == null || counts.failures == 0
                ? publishesNoFailuresOrErrors
                || McpRuleSupport.containsAny(lower, "0 failures", "zero failures", "failures: 0", "failures=0")
                : lower.contains(String.valueOf(counts.failures));
        boolean publishesZeroErrors = counts.errors == null || counts.errors == 0
                ? publishesNoFailuresOrErrors
                || McpRuleSupport.containsAny(lower, "0 errors", "zero errors", "errors: 0", "errors=0")
                : lower.contains(String.valueOf(counts.errors));
        return lower.contains("mcp") && namesCloseoutProfile && publishesTotal && publishesPassed
                && publishesAllPassed && publishesZeroFailures && publishesZeroErrors;
    }

    private static void validateAuditedMavenCommand(McpPackageContext context,
                                                    McpPackageValidationResult result,
                                                    Path finalReport) {
        Path origin = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN);
        if (!Files.isRegularFile(origin) || !Files.isRegularFile(finalReport)) {
            return;
        }
        String originText = safeRead(origin);
        String command = McpCloseoutText.jsonString(originText, "command");
        if (command == null || command.isBlank() || !McpCloseoutCommandProfile.hasFinalCloseoutCommand(command)) {
            return;
        }

        String report = normalizeReportText(safeRead(finalReport));
        List<String> missing = requiredCommandTokens(command).stream()
                .filter(token -> !report.contains(token))
                .collect(Collectors.toList());
        boolean linksOrigin = hasHrefTo(report, "../" + McpArtifactPaths.MCP_EXTENT_ORIGIN)
                || hasHrefTo(report, McpArtifactPaths.MCP_EXTENT_ORIGIN);
        boolean namesCommand = report.contains("maven command")
                || report.contains("mcp command")
                || report.contains("command:");
        boolean namesExtent = report.contains("extentreport.html");
        if (!linksOrigin || !namesCommand || !namesExtent || !missing.isEmpty()) {
            result.add(McpPackageViolation.blocker("report.mcp-origin-command.missing",
                    McpArtifactPaths.FINAL_REPORT + " must hyperlink " + McpArtifactPaths.MCP_EXTENT_ORIGIN
                            + " and visibly publish a sanitized Maven command for "
                            + McpArtifactPaths.MCP_EXTENT_REPORT + "; "
                            + fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN)
                            + " remains the exact absolute command source. "
                            + "Missing/weak tokens: originLink=" + linksOrigin
                            + ", commandLabel=" + namesCommand
                            + ", extentLabel=" + namesExtent
                            + ", commandTokens=" + missing,
                    finalReport));
        }
    }

    private static List<String> requiredCommandTokens(String command) {
        String normalized = normalizeReportText(command);
        List<String> tokens = new java.util.ArrayList<>();
        tokens.add("mvn");
        tokens.add(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName());
        tokens.add("mcp.package.root");
        if (normalized.contains("mcp.project.root")) {
            tokens.add("mcp.project.root");
        }
        return tokens;
    }

    private static boolean hasHrefTo(String report, String target) {
        return report.contains("href=\"" + target + "\"")
                || report.contains("href='" + target + "'");
    }

    private static String fileName(String artifactPath) {
        int slash = artifactPath.lastIndexOf('/');
        return slash >= 0 ? artifactPath.substring(slash + 1) : artifactPath;
    }

    private static String normalizeReportText(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace("&quot;", "\"")
                .replace("&#34;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("-d", "d")
                .replaceAll("\\s+", " ");
    }

    private static String safeRead(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.readString(path) : "";
        } catch (IOException exception) {
            return "";
        }
    }
}

