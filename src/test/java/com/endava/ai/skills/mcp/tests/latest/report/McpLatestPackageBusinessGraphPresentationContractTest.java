package com.endava.ai.skills.mcp.tests.latest.report;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-latest-package-business-graph-presentation-contract-test")
public class McpLatestPackageBusinessGraphPresentationContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("business-graph.rendered-application-map")
    public void businessFlowGraphIsRenderedAsDiagram() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphIsRenderedAsDiagram();
    }

    @Test
    @McpCheckRef("business-graph.canvas-density")
    public void businessFlowGraphUsesBalancedCanvasDensity() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesBalancedCanvasDensity();
    }

    @Test
    @McpCheckRef("business-graph.visible-owned-frontier-semantics")
    public void businessFlowGraphUsesVisibleOwnedAndFrontierSemantics() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesVisibleOwnedAndFrontierSemantics();
    }

    @Test
    @McpCheckRef("business-graph.calm-typography")
    public void businessFlowGraphUsesCalmNodeTypography() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesCalmNodeTypography();
    }

    @Test
    @McpCheckRef("business-graph.collapsible-legend")
    public void businessFlowGraphLegendIsCollapsible() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLegendIsCollapsible();
    }

    @Test
    @McpCheckRef("business-graph.no-inline-svg-legend-band")
    public void businessFlowGraphDoesNotRenderInlineSvgLegendBand() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphDoesNotRenderInlineSvgLegendBand();
    }

    @Test
    @McpCheckRef("business-graph.legend-content")
    public void businessFlowGraphLegendContentIsRelevant() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLegendContentIsRelevant();
    }

    @Test
    @McpCheckRef("business-graph.legend-relationship-proof-boundary")
    public void businessFlowGraphLegendNamesEvidencePlacementAndRelationshipMeaning() throws IOException {
        McpPackageAuditValidation audit = McpPackageAuditValidation.latestPackage();
        String html = audit.readFinalReport();
        String legend = audit.graphLegendBody(html).toLowerCase(Locale.ROOT);

        assertTrue(containsAny(legend, "proof boundary", "evidence mapping")
                        || containsAll(legend, "source", "xml", "extent", "diff"),
                "Business Flow Graph legend must explicitly name proof boundary/evidence mapping, or list the source/XML/Extent/diff proof boundary. "
                        + "Generic 'run proof' wording is too weak compared with the calibrated html-ex graph legends.");
        assertTrue(containsAny(legend, "arrow", "edge", "dependency", "selects", "creates", "mutates", "reopens", "submits"),
                "Business Flow Graph legend must explain relationship/edge meaning, not only node colors and frontier status.");
    }

    @Test
    @McpCheckRef("business-graph.html-example-scale")
    public void businessFlowGraphUsesHtmlExampleScaleForMultiNodeApplicationMap() throws IOException {
        McpPackageAuditValidation audit = McpPackageAuditValidation.latestPackage();
        String html = audit.readFinalReport();
        String graph = audit.detailsBody(html, "Business Flow Graph");
        int nodeCount = count(Pattern.compile("(?is)<(rect|circle|polygon)\\b[^>]*class=[\"'][^\"']*\\bnode\\b"), graph);
        Matcher viewBox = Pattern.compile("(?is)viewBox\\s*=\\s*[\"']\\s*[-\\d.]+\\s+[-\\d.]+\\s+([\\d.]+)\\s+([\\d.]+)")
                .matcher(graph);
        assertTrue(viewBox.find(), "Business Flow Graph SVG must declare a viewBox so report contracts can audit visual scale.");
        double width = Double.parseDouble(viewBox.group(1));
        double height = Double.parseDouble(viewBox.group(2));

        if (nodeCount >= 6) {
            assertTrue(width >= 1100 || height >= 320,
                    "Business Flow Graph with six or more business nodes must use the calibrated html-ex scale: wide application map or multi-lane height. "
                            + "Found viewBox " + width + "x" + height + " for " + nodeCount + " nodes.");
        }
    }

    @Test
    @McpCheckRef("business-graph.edge-text-clearance")
    public void businessFlowGraphEdgesDoNotCrossNodeText() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphEdgesDoNotCrossNodeText();
    }

    @Test
    @McpCheckRef("business-graph.centered-node-labels")
    public void businessFlowGraphLabelsAreCenteredInsideNodes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLabelsAreCenteredInsideNodes();
    }

    @Test
    @McpCheckRef("business-graph.visible-svg-shapes")
    public void businessFlowGraphStylesVisibleSvgShapes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphStylesVisibleSvgShapes();
    }

    @Test
    @McpCheckRef("business-graph.declared-edge-variants")
    public void businessFlowGraphUsesDeclaredEdgeVariants() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesDeclaredEdgeVariants();
    }

    @Test
    @McpCheckRef("business-graph.frontier-edge-cue")
    public void businessFlowGraphUsesFrontierEdgeCue() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesFrontierEdgeCue();
    }

    @Test
    @McpCheckRef("business-graph.no-run-ids-as-nodes")
    public void businessFlowGraphDoesNotExposeRunIdsAsBusinessNodes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphDoesNotExposeRunIdsAsBusinessNodes();
    }

    @Test
    @McpCheckRef("business-graph.no-package-overlay")
    public void businessFlowGraphAvoidsCompactPackageOverlayNodeList() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphAvoidsCompactPackageOverlayNodeList();
    }

    @Test
    @McpCheckRef("business-graph.primary-owned-object.truth")
    public void businessFlowGraphDeclaresSinglePrimaryOwnedObject() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphDeclaresSinglePrimaryOwnedObject();
    }

    @Test
    @McpCheckRef("business-graph.proof-artifacts-not-owned")
    public void businessFlowGraphKeepsProofArtifactsOutsideOwnedNodes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphKeepsProofArtifactsOutsideOwnedNodes();
    }

    @Test
    @McpCheckRef("business-graph.frontier-report-consistency")
    public void businessFlowGraphFrontierMatchesReportCapAndLegend() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphFrontierMatchesReportCapAndLegend();
    }

    @Test
    @McpCheckRef("business-graph.proof-score-consistency")
    public void businessFlowGraphOwnedAndFrontierNodesMatchProofAndScore() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphOwnedAndFrontierNodesMatchProofAndScore();
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAll(String text, String... tokens) {
        for (String token : tokens) {
            if (!text.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private int count(Pattern pattern, String text) {
        int count = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
