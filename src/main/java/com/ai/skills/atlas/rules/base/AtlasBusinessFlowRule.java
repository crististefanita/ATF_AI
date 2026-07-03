package com.ai.skills.atlas.rules.base;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.List;

public final class AtlasBusinessFlowRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.business-flow.present";
    }

    @Override
    public String expected() {
        return "Atlas contains real business-flow diagrams, not just a Java class inventory.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        requireAll(context, result, html, List.of(
                "business flow graph",
                "business object coverage",
                "source-to-business trace",
                "comprehension limits"));
        if (AtlasText.count(html, "<svg") < 3) {
            result.add(id(), "Atlas has too few diagrams for a flow atlas", context.atlasHtml(),
                    "Add business flow, business-object coverage, lifecycle/corridor, and comprehension-limit diagrams.");
        }
    }

    private void requireAll(AtlasContext context,
                            AtlasValidationResult result,
                            String html,
                            List<String> tokens) {
        for (String token : tokens) {
            if (!html.contains(token)) {
                result.add(id(), "Missing atlas token: " + token, context.atlasHtml(),
                        "Ensure the atlas has the required business-flow section and vocabulary.");
            }
        }
    }

}
