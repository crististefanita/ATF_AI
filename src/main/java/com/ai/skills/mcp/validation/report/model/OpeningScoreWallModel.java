package com.ai.skills.mcp.validation.report.model;

import com.ai.core.reporting.html.HtmlClassMatcher;

import java.util.regex.Pattern;

public final class OpeningScoreWallModel {
    private static final Pattern COMPRESSED_SCORE_CARD = Pattern.compile(
            "(?is)<span>\\s*(Business Substance|Business Graph|Historical Reopenability|Proof Chain|Contract Artifact)\\s*</span>\\s*<strong>\\s*\\d+\\s*</strong>");

    private final String html;
    private final int heroIndex;
    private final int scoreWallIndex;
    private final int executiveContextIndex;

    private OpeningScoreWallModel(String html) {
        this.html = html == null ? "" : html;
        this.heroIndex = firstIndexOf(this.html, "class=\"hero\"", "class='hero'", " hero ");
        this.scoreWallIndex = firstIndexOf(this.html, "class=\"score-wall\"", "class='score-wall'", "score-wall");
        this.executiveContextIndex = firstIndexOf(this.html, "id=\"executive-context\"", "id='executive-context'",
                "Executive Context");
    }

    public static OpeningScoreWallModel fromHtml(String html) {
        return new OpeningScoreWallModel(html);
    }

    public boolean hasScoreWallBeforeExecutiveContext() {
        return scoreWallIndex >= 0 && executiveContextIndex >= 0 && scoreWallIndex < executiveContextIndex;
    }

    public boolean hasHeroBeforeScoreWall() {
        return heroIndex >= 0 && scoreWallIndex >= 0 && heroIndex < scoreWallIndex;
    }

    public int scoreCardCount() {
        return HtmlClassMatcher.countClass(openingScoreWall(), "score-card");
    }

    public int labelCount() {
        return HtmlClassMatcher.countClass(openingScoreWall(), "label");
    }

    public int scoreCount() {
        return HtmlClassMatcher.countClass(openingScoreWall(), "score");
    }

    public boolean hasCompressedSpanStrongScoreCard() {
        return COMPRESSED_SCORE_CARD.matcher(openingScoreWall()).find();
    }

    public boolean hasScoreCardChildren() {
        return firstScoreCardIndex() >= 0;
    }

    public boolean hasLooseLabelOrScoreBeforeFirstCard() {
        int firstCard = firstScoreCardIndex();
        if (firstCard < 0) {
            return false;
        }
        String beforeFirstCard = openingScoreWall().substring(0, firstCard);
        return HtmlClassMatcher.hasClass(beforeFirstCard, "label")
                || HtmlClassMatcher.hasClass(beforeFirstCard, "score");
    }

    public boolean hasRuntimeStripBetweenScoreWallAndExecutiveContext() {
        String between = openingScoreWall();
        return HtmlClassMatcher.hasClass(between, "runtime-strip")
                || between.contains("RUN_EXPECTED_DELTA_QUALITY")
                || between.contains("ROUND_TARGET_TEST_COUNT")
                || between.contains("META_ITERATION_COUNT")
                || between.contains("runs consumed")
                || between.contains("tests produced");
    }

    public boolean hasVerdictOrRuntimeBeforeScoreCards() {
        if (!hasHeroBeforeScoreWall()) {
            return false;
        }
        String beforeScoreCards = html.substring(heroIndex, scoreWallIndex);
        return beforeScoreCards.contains("<h2>Verdict</h2>")
                || HtmlClassMatcher.hasClass(beforeScoreCards, "runtime-strip")
                || beforeScoreCards.contains("RUN_EXPECTED_DELTA_QUALITY")
                || beforeScoreCards.contains("ROUND_TARGET_TEST_COUNT")
                || beforeScoreCards.contains("META_ITERATION_COUNT");
    }

    private int firstScoreCardIndex() {
        return HtmlClassMatcher.firstClassIndex(openingScoreWall(), "score-card");
    }

    private String openingScoreWall() {
        if (!hasScoreWallBeforeExecutiveContext()) {
            return "";
        }
        return html.substring(scoreWallIndex, executiveContextIndex);
    }

    private static int firstIndexOf(String source, String... markers) {
        int first = -1;
        for (String marker : markers) {
            int index = source.indexOf(marker);
            if (index >= 0 && (first < 0 || index < first)) {
                first = index;
            }
        }
        return first;
    }
}
