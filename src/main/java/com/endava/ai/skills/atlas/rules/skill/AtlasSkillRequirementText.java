package com.endava.ai.skills.atlas.rules.skill;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

final class AtlasSkillRequirementText {
    private AtlasSkillRequirementText() {
    }

    static String readLowercase(AtlasContext context, AtlasValidationResult result, String ruleId) {
        try {
            return Files.readString(context.requirementsHtml(), StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);
        } catch (IOException e) {
            result.add(ruleId, "Cannot read business atlas requirements HTML",
                    context.requirementsHtml(),
                    "Keep " + AtlasContext.REQUIREMENTS_HTML
                            + " readable so atlas contract tests can validate skill readiness.");
            return "";
        }
    }
}
