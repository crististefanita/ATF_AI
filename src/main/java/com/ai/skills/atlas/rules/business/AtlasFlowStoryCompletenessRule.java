package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasText;

public final class AtlasFlowStoryCompletenessRule implements AtlasValidationRule {
    private static final int REQUIRED_STORY_FAMILIES = 5;

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
        String section = AtlasText.sectionAfter(context.lowerAtlasText(), "source-to-business trace", 9_000);
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace for flow story validation", context.atlasHtml(),
                    "Add a mapping section that turns tests into product stories, not just source inventories.");
            return;
        }
        int coverage = AtlasSemanticVocabulary.familyCoverage(section, AtlasSemanticVocabulary.FLOW_STORY_FAMILIES);
        if (coverage < REQUIRED_STORY_FAMILIES) {
            result.add(id(), "Mapping does not expose the full product-story chain: families=" + coverage,
                    context.atlasHtml(),
                    "Each important flow should be readable as actor -> intent -> visible action -> business object -> observable result, using the atlas semantic vocabulary.");
        }
    }

}
