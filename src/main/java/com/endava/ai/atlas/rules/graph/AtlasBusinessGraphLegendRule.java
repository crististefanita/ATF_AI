package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasBusinessGraphLegendRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.business-graph.legend";
    }

    @Override
    public String expected() {
        return "The primary Business Flow Graph is the large business map, uses a collapsed Legend subdetail, and separates application map, package overlay, maturity, and comprehension limits.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int graphIndex = html.indexOf("business flow graph");
        if (graphIndex < 0) {
            result.add(id(), "Missing Business Flow Graph section", context.atlasHtml(),
                    "Add a primary Business Flow Graph section.");
            return;
        }
        String graphTail = html.substring(graphIndex, Math.min(html.length(), graphIndex + 12_000));
        if (!graphTail.contains("<svg")) {
            result.add(id(), "Business Flow Graph section has no SVG", context.atlasHtml(),
                    "Add a large SVG business map before secondary diagrams.");
        }
        if (!graphTail.contains("legend")) {
            result.add(id(), "Business Flow Graph has no Legend subdetail", context.atlasHtml(),
                    "Add a collapsed Legend under the main graph.");
        }
        if (graphTail.contains("legend") && !graphTail.contains("<details")) {
            result.add(id(), "Business Flow Graph legend is not collapsible", context.atlasHtml(),
                    "Wrap Legend in a details/subdetails block.");
        }
        for (String token : List.of("application map", "package overlay", "owned", "comprehension limits")) {
            if (!graphTail.contains(token)) {
                result.add(id(), "Business Flow Graph legend missing concept: " + token, context.atlasHtml(),
                        "Legend should explain application map, package overlay, maturity/owned meaning, and comprehension limits.");
            }
        }
    }
}
