package com.endava.ai.mcp.validation.profile;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class McpGraphTemplateProfile {
    private final String graphSection;
    private final String legendSection;
    private final String css;

    private McpGraphTemplateProfile(String graphSection, String legendSection, String css) {
        this.graphSection = graphSection;
        this.legendSection = legendSection;
        this.css = css;
    }

    public static McpGraphTemplateProfile from(String graphSection, String legendSection, String css) {
        return new McpGraphTemplateProfile(graphSection, legendSection, css);
    }

    public List<String> diagramBlockers(McpPackageAuditValidation audit) {
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(graphSection, List.of("<svg", "class=\"diag\"", "class='diag'"),
                "Business Flow Graph must contain a real diagram/SVG container", blockers);
        audit.requireAnyToken(graphSection, List.of("role=\"img\"", "aria-label"),
                "Business Flow Graph SVG must expose an accessible diagram label", blockers);
        audit.requireAnyToken(graphSection, List.of("<rect", "<path", "<circle"),
                "Business Flow Graph diagram must contain visual nodes/edges", blockers);
        if (audit.stripTags(graphSection).contains("->") && !graphSection.contains("<svg")) {
            blockers.add("Business Flow Graph is still a text arrow chain instead of a rendered graph");
        }
        return blockers;
    }

    public List<String> typographyBlockers(McpPackageAuditValidation audit) {
        String normalizedCss = css.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        List<String> blockers = new ArrayList<>();
        if (normalizedCss.contains(".diagtext{") && normalizedCss.contains("font-weight:700")) {
            blockers.add("Shared CSS makes every SVG text node bold; use .nt for title and .ns for secondary text instead");
        }
        audit.requireToken(css, ".nt", "Shared CSS missing calm graph node-title class .nt", blockers);
        audit.requireToken(css, ".ns", "Shared CSS missing calm graph node-subtitle class .ns", blockers);
        audit.requireAnyToken(graphSection, List.of("class=\"nt\"", "class='nt'"),
                "Business Flow Graph SVG does not use .nt node title text", blockers);
        audit.requireAnyToken(graphSection, List.of("class=\"ns\"", "class='ns'"),
                "Business Flow Graph SVG does not use .ns secondary node text", blockers);
        return blockers;
    }

    public List<String> collapsibleLegendBlockers(McpPackageAuditValidation audit) {
        List<String> blockers = new ArrayList<>();
        audit.requireToken(graphSection, "subdetails",
                "Business Flow Graph legend must be a nested collapsible subdetails section", blockers);
        audit.requireToken(graphSection, "graph-legend",
                "Business Flow Graph legend must use the reusable graph-legend class", blockers);
        audit.requireAnyToken(graphSection, List.of("Graph legend", "Legend"),
                "Business Flow Graph nested legend summary is missing", blockers);
        if (graphSection.contains("<p class=\"graph-legend\"") || graphSection.contains("<p class='graph-legend'")) {
            blockers.add("Business Flow Graph legend is still a flat paragraph; use a collapsible legend with bullet/list content");
        }
        return blockers;
    }

    public List<String> inlineLegendBandBlockers() {
        List<String> blockers = new ArrayList<>();
        if (graphSection.contains("<g class=\"graph-legend\"") || graphSection.contains("<g class='graph-legend'")) {
            blockers.add("Business Flow Graph renders legend text inside the SVG; move legend content to the collapsible Legend subdetails");
        }
        if (graphSection.contains("class=\"legend\"") || graphSection.contains("class='legend'")) {
            blockers.add("Business Flow Graph contains an inline SVG legend rectangle; it can create a dark unreadable band");
        }
        return blockers;
    }

    public List<String> legendContentBlockers(McpPackageAuditValidation audit) {
        List<String> blockers = new ArrayList<>();
        audit.requireToken(legendSection, "<ul", "Graph legend must organize meaning as bullets", blockers);
        audit.requireToken(legendSection, "<li", "Graph legend must contain bullet items", blockers);
        audit.requireAnyToken(legendSection, List.of("application map", "Application map", "application is the map", "application"),
                "Graph legend must explain the application map", blockers);
        audit.requireAnyToken(legendSection, List.of("package overlay", "Package overlay", "overlay"),
                "Graph legend must explain the package overlay", blockers);
        audit.requireAnyToken(legendSection, List.of("owned", "Owned", "countable"),
                "Graph legend must explain owned/countable nodes", blockers);
        audit.requireAnyToken(legendSection, List.of("blocked", "Blocked", "not owned", "not claimed", "cap"),
                "Graph legend must explain blocked/not-owned/capped nodes", blockers);
        audit.requireAnyToken(legendSection, List.of("next closure", "Next closure", "remaining", "next package"),
                "Graph legend must name the next closure pressure", blockers);
        audit.requireAnyToken(legendSection, List.of("proof", "Proof", "Extent", "XML", "diff"),
                "Graph legend must connect visual meaning to proof boundaries", blockers);
        return blockers;
    }
}
