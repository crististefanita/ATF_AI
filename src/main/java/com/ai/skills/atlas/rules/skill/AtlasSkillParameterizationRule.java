package com.ai.skills.atlas.rules.skill;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;

public final class AtlasSkillParameterizationRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_PARAMETERS = List.of(
            "input_test_package_root",
            "output_atlas_html",
            "shared_report_reference_css",
            "living_architecture_reference",
            "atlas_test_extent",
            "must_produce",
            "must_not_modify_analyzed_files");

    @Override
    public String id() {
        return "atlas.candidate.skill-parameters.generic";
    }

    @Override
    public String expected() {
        return "Future atlas skill is parameterized around an explicit input folder and does not lock the contract to the current ATF package.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String requirements = AtlasSkillRequirementText.readLowercase(context, result, id());
        if (requirements.isBlank()) {
            return;
        }
        for (String parameter : REQUIRED_PARAMETERS) {
            if (!requirements.contains(parameter)) {
                result.add(id(), "Missing skill parameter in atlas requirements: " + parameter,
                        context.requirementsHtml(),
                        "Keep the future skill generic by documenting input, output, CSS, architecture reference and read-only flags as parameters.");
            }
        }
        if (!requirements.contains("folder / package primit explicit")
                && !requirements.contains("<folder / package")
                && !requirements.contains("<input-folder>")) {
            result.add(id(), "Input folder is not described as an explicit external parameter",
                    context.requirementsHtml(),
                    "The future skill must start from an explicitly provided test package folder, not from a hardcoded ATF path.");
        }
        if (requirements.contains("html-ex readiness") && !requirements.contains("nu decide")) {
            result.add(id(), "Requirements mention html-ex readiness without a clear non-owner boundary",
                    context.requirementsHtml(),
                    "State that the atlas skill does not decide package/html-ex readiness; it only explains business comprehension.");
        }
    }
}
