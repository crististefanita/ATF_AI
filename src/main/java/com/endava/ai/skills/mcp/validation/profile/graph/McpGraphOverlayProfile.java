package com.endava.ai.skills.mcp.validation.profile.graph;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class McpGraphOverlayProfile {
    private final String graphSection;

    public List<String> runIdLeakBlockers(List<String> runIds) {
        List<String> blockers = new ArrayList<>();
        String visibleText = graphSection.replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        for (String runId : runIds) {
            if (visibleText.contains(runId.toLowerCase(Locale.ROOT))) {
                blockers.add("Business Flow Graph exposes runtime slot '" + runId
                        + "' as visible graph content; move run evidence to Proof Mapping, Run Audit or Artifact Index");
            }
        }
        return blockers;
    }
}
