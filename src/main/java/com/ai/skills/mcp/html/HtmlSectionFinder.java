package com.ai.skills.mcp.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlSectionFinder {
    private static final Pattern DETAILS_BLOCK_WITH_SUMMARY =
            Pattern.compile("(?is)<details\\b([^>]*)>\\s*<summary>(.*?)</summary>(.*?)</details>");

    public String detailsBlockBody(String html, String summaryText) {
        Matcher matcher = DETAILS_BLOCK_WITH_SUMMARY.matcher(html);
        while (matcher.find()) {
            String summary = stripTags(matcher.group(2)).trim();
            if (summary.contains(summaryText)) {
                return matcher.group(3);
            }
        }
        return "";
    }

    public int openDetailsCount(String html) {
        int count = 0;
        Matcher matcher = DETAILS_BLOCK_WITH_SUMMARY.matcher(html);
        while (matcher.find()) {
            if (matcher.group(1).toLowerCase(java.util.Locale.ROOT).contains("open")) {
                count++;
            }
        }
        return count;
    }

    public boolean isDetailsOpen(String html, String summaryText) {
        Matcher matcher = DETAILS_BLOCK_WITH_SUMMARY.matcher(html);
        while (matcher.find()) {
            String summary = stripTags(matcher.group(2)).trim();
            if (summary.contains(summaryText)) {
                return matcher.group(1).toLowerCase(java.util.Locale.ROOT).contains("open");
            }
        }
        return false;
    }

    public String stripTags(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ");
    }
}
