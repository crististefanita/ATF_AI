package com.endava.ai.skills.atlas.rules.base;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;

import java.nio.file.Files;

public final class AtlasSharedCssRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.html.uses-shared-css";
    }

    @Override
    public String expected() {
        return "Atlas links shared-report-reference.css and does not replace it with a full local visual system.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.atlasText();
        if (!Files.exists(context.sharedCss())) {
            result.add(id(), "Missing project shared-report-reference.css", context.sharedCss(),
                    "Restore docs/shared-report-reference.css.");
        }
        if (!html.contains("shared-report-reference.css")) {
            result.add(id(), "Atlas does not link shared-report-reference.css", context.atlasHtml(),
                    "Add a relative link to ../shared-report-reference.css.");
        }
        if (AtlasText.count(html, "<style") > 2) {
            result.add(id(), "Atlas uses too many local <style> blocks", context.atlasHtml(),
                    "Move reusable styling into shared-report-reference.css or keep only tiny atlas-specific layout helpers.");
        }
    }
}
