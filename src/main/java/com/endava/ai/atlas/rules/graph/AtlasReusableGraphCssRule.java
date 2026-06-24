package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasReusableGraphCssRule implements AtlasValidationRule {
    private static final List<String> LOCAL_GRAPH_PRIMITIVE_OVERRIDES = List.of(
            ".atlas-diagram .node",
            ".atlas-diagram .strong",
            ".atlas-diagram .soft-node",
            ".atlas-diagram .cap",
            ".atlas-diagram .nt",
            ".atlas-diagram .ns",
            ".atlas-diagram .edge",
            ".atlas-diagram .edge-soft",
            ".atlas-diagram .edge-dashed");

    @Override
    public String id() {
        return "atlas.candidate.graph.css-reusable";
    }

    @Override
    public String expected() {
        return "Candidate atlas graph styling is reusable: graph primitives come from shared-report-reference.css, not atlas-local overrides.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (html.isBlank()) {
            result.add(id(), "Cannot validate reusable graph CSS because atlas HTML is missing or empty", context.atlasHtml(),
                    "Produce docs/atlas/business-test-atlas.html before checking candidate presentation quality.");
            return;
        }
        for (String token : LOCAL_GRAPH_PRIMITIVE_OVERRIDES) {
            if (html.contains(token)) {
                result.add(id(), "Atlas defines local graph primitive override: " + token, context.atlasHtml(),
                        "Move reusable graph primitives to shared-report-reference.css or reuse the existing html-ex graph classes directly.");
            }
        }
    }
}
