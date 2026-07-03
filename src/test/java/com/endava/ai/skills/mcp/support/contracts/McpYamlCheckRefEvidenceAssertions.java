package com.endava.ai.skills.mcp.support.contracts;

import com.endava.ai.skills.mcp.contracts.checks.McpCheckRefEvidenceRenderer;
import com.endava.ai.skills.mcp.contracts.governance.McpGovernanceMapEntry;
import com.endava.ai.skills.mcp.contracts.governance.McpGovernanceMapRenderer;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class McpYamlCheckRefEvidenceAssertions {
    private static final Path TARGET_MATRIX_MD = Path.of("target/mcp-check-ref-matrix.md");
    private static final Path TARGET_MATRIX_HTML = Path.of("target/mcp-check-ref-matrix.html");
    private static final Path TARGET_REPORT_SECTION = Path.of("target/mcp-check-ref-final-report-section.html");
    private static final Path TARGET_GOVERNANCE_MAP_MD = Path.of("target/mcp-governance-map.md");
    private static final Path TARGET_GOVERNANCE_MAP_HTML = Path.of("target/mcp-governance-map.html");

    private McpYamlCheckRefEvidenceAssertions() {
    }

    public static void assertEveryJavaMcpCheckRefAppearsInMatrixAndFinalReportSection() throws IOException {
        List<String> referencedIds = McpYamlCheckRefScanner.referencedCheckIds();
        String matrix = McpCheckRefEvidenceRenderer.renderMatrix(referencedIds);
        String htmlMatrix = McpCheckRefEvidenceRenderer.renderHtmlMatrix(referencedIds);
        String reportSection = McpCheckRefEvidenceRenderer.renderFinalReportSection(referencedIds);

        Files.writeString(TARGET_MATRIX_MD, matrix);
        Files.writeString(TARGET_MATRIX_HTML, htmlMatrix);
        Files.writeString(TARGET_REPORT_SECTION, reportSection);

        for (String id : referencedIds) {
            Assert.assertTrue(matrix.contains(id), "Markdown MCP CheckRef matrix must contain " + id);
            Assert.assertTrue(htmlMatrix.contains(id), "HTML MCP CheckRef matrix must contain " + id);
            Assert.assertTrue(reportSection.contains(id), "Final report CheckRef section must contain " + id);
        }
        Assert.assertTrue(matrix.contains("Failure meaning"), "Matrix must expose failure meaning.");
        Assert.assertTrue(matrix.contains("Repair hint"), "Matrix must expose repair hint.");
        Assert.assertTrue(htmlMatrix.contains("font-family:Segoe UI,Arial,sans-serif"),
                "HTML MCP CheckRef matrix must use the shared diagnostic report shell.");
        Assert.assertFalse(htmlMatrix.contains("<script id=\"ATF_EXTENT_REPORT_MODE\"")
                        || htmlMatrix.contains("data-atf-report-mode=")
                        || htmlMatrix.contains("class=\"container-fluid p-4 view dashboard-view"),
                "HTML MCP CheckRef matrix is a small diagnostic report, not an Extent/Spark shell.");
        Assert.assertTrue(reportSection.contains("mcp-check-ref-evidence"),
                "Final report section must use a stable anchor.");
        Assert.assertFalse(reportSection.contains("<!doctype html>"),
                "Final report CheckRef evidence must remain an embeddable section fragment.");
    }

    public static void assertMcpGovernanceMapConnectsTestsYamlArtifactsAndFinalReportEvidence()
            throws IOException {
        List<McpGovernanceMapEntry> entries = McpYamlCheckRefScanner.governanceEntries();
        String markdown = McpGovernanceMapRenderer.renderMarkdown(entries);
        String html = McpGovernanceMapRenderer.renderHtml(entries);

        Files.writeString(TARGET_GOVERNANCE_MAP_MD, markdown);
        Files.writeString(TARGET_GOVERNANCE_MAP_HTML, html);

        Assert.assertTrue(entries.size() >= 150,
                "MCP governance map should cover the YAML-backed MCP test surface.");
        Assert.assertTrue(markdown.contains("Check ref | Test location | YAML artifacts | Final report anchor"),
                "Governance map must expose check, test, artifact and report-link columns.");
        Assert.assertTrue(markdown.contains(McpGovernanceMapRenderer.FINAL_REPORT_ANCHOR),
                "Governance map must point to the final-report-ready CheckRef evidence section.");
        Assert.assertTrue(markdown.contains(McpGovernanceMapRenderer.ADVISOR_ANCHOR),
                "Governance map must point to the advisor final-report section.");
        Assert.assertTrue(markdown.contains("target/mcp-ai-task-advisor.md"),
                "Governance map must make the advisor artifact discoverable.");
        Assert.assertTrue(markdown.contains("src/main/resources/skills/mcp/contracts/atlas-quality-bridge.yml"),
                "Governance map must make the MCP to Atlas bridge contract discoverable.");
        Assert.assertTrue(markdown.contains("MCP to Atlas Quality Bridge"),
                "Governance map must explain the MCP final-report to Atlas business-quality bridge.");
        Assert.assertTrue(markdown.contains("MCP owns close-out evidence. Atlas owns business-meaning judgment."),
                "Governance map must keep MCP gate ownership separate from Atlas quality judgment.");
        Assert.assertTrue(markdown.contains("atlas.quality.business-map")
                        && markdown.contains("atlas.quality.source-trace")
                        && markdown.contains("atlas.quality.score-truth")
                        && markdown.contains("atlas.quality.graph-proof")
                        && markdown.contains("atlas.quality.validation-audit"),
                "Governance map must expose every Atlas quality lane used to calibrate final-report closeout.");
        Assert.assertTrue(html.contains(McpGovernanceMapRenderer.TITLE),
                "HTML governance map must have a stable title.");
        Assert.assertTrue(html.contains(McpGovernanceMapRenderer.ADVISOR_ANCHOR),
                "HTML governance map must embed the non-blocking advisor section.");
        Assert.assertTrue(html.contains("atlas.quality.business-map")
                        && html.contains("atlas.quality.validation-audit"),
                "HTML governance map must render the Atlas quality bridge table.");
        Assert.assertTrue(html.contains("font-family:Segoe UI,Arial,sans-serif"),
                "HTML governance map must use the shared diagnostic report shell.");
    }
}
