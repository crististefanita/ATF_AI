package com.endava.ai.skills.mcp.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlScoreExtractor {
    private final HtmlSectionFinder sections = new HtmlSectionFinder();

    public Integer firstScoreValue(String html, String label) {
        Integer direct = extractIntValue(html, label);
        if (direct != null) {
            return direct;
        }
        String text = sections.stripTags(html);
        Matcher matcher = Pattern.compile("(?is)\\b" + Pattern.quote(label) + "\\b\\D{0,80}(\\d{1,3})\\b")
                .matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    public Integer extractIntValue(String content, String key) {
        Pattern pattern = Pattern.compile("(?is)[\"']?" + Pattern.quote(key) + "[\"']?\\s*[:=]?\\s*[\"']?(\\d+)[\"']?");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    public String extractStringValue(String content, String key) {
        Pattern pattern = Pattern.compile("(?is)[\"']?" + Pattern.quote(key) + "[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
}
