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

    public void assertBusinessFlowGraphEdgesDoNotCrossNodeText() throws IOException {
        List<String> blockers = graphProfile().nodeTextClearanceBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph edge-to-text clearance", blockers, audit.finalReport(),
                "report.graph.edge-crosses-node-text",
                audit.details(
                        "Asserts: graph edges enter nodes through borders, not through title/subtitle text bands.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: no SVG edge endpoint/control point inside a node near .nt/.ns text baselines.",
                        "Repair: route the edge to the rectangle border or move the label/edge so they do not overlap visually."));
    }

    public void assertBusinessFlowGraphLabelsAreCenteredInsideNodes() throws IOException {
        List<String> blockers = graphProfile().nodeLabelAlignmentBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph label alignment", blockers, audit.finalReport(),
                "report.graph.label-alignment",
                audit.details(
                        "Asserts: graph node labels are centered and fit inside their rectangles.",
                        "Artifact: analysis/langgraph-business-understanding.html and analysis/shared-report-reference.css.",
                        "Expected: .nt and .ns use text-anchor: middle, or every text element declares text-anchor=\"middle\".",
                        "Repair: add the anchor to shared CSS or widen/reposition nodes whose labels overflow."));
    }

    public void assertBusinessFlowGraphStylesVisibleSvgShapes() throws IOException {
        List<String> blockers = graphProfile().visibleNodeShapeBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph visible node styling", blockers, audit.finalReport(),
                "report.graph.node-style-target",
                audit.details(
                        "Asserts: SVG node classes are attached to visible shapes, not only wrapper groups.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: <rect class=\"node ...\"> so shared CSS paints fill/stroke directly.",
                        "Repair: move node/owned/cap/strong classes from <g> wrappers to the rendered <rect> elements."));
    }

    public void assertBusinessFlowGraphUsesDeclaredEdgeVariants() throws IOException {
        List<String> blockers = graphProfile().edgeVariantBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph declared edge variants", blockers, audit.finalReport(),
                "report.graph.edge-variant-undefined",
                audit.details(
                        "Asserts: every SVG edge variant used by the graph is defined by shared CSS.",
                        "Artifacts: analysis/langgraph-business-understanding.html and analysis/shared-report-reference.css.",
                        "Expected: edge classes use canonical variants such as edge, edge soft, edge dashed, or a CSS-backed variant.",
                        "Repair: replace ad-hoc edge classes or promote the useful variant into shared-report-reference.css first."));
    }

    public void assertBusinessFlowGraphShowsPackageRunOverlay() throws IOException {
        List<String> runIds = new java.util.ArrayList<>();
        audit.forEachRun(runIds::add);
        List<String> blockers = graphProfile().runOverlayBlockers(runIds);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph package run overlay", blockers, audit.finalReport(),
                "report.graph.run-overlay-missing",
                audit.details(
                        "Asserts: Business Flow Graph exposes the package overlay lane, not only the application macro-map.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: visible run overlay nodes for every runtime run slot, e.g. run_01..run_N.",
                        "Repair: use the html-ex lane pattern: application row, run evidence row, next-closure pressure row."));
    }

    private McpGraphTemplateProfile graphProfile() throws IOException {
        String html = audit.readFinalReport();
        return McpGraphTemplateProfile.from(
                audit.detailsBody(html, McpReportSections.BUSINESS_FLOW_GRAPH),
                audit.graphLegendBody(html),
                Files.readString(audit.context.packageRoot().resolve("analysis/shared-report-reference.css")));
    }
}
