package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;
import java.util.Locale;

public final class AtlasGraphMinimalStylePerSectionRule implements AtlasValidationRule {
    private static final List<String> GRAPH_LABELS = List.of(
            "Business Flow Graph",
            "Checkout to invoice lifecycle",
            "Support message lifecycle",
            "Profile account memory");

    private static final List<String> STRONG_STYLE_TOKENS = List.of(
            "class=\"owned\"",
            "class=\"partial\"",
            "class=\"blocked\"",
            "#143b2a",
            "#4c391c",
            "#4b1f2b",
            "#4fc77b",
            "#f0b84c",
            "#e35f75");

    @Override
    public String id() {
        return "atlas.candidate.graphs.minimal-style-per-section";
    }

    @Override
    public String expected() {
        return "Every atlas graph follows the minimal html-ex visual language: neutral nodes, subtle strokes, no traffic-light node fills.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        for (String graphLabel : GRAPH_LABELS) {
            String graph = graphLabel.toLowerCase(Locale.ROOT);
            int index = html.indexOf(graph);
            if (index < 0) {
                result.add(id(), "Missing graph section for style audit: " + graphLabel, context.atlasHtml(),
                        "Keep canonical graph sections so atlas style can be audited graph by graph.");
                continue;
            }
            String slice = html.substring(index, Math.min(html.length(), index + 2_500));
            List<String> present = STRONG_STYLE_TOKENS.stream()
                    .filter(slice::contains)
                    .collect(java.util.stream.Collectors.toList());
            if (!present.isEmpty()) {
                result.add(id(), "Graph uses strong color/status styling: " + graphLabel + " -> " + String.join(", ", present),
                        context.atlasHtml(),
                        "Use the same minimal primitives as html-ex: neutral node boxes, nt/ns text, edge lines, and move maturity into the legend.");
            }
            if (slice.contains("<svg") && !slice.contains("class=\"node")) {
                result.add(id(), "Graph does not use neutral node primitives: " + graphLabel, context.atlasHtml(),
                        "Replace atlas-only box/owned/partial/blocked primitives with shared node/text/edge primitives.");
            }
        }
    }
}
