package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageBusinessGraphPresentationContractTest {
    @Test
    public void businessFlowGraphIsRenderedAsDiagram() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphIsRenderedAsDiagram();
    }

    @Test
    public void businessFlowGraphUsesCalmNodeTypography() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesCalmNodeTypography();
    }

    @Test
    public void businessFlowGraphLegendIsCollapsible() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLegendIsCollapsible();
    }

    @Test
    public void businessFlowGraphDoesNotRenderInlineSvgLegendBand() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphDoesNotRenderInlineSvgLegendBand();
    }

    @Test
    public void businessFlowGraphLegendContentIsRelevant() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLegendContentIsRelevant();
    }

    @Test
    public void businessFlowGraphEdgesDoNotCrossNodeText() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphEdgesDoNotCrossNodeText();
    }

    @Test
    public void businessFlowGraphLabelsAreCenteredInsideNodes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphLabelsAreCenteredInsideNodes();
    }

    @Test
    public void businessFlowGraphStylesVisibleSvgShapes() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphStylesVisibleSvgShapes();
    }

    @Test
    public void businessFlowGraphUsesDeclaredEdgeVariants() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphUsesDeclaredEdgeVariants();
    }

    @Test
    public void businessFlowGraphShowsPackageRunOverlay() throws IOException {
        McpPackageAuditValidation.latestPackage().businessGraph().assertBusinessFlowGraphShowsPackageRunOverlay();
    }
}
