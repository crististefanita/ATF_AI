package com.ai.core.reporting.adapters.extent.patch.normalize;

import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

public final class ExtentHtmlNormalizer {
    private static final Pattern FQCN_DETAIL_LINE = Pattern.compile(
            "<div class=\"m-t-10 m-l-5\">[a-zA-Z0-9_$]+(?:\\.[a-zA-Z0-9_$]+)+</div>"
    );
    private static final Pattern CARD_TITLE = Pattern.compile("<h6 class=\"card-title\">.*?</h6>");
    private static final Pattern BOLD_COUNT = Pattern.compile("<b[^>]*>(\\d+)</b>");
    private static final List<HtmlReplacement> LITERAL_REPLACEMENTS = List.of(
            replacement("\u00E2\u20AC\u00AF", " "),
            replacement("\u00E2\u0080\u00AF", " "),
            replacement("\u00C2\u00A0", " "),
            replacement("\u202F", " "),
            replacement("\u00A0", " "),
            replacement("class=\"fa fa-tag\"", "class=\"fa fa-bug\"")
    );

    private ExtentHtmlNormalizer() {
    }

    public static ExtentHtmlDocument normalize(ExtentHtmlDocument document) {
        String html = document.html();
        for (HtmlReplacement replacement : LITERAL_REPLACEMENTS) {
            html = replacement.apply(html);
        }
        html = normalizeSparkScoreCards(html);
        html = FQCN_DETAIL_LINE.matcher(html).replaceAll("");
        return ExtentHtmlDocument.of(html);
    }

    private static HtmlReplacement replacement(String source, String target) {
        return new HtmlReplacement(source, target);
    }

    private static String normalizeSparkScoreCards(String html) {
        String normalized = normalizeAnalysisCard(
                html,
                "parent-analysis",
                ExtentDashboardLabels.TEST_CASES,
                ExtentDashboardLabels.TEST_CASE_NOUN
        );
        normalized = normalizeAnalysisCard(
                normalized,
                "child-analysis",
                ExtentDashboardLabels.TESTS,
                ExtentDashboardLabels.TEST_NOUN
        );
        return normalized;
    }

    private static String normalizeAnalysisCard(String html, String canvasId, String title, String noun) {
        int canvas = indexOfCanvas(html, canvasId);
        if (canvas < 0) {
            return html;
        }
        int cardStart = html.lastIndexOf("<div class=\"card\"", canvas);
        if (cardStart < 0) {
            return html;
        }
        int cardEnd = html.indexOf("<div class=\"card\"", canvas + canvasId.length());
        if (cardEnd < 0) {
            cardEnd = html.length();
        }
        String card = html.substring(cardStart, cardEnd);
        if (card.contains("atf-runtime-dashboard-tests-card")) {
            String normalizedCard = ensureExplicitTotal(card, noun);
            return html.substring(0, cardStart) + normalizedCard + html.substring(cardEnd);
        }
        String normalized = CARD_TITLE.matcher(card)
                .replaceFirst("<h6 class=\"card-title\">" + title + "</h6>");
        normalized = normalizeCardStatusNoun(normalized, noun);
        return html.substring(0, cardStart) + normalized + html.substring(cardEnd);
    }

    private static String ensureExplicitTotal(String card, String noun) {
        if (card.contains(ExtentHtmlStructure.dataAttributeEquals(ExtentDataAttributes.TEST_CASES_TOTAL, "true"))) {
            return card;
        }
        int total = sumBoldCounts(card);
        return card.replaceFirst(
                "<div class=\"card-body\">",
                "<div class=\"card-body\"><h3 "
                        + ExtentHtmlStructure.dataAttributeEquals(ExtentDataAttributes.TEST_CASES_TOTAL, "true") + ">"
                        + total
                        + "</h3><p>total " + noun + "</p>"
        );
    }

    private static int sumBoldCounts(String card) {
        java.util.regex.Matcher matcher = BOLD_COUNT.matcher(card);
        int total = 0;
        while (matcher.find()) {
            total += Integer.parseInt(matcher.group(1));
        }
        return total;
    }

    private static int indexOfCanvas(String html, String canvasId) {
        int singleQuoted = html.indexOf("id='" + canvasId + "'");
        if (singleQuoted >= 0) {
            return singleQuoted;
        }
        return html.indexOf("id=\"" + canvasId + "\"");
    }

    private static String normalizeCardStatusNoun(String card, String noun) {
        String normalized = card;
        normalized = normalized.replaceAll(
                "(<b>\\d+</b>)\\s*(?:test cases|test classes|tests|steps|configuration methods) passed",
                "$1 " + noun + " passed"
        );
        normalized = normalized.replaceAll(
                "(<b>\\d+</b>)\\s*(?:test cases|test classes|tests|steps|configuration methods) failed",
                "$1 " + noun + " failed"
        );
        return normalized;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class HtmlReplacement {
        private final String source;
        private final String target;

        private String apply(String html) {
            return html.replace(source, target);
        }
    }

}
