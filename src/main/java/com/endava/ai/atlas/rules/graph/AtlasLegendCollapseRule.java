package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasLegendCollapseRule implements AtlasValidationRule {
    private static final Pattern DETAILS_WITH_LEGEND_SUMMARY = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary>\\s*legend\\s*</summary>");

    @Override
    public String id() {
        return "atlas.candidate.legends.collapsed";
    }

    @Override
    public String expected() {
        return "Every Legend is a dedicated details/subdetails block and every Legend block is collapsed by default.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        Matcher matcher = DETAILS_WITH_LEGEND_SUMMARY.matcher(html);
        int legendDetails = 0;
        while (matcher.find()) {
            legendDetails++;
            if (matcher.group(1).contains("open")) {
                result.add(id(), "Legend details is open by default", context.atlasHtml(),
                        "Keep every Legend collapsed by default; graph panels may open, but legend explanations should stay closed.");
            }
        }
        int legendMentions = count(html, "legend:");
        if (legendMentions > legendDetails) {
            result.add(id(), "Legend text is written inline instead of collapsed: inline=" + legendMentions
                            + ", collapsed=" + legendDetails,
                    context.atlasHtml(),
                    "Move inline 'Legend:' paragraphs into dedicated collapsed Legend subdetails for each graph.");
        }
        if (legendDetails == 0 && html.contains("legend")) {
            result.add(id(), "Legend appears in text but no collapsed Legend details exists", context.atlasHtml(),
                    "Wrap each graph legend in <details class=\"subdetails\"><summary>Legend</summary>...</details>.");
        }
    }

    private static int count(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
