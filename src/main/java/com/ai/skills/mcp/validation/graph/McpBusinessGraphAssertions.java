package com.ai.skills.mcp.validation.graph;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.profile.McpGraphTemplateProfile;
import com.ai.skills.mcp.validation.profile.McpReportSections;
import com.ai.skills.mcp.validation.profile.graph.McpGraphSemanticTruthProfile;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpBusinessGraphAssertions {
    private static final String FINAL_REPORT_ARTIFACT = "Artifact: " + McpArtifactPaths.FINAL_REPORT + ".";
    private static final String FINAL_REPORT_AND_CSS_ARTIFACTS =
            "Artifacts: " + McpArtifactPaths.FINAL_REPORT + " and " + McpArtifactPaths.REPORT_CSS + ".";

    private final McpPackageAuditValidation audit;

    public void assertBusinessFlowGraphIsRenderedAsDiagram() throws IOException {
        List<String> blockers = graphProfile().diagramBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph rendered diagram", blockers, audit.finalReport(),
                "report.graph.diagram.missing",
                audit.details(
                        "Asserts: Business Flow Graph is a visual application map with diagram nodes, not only prose.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: SVG/diag container, accessible label, visual nodes/edges, legend below."));
    }

    public void assertBusinessFlowGraphUsesBalancedCanvasDensity() throws IOException {
        List<String> blockers = graphProfile().diagramDensityBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph balanced canvas density",
                blockers, audit.finalReport(), "report.graph.canvas.too-sparse",
                audit.details(
                        "Asserts: graph canvas is visually dense enough to read as a designed business diagram, not a tiny chain floating in a large empty panel.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: at least five business nodes and node geometry occupying a meaningful share of the SVG viewBox height.",
                        "Repair: tighten viewBox height, reduce empty graph-panel space, or add a real second lane for frontier/proof context."));
    }

    public void assertBusinessFlowGraphUsesVisibleOwnedAndFrontierSemantics() throws IOException {
        List<String> blockers = graphProfile().semanticNodeBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph visible owned/frontier semantics",
                blockers, audit.finalReport(), "report.graph.semantic-node-style",
                audit.details(
                        "Asserts: graph nodes expose visually styled business meaning, not only neutral boxes.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected: owned business nodes use CSS-backed owned class; frontier/not-owned nodes use CSS-backed cap or blocked class.",
                        "Forbidden: unsupported semantic classes such as capped when shared CSS only styles cap/blocked."));
    }

    public void assertBusinessFlowGraphDeclaresSinglePrimaryOwnedObject() throws IOException {
        List<String> blockers = semanticProfile().primaryOwnedObjectBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph primary owned object truth",
                blockers, audit.finalReport(), "report.graph.primary-owned.truth",
                audit.details(
                        "Asserts: graph ownership is semantic, not only decorative CSS.",
                        "Artifacts: " + McpArtifactPaths.FINAL_REPORT + " and " + McpArtifactPaths.PACKAGE_STATE + ".",
                        "Expected: exactly one node owned strong, named as the durable package-owned business object.",
                        "Forbidden: making XML, Extent, diff, MCP gate, report, screenshot, or run evidence the owned business object."));
    }

    public void assertBusinessFlowGraphKeepsProofArtifactsOutsideOwnedNodes() throws IOException {
        List<String> blockers = semanticProfile().proofArtifactsNotOwnedBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph proof artifacts not owned",
                blockers, audit.finalReport(), "report.graph.proof-artifact-owned",
                audit.details(
                        "Asserts: supporting/corroborating/boundary evidence is not visually claimed as business ownership.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: proof artifacts stay in Proof Mapping, Run Audit, Artifact Index and legend proof-boundary wording.",
                        "Forbidden owned nodes: Extent, XML, diff, MCP gate, report, screenshot, run-state, package-state, method ledger."));
    }

    public void assertBusinessFlowGraphFrontierMatchesReportCapAndLegend() throws IOException {
        List<String> blockers = semanticProfile().frontierConsistencyBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph frontier consistency",
                blockers, audit.finalReport(), "report.graph.frontier.consistency",
                audit.details(
                        "Asserts: capped/not-owned frontier is the same frontier named by package state, cap wording and legend.",
                        "Artifacts: " + McpArtifactPaths.FINAL_REPORT + " and " + McpArtifactPaths.PACKAGE_STATE + ".",
                        "Expected: cap/blocked node is not owned/strong and is explained by Executive Context, Score Overview or Legend.",
                        "Repair: put the next closure/frontier object in the graph as node cap/blocked and keep its meaning visible in report text."));
    }

    public void assertBusinessFlowGraphOwnedAndFrontierNodesMatchProofAndScore() throws IOException {
        List<String> blockers = semanticProfile().proofAndScoreConsistencyBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph proof and score consistency",
                blockers, audit.finalReport(), "report.graph.proof-score.consistency",
                audit.details(
                        "Asserts: graph labels are anchored in the report evidence spine, not invented by presentation.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: primary owned node is traceable in Proof Mapping or Score Overview; frontier node is traceable in Executive Context, Score Overview or Legend.",
                        "Repair: align graph node labels with Proof Mapping rows, cap reason, weakest axis and next closure wording."));
    }

    public void assertBusinessFlowGraphUsesCalmNodeTypography() throws IOException {
        List<String> blockers = graphProfile().typographyBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph calm typography", blockers, audit.finalReport(),
                "report.graph.typography.too-bold",
                audit.details(
                        "Asserts: graph nodes follow the html-ex calm typography contract.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected: primary labels use .nt; secondary labels use .ns; no blanket bold rule for all SVG text."));
    }

    public void assertBusinessFlowGraphLegendIsCollapsible() throws IOException {
        List<String> blockers = graphProfile().collapsibleLegendBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph collapsible legend", blockers, audit.finalReport(),
                "report.graph.legend.structure",
                audit.details(
                        "Asserts: Business Flow Graph has a nested collapsible legend, not a flat paragraph.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: details.subdetails inside Business Flow Graph with summary Legend / Graph legend."));
    }

    public void assertBusinessFlowGraphDoesNotRenderInlineSvgLegendBand() throws IOException {
        List<String> blockers = graphProfile().inlineLegendBandBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph has no inline SVG legend band",
                blockers, audit.finalReport(), "report.graph.legend.inline-band",
                audit.details(
                        "Asserts: graph SVG remains a graph; explanatory legend belongs in the collapsible Legend section.",
                        FINAL_REPORT_ARTIFACT,
                        "Forbidden inside SVG: <g class=\"graph-legend\"> and rect/text legend bands.",
                        "Expected: details.subdetails.graph-legend-list with bullet content below the SVG."));
    }

    public void assertBusinessFlowGraphLegendContentIsRelevant() throws IOException {
        List<String> blockers = graphProfile().legendContentBlockers(audit);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph legend content", blockers, audit.finalReport(),
                "report.graph.legend.content",
                audit.details(
                        "Asserts: graph legend teaches how to read the application map, owned/capped nodes, next closure and proof boundary.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: bullet list covering application map, owned/countable, blocked/capped, next closure, and proof boundary. Run slots belong in Proof Mapping / Run Audit, not in this graph."));
    }

    public void assertBusinessFlowGraphEdgesDoNotCrossNodeText() throws IOException {
        List<String> blockers = graphProfile().nodeTextClearanceBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph edge-to-text clearance", blockers, audit.finalReport(),
                "report.graph.edge-crosses-node-text",
                audit.details(
                        "Asserts: graph edges enter nodes through borders, not through title/subtitle text bands.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: no SVG edge endpoint/control point inside a node near .nt/.ns text baselines.",
                        "Repair: route the edge to the rectangle border or move the label/edge so they do not overlap visually."));
    }

    public void assertBusinessFlowGraphLabelsAreCenteredInsideNodes() throws IOException {
        List<String> blockers = graphProfile().nodeLabelAlignmentBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph label alignment", blockers, audit.finalReport(),
                "report.graph.label-alignment",
                audit.details(
                        "Asserts: graph node labels are centered and fit inside their rectangles.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected: .nt and .ns use text-anchor: middle, or every text element declares text-anchor=\"middle\".",
                        "Repair: add the anchor to shared CSS or widen/reposition nodes whose labels overflow."));
    }

    public void assertBusinessFlowGraphStylesVisibleSvgShapes() throws IOException {
        List<String> blockers = graphProfile().visibleNodeShapeBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph visible node styling", blockers, audit.finalReport(),
                "report.graph.node-style-target",
                audit.details(
                        "Asserts: SVG node classes are attached to visible shapes, not only wrapper groups.",
                        FINAL_REPORT_ARTIFACT,
                        "Expected: <rect class=\"node ...\"> so shared CSS paints fill/stroke directly.",
                        "Repair: move node/owned/cap/strong classes from <g> wrappers to the rendered <rect> elements."));
    }

    public void assertBusinessFlowGraphUsesDeclaredEdgeVariants() throws IOException {
        List<String> blockers = graphProfile().edgeVariantBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph declared edge variants", blockers, audit.finalReport(),
                "report.graph.edge-variant-undefined",
                audit.details(
                        "Asserts: every SVG edge variant used by the graph is defined by shared CSS.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected: edge classes use canonical variants such as edge, edge soft, edge dashed, or a CSS-backed variant.",
                        "Repair: replace ad-hoc edge classes or promote the useful variant into shared-report-reference.css first."));
    }

    public void assertBusinessFlowGraphUsesFrontierEdgeCue() throws IOException {
        List<String> blockers = graphProfile().frontierEdgeCueBlockers();
        audit.assertNoSyntheticViolations("Final report Business Flow Graph frontier edge cue",
                blockers, audit.finalReport(), "report.graph.frontier-edge-cue",
                audit.details(
                        "Asserts: next/frontier pressure is visible in the graph, not only described in prose.",
                        FINAL_REPORT_AND_CSS_ARTIFACTS,
                        "Expected: frontier/next-object edges use CSS-backed edge variants such as edge dashed, edge soft, edge frontier or edge cap.",
                        "Forbidden: plain edge paths with inline stroke color standing in for semantic frontier styling."));
    }

    public void assertBusinessFlowGraphDoesNotExposeRunIdsAsBusinessNodes() throws IOException {
        List<String> runIds = new java.util.ArrayList<>();
        audit.forEachRun(runIds::add);
        List<String> blockers = graphProfile().runIdLeakBlockers(runIds);
        audit.assertNoSyntheticViolations("Final report Business Flow Graph business-only map", blockers, audit.finalReport(),
                "report.graph.run-id-leak",
                audit.details(
                        "Asserts: Business Flow Graph remains a business-object map, not a run inventory.",
                        FINAL_REPORT_ARTIFACT,
                        "Forbidden: visible run_01..run_N labels inside the Business Flow Graph section.",
                        "Expected: runtime run slots appear in Proof Mapping, Run Audit, Artifact Index, XML/Extent links and command evidence."));
    }

    public void assertBusinessFlowGraphAvoidsCompactPackageOverlayNodeList() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        if (html.contains("Package overlay nodes:")) {
            blockers.add("Business Flow Graph publishes compact 'Package overlay nodes:' text; explain run overlay meaning in the collapsible Legend or structured proof rows instead");
        }
        if (Pattern.compile("(?is)<div\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bpackage-overlay\\b[^\"']*[\"'][^>]*>")
                .matcher(html).find()) {
            blockers.add("Business Flow Graph uses a standalone package-overlay div; this renders as a cramped procedural strip instead of an auditable legend/proof strip");
        }
        if (Pattern.compile("(?is)<span>\\s*run_\\d{2}[^<]*</span>\\s*<span>\\s*run_\\d{2}")
                .matcher(html).find()) {
            blockers.add("Business Flow Graph lists run overlay nodes as adjacent inline spans; use bullets or structured rows so the overlay remains readable");
        }
        audit.assertNoSyntheticViolations("Final report Business Flow Graph separates run evidence from business map",
                blockers, audit.finalReport(), "report.graph.run-evidence.compact-list",
                audit.details(
                        "Asserts: run evidence is not rendered as graph content or a compact run token strip.",
                        FINAL_REPORT_ARTIFACT,
                        "Forbidden: standalone package-overlay div, 'Package overlay nodes:' label, or adjacent inline run_XX span lists.",
                        "Expected: run evidence lives in Proof Mapping, Run Audit, Artifact Index, XML/Extent links and command evidence."));

    }

    private McpGraphTemplateProfile graphProfile() throws IOException {
        String html = audit.readFinalReport();
        return McpGraphTemplateProfile.from(
                audit.detailsBody(html, McpReportSections.BUSINESS_FLOW_GRAPH),
                audit.graphLegendBody(html),
                Files.readString(audit.context.packageRoot().resolve(McpArtifactPaths.REPORT_CSS)));
    }

    private McpGraphSemanticTruthProfile semanticProfile() throws IOException {
        String html = audit.readFinalReport();
        java.nio.file.Path packageState = audit.context.packageRoot().resolve(McpArtifactPaths.PACKAGE_STATE);
        String state = Files.isRegularFile(packageState) ? Files.readString(packageState) : "";
        return McpGraphSemanticTruthProfile.from(html, state);
    }
}
