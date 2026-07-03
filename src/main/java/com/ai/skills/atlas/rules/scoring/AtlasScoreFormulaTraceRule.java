package com.ai.skills.atlas.rules.scoring;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class AtlasScoreFormulaTraceRule implements AtlasValidationRule {
    private static final int WEIGHT_LOOKAHEAD_CHARS = 180;
    private static final List<String> REQUIRED_FORMULA_TOKENS = List.of(
            "axa",
            "nota",
            "pondere",
            "contributie",
            "overall");

    @Override
    public String id() {
        return "atlas.candidate.score-formula.trace";
    }

    @Override
    public String expected() {
        return "Overall formula table repeats score-card axes, scores, weights and contribution so the result is recalculable.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Map<String, Integer> cards = AtlasScoreContract.scoreCards(context.atlasText());
        String formula = AtlasScoreContract.normalized(AtlasScoreContract.scoreFormulaSection(context.lowerAtlasText()));
        if (formula.isBlank()) {
            result.add(id(), "Missing 4.6 Overall formula section", context.atlasHtml(),
                    "Add a collapsed formula table with axis, score, weight, contribution and Overall.");
            return;
        }
        for (String token : REQUIRED_FORMULA_TOKENS) {
            if (!formula.contains(token)) {
                result.add(id(), "Overall formula misses required column/token: " + token,
                        context.atlasHtml(), "Formula must expose axis, score, weight, contribution and Overall.");
            }
        }
        for (Map.Entry<String, Integer> card : cards.entrySet()) {
            String label = card.getKey();
            String row = "overall".equals(label)
                    ? AtlasScoreContract.lastRowContext(formula, label)
                    : AtlasScoreContract.rowContext(formula, label);
            if (row.isBlank()) {
                result.add(id(), "Formula does not include score-card axis: " + label,
                        context.atlasHtml(), "Every score-card axis must appear in the Overall formula table.");
                continue;
            }
            int formulaScore = "overall".equals(label)
                    ? AtlasScoreContract.scoreNearLast(formula, label)
                    : AtlasScoreContract.scoreNear(formula, label);
            if (formulaScore != card.getValue()) {
                result.add(id(), "Formula score does not match score-card value: " + label,
                        context.atlasHtml(), "Keep score-card score and formula score synchronized.");
            }
            if (!"overall".equals(label) && !weightPattern(label).matcher(formula).find()) {
                result.add(id(), "Formula row has no weight percentage: " + label,
                        context.atlasHtml(), "Each component axis must publish its weight.");
            }
        }
        if (!formula.contains("rotunjit") && !formula.contains("calculat") && !formula.contains("plafonat")) {
            result.add(id(), "Formula does not explain how Overall is derived", context.atlasHtml(),
                    "State whether Overall is rounded/capped/calculated from the contribution sum.");
        }
    }

    private static Pattern weightPattern(String label) {
        return Pattern.compile(Pattern.quote(label) + ".{0," + WEIGHT_LOOKAHEAD_CHARS + "}?%",
                Pattern.DOTALL);
    }
}
