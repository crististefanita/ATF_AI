package com.ai.skills.mcp.contracts.governance;

import com.ai.core.support.html.HtmlTable;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.checks.McpCheckSpec;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorRenderer;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorSpec;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the agent-facing map from executable MCP tests to YAML contract text
 * and package evidence.
 */
public final class McpGovernanceMapRenderer {
    public static final String TITLE = "MCP Governance Map";
    public static final String FINAL_REPORT_ANCHOR = "mcp-check-ref-evidence";
    public static final String ADVISOR_ANCHOR = McpAiTaskAdvisorRenderer.SECTION_ANCHOR;
    private static final String DESCRIPTION =
            "Landing artifact generated from @McpCheckRef usages, YAML contracts and MCP advisor guidance.";

    private McpGovernanceMapRenderer() {
    }

    public static String renderMarkdown(Collection<McpGovernanceMapEntry> entries) {
        StringBuilder markdown = new StringBuilder()
                .append("# ").append(TITLE).append(System.lineSeparator()).append(System.lineSeparator())
                .append(DESCRIPTION)
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Landing artifact | Purpose")
                .append(System.lineSeparator())
                .append("--- | ---")
                .append(System.lineSeparator())
                .append("target/mcp-governance-map.md | Contract to test to artifact to final-report routing")
                .append(System.lineSeparator())
                .append("target/mcp-ai-task-advisor.md | INFO/WARN guidance for the next agentic run")
                .append(System.lineSeparator())
                .append("src/main/resources/skills/mcp/contracts/atlas-quality-bridge.yml | MCP final report to Atlas business-quality calibration")
                .append(System.lineSeparator())
                .append("target/mcp-check-ref-matrix.md | Full YAML-backed @McpCheckRef evidence")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Check ref | Test location | YAML artifacts | Final report anchor | Repair hint")
                .append(System.lineSeparator())
                .append("--- | --- | --- | --- | ---")
                .append(System.lineSeparator());
        for (McpGovernanceMapEntry entry : sorted(entries)) {
            McpCheckSpec spec = ContractCatalog.requireMcpCheck(entry.checkRef());
            markdown.append(HtmlText.markdownCell(spec.id())).append(" | ")
                    .append(HtmlText.markdownCell(entry.testLocation())).append(" | ")
                    .append(HtmlText.markdownCell(String.join(", ", spec.artifacts()))).append(" | ")
                    .append(FINAL_REPORT_ANCHOR).append(" | ")
                    .append(HtmlText.markdownCell(spec.repairHint()))
                    .append(System.lineSeparator());
        }
        appendAtlasBridgeMarkdown(markdown, ContractCatalog.atlasQualityBridgeContracts());
        appendAdvisorMarkdown(markdown, ContractCatalog.aiTaskAdvisorContracts());
        return markdown.toString();
    }

    public static String renderHtml(Collection<McpGovernanceMapEntry> entries) {
        HtmlTable table = HtmlTable.withHeadings(
                "Check ref", "Test location", "YAML artifacts", "Final report anchor", "Repair hint");
        for (McpGovernanceMapEntry entry : sorted(entries)) {
            McpCheckSpec spec = ContractCatalog.requireMcpCheck(entry.checkRef());
            table.row(
                    HtmlTable.Cell.code(spec.id()),
                    HtmlTable.Cell.code(entry.testLocation()),
                    HtmlTable.Cell.code(String.join(", ", spec.artifacts())),
                    HtmlTable.Cell.code(FINAL_REPORT_ANCHOR),
                    HtmlTable.Cell.text(spec.repairHint()));
        }
        String body = "<h1>" + HtmlText.html(TITLE) + "</h1>"
                + "<p>" + HtmlText.html(DESCRIPTION) + "</p>"
                + landingArtifactsTable().render()
                + atlasBridgeTable(ContractCatalog.atlasQualityBridgeContracts()).render()
                + table.render();
        return HtmlText.diagnosticDocument(TITLE,
                body + McpAiTaskAdvisorRenderer.renderFinalReportSection(ContractCatalog.aiTaskAdvisorContracts()));
    }

