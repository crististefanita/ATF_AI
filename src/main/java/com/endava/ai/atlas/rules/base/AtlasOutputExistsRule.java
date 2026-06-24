package com.endava.ai.atlas.rules.base;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.nio.file.Files;

public final class AtlasOutputExistsRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.output.exists";
    }

    @Override
    public String expected() {
        return "business-test-atlas.html exists and is not a tiny placeholder.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        try {
            if (!Files.exists(context.atlasHtml())) {
                result.add(id(), "Missing business-test-atlas.html", context.atlasHtml(),
                        "Produce docs/atlas/business-test-atlas.html.");
                return;
            }
            if (Files.size(context.atlasHtml()) < 8_000) {
                result.add(id(), "Atlas HTML is too small to be a useful business atlas", context.atlasHtml(),
                        "Regenerate the atlas with diagrams, tables, and flow analysis.");
            }
        } catch (java.io.IOException e) {
            result.add(id(), "Cannot inspect atlas HTML: " + e.getMessage(), context.atlasHtml(),
                    "Make the atlas file readable.");
        }
    }
}
