package com.ai.skills.atlas.rules.scoring;

import com.ai.skills.atlas.report.AtlasReportModel;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasScoreContract {
    public static final List<String> BUSINESS_AXES = List.of(
            "intelegere business",
            "reconstructie flow",
            "trasabilitate layere",
            "claritate obiect business",
            "fidelitate dovada");

    public static final List<String> ALL_CARDS = List.of(
            "intelegere business",
            "reconstructie flow",
            "trasabilitate layere",
            "claritate obiect business",
            "fidelitate dovada",
            "overall");

    public static final String REPAIR_CANONICAL_CARDS = "Use canonical atlas score cards: Intelegere Business, "
            + "Reconstructie Flow, Trasabilitate Layere, Claritate Obiect Business, Fidelitate Dovada, Overall.";

    private static final int SCORE_LOOKAHEAD_CHARS = 180;
    private static final int SCORE_ROW_CONTEXT_CHARS = 700;
    private static final List<String> SCORE_OPENING_SUMMARIES = List.of(
            "<summary>motivatie scor",
            "<summary>unde se auditeaza scorul",
            "<summary>score rationale");
    private static final List<String> SCORE_CARD_VOCABULARY = List.of(
            "intelegere business",
            "reconstructie flow",
            "reconstructie flux",
            "trasabilitate layere",
            "claritate obiect business",
            "fidelitate dovada",
            "overall");

    private AtlasScoreContract() {
    }

    public static String normalized(String text) {
        return AtlasText.plainText(text).toLowerCase(Locale.ROOT);
    }

    public static String sectionAfter(String text, String startToken, String endToken) {
        if (text == null || startToken == null || endToken == null) {
            return "";
        }
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

    public static String sectionBetween(String text, String startToken, String endToken) {
        if (text == null || startToken == null || endToken == null) {
            return "";
        }
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

    public static String scoreOpeningPointer(String html) {
        for (String summary : SCORE_OPENING_SUMMARIES) {
            String section = sectionAfter(html, summary, "</details>");
            if (!section.isBlank()) {
                return section;
            }
        }
        return "";
    }

    public static String scoreSupportAuditText(String html) {
        return scoreOpeningPointer(html)
                + " " + sectionBetween(html, "4.5 suport scor", "4.6 formula overall")
                + " " + scoreFormulaSection(html)
                + " " + sectionAfter(html, "5.1 suport scor", "</details>");
    }

    public static String scoreFormulaSection(String html) {
        return sectionAfter(html, "4.6 formula overall", "</details>");
    }

    public static int scoreNear(String text, String axis) {
        if (text == null || axis == null || axis.isBlank()) {
            return -1;
        }
        Matcher matcher = scoreNearPattern(axis).matcher(text);
        if (!matcher.find()) {
            return -1;
        }
        int score = Integer.parseInt(matcher.group(1));
        return score >= 0 && score <= 100 ? score : -1;
    }

    public static int scoreNearLast(String text, String axis) {
        String row = lastRowContext(text, axis);
        if (row.isBlank()) {
            return -1;
        }
        return scoreNear(row, axis);
    }

    public static Map<String, Integer> scoreCards(String html) {
        return AtlasReportModel.parse(html).scoreCardScoresByLabel();
    }

    public static String rowContext(String text, String label) {
        if (text == null || label == null) {
            return "";
        }
        int start = text.indexOf(label);
        if (start < 0) {
            return "";
        }
        int end = text.length();
        for (String knownLabel : SCORE_CARD_VOCABULARY) {
            int next = text.indexOf(knownLabel, start + label.length());
            if (next > start && next < end) {
                end = next;
            }
        }
        return text.substring(start, Math.min(end, start + SCORE_ROW_CONTEXT_CHARS));
    }

    public static String lastRowContext(String text, String label) {
        if (text == null || label == null) {
            return "";
        }
        int start = text.lastIndexOf(label);
        if (start < 0) {
            return "";
        }
        return text.substring(start, Math.min(text.length(), start + SCORE_ROW_CONTEXT_CHARS));
    }

    private static Pattern scoreNearPattern(String axis) {
        return Pattern.compile(Pattern.quote(axis) + ".{0," + SCORE_LOOKAHEAD_CHARS + "}?(\\d{1,3})",
                Pattern.DOTALL);
    }

}
