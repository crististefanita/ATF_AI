package com.endava.ai.skills.mcp.contracts.execution;

import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.support.config.TestExecutionConfigKeys;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpProjectLocator;
import com.endava.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpCloseoutEvidence {
    public static final String PACKAGE_ROOT_PLACEHOLDER = "<package-root>";
    public static final String PROJECT_ROOT_PLACEHOLDER = "<project-root>";

    private final McpCloseoutArtifactSpec artifactSpec;

    private McpCloseoutEvidence(McpCloseoutArtifactSpec artifactSpec) {
        this.artifactSpec = Objects.requireNonNull(artifactSpec, "artifactSpec");
    }

    public static McpCloseoutEvidence from(McpCloseoutArtifactSpec artifactSpec) {
        return new McpCloseoutEvidence(artifactSpec);
    }

    public McpExecutionProfile profile() {
        return McpExecutionProfile.PACKAGE_CLOSEOUT;
    }

    public McpExecutionProfile postSyncProfile() {
        return McpExecutionProfile.POST_SYNC_SMOKE;
    }

    public McpExecutionProfile finalReportProfile() {
        return McpExecutionProfile.FINAL_REPORT_CLOSEOUT;
    }

    public McpExecutionProfile langgraphFinalReportProfile() {
        return McpExecutionProfile.LANGGRAPH_FINAL_REPORT;
    }

    public List<McpArtifactFamily> artifactFamilies() {
        return List.of(
                McpArtifactFamily.EXTENT,
                McpArtifactFamily.TESTNG_XML,
                McpArtifactFamily.GATE,
                McpArtifactFamily.METHOD_LEDGER,
                McpArtifactFamily.KPI,
                McpArtifactFamily.SOURCE_EVOLUTION);
    }

    public List<McpCloseoutEvidenceStep> closeoutPlan() {
        return List.of(
                McpCloseoutEvidenceStep.of(
                        "package-closeout",
                        profile(),
                        "Generate the primary final MCP Extent/XML/gate/ledger/KPI evidence with explicit package and project roots.",
                        packageCloseoutCommandTemplate(),
                        artifactFamilies(),
                        McpArtifactPaths.MCP_VALIDATION_ROOT),
                McpCloseoutEvidenceStep.of(
                        "post-sync-smoke",
                        postSyncProfile(),
                        "Verify copied or regenerated frozen MCP artifacts still agree after synchronization.",
                        postSyncCommandTemplate(),
                        List.of(McpArtifactFamily.EXTENT, McpArtifactFamily.TESTNG_XML, McpArtifactFamily.GATE),
                        McpArtifactPaths.MCP_VALIDATION_ROOT + " + " + McpArtifactPaths.FINAL_REPORT),
                McpCloseoutEvidenceStep.of(
                        "final-report-closeout",
                        finalReportProfile(),
                        "Verify the final report surface links and summarizes the frozen MCP close-out evidence.",
                        finalReportCommandTemplate(),
                        List.of(McpArtifactFamily.FINAL_REPORT, McpArtifactFamily.EXTENT, McpArtifactFamily.TESTNG_XML,
                                McpArtifactFamily.GATE, McpArtifactFamily.KPI, McpArtifactFamily.SOURCE_EVOLUTION),
                        McpArtifactPaths.FINAL_REPORT),
                McpCloseoutEvidenceStep.of(
                        "langgraph-final-report",
                        langgraphFinalReportProfile(),
                        "Validate analysis/langgraph-business-understanding.html business structure, graph, score truth and proof mapping after report surface close-out.",
                        langgraphFinalReportCommandTemplate(),
                        List.of(McpArtifactFamily.FINAL_REPORT, McpArtifactFamily.KPI, McpArtifactFamily.SOURCE_EVOLUTION),
                        McpArtifactPaths.FINAL_REPORT));
    }

    public String closeoutPlanMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("| Step | Profile | Purpose | Artifact families | Repair target |")
                .append(System.lineSeparator());
        markdown.append("| --- | --- | --- | --- | --- |").append(System.lineSeparator());
        for (McpCloseoutEvidenceStep step : closeoutPlan()) {
            markdown.append("| ")
                    .append(step.id()).append(" | ")
                    .append(step.profile().id()).append(" | ")
                    .append(step.purpose()).append(" | ")
                    .append(step.artifactFamilyNames()).append(" | ")
                    .append(step.repairTarget()).append(" |")
                    .append(System.lineSeparator());
        }
        return markdown.toString();
    }

    public String packageCloseoutCommandTemplate() {
        return "mvn -q "
                + property(TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES, profile().suiteXml()) + " "
                + property(McpProjectLocator.PACKAGE_ROOT_PROPERTY, PACKAGE_ROOT_PLACEHOLDER) + " "
                + property(McpProjectLocator.PROJECT_ROOT_PROPERTY, PROJECT_ROOT_PLACEHOLDER) + " "
                + property(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT) + " "
                + property(ReportingConfigKeys.REPORTS_DIR, PACKAGE_ROOT_PLACEHOLDER + "/"
                        + McpArtifactPaths.MCP_VALIDATION_ROOT) + " "
                + property(ReportingConfigKeys.REPORTS_FILE_NAME, ExtentReportFiles.STABLE_REPORT_FILE_NAME) + " "
                + property(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED) + " "
                + "test";
    }

    public String postSyncCommandTemplate() {
        return "mvn -q "
                + property(TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES, postSyncProfile().suiteXml()) + " "
                + property(McpProjectLocator.PACKAGE_ROOT_PROPERTY, PACKAGE_ROOT_PLACEHOLDER) + " "
                + property(McpProjectLocator.PROJECT_ROOT_PROPERTY, PROJECT_ROOT_PLACEHOLDER) + " "
                + "test";
    }

    public String finalReportCommandTemplate() {
        return "mvn -q "
                + property(TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES, finalReportProfile().suiteXml()) + " "
                + property(McpProjectLocator.PACKAGE_ROOT_PROPERTY, PACKAGE_ROOT_PLACEHOLDER) + " "
                + property(McpProjectLocator.PROJECT_ROOT_PROPERTY, PROJECT_ROOT_PLACEHOLDER) + " "
                + "test";
    }

    public String langgraphFinalReportCommandTemplate() {
        return "mvn -q "
                + property(TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES, langgraphFinalReportProfile().suiteXml()) + " "
                + property(McpProjectLocator.PACKAGE_ROOT_PROPERTY, PACKAGE_ROOT_PLACEHOLDER) + " "
                + property(McpProjectLocator.PROJECT_ROOT_PROPERTY, PROJECT_ROOT_PLACEHOLDER) + " "
                + "test";
    }

    private static String property(String key, String value) {
        return "-D" + key + "=" + value;
    }
}
