package com.ai.skills.mcp.rules.packageaudit.gate;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class McpExtentReportSurfaceEvidence {
    private McpExtentReportSurfaceEvidence() {
    }

    public static void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        if (!Files.isRegularFile(report)) {
            return;
        }
        try {
            String html = Files.readString(report);
            String lower = html.toLowerCase(Locale.ROOT);
            if (Files.size(report) < 50_000) {
                result.add(McpPackageViolation.blocker("mcp.extent-report.incomplete",
                        "Frozen MCP Extent report is too small to be the package close-out report", report));
            }
            if (!McpRuleSupport.containsAny(lower, "mcp", "mcpgeneratedpackagevalidator", "mcp contract")) {
                result.add(McpPackageViolation.blocker("mcp.extent-report.incomplete",
                        "Frozen MCP Extent report does not identify the MCP contract suite", report));
            }
            if (!McpRuleSupport.containsAny(lower, "text-pass", "status=\"pass\"", "pass-bg", ">pass<")) {
                result.add(McpPackageViolation.blocker("mcp.extent-report.incomplete",
                        "Frozen MCP Extent report does not expose passed MCP test results", report));
            }
            validateDashboardSurface(html, report, result);
            long mcpTokens = Pattern.compile("(?i)\\bmcp").matcher(html).results().count();
            long passTokens = Pattern.compile("(?i)\\bpass\\b").matcher(html).results().count();
            long testTokens = Pattern.compile("(?i)\\btest").matcher(html).results().count();
            if (mcpTokens < 10 || passTokens < 10 || testTokens < 25) {
                result.add(McpPackageViolation.blocker("mcp.extent-report.incomplete",
                        "Frozen MCP Extent report lacks enough suite/result surface: mcpTokens="
                                + mcpTokens + ", passTokens=" + passTokens + ", testTokens=" + testTokens,
                        report));
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("mcp.extent-report.unreadable",
                    "Cannot read frozen MCP Extent report: " + exception.getMessage(), report));
        }
    }

    private static void validateDashboardSurface(String html,
                                                 Path report,
                                                 McpPackageValidationResult result) {
        String lower = html.toLowerCase(Locale.ROOT);
        if (!lower.contains("nav-dashboard") && !lower.contains("dashboard-view")) {
            result.add(McpPackageViolation.blocker("mcp.extent-dashboard.missing",
                    "Frozen MCP Extent report must expose the Spark dashboard/overview surface, not only the test list",
                    report));
            return;
        }
        if (!McpRuleSupport.containsAny(lower,
                "passparent",
                "statusgroup",
                "tests run",
                "passed",
                "failed",
                "skipped")) {
            result.add(McpPackageViolation.blocker("mcp.extent-dashboard.incomplete",
                    "Frozen MCP Extent dashboard/overview must publish visible result accounting", report));
        }
    }
}
