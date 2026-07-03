package com.ai.skills.atlas.rules.scoring;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasScoreRationaleAuditRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-rationale.audit";
    }

    @Override
    public String expected() {
        return "Each atlas score card has a matching score rationale row with axis name, numeric score, and explanation.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String auditText = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (auditText.isBlank()) {
            result.add(id(), "Missing score audit trail", context.atlasHtml(),
                    "Add collapsed score audit sections: opening pointer, support rows, Overall formula and source trace.");
            return;
        }

        for (String axis : AtlasScoreContract.ALL_CARDS) {
            if (!auditText.contains(axis)) {
                result.add(id(), "Score rationale does not explain score card: " + axis, context.atlasHtml(),
                        "Every score-card axis must appear again in score support, formula or source trace.");
                continue;
            }
            if (AtlasScoreContract.scoreNear(auditText, axis) < 0) {
                result.add(id(), "Score rationale has no numeric score for: " + axis, context.atlasHtml(),
                        "Every score audit row must include a numeric 0-100 score so the atlas can be audited.");
            }
        }
    }

    static String scoreRationale(String html) {
        return AtlasScoreContract.scoreOpeningPointer(html);
    }
}
