package com.endava.ai.skills.atlas.rules.graph;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

public final class AtlasReusableGraphCssRule implements AtlasValidationRule {
    private static final Pattern EDGE_LABEL_SELECTOR = Pattern.compile("(?is)\\.diag\\s+\\.el\\b\\s*\\{(?<body>[^}]+)}");
    private static final Pattern READABLE_FILL = Pattern.compile("(?is)fill\\s*:\\s*(?:var\\(--text\\)|#(?:d7e3ff|e6edf3|cbd5e1|[a-f0-9]{6}))");
    private static final List<String> LOCAL_GRAPH_PRIMITIVE_OVERRIDES = List.of(
            ".atlas-diagram .node",
            ".atlas-diagram .strong",
            ".atlas-diagram .soft-node",
            ".atlas-diagram .cap",
            ".atlas-diagram .nt",
            ".atlas-diagram .ns",
            ".atlas-diagram .el",
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
                    "Produce " + AtlasContext.ATLAS_HTML + " before checking candidate presentation quality.");
            return;
        }
        for (String token : LOCAL_GRAPH_PRIMITIVE_OVERRIDES) {
            if (html.contains(token)) {
                result.add(id(), "Atlas defines local graph primitive override: " + token, context.atlasHtml(),
                        "Move reusable graph primitives to shared-report-reference.css or reuse the existing html-ex graph classes directly.");
            }
        }
        if (html.contains("class=\"el\"") || html.contains("class='el'")) {
            String css = sharedCss(context);
            var matcher = EDGE_LABEL_SELECTOR.matcher(css);
            if (!matcher.find() || !READABLE_FILL.matcher(matcher.group("body")).find()) {
                result.add(id(), "Atlas uses edge-label class el but shared CSS does not style it", context.sharedCss(),
                        "Add reusable .diag .el styling with readable fill to shared-report-reference.css so relationship labels are visible, not default black SVG text.");
            }
        }
    }

    private static String sharedCss(AtlasContext context) {
        if (!Files.exists(context.sharedCss())) {
            return "";
        }
        try {
            return Files.readString(context.sharedCss(), StandardCharsets.UTF_8).toLowerCase();
        } catch (IOException e) {
            return "";
        }
    }
}
