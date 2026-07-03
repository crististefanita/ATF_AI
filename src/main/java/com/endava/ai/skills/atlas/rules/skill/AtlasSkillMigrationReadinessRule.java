package com.endava.ai.skills.atlas.rules.skill;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;

public final class AtlasSkillMigrationReadinessRule implements AtlasValidationRule {
    private static final List<String> SKILL_PACKAGING_TOKENS = List.of(
            "skill.md",
            "frontmatter",
            "description",
            "progressive disclosure",
            "references/",
            "assets/",
            "scripts/",
            "business-test-atlas-template.html",
            "validate-atlas",
            "quick_validate.py");

    @Override
    public String id() {
        return "atlas.candidate.skill-migration.readiness";
    }

    @Override
    public String expected() {
        return "Atlas requirements are ready to migrate into a Codex skill without loading the full HTML contract every time.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String requirements = AtlasSkillRequirementText.readLowercase(context, result, id());
        if (requirements.isBlank()) {
            return;
        }
        for (String token : SKILL_PACKAGING_TOKENS) {
            if (!requirements.contains(token)) {
                result.add(id(), "Business atlas reference is not skill-ready; missing token: " + token,
                        context.requirementsHtml(),
                        "Document the Codex skill packaging contract: SKILL.md trigger, references, assets, scripts and validation command.");
            }
        }
        if (!requirements.contains("trigger") && !requirements.contains("activeaza")) {
            result.add(id(), "Business atlas reference does not explain when the future skill should trigger",
                    context.requirementsHtml(),
                    "Add a short trigger description for requests that ask to understand a test package as a business atlas.");
        }
    }
}
