package com.ai.skills.mcp.validation.profile;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.profile.graph.McpGraphDiagramProfile;
import com.ai.skills.mcp.validation.profile.graph.McpGraphEdgeProfile;
import com.ai.skills.mcp.validation.profile.graph.McpGraphLegendProfile;
import com.ai.skills.mcp.validation.profile.graph.McpGraphOverlayProfile;
import com.ai.skills.mcp.validation.profile.graph.McpGraphShapeProfile;
import com.ai.skills.mcp.validation.profile.graph.McpGraphTypographyProfile;

import java.util.List;

public final class McpGraphTemplateProfile {
    private final McpGraphDiagramProfile diagram;
    private final McpGraphTypographyProfile typography;
    private final McpGraphLegendProfile legend;
    private final McpGraphShapeProfile shape;
    private final McpGraphEdgeProfile edge;
    private final McpGraphOverlayProfile overlay;

    private McpGraphTemplateProfile(String graphSection, String legendSection, String css) {
        this.diagram = new McpGraphDiagramProfile(graphSection);
        this.typography = new McpGraphTypographyProfile(graphSection, css);
        this.legend = new McpGraphLegendProfile(graphSection, legendSection);
        this.shape = new McpGraphShapeProfile(graphSection, css);
        this.edge = new McpGraphEdgeProfile(graphSection, css);
        this.overlay = new McpGraphOverlayProfile(graphSection);
    }

    public static McpGraphTemplateProfile from(String graphSection, String legendSection, String css) {
        return new McpGraphTemplateProfile(graphSection, legendSection, css);
    }

    public List<String> diagramBlockers(McpPackageAuditValidation audit) {
        return diagram.blockers(audit);
    }

    public List<String> diagramDensityBlockers() {
        return diagram.densityBlockers();
    }

    public List<String> semanticNodeBlockers() {
        return diagram.semanticNodeBlockers();
    }

    public List<String> typographyBlockers(McpPackageAuditValidation audit) {
        return typography.blockers(audit);
    }

    public List<String> collapsibleLegendBlockers(McpPackageAuditValidation audit) {
        return legend.collapsibleBlockers(audit);
    }

    public List<String> inlineLegendBandBlockers() {
        return legend.inlineBandBlockers();
    }

    public List<String> legendContentBlockers(McpPackageAuditValidation audit) {
        return legend.contentBlockers(audit);
    }

    public List<String> nodeLabelAlignmentBlockers() {
        return shape.labelAlignmentBlockers();
    }

    public List<String> nodeTextClearanceBlockers() {
        return shape.textClearanceBlockers();
    }

    public List<String> visibleNodeShapeBlockers() {
        return shape.visibleNodeShapeBlockers();
    }

    public List<String> edgeVariantBlockers() {
        return edge.variantBlockers();
    }

    public List<String> frontierEdgeCueBlockers() {
        return edge.frontierCueBlockers();
    }

    public List<String> runIdLeakBlockers(List<String> runIds) {
        return overlay.runIdLeakBlockers(runIds);
    }
}
