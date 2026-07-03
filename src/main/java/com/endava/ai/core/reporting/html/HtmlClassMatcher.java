package com.endava.ai.core.reporting.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small HTML helper for framework-generated reports.
 * It treats CSS classes as whitespace-separated tokens, so {@code score}
 * cannot accidentally match {@code score-card}.
 */
public final class HtmlClassMatcher {
    private static final Pattern CLASS_ATTRIBUTE = Pattern.compile("(?is)class\\s*=\\s*['\"]([^'\"]*)['\"]");

    private HtmlClassMatcher() {
    }

    public static boolean hasClass(String html, String className) {
        Matcher matcher = CLASS_ATTRIBUTE.matcher(html);
        while (matcher.find()) {
            if (classListContains(matcher.group(1), className)) {
                return true;
            }
        }
        return false;
    }

    public static boolean attributesContainClass(String attributes, String className) {
        Matcher matcher = CLASS_ATTRIBUTE.matcher(attributes);
        return matcher.find() && classListContains(matcher.group(1), className);
    }

    public static int countClass(String html, String className) {
        Matcher matcher = CLASS_ATTRIBUTE.matcher(html);
        int count = 0;
        while (matcher.find()) {
            if (classListContains(matcher.group(1), className)) {
                count++;
            }
        }
        return count;
    }

    public static int firstClassIndex(String html, String className) {
        Matcher matcher = CLASS_ATTRIBUTE.matcher(html);
        while (matcher.find()) {
            if (classListContains(matcher.group(1), className)) {
                return matcher.start();
            }
        }
        return -1;
    }

    public static String firstElementBodyByClass(String html, String tagName, String className) {
        Matcher matcher = elementPattern(tagName).matcher(html);
        while (matcher.find()) {
            if (attributesContainClass(matcher.group(1), className)) {
                return matcher.group(2);
            }
        }
        return "";
    }

    private static Pattern elementPattern(String tagName) {
        if (!tagName.matches("[A-Za-z][A-Za-z0-9-]*")) {
            throw new IllegalArgumentException("Invalid HTML tag name: " + tagName);
        }
        return Pattern.compile("(?is)<" + tagName + "\\b([^>]*)>(.*?)</" + tagName + ">");
    }

    private static boolean classListContains(String classList, String expected) {
        for (String actual : classList.trim().split("\\s+")) {
            if (actual.equals(expected)) {
                return true;
            }
        }
        return false;
    }
}
