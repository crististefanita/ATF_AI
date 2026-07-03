package com.ai.skills.mcp.tests.support;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public final class HtmlDetailsMutator {
    private HtmlDetailsMutator() {
    }

    public static String closeDetailsSection(String html, String summaryText) {
        SectionRange range = findDetailsSection(html, summaryText);
        String block = html.substring(range.start(), range.end());
        int openingEnd = block.indexOf('>');
        if (openingEnd < 0) {
            throw new IllegalStateException("Malformed <details> section for summary: " + summaryText);
        }
        String opening = block.substring(0, openingEnd + 1);
        String closedOpening = opening.replaceFirst("(?i)\\s+open(?=\\s|>)", "");
        if (opening.equals(closedOpening)) {
            throw new IllegalStateException("Expected default-open <details> for summary: " + summaryText);
        }
        return html.substring(0, range.start())
                + closedOpening
                + block.substring(openingEnd + 1)
                + html.substring(range.end());
    }

    public static String replaceDetailsSection(String html, String summaryText, String replacementBody) {
        SectionRange range = findDetailsSection(html, summaryText);
        String replacement = "<details class=\"card\"><summary><span class=\"summary-title\">"
                + summaryText
                + "</span></summary>"
                + replacementBody
                + "</details>";
        return html.substring(0, range.start()) + replacement + html.substring(range.end());
    }

    private static SectionRange findDetailsSection(String html, String summaryText) {
        var matcher = Pattern.compile("(?is)<details\\b").matcher(html);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matchingDetailsEnd(html, start);
            if (end < 0) {
                continue;
            }
            String block = html.substring(start, end);
            if (firstSummaryText(block).contains(summaryText)) {
                return new SectionRange(start, end);
            }
        }
        throw new IllegalStateException("Could not find <details> section with summary: " + summaryText);
    }

    private static int matchingDetailsEnd(String html, int detailsStart) {
        var matcher = Pattern.compile("(?is)</?details\\b[^>]*>").matcher(html);
        matcher.region(detailsStart, html.length());
        int depth = 0;
        while (matcher.find()) {
            if (matcher.group().startsWith("</")) {
                depth--;
            } else {
                depth++;
            }
            if (depth == 0) {
                return matcher.end();
            }
        }
        return -1;
    }

    private static String firstSummaryText(String detailsBlock) {
        var matcher = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>").matcher(detailsBlock);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SectionRange {
        private final int start;
        private final int end;
    }
}
