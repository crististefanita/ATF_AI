package com.endava.ai.atlas.rules.base;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

public final class AtlasCollapsibleSectionsRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.sections.collapsible";
    }

    @Override
    public String expected() {
        return "Atlas has navigable collapsible sections and only a small opening surface is open by default.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int details = count(html, "<details");
        int openDetails = count(html, "<details open");
        if (details < 5) {
            result.add(id(), "Atlas has too few collapsible sections: " + details, context.atlasHtml(),
                    "Use details/summary for atlas sections.");
        }
        if (openDetails > 2) {
            result.add(id(), "Atlas opens too many sections by default: " + openDetails, context.atlasHtml(),
                    "Keep only the minimal overview and primary business graph open by default.");
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
