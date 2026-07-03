package com.endava.ai.skills.atlas.rules.scoring;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasScoreOverallCalculationRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-overall.calculation";
    }

    @Override
    public String expected() {
        return "Overall is a derived comprehension score: calculable from the five axis scores and explicitly not package/html-ex readiness.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String normalized = AtlasScoreContract.normalized(AtlasScoreContract.scoreFormulaSection(context.lowerAtlasText()));
        if (normalized.isBlank()) {
            result.add(id(), "Cannot calculate Overall because 4.6 formula is missing", context.atlasHtml(),
                    "Publish a 4.6 Overall formula table before publishing an Overall score.");
            return;
        }

        int sum = 0;
        int min = 101;
        int max = -1;
        for (String axis : AtlasScoreContract.BUSINESS_AXES) {
            int score = AtlasScoreContract.scoreNear(normalized, axis);
            if (score < 0) {
                result.add(id(), "Cannot calculate Overall because axis score is missing: " + axis,
                        context.atlasHtml(), "Add all five axis scores to the score rationale table.");
                return;
            }
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
        }

        int overall = AtlasScoreContract.scoreNearLast(normalized, "overall");
        if (overall < 0) {
            result.add(id(), "Overall score is missing from score rationale", context.atlasHtml(),
                    "Add an Overall row that explains how the atlas comprehension score was derived.");
            return;
        }

        int roundedAverage = Math.round((float) sum / AtlasScoreContract.BUSINESS_AXES.size());
        if (Math.abs(overall - roundedAverage) > 1) {
            result.add(id(), "Overall is not explainable from axis scores: overall=" + overall
                            + ", rounded average=" + roundedAverage,
                    context.atlasHtml(),
                    "Keep Overall within one point of the rounded axis average unless a colder cap is explicitly documented.");
        }
        if (overall > max) {
            result.add(id(), "Overall exceeds the strongest axis: overall=" + overall + ", max axis=" + max,
                    context.atlasHtml(), "Overall cannot be higher than every component axis.");
        }
        if (overall < min - 3) {
            result.add(id(), "Overall is much lower than the weakest axis without visible cap: overall=" + overall
                            + ", weakest axis=" + min,
                    context.atlasHtml(), "If Overall is capped far below the axes, explain the cap in the score rationale.");
        }
        if ((!normalized.contains("derivat") && !normalized.contains("plafonat") && !normalized.contains("rotunjit"))
                || (!normalized.contains("readiness") && !normalized.contains("pachet"))) {
            result.add(id(), "Overall rationale does not say it is derived comprehension, not package readiness",
                    context.atlasHtml(),
                    "State that Overall is derived from business understanding and is not package/html-ex readiness.");
        }
    }
}
