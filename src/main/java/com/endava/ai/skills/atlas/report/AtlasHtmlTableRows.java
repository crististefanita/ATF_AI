package com.endava.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasHtmlTableRows {
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern TABLE_CELL = Pattern.compile("(?is)<t[dh]\\b[^>]*>(.*?)</t[dh]>");

    private AtlasHtmlTableRows() {
    }

    static List<List<String>> dataRowsFrom(String htmlFragment) {
        List<List<String>> rows = new ArrayList<>();
        Matcher rowMatcher = TABLE_ROW.matcher(htmlFragment);
        while (rowMatcher.find()) {
            String rowHtml = rowMatcher.group(1);
            if (rowHtml.toLowerCase(Locale.ROOT).contains("<th")) {
                continue;
            }
            List<String> cells = cells(rowHtml);
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }
        return Collections.unmodifiableList(rows);
    }

    private static List<String> cells(String rowHtml) {
        List<String> cells = new ArrayList<>();
        Matcher matcher = TABLE_CELL.matcher(rowHtml);
        while (matcher.find()) {
            cells.add(AtlasHtmlDocument.plain(matcher.group(1)));
        }
        return Collections.unmodifiableList(cells);
    }
}
