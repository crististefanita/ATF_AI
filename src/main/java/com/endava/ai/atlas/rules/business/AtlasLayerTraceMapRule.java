package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasLayerTraceMapRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_LAYER_TERMS = List.of(
            "test",
            "action",
            "component",
            "page",
            "service",
            "validation",
            "model",
            "constant",
            "business object");

    @Override
    public String id() {
        return "atlas.candidate.layer-trace-map.complete";
    }

    @Override
    public String expected() {
        return "Atlas includes a Layer Trace Map that follows important flows from test entrypoint through framework layers into business objects.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (!html.contains("layer trace map") && !html.contains("layer traceability map")) {
            result.add(id(), "Missing dedicated Layer Trace Map section", context.atlasHtml(),
                    "Add a collapsed Layer Trace Map so every major flow shows test -> action/component/page/service/validation/model/constants -> business object.");
        }
        for (String term : REQUIRED_LAYER_TERMS) {
            if (!html.contains(term)) {
                result.add(id(), "Layer Trace Map missing required layer/concept: " + term, context.atlasHtml(),
                        "The atlas must prove business comprehension by traversing source layers, not by naming test classes only.");
            }
        }
    }
}
