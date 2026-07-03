package com.endava.ai.skills.atlas.rules.graph.legend;

import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasBusinessGraphLegendRule implements AtlasValidationRule {
    private static final int BUSINESS_GRAPH_SCAN_LIMIT = 12_000;
    private static final List<LegendConcept> REQUIRED_LEGEND_CONCEPTS = List.of(
            new LegendConcept("application map", List.of(
                    "application map", "harta aplicatiei", "harta aplicatie", "business nodes", "noduri business")),
            new LegendConcept("package overlay", List.of(
                    "package overlay", "overlay pachet", "overlay-ul pachetului", "package coverage", "acoperire pachet")),
            new LegendConcept("maturity/owned meaning", AtlasSemanticVocabulary.MATURITY_MARKERS),
            new LegendConcept("comprehension limits", AtlasSemanticVocabulary.LIMIT_MARKERS));

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.BUSINESS_GRAPH_LEGEND;
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
        String graphTail = html.substring(graphIndex, Math.min(html.length(), graphIndex + BUSINESS_GRAPH_SCAN_LIMIT));
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
        for (LegendConcept concept : REQUIRED_LEGEND_CONCEPTS) {
            if (!AtlasSemanticVocabulary.containsAny(graphTail, concept.markers())) {
                result.add(id(), "Business Flow Graph legend missing concept: " + concept.label(), context.atlasHtml(),
                        "Legend should explain application map, package overlay, maturity/owned meaning, and comprehension limits.");
            }
        }
    }

    private static final class LegendConcept {
        private final String label;
        private final List<String> markers;

        private LegendConcept(String label, List<String> markers) {
            this.label = label;
            this.markers = markers;
        }

        private String label() {
            return label;
        }

        private List<String> markers() {
            return markers;
        }
    }
}
