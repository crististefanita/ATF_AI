package com.endava.ai.mcp.validation.profile.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class McpGraphOverlayProfile {
    private final String graphSection;

    public McpGraphOverlayProfile(String graphSection) {
        this.graphSection = graphSection;
    }

    public List<String> runOverlayBlockers(List<String> runIds) {
        List<String> blockers = new ArrayList<>();
        String visibleText = graphSection.replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        for (String runId : runIds) {
            if (!visibleText.contains(runId.toLowerCase(Locale.ROOT))) {
                blockers.add("Business Flow Graph does not expose package overlay node '" + runId
                        + "'; show the run evidence lane instead of hiding iteration history in prose");
            }
        }
        return blockers;
    }
}
