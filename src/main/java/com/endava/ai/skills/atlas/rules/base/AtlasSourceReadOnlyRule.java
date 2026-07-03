package com.endava.ai.skills.atlas.rules.base;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

import java.nio.file.Files;
import java.util.List;

public final class AtlasSourceReadOnlyRule implements AtlasValidationRule {
    private static final List<String> FORBIDDEN_GENERATION_CLAIMS = List.of(
            "generated new tests",
            "created new tests",
            "patched tests",
            "modified tests");

    @Override
    public String id() {
        return "atlas.source.read-only";
    }

    @Override
    public String expected() {
        return "Atlas is analysis-only: it reads source tests and must not claim to generate or modify them.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (!Files.exists(context.sourceRoot())) {
            result.add(id(), "Configured source test root is missing", context.sourceRoot(),
                    "Point INPUT_TEST_PACKAGE_ROOT to the actual source test package.");
        }
        if (!html.contains("read-only") && !html.contains("nu modifica")) {
            result.add(id(), "Atlas does not state the source-read-only contract", context.atlasHtml(),
                    "State clearly that analyzed source files are not modified.");
        }
        for (String forbidden : FORBIDDEN_GENERATION_CLAIMS) {
            if (html.contains(forbidden)) {
                result.add(id(), "Atlas contains forbidden generation/modification claim: " + forbidden, context.atlasHtml(),
                        "Remove claims that the atlas generated or modified tests.");
            }
        }
    }
}
