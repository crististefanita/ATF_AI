package com.endava.ai.atlas.rules.scoring;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasScoreCardReadabilityRule implements AtlasValidationRule {
    private static final int MAX_SCORE_CARD_DESCRIPTION_CHARS = 88;
    private static final Pattern SCORE_CARD_PATTERN = Pattern.compile(
            "(?is)<article\\b[^>]*class=\"[^\"]*score-card[^\"]*\"[^>]*>(.*?)</article>");
    private static final Pattern LABEL_PATTERN = Pattern.compile("(?is)<div\\b[^>]*class=\"[^\"]*label[^\"]*\"[^>]*>(.*?)</div>");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?is)<p\\b[^>]*>(.*?)</p>");

    @Override
    public String id() {
        return "atlas.candidate.score-cards.readable";
    }

    @Override
    public String expected() {
        return "Score-card descriptions are short enough to remain readable inside the card layout.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher cardMatcher = SCORE_CARD_PATTERN.matcher(context.atlasText());
        int cards = 0;
        while (cardMatcher.find()) {
            cards++;
            String card = cardMatcher.group(1);
            String label = extract(LABEL_PATTERN, card, "unknown score card");
            String description = AtlasScoreContract.normalized(extract(DESCRIPTION_PATTERN, card, ""));
            if (description.isBlank()) {
                result.add(id(), "Score card has no short description: " + label, context.atlasHtml(),
                        "Each score card needs a short one-line explanation.");
                continue;
            }
            if (description.length() > MAX_SCORE_CARD_DESCRIPTION_CHARS) {
                result.add(id(), "Score-card description is too long for readable layout: " + label
                                + " (" + description.length() + " chars)",
                        context.atlasHtml(),
                        "Keep score-card descriptions under " + MAX_SCORE_CARD_DESCRIPTION_CHARS
                                + " characters; move detail to the collapsed score rationale.");
            }
        }
        if (cards == 0) {
            result.add(id(), "No score-card articles found", context.atlasHtml(),
                    "Publish score cards as <article class=\"score-card\"> blocks so readability can be audited.");
        }
    }

    private static String extract(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return fallback;
        }
        return AtlasScoreContract.normalized(matcher.group(1));
    }
}
