package com.endava.ai.atlas.rules.scoring;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

public final class AtlasScoreOpeningAuditPointerRule implements AtlasValidationRule {
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
        for (String section : java.util.List.of("4.5", "4.6", "5.1")) {
            if (!rationale.contains(section)) {
                result.add(id(), "Score motivation does not point to section " + section, context.atlasHtml(),
                        "Point score readers to 4.5 score support, 4.6 Overall formula and 5.1 source-to-business score trace.");
            }
        }
        for (String token : java.util.List.of("score-card", "overall", "trace")) {
            if (!rationale.contains(token)) {
                result.add(id(), "Score motivation misses audit token: " + token, context.atlasHtml(),
                        "Keep score motivation explicit: score-card -> support table -> Overall formula -> source trace.");
            }
        }
    }
}
