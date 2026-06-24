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
import java.util.List;

@McpContract(id = "report.business-graph.canonical",
        category = McpRuleCategory.REPORT,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class BusinessGraphReportRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }
        String html = context.files().read(report, result);
        McpRuleSupport.requireText(html, report, result, "report.graph", "Missing Business Flow Graph", "Business Flow Graph");
        McpRuleSupport.requireText(html, report, result, "report.legend", "Missing graph legend", "Legend");
        requireBusinessGraphDiagram(html, report, result);
        requireCollapsibleReportSections(html, report, result);
        requireOpenBusinessGraph(html, report, result);
    }

    private void requireBusinessGraphDiagram(String html, Path report, McpPackageValidationResult result) {
        String graph = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Business Flow Graph");
        McpRuleSupport.requireText(graph, report, result, "report.graph.diagram",
                "Business Flow Graph must contain SVG or diagram container", "<svg", "class=\"diag\"", "class='diag'");
        McpRuleSupport.requireText(graph, report, result, "report.graph.accessible",
                "Business Flow Graph must expose an accessible diagram label", "role=\"img\"", "aria-label");
        McpRuleSupport.requireText(graph, report, result, "report.graph.nodes",
                "Business Flow Graph must contain visual nodes or edges", "<rect", "<path", "<circle");
        if (ReportRuleSupport.SECTIONS.stripTags(graph).contains("->") && !graph.contains("<svg")) {
            result.add(McpPackageViolation.blocker("report.graph.text-chain",
                    "Business Flow Graph is still a text arrow chain instead of a rendered graph", report));
        }
    }

    private void requireCollapsibleReportSections(String html, Path report, McpPackageValidationResult result) {
        for (String required : List.of(
                "Business Flow Graph", "Score Overview", "Proof Mapping", "Run Audit", "Artifact Index", "Close-Out")) {
            if (ReportRuleSupport.SECTIONS.detailsBlockBody(html, required).isBlank()) {
                result.add(McpPackageViolation.blocker("report.details.section-missing",
                        "Main report section must be a collapsible <details>: " + required, report));
            }
        }
    }

    private void requireOpenBusinessGraph(String html, Path report, McpPackageValidationResult result) {
        if (!ReportRuleSupport.SECTIONS.isDetailsOpen(html, "Business Flow Graph")) {
            result.add(McpPackageViolation.blocker("report.graph.default-open",
                    "Business Flow Graph must be the default-open report section", report));
        }
        if (ReportRuleSupport.SECTIONS.openDetailsCount(html) > 1) {
            result.add(McpPackageViolation.warning("report.too-many-open-sections",
                    "Only Business Flow Graph should be open by default", report));
        }
    }
}
