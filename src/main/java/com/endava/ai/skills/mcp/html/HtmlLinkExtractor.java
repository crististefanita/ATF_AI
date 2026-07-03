package com.endava.ai.skills.mcp.html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlLinkExtractor {
    private static final Pattern LOCAL_LINK_PATTERN = Pattern.compile("(?i)href\\s*=\\s*[\"']([^\"']+)[\"']");

    public List<String> hrefs(String html) {
        List<String> hrefs = new ArrayList<>();
        Matcher matcher = LOCAL_LINK_PATTERN.matcher(html);
        while (matcher.find()) {
            hrefs.add(matcher.group(1).trim());
        }
        return hrefs;
    }
}
