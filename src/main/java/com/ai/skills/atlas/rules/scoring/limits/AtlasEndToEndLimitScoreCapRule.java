package com.ai.skills.atlas.rules.scoring.limits;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreContract;

public final class AtlasEndToEndLimitScoreCapRule implements AtlasValidationRule {
    private static final int MANY_MAJOR_LIMITS = 10;
    private static final int EXCELLENT_OVERALL = 95;

    @Override
    public String id() {
        return "atlas.candidate.end-to-end-limit.score-cap";
    }

    @Override
    public String expected() {
        return "Many major end-to-end comprehension limits cap Overall below excellent range unless the cap is explicit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String auditText = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (auditText.isBlank()) {
            result.add(id(), "Cannot audit end-to-end score cap because score support text is missing",
                    context.atlasHtml(),
                    "Publish score support, Overall formula and comprehension-limit rationale before using an excellent Overall.");
            return;
        }

        int overall = AtlasScoreContract.scoreNearLast(auditText, "overall");
        if (overall < 0) {
            result.add(id(), "Cannot audit end-to-end score cap because Overall is missing",
                    context.atlasHtml(),
                    "Add an Overall row in the atlas score formula.");
            return;
        }

        int majorLimits = AtlasSemanticVocabulary.countAny(auditText, AtlasSemanticVocabulary.LIMIT_MARKERS);
        boolean explicitCap = AtlasSemanticVocabulary.containsAny(auditText, AtlasSemanticVocabulary.CAP_MARKERS);

        if (majorLimits >= MANY_MAJOR_LIMITS && overall >= EXCELLENT_OVERALL) {
            result.add(id(), "Overall is too high for the declared end-to-end limits: overall="
                            + overall + ", limit markers=" + majorLimits,
                    context.atlasHtml(),
                    "When the atlas exposes many partial/diagnostic/incomplete flows, cap Overall below 95 or explain a colder cap.");
        }
        if (majorLimits >= MANY_MAJOR_LIMITS && !explicitCap) {
            result.add(id(), "Many end-to-end limits are listed but the score rationale does not expose a cap",
                    context.atlasHtml(),
                    "Connect the declared comprehension limits to an explicit score cap in the score support or Overall formula.");
        }
    }

}
