package com.endava.ai.skills.atlas.rules;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasText {
    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_LABEL_CHARACTER = Pattern.compile("[^a-z0-9 ]");

    private AtlasText() {
    }

    public static int count(String text, String token) {
        if (text == null || token == null || token.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    public static int count(Pattern pattern, String text) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static boolean containsAll(String text, String... tokens) {
        if (text == null) {
            return false;
        }
        return Arrays.stream(tokens).allMatch(text::contains);
    }

    public static String sectionAfter(String text, String marker, int maxChars) {
        if (text == null || marker == null) {
            return "";
        }
        int index = text.indexOf(marker);
        if (index < 0) {
            return "";
        }
        return text.substring(index, Math.min(text.length(), index + maxChars));
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

    public static int firstExistingIndex(String text, String... tokens) {
        if (text == null) {
            return -1;
        }
        int first = -1;
        for (String token : tokens) {
            int index = text.indexOf(token);
            if (index >= 0 && (first < 0 || index < first)) {
                first = index;
            }
        }
        return first;
    }

    public static String stripTags(String html) {
        if (html == null) {
            return "";
        }
        return TAG_PATTERN.matcher(html).replaceAll(" ");
    }

    public static String normalizeWhitespace(String text) {
        if (text == null) {
            return "";
        }
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }

    public static String normalizeLabel(String text) {
        if (text == null) {
            return "";
        }
        String lowerCaseText = text.toLowerCase(Locale.ROOT);
        return normalizeWhitespace(NON_LABEL_CHARACTER.matcher(lowerCaseText).replaceAll(" "));
    }

    public static String plainText(String html) {
        return normalizeWhitespace(stripTags(html));
    }

    public static String abbreviate(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "...";
    }
}
