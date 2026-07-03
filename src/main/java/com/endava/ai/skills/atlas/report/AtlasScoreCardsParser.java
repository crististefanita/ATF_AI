package com.endava.ai.skills.atlas.report;

import com.endava.ai.core.reporting.html.HtmlClassMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasScoreCardsParser {
    private static final Pattern ARTICLE = Pattern.compile("(?is)<article\\b([^>]*)>(.*?)</article>");
    private static final Pattern DATA_ATLAS_SCORE_CARD = Pattern.compile(
            "(?is)data-atlas-role\\s*=\\s*['\"][^'\"]*\\bscore-card\\b[^'\"]*['\"]");
    private static final Pattern PARAGRAPH = Pattern.compile("(?is)<p\\b[^>]*>(.*?)</p>");

    List<AtlasReportModel.ScoreCard> parse(AtlasHtmlDocument document) {
        List<AtlasReportModel.ScoreCard> cards = new ArrayList<>();
        Matcher matcher = ARTICLE.matcher(document.html());
        while (matcher.find()) {
            String attributes = matcher.group(1);
            if (!isScoreCardArticle(attributes)) {
                continue;
            }
            String body = matcher.group(2);
            String label = AtlasHtmlDocument.plain(HtmlClassMatcher.firstElementBodyByClass(body, "div", "label"));
            String scoreText = AtlasHtmlDocument.plain(HtmlClassMatcher.firstElementBodyByClass(body, "div", "score"));
            if (label.isBlank() || scoreText.isBlank()) {
                continue;
            }
            cards.add(new AtlasReportModel.ScoreCard(
                    label,
                    Integer.parseInt(scoreText),
                    AtlasHtmlDocument.firstPlain(PARAGRAPH, body)));
        }
        return Collections.unmodifiableList(cards);
    }

    private boolean isScoreCardArticle(String attributes) {
        return HtmlClassMatcher.attributesContainClass(attributes, "score-card")
                || DATA_ATLAS_SCORE_CARD.matcher(attributes).find();
    }
}
