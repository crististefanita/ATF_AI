package com.endava.ai.mcp.validation.profile.graph;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;

import java.util.ArrayList;
import java.util.List;

public final class McpGraphDiagramProfile {
    private final String graphSection;

    public McpGraphDiagramProfile(String graphSection) {
        this.graphSection = graphSection;
    }

    public List<String> blockers(McpPackageAuditValidation audit) {
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
}
