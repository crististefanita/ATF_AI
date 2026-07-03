package com.ai.skills.mcp.html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlDetailsParser {
    public List<String> tableRowsOrListItems(String html) {
        List<String> rows = new ArrayList<>();
        Matcher tr = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>").matcher(html);
        while (tr.find()) {
            rows.add(tr.group(1));
        }
        if (!rows.isEmpty()) {
            return rows;
        }
        Matcher li = Pattern.compile("(?is)<li\\b[^>]*>(.*?)</li>").matcher(html);
        while (li.find()) {
            rows.add(li.group(1));
        }
        return rows;
    }
}
