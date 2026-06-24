package com.endava.ai.atlas.rules.base;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasRowTemplateRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.row-template.complete";
    }

    @Override
    public String expected() {
        return "Atlas rows expose test/class, business object, source trace, maturity, and comprehension limit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        for (String token : List.of("test", "business object", "maturity", "comprehension limit")) {
            if (!html.contains(token)) {
                result.add(id(), "Missing atlas row concept: " + token, context.atlasHtml(),
                        "Add the required row field to Source-to-Business Trace or equivalent atlas matrix.");
            }
        }
        if (!html.contains("source trace") && !html.contains("layer traceability")) {
            result.add(id(), "Missing source trace / layer traceability concept", context.atlasHtml(),
                    "Show how test methods map to page/action/validation/service code.");
        }
    }
}
