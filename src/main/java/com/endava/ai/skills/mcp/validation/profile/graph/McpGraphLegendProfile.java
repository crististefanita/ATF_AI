package com.endava.ai.skills.mcp.validation.profile.graph;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpGraphLegendProfile {
    private final String graphSection;
    private final String legendSection;

    public List<String> collapsibleBlockers(McpPackageAuditValidation audit) {
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

    public List<String> inlineBandBlockers() {
        List<String> blockers = new ArrayList<>();
        if (graphSection.contains("<g class=\"graph-legend\"") || graphSection.contains("<g class='graph-legend'")) {
            blockers.add("Business Flow Graph renders legend text inside the SVG; move legend content to the collapsible Legend subdetails");
        }
        if (graphSection.contains("class=\"legend\"") || graphSection.contains("class='legend'")) {
            blockers.add("Business Flow Graph contains an inline SVG legend rectangle; it can create a dark unreadable band");
        }
        return blockers;
    }

    public List<String> contentBlockers(McpPackageAuditValidation audit) {
        List<String> blockers = new ArrayList<>();
        audit.requireToken(legendSection, "<ul", "Graph legend must organize meaning as bullets", blockers);
        audit.requireToken(legendSection, "<li", "Graph legend must contain bullet items", blockers);
        audit.requireAnyToken(legendSection, List.of("application map", "Application map", "application is the map", "application"),
                "Graph legend must explain the application map", blockers);
        audit.requireAnyToken(legendSection, List.of("owned", "Owned", "countable"),
                "Graph legend must explain owned/countable nodes", blockers);
        audit.requireAnyToken(legendSection, List.of("blocked", "Blocked", "not owned", "not claimed", "cap"),
                "Graph legend must explain blocked/not-owned/capped nodes", blockers);
        audit.requireAnyToken(legendSection, List.of("next closure", "Next closure", "remaining", "next package"),
                "Graph legend must name the next closure pressure", blockers);
        audit.requireAnyToken(legendSection, List.of("proof", "Proof", "Extent", "XML", "diff"),
                "Graph legend must connect visual meaning to proof boundaries", blockers);
        int bulletCount = bulletCount();
        if (bulletCount < 4) {
            blockers.add("Graph legend must have at least four focused bullets: application map, owned nodes, capped/not-owned frontier, and proof boundary; found "
                    + bulletCount);
        }
        return blockers;
    }

    private int bulletCount() {
        int count = 0;
        Matcher matcher = Pattern.compile("(?is)<li\\b").matcher(legendSection);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
