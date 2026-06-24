package com.endava.ai.mcp.validation.graph;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpGraphTemplateProfile;
import com.endava.ai.mcp.validation.profile.McpReportSections;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class McpBusinessGraphAssertions {
    private final McpPackageAuditValidation audit;

    public McpBusinessGraphAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertBusinessFlowGraphIsRenderedAsDiagram() throws IOException {
        List<String> blockers = graphProfile().diagramBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph rendered diagram", blockers, audit.finalReport(),
                "report.graph.diagram.missing",
                audit.details(
                        "Asserts: Business Flow Graph is a visual application map with diagram nodes, not only prose.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: SVG/diag container, accessible label, visual nodes/edges, legend below."));
    }

    public void assertBusinessFlowGraphUsesCalmNodeTypography() throws IOException {
        List<String> blockers = graphProfile().typographyBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph calm typography", blockers, audit.finalReport(),
                "report.graph.typography.too-bold",
                audit.details(
                        "Asserts: graph nodes follow the html-ex calm typography contract.",
                        "Artifacts: analysis/langgraph-business-understanding.html and analysis/shared-report-reference.css.",
                        "Expected: primary labels use .nt; secondary labels use .ns; no blanket bold rule for all SVG text."));
    }

    public void assertBusinessFlowGraphLegendIsCollapsible() throws IOException {
        List<String> blockers = graphProfile().collapsibleLegendBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph collapsible legend", blockers, audit.finalReport(),
                "report.graph.legend.structure",
                audit.details(
                        "Asserts: Business Flow Graph has a nested collapsible legend, not a flat paragraph.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: details.subdetails inside Business Flow Graph with summary Legend / Graph legend."));
    }

    public void assertBusinessFlowGraphDoesNotRenderInlineSvgLegendBand() throws IOException {
        List<String> blockers = graphProfile().inlineLegendBandBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph has no inline SVG legend band",
                blockers, audit.finalReport(), "report.graph.legend.inline-band",
                audit.details(
                        "Asserts: graph SVG remains a graph; explanatory legend belongs in the collapsible Legend section.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden inside SVG: <g class=\"graph-legend\"> and rect/text legend bands.",
                        "Expected: details.subdetails.graph-legend-list with bullet content below the SVG."));
    }

    public void assertBusinessFlowGraphLegendContentIsRelevant() throws IOException {
        List<String> blockers = graphProfile().legendContentBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph legend content", blockers, audit.finalReport(),
                "report.graph.legend.content",
                audit.details(
                        "Asserts: graph legend teaches how to read the application map, package overlay, owned/capped nodes, and proof boundary.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: bullet list covering application map, package overlay, owned/countable, blocked/capped, next closure, and proof boundary."));
    }

    private McpGraphTemplateProfile graphProfile() throws IOException {
        String html = audit.readFinalReport();
        return McpGraphTemplateProfile.from(
                audit.detailsBody(html, McpReportSections.BUSINESS_FLOW_GRAPH),
                audit.graphLegendBody(html),
                Files.readString(audit.context.packageRoot().resolve("analysis/shared-report-reference.css")));
    }
}
