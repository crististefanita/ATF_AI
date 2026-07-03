package com.endava.ai.skills.atlas.rules.scoring;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;

public final class AtlasScoreOpeningAuditPointerRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_SCORE_POINTER_SECTIONS = List.of("4.5", "4.6", "5.1");

    @Override
    public String id() {
        return "atlas.candidate.score-opening.audit-pointer";
    }

    @Override
    public String expected() {
        return "Executive Context points readers to the exact score-support, Overall-formula and source-trace sections.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String rationale = AtlasScoreContract.normalized(AtlasScoreContract.scoreOpeningPointer(context.lowerAtlasText()));
        if (rationale.isBlank()) {
            result.add(id(), "Executive Context has no score motivation pointer", context.atlasHtml(),
                    "Add a collapsed Motivatie scor subsection that points to the score support, Overall formula and source trace.");
            return;
        }
        for (String section : REQUIRED_SCORE_POINTER_SECTIONS) {
            if (!rationale.contains(section)) {
                result.add(id(), "Score motivation does not point to section " + section, context.atlasHtml(),
                        "Point score readers to 4.5 score support, 4.6 Overall formula and 5.1 source-to-business score trace.");
            }
        }
        if (!rationale.contains("score-card") && !rationale.contains("card")) {
            result.add(id(), "Score motivation does not explain where score cards are reopened",
                    context.atlasHtml(),
                    "Keep score motivation explicit: cards -> support table -> Overall formula -> source-to-business score support.");
        }
        if (!rationale.contains("overall")) {
            result.add(id(), "Score motivation does not explain where Overall is recalculated",
                    context.atlasHtml(),
                    "Point readers to the Overall formula so the atlas score is calculable, not decorative.");
        }
        if (!rationale.contains("5.1") && !rationale.contains("source-to-business") && !rationale.contains("trace")) {
            result.add(id(), "Score motivation does not point to source-to-business score support",
                    context.atlasHtml(),
                    "Point readers to the section that links score rationale back to business flow, source and layers.");
        }
    }
}
