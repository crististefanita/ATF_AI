package com.ai.skills.mcp.validation.run.extent;

import com.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.ai.skills.mcp.McpPackageViolation;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Accessors(fluent = true)
public final class McpExtentReportMode {
    public enum Mode {
        RUNTIME_AUDITED,
        COMPONENT_ADAPTER,
        UNKNOWN
    }

    private static final String MODE_MARKER = "ATF_EXTENT_REPORT_MODE";

    private final Mode mode;

    private McpExtentReportMode(Mode mode) {
        this.mode = mode;
    }

    public static McpExtentReportMode fromHtml(String html) {
        if (html == null || html.isBlank()) {
            return new McpExtentReportMode(Mode.UNKNOWN);
        }
        String lower = html.toLowerCase(Locale.ROOT);
        if (lower.contains("data-atf-report-mode=\"runtime-audited\"")
                || lower.contains("\"mode\":\"runtime-audited\"")
                || lower.contains("atf_runtime_tag_audit_start")
                || lower.contains("atf runtime tag audit")) {
            return new McpExtentReportMode(Mode.RUNTIME_AUDITED);
        }
        if (lower.contains("data-atf-report-mode=\"component-adapter\"")
                || lower.contains("\"mode\":\"component-adapter\"")) {
            return new McpExtentReportMode(Mode.COMPONENT_ADAPTER);
        }
        return new McpExtentReportMode(Mode.UNKNOWN);
    }

    public List<McpPackageViolation> consistencyViolations(Path extentReport, String html) {
        List<McpPackageViolation> violations = new ArrayList<>();
        String lower = html == null ? "" : html.toLowerCase(Locale.ROOT);
        if (mode == Mode.RUNTIME_AUDITED && !containsRuntimeAudit(lower)) {
            violations.add(McpPackageViolation.blocker("run.extent.mode-runtime-audit.missing",
                    "Extent report declares runtime-audited mode but does not expose runtime audit sections",
                    extentReport));
        }
        if (lower.contains(MODE_MARKER.toLowerCase(Locale.ROOT))
                && mode == Mode.UNKNOWN) {
            violations.add(McpPackageViolation.blocker("run.extent.mode.unknown",
                    "Extent report mode marker is present but cannot be classified as runtime-audited or component-adapter",
                    extentReport));
        }
        return violations;
    }

    public List<McpPackageViolation> publishedValidationViolations(Path extentReport, String html) {
        List<McpPackageViolation> violations = new ArrayList<>(consistencyViolations(extentReport, html));
        ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportKind.MCP_CLOSEOUT)
                .forEach(message -> violations.add(McpPackageViolation.blocker(
                        "mcp.extent.published-overview.invalid",
                        "Final MCP Extent report is not publishable: " + message,
                        extentReport
                )));
        return List.copyOf(violations);
    }

    private static boolean containsRuntimeAudit(String lower) {
        return lower.contains("atf_runtime_tag_audit_start")
                || lower.contains("atf_runtime_failure_groups_start")
                || lower.contains("atf_core_final_audit_start")
                || lower.contains("atf runtime tag audit")
                || lower.contains("atf runtime failure groups");
    }
}