    private static List<McpGovernanceMapEntry> sorted(Collection<McpGovernanceMapEntry> entries) {
        return entries.stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    private static HtmlTable landingArtifactsTable() {
        return HtmlTable.withHeadings("Landing artifact", "Purpose")
                .row(HtmlTable.Cell.code("target/mcp-governance-map.md"),
                        HtmlTable.Cell.text("Contract to test to artifact to final-report routing"))
                .row(HtmlTable.Cell.code("target/mcp-ai-task-advisor.md"),
                        HtmlTable.Cell.text("INFO/WARN guidance for the next agentic run"))
                .row(HtmlTable.Cell.code("src/main/resources/skills/mcp/contracts/atlas-quality-bridge.yml"),
                        HtmlTable.Cell.text("MCP final report to Atlas business-quality calibration"))
                .row(HtmlTable.Cell.code("target/mcp-check-ref-matrix.md"),
                        HtmlTable.Cell.text("Full YAML-backed @McpCheckRef evidence"));
    }

    private static HtmlTable atlasBridgeTable(Collection<McpGovernanceMapEntry.AtlasQualityBridgeSpec> specs) {
        HtmlTable table = HtmlTable.withHeadings(
                "MCP profile", "Atlas lane", "Final report surface", "MCP responsibility", "Atlas responsibility");
        for (McpGovernanceMapEntry.AtlasQualityBridgeSpec spec : sortedBridge(specs)) {
            table.row(
                    HtmlTable.Cell.code(spec.mcpProfileId()),
                    HtmlTable.Cell.code(spec.atlasLane()),
                    HtmlTable.Cell.text(spec.finalReportSurface()),
                    HtmlTable.Cell.text(spec.mcpResponsibility()),
                    HtmlTable.Cell.text(spec.atlasResponsibility()));
        }
        return table;
    }

    private static void appendAtlasBridgeMarkdown(
            StringBuilder markdown,
            Collection<McpGovernanceMapEntry.AtlasQualityBridgeSpec> specs) {
        markdown.append(System.lineSeparator())
                .append("## MCP to Atlas Quality Bridge")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("MCP owns close-out evidence. Atlas owns business-meaning judgment.")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("MCP profile | Atlas lane | Final report surface | MCP responsibility | Atlas responsibility")
                .append(System.lineSeparator())
                .append("--- | --- | --- | --- | ---")
                .append(System.lineSeparator());
        for (McpGovernanceMapEntry.AtlasQualityBridgeSpec spec : sortedBridge(specs)) {
            markdown.append(HtmlText.markdownCell(spec.mcpProfileId())).append(" | ")
                    .append(HtmlText.markdownCell(spec.atlasLane())).append(" | ")
                    .append(HtmlText.markdownCell(spec.finalReportSurface())).append(" | ")
                    .append(HtmlText.markdownCell(spec.mcpResponsibility())).append(" | ")
                    .append(HtmlText.markdownCell(spec.atlasResponsibility()))
                    .append(System.lineSeparator());
        }
    }

    private static List<McpGovernanceMapEntry.AtlasQualityBridgeSpec> sortedBridge(
            Collection<McpGovernanceMapEntry.AtlasQualityBridgeSpec> specs) {
        return specs.stream()
                .sorted(Comparator.comparing(McpGovernanceMapEntry.AtlasQualityBridgeSpec::id))
                .collect(Collectors.toUnmodifiableList());
    }

    private static void appendAdvisorMarkdown(StringBuilder markdown, Collection<McpAiTaskAdvisorSpec> specs) {
        markdown.append(System.lineSeparator())
                .append("## MCP AI Task Advisor")
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Final report anchor: ").append(ADVISOR_ANCHOR)
                .append(System.lineSeparator()).append(System.lineSeparator())
                .append("Severity | Advisor | Recommendation")
                .append(System.lineSeparator())
                .append("--- | --- | ---")
                .append(System.lineSeparator());
        for (McpAiTaskAdvisorSpec spec : specs.stream()
                .sorted((left, right) -> left.id().compareTo(right.id()))
                .collect(Collectors.toUnmodifiableList())) {
            markdown.append(HtmlText.markdownCell(spec.severity())).append(" | ")
                    .append(HtmlText.markdownCell(spec.id())).append(" | ")
                    .append(HtmlText.markdownCell(spec.recommendation()))
                    .append(System.lineSeparator());
        }
    }
}
