package com.ai.skills.mcp.rules.report;

import com.ai.skills.business.parse.BusinessUnderstandingArtifactParser;
import com.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

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
        requireSharedBusinessUnderstandingSemantics(html, report, result);
        requireBusinessGraphDiagram(html, report, result);
        requireCollapsibleReportSections(html, report, result);
        requireOpenBusinessGraph(html, report, result);
    }

    private void requireSharedBusinessUnderstandingSemantics(String html,
                                                             Path report,
                                                             McpPackageValidationResult result) {
        BusinessUnderstandingArtifactParser.ArtifactSummary summary =
                BusinessUnderstandingArtifactParser.parseHtml(
                        BusinessUnderstandingInputType.FINAL_BUSINESS_REPORT,
                        report.toString(),
                        html);
        if (!summary.hasConcepts(Set.of(
                BusinessUnderstandingConcept.BUSINESS_FLOW_STORY,
                BusinessUnderstandingConcept.PROOF_CHAIN,
                BusinessUnderstandingConcept.SCORE_RATIONALE))) {
            result.add(McpPackageViolation.blocker("report.business-understanding.shared-semantics",
                    "LangGraph final report must reduce to shared Business Understanding semantics: "
                            + "business flow story, proof chain and score rationale",
                    report));
        }
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
