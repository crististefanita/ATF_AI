package com.ai.skills.atlas.rules.graph;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AtlasGraphMinimalStylePerSectionRule implements AtlasValidationRule {
    private static final Pattern GRAPH_SECTION = Pattern.compile(
            "(?is)(?:business flow graph|<summary>\\s*3\\.\\d+\\s+[^<]+</summary>)(.{0,2500}?<svg.*?</svg>)");

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
        Matcher matcher = GRAPH_SECTION.matcher(html);
        int auditedGraphs = 0;
        while (matcher.find()) {
            auditedGraphs++;
            String slice = matcher.group(1);
            List<String> present = STRONG_STYLE_TOKENS.stream()
                    .filter(slice::contains)
                    .collect(Collectors.toList());
            if (!present.isEmpty()) {
                result.add(id(), "Graph uses strong color/status styling: " + String.join(", ", present),
                        context.atlasHtml(),
                        "Use the same minimal primitives as html-ex: neutral node boxes, nt/ns text, edge lines, and move maturity into the legend.");
            }
            if (slice.contains("<svg") && !slice.contains("class=\"node")) {
                result.add(id(), "Graph does not use neutral node primitives", context.atlasHtml(),
                        "Replace atlas-only box/owned/partial/blocked primitives with shared node/text/edge primitives.");
            }
        }
        if (auditedGraphs < 4) {
            result.add(id(), "Too few business graphs were available for style audit: " + auditedGraphs,
                    context.atlasHtml(),
                    "Keep the primary Business Flow Graph plus several generic 3.x business-flow subgraphs.");
        }
    }
}
