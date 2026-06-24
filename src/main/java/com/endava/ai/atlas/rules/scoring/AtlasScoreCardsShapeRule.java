package com.endava.ai.atlas.rules.scoring;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

import java.util.ArrayList;
import java.util.List;
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
        String html = context.lowerAtlasText();
        String normalizedHtml = AtlasScoreContract.normalized(html);
        String scoreWall = AtlasScoreContract.normalized(sectionAfter(html, "score-wall", "</section>"));
        if (!html.contains("score-wall") && !html.contains("score-card")) {
            result.add(id(), "Missing score-card wall before Executive Context", context.atlasHtml(),
                    "Add a score-card strip near the top, modeled after html-ex reports.");
        }
        for (String card : AtlasScoreContract.ALL_CARDS) {
            if (!scoreWall.contains(card)) {
                result.add(id(), "Missing atlas score card axis: " + card, context.atlasHtml(),
                        AtlasScoreContract.REPAIR_CANONICAL_CARDS);
            }
        }
        int executiveIndex = normalizedHtml.indexOf("executive context");
        int firstScoreIndex = firstIndexOf(normalizedHtml, "score-wall", "score-card");
        if (executiveIndex >= 0 && firstScoreIndex >= 0 && firstScoreIndex > executiveIndex) {
            result.add(id(), "Score cards appear after Executive Context", context.atlasHtml(),
                    "Move score cards above Executive Context so the opening shape matches html-ex.");
        }

        Map<String, Integer> cards = AtlasScoreContract.scoreCards(context.atlasText());
        List<String> labels = new ArrayList<>(cards.keySet());
        if (!labels.isEmpty() && !"overall".equals(labels.get(labels.size() - 1))) {
            result.add(id(), "Overall is not the final score-card axis", context.atlasHtml(),
                    "Place Overall as the last/rightmost score card; it is a derived/capped value, not the first card.");
        }
    }

    private static int firstIndexOf(String text, String first, String second) {
        int firstIndex = text.indexOf(first);
        int secondIndex = text.indexOf(second);
        if (firstIndex < 0) {
            return secondIndex;
        }
        if (secondIndex < 0) {
            return firstIndex;
        }
        return Math.min(firstIndex, secondIndex);
    }

    private static String sectionAfter(String text, String startToken, String endToken) {
        return AtlasScoreContract.sectionAfter(text, startToken, endToken);
    }
}
