package com.endava.ai.skills.atlas.rules.scoring;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasScoreAxisConsistencyRule implements AtlasValidationRule {
    private static final List<String> SHORTHAND_AXIS_LABELS = List.of(
            "layer linkage",
            "atlas value",
            "flow clarity",
            "visible object depth",
            "business novelty balance",
            "atlas usefulness",
            "business flow clarity",
            "ui-visible object depth",
            "source traceability");

    @Override
    public String id() {
        return "atlas.candidate.score-axes.consistent";
    }

    @Override
    public String expected() {
        return "Score cards and score rationale use the same canonical atlas axis names, without shorthand drift.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = AtlasScoreContract.normalized(context.lowerAtlasText());
        for (String axis : AtlasScoreContract.BUSINESS_AXES) {
            if (!html.contains(axis)) {
                result.add(id(), "Missing canonical score axis: " + axis, context.atlasHtml(),
                        "Use the same canonical axis names in score cards and score rationale.");
            }
        }
        for (String shorthand : SHORTHAND_AXIS_LABELS) {
            if (html.contains(shorthand)) {
                result.add(id(), "Score rationale uses shorthand axis label: " + shorthand, context.atlasHtml(),
                        "Align score rationale labels with the score cards: Intelegere Business, Reconstructie Flow, Trasabilitate Layere, Claritate Obiect Business, Fidelitate Dovada.");
            }
        }
    }
}
