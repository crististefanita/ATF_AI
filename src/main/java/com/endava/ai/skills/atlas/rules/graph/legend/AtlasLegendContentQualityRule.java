package com.endava.ai.skills.atlas.rules.graph.legend;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasLegendContentQualityRule implements AtlasValidationRule {
    private static final Pattern LEGEND_DETAILS = Pattern.compile(
            "(?is)<details\\b[^>]*>\\s*<summary>\\s*legend\\s*</summary>(.*?)</details>");

    @Override
    public String id() {
        return "atlas.candidate.legends.bulleted-quality";
    }

    @Override
    public String expected() {
        return "Every graph Legend is schematic and useful: bullet-based, business-aware, and explicit about what the graph adds.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher matcher = LEGEND_DETAILS.matcher(context.lowerAtlasText());
        int index = 0;
        while (matcher.find()) {
            index++;
            String legend = matcher.group(1);
            int bullets = AtlasText.count(legend, "<li");
            if (!legend.contains("<ul") || bullets < 3) {
                result.add(id(), "Legend #" + index + " is not organized as at least 3 bullets", context.atlasHtml(),
                        "Use a compact <ul> with bullets for: what this graph shows, what it adds to the main map, and what comprehension limit remains.");
            }
            requireAny(context, result, legend, index, "graph purpose",
                    "shows", "maps", "separates", "groups", "distinguishes",
                    "scop", "arata", "mapeaza", "separa", "grupeaza", "distinge");
            requireAny(context, result, legend, index, "relationship to main Business Flow Graph",
                    "business flow graph", "main map", "application map", "package overlay", "deepens",
                    "harta principala", "graful principal", "harta aplicatiei", "overlay", "adanceste");
            requireAny(context, result, legend, index, "business maturity or ownership meaning",
                    AtlasSemanticVocabulary.MATURITY_MARKERS);
            requireAny(context, result, legend, index, "comprehension limit",
                    AtlasSemanticVocabulary.LIMIT_MARKERS);
        }
        if (index == 0) {
            result.add(id(), "No Legend details found", context.atlasHtml(),
                    "Add collapsed Legend details under each graph.");
        }
    }

    private void requireAny(AtlasContext context,
                            AtlasValidationResult result,
                            String legend,
                            int index,
                            String concept,
                            String... tokens) {
        for (String token : tokens) {
            if (legend.contains(token)) {
                return;
            }
        }
        result.add(id(), "Legend #" + index + " missing " + concept, context.atlasHtml(),
                "Make every legend useful to an agent: state purpose, relation to main graph, maturity meaning, and remaining comprehension limit.");
    }

    private void requireAny(AtlasContext context,
                            AtlasValidationResult result,
                            String legend,
                            int index,
                            String concept,
                            List<String> tokens) {
        if (AtlasSemanticVocabulary.containsAny(legend, tokens)) {
            return;
        }
        result.add(id(), "Legend #" + index + " missing " + concept, context.atlasHtml(),
                "Make every legend useful to an agent: state purpose, relation to main graph, maturity meaning, and remaining comprehension limit.");
    }

}
