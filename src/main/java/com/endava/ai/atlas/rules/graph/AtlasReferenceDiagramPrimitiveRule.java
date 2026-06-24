package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasReferenceDiagramPrimitiveRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.graph.reference-primitives";
    }

    @Override
    public String expected() {
        return "Candidate atlas graphs reuse the same primitive vocabulary as html-ex reports: graph-panel, diag, node, nt, ns, and edge.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        for (String token : List.of("graph-panel", "class=\"diag\"", "class=\"node", "class=\"nt\"", "class=\"ns\"", "class=\"edge")) {
            if (!html.contains(token)) {
                result.add(id(), "Missing reference graph primitive: " + token, context.atlasHtml(),
                        "Align atlas SVG markup with the html-ex graph shell instead of inventing atlas-only graph primitives.");
            }
        }
    }
}
