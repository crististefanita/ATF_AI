package com.endava.ai.skills.atlas.rules.scoring.cards;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.report.AtlasReportModel;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreContract;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Map;

public final class AtlasScoreCardsShapeRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-cards.shape";
    }

    @Override
    public String expected() {
        return "Candidate atlas opens with comprehension score cards plus Overall as the final/rightmost card.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        AtlasReportModel report = AtlasReportModel.parse(context.atlasText());
        if (!report.hasScoreWallOrCards()) {
            result.add(id(), "Missing score-card wall before Executive Context", context.atlasHtml(),
                    "Add a score-card strip near the top, modeled after html-ex reports.");
        }
        Map<String, Integer> cards = report.scoreCardScoresByLabel();
        for (String card : AtlasScoreContract.ALL_CARDS) {
            if (!cards.containsKey(card)) {
                result.add(id(), "Missing atlas score card axis: " + card, context.atlasHtml(),
                        AtlasScoreContract.REPAIR_CANONICAL_CARDS);
            }
        }
        if (!report.scoreCardsOpenBeforeExecutiveContext()) {
            result.add(id(), "Score cards appear after Executive Context", context.atlasHtml(),
                    "Move score cards above Executive Context so the opening shape matches html-ex.");
        }

        if (!report.overallScoreCardIsLast()) {
            result.add(id(), "Overall is not the final score-card axis", context.atlasHtml(),
                    "Place Overall as the last/rightmost score card; it is a derived/capped value, not the first card.");
        }
    }
}
