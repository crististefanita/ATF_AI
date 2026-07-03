package com.ai.skills.atlas.rules.scoring.limits;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreContract;

public final class AtlasExcellentOverallLimitDisciplineRule implements AtlasValidationRule {
    private static final int EXCELLENT_OVERALL = 95;
    private static final int MANY_LIMITS = 8;

    @Override
    public String id() {
        return "atlas.candidate.95-plus.limit-discipline";
    }

    @Override
    public String expected() {
        return "Overall 95+ is allowed only when declared limits are few or explicitly capped by object maturity evidence.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String auditText = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (auditText.isBlank()) {
            result.add(id(), "Cannot audit 95+ discipline because score support text is missing",
                    context.atlasHtml(),
                    "Publish score support, Overall formula and comprehension-limit rationale before publishing a high Overall.");
            return;
        }

        int overall = AtlasScoreContract.scoreNearLast(auditText, "overall");
        int limits = AtlasSemanticVocabulary.countAny(auditText, AtlasSemanticVocabulary.LIMIT_MARKERS);
        boolean explicit95Discipline = AtlasSemanticVocabulary.containsAny(auditText, AtlasSemanticVocabulary.CAP_MARKERS);

        if (overall >= EXCELLENT_OVERALL && limits >= MANY_LIMITS) {
            result.add(id(), "Overall 95+ is too high for many declared incomplete/partial flows: overall="
                            + overall + ", limit markers=" + limits,
                    context.atlasHtml(),
                    "Keep Overall below 95 or explain why object maturity makes the high score defensible.");
        }
        if (overall >= EXCELLENT_OVERALL && !explicit95Discipline) {
            result.add(id(), "Overall 95+ has no visible 95+ discipline",
                    context.atlasHtml(),
                    "Explain why the atlas crosses 95+ and how suite incompleteness is separated from atlas quality.");
        }
    }

}
