package com.endava.ai.skills.atlas.rules.base;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasCollapsibleSectionsRule implements AtlasValidationRule {
    private static final Pattern DETAILS_WITH_SUMMARY = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary\\b[^>]*>(.*?)</summary>");

    @Override
    public String id() {
        return "atlas.sections.collapsible";
    }

    @Override
    public String expected() {
        return "Atlas has navigable collapsible sections and only 2. Business Flow Graph is open by default.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.atlasText();
        String lowerHtml = context.lowerAtlasText();
        int details = AtlasText.count(html, "<details");
        if (details < 5) {
            result.add(id(), "Atlas has too few collapsible sections: " + details, context.atlasHtml(),
                    "Use details/summary for atlas sections.");
        }
        Matcher matcher = DETAILS_WITH_SUMMARY.matcher(html);
        int openDetails = 0;
        boolean businessGraphOpen = false;
        while (matcher.find()) {
            String attributes = matcher.group(1).toLowerCase(Locale.ROOT);
            String summary = AtlasText.plainText(matcher.group(2)).toLowerCase(Locale.ROOT);
            boolean open = Pattern.compile("(?i)(^|\\s)open(\\s|=|$)").matcher(attributes).find();
            if (!open) {
                continue;
            }
            openDetails++;
            boolean primaryBusinessGraph = summary.contains("2. business flow graph")
                    || (summary.contains("business flow graph")
                    && !summary.contains("business flow understanding"));
            if (primaryBusinessGraph) {
                businessGraphOpen = true;
            } else {
                result.add(id(), "Atlas opens a non-primary section by default: " + summary,
                        context.atlasHtml(),
                        "Close every details/subdetails block by default except 2. Business Flow Graph.");
            }
        }
        if (!businessGraphOpen && lowerHtml.contains("business flow graph")) {
            result.add(id(), "2. Business Flow Graph is not open by default", context.atlasHtml(),
                    "The primary business graph must be the only default-open Atlas section.");
        }
        if (openDetails > 1) {
            result.add(id(), "Atlas opens too many details blocks by default: " + openDetails, context.atlasHtml(),
                    "Only 2. Business Flow Graph may be open; all sections, subgraphs, legends and proof strips must stay collapsed.");
        }
    }

}
