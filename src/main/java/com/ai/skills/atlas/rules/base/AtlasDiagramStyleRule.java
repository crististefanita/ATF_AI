package com.ai.skills.atlas.rules.base;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class AtlasDiagramStyleRule implements AtlasValidationRule {
    private static final List<String> STRONG_STATUS_NODE_CLASSES = List.of(
            "class=\"owned\"",
            "class=\"partial\"",
            "class=\"blocked\"",
            ".owned{fill:",
            ".partial{fill:",
            ".blocked{fill:",
            ".lifeowned{fill:",
            ".lifepartial{fill:",
            ".lifegap{fill:",
            ".nfgap{fill:");

    private static final List<String> STRONG_STATUS_COLORS = List.of(
            "#143b2a",
            "#4c391c",
            "#4b1f2b",
            "#4fc77b",
            "#f0b84c",
            "#e35f75");

    @Override
    public String id() {
        return "atlas.diagram-style.minimal";
    }

    @Override
    public String expected() {
        return "Business-flow diagrams use minimal shared-report styling: mostly neutral nodes, subtle overlay, no strong traffic-light fills.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText().replace(" ", "");
        List<String> statusClasses = presentTokens(html, STRONG_STATUS_NODE_CLASSES);
        if (!statusClasses.isEmpty()) {
            result.add(id(), "Diagram uses strong status node classes: " + String.join(", ", statusClasses),
                    context.atlasHtml(),
                    "Use neutral graph primitives like class=\"node\", class=\"nt\", class=\"ns\"; move maturity into legends/tables.");
        }
        List<String> colors = presentTokens(html, STRONG_STATUS_COLORS);
        if (!colors.isEmpty()) {
            result.add(id(), "Diagram uses strong semantic colors: " + String.join(", ", colors),
                    context.atlasHtml(),
                    "Replace traffic-light fills/strokes with subdued shared-report-reference.css graph tokens.");
        }
        if (!html.contains("class=\"node\"") && html.contains("<svg")) {
            result.add(id(), "Diagram does not appear to use neutral node classes", context.atlasHtml(),
                    "Model diagrams after html-ex graph style: neutral node boxes and subtle overlay.");
        }
    }

    private static List<String> presentTokens(String html, List<String> tokens) {
        return tokens.stream()
                .filter(token -> html.contains(token.toLowerCase(Locale.ROOT).replace(" ", "")))
                .collect(Collectors.toList());
    }
}
