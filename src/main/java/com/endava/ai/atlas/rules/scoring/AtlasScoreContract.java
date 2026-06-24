package com.endava.ai.atlas.rules.scoring;

import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasScoreContract {
    static final List<String> BUSINESS_AXES = List.of(
            "intelegere business",
            "reconstructie flow",
            "trasabilitate layere",
            "claritate obiect business",
            "fidelitate dovada");

    static final List<String> ALL_CARDS = List.of(
            "intelegere business",
            "reconstructie flow",
            "trasabilitate layere",
            "claritate obiect business",
            "fidelitate dovada",
            "overall");

    static final String REPAIR_CANONICAL_CARDS = "Use canonical atlas score cards: Intelegere Business, "
            + "Reconstructie Flow, Trasabilitate Layere, Claritate Obiect Business, Fidelitate Dovada, Overall.";

    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
    private static final Pattern SCORE_CARD_PATTERN = Pattern.compile(
            "(?is)<article\\b[^>]*class=\"[^\"]*score-card[^\"]*\"[^>]*>(.*?)</article>");
    private static final Pattern LABEL_PATTERN = Pattern.compile("(?is)<div\\b[^>]*class=\"[^\"]*label[^\"]*\"[^>]*>(.*?)</div>");
    private static final Pattern SCORE_PATTERN = Pattern.compile("(?is)<div\\b[^>]*class=\"[^\"]*score[^\"]*\"[^>]*>(\\d{1,3})</div>");

    private AtlasScoreContract() {
    }

    static String normalized(String text) {
        return TAG_PATTERN.matcher(text.toLowerCase(Locale.ROOT))
                .replaceAll(" ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    static String sectionAfter(String text, String startToken, String endToken) {
        int start = text.indexOf(startToken);
        if (start < 0) {
            return "";
        }
        int end = text.indexOf(endToken, start);
        if (end < 0) {
            return text.substring(start);
        }
        return text.substring(start, end + endToken.length());
    }

    static String sectionBetween(String text, String startToken, String endToken) {
        int start = text.indexOf(startToken);
        if (start < 0) {
            return "";
        }
        int end = text.indexOf(endToken, start + startToken.length());
        if (end < 0) {
            return text.substring(start);
        }
        return text.substring(start, end);
    }

    static String scoreOpeningPointer(String html) {
        for (String summary : List.of(
                "<summary>motivatie scor",
                "<summary>unde se auditeaza scorul",
                "<summary>score rationale")) {
            String section = sectionAfter(html, summary, "</details>");
            if (!section.isBlank()) {
                return section;
            }
        }
        return "";
    }

    static String scoreSupportAuditText(String html) {
        return scoreOpeningPointer(html)
                + " " + sectionBetween(html, "4.5 suport scor", "4.6 formula overall")
                + " " + scoreFormulaSection(html)
                + " " + sectionAfter(html, "5.1 suport scor", "</details>");
    }

    static String scoreFormulaSection(String html) {
        return sectionAfter(html, "4.6 formula overall", "</details>");
    }

    static int scoreNear(String text, String axis) {
        Matcher matcher = Pattern.compile(Pattern.quote(axis) + ".{0,180}?(\\d{1,3})", Pattern.DOTALL)
                .matcher(text);
        if (!matcher.find()) {
            return -1;
        }
        int score = Integer.parseInt(matcher.group(1));
        return score >= 0 && score <= 100 ? score : -1;
    }

    static int scoreNearLast(String text, String axis) {
        String row = lastRowContext(text, axis);
        if (row.isBlank()) {
            return -1;
        }
        return scoreNear(row, axis);
    }

    static Map<String, Integer> scoreCards(String html) {
        Map<String, Integer> cards = new LinkedHashMap<>();
        Matcher cardMatcher = SCORE_CARD_PATTERN.matcher(html);
        while (cardMatcher.find()) {
            String card = cardMatcher.group(1);
            String label = firstNormalized(LABEL_PATTERN, card);
            String scoreText = firstNormalized(SCORE_PATTERN, card);
            if (!label.isBlank() && !scoreText.isBlank()) {
                cards.put(label, Integer.parseInt(scoreText));
            }
        }
        return cards;
    }

    static String rowContext(String text, String label) {
        int start = text.indexOf(label);
        if (start < 0) {
            return "";
        }
        int end = text.length();
        for (String knownLabel : scoreCardsFromKnownVocabulary(text)) {
            int next = text.indexOf(knownLabel, start + label.length());
            if (next > start && next < end) {
                end = next;
            }
        }
        return text.substring(start, Math.min(end, start + 700));
    }

    static String lastRowContext(String text, String label) {
        int start = text.lastIndexOf(label);
        if (start < 0) {
            return "";
        }
        return text.substring(start, Math.min(text.length(), start + 700));
    }

    private static List<String> scoreCardsFromKnownVocabulary(String ignored) {
        return List.of(
                "intelegere business",
                "reconstructie flow",
                "reconstructie flux",
                "trasabilitate layere",
                "claritate obiect business",
                "fidelitate dovada",
                "overall");
    }

    private static String firstNormalized(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return "";
        }
        return normalized(matcher.group(1));
    }
}
