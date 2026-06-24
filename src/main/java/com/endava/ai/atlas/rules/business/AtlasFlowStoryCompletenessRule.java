package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasFlowStoryCompletenessRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_STORY_CONCEPTS = List.of(
            "actor",
            "intent",
            "visible action",
            "business object",
            "observable result");

    @Override
    public String id() {
        return "atlas.candidate.flow-story.complete";
    }

    @Override
    public String expected() {
        return "Important atlas flows read as product stories: actor -> intent -> visible action -> business object -> observable result.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = sectionAfter(context.lowerAtlasText(), "source-to-business trace", 9_000);
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace for flow story validation", context.atlasHtml(),
                    "Add a mapping section that turns tests into product stories, not just source inventories.");
            return;
        }
        for (String concept : REQUIRED_STORY_CONCEPTS) {
            if (!section.contains(concept)) {
                result.add(id(), "Mapping does not expose story concept: " + concept, context.atlasHtml(),
                        "Each important flow should be readable as actor -> intent -> visible action -> business object -> observable result.");
            }
        }
    }

    private static String sectionAfter(String html, String marker, int maxChars) {
        int index = html.indexOf(marker);
        if (index < 0) {
            return "";
        }
        return html.substring(index, Math.min(html.length(), index + maxChars));
    }
}
