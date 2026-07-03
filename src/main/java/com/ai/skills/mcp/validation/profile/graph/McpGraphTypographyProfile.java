package com.ai.skills.mcp.validation.profile.graph;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.util.ArrayList;
import java.util.List;

public final class McpGraphTypographyProfile {
    private final String graphSection;
    private final String css;
    private final McpGraphCss graphCss;

    public McpGraphTypographyProfile(String graphSection, String css) {
        this.graphSection = graphSection;
        this.css = css;
        this.graphCss = new McpGraphCss(css);
    }

    public List<String> blockers(McpPackageAuditValidation audit) {
        String allSvgText = graphCss.block(".diagtext");
        String nodeGroup = graphCss.block(".diag.node");
        List<String> blockers = new ArrayList<>();
        if (allSvgText.contains("font-weight:700")) {
            blockers.add("Shared CSS makes every SVG text node bold; use .nt for title and .ns for secondary text instead");
        }
        if (nodeGroup.contains("stroke:")) {
            blockers.add("Shared CSS applies stroke to the SVG node group; text inherits the stroke and becomes visually outlined");
        }
        if (nodeGroup.contains("stroke-width:")) {
            blockers.add("Shared CSS applies node stroke-width on the SVG group; stroke must target node shapes only");
        }
        if (graphCss.classHasNonNoneProperty(".nt", "text-shadow")
                || graphCss.classHasNonNoneProperty(".ns", "text-shadow")
                || graphCss.classHasNonNoneProperty(".nt", "filter")
                || graphCss.classHasNonNoneProperty(".ns", "filter")
                || graphSection.matches("(?is).*<text\\b[^>]*(text-shadow|filter)\\s*=.*")) {
            blockers.add("Business Flow Graph node text must not use shadow/filter halos; keep SVG labels calm and readable");
        }
        audit.requireAnyToken(css, List.of(".diag text", ".diag svg text"),
                "Shared CSS must explicitly reset SVG text stroke so node borders do not outline labels", blockers);
        if (!allSvgText.contains("stroke:none") && !graphCss.block(".diagsvgtext").contains("stroke:none")) {
            blockers.add("Shared CSS must set stroke:none for SVG text inside the Business Flow Graph");
        }
        audit.requireToken(css, ".nt", "Shared CSS missing calm graph node-title class .nt", blockers);
        audit.requireToken(css, ".ns", "Shared CSS missing calm graph node-subtitle class .ns", blockers);
        audit.requireAnyToken(graphSection, List.of("class=\"nt\"", "class='nt'"),
                "Business Flow Graph SVG does not use .nt node title text", blockers);
        audit.requireAnyToken(graphSection, List.of("class=\"ns\"", "class='ns'"),
                "Business Flow Graph SVG does not use .ns secondary node text", blockers);
        return blockers;
    }
}
