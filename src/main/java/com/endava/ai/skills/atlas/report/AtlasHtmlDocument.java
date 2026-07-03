package com.endava.ai.skills.atlas.report;

import com.endava.ai.skills.atlas.rules.AtlasText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

final class AtlasHtmlDocument {
    private static final Pattern SUMMARY = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern DATA_SECTION_TAG = Pattern.compile(
            "(?is)<(?<tag>section|details)\\b(?<attributes>[^>]*data-atlas-section\\s*=\\s*['\"](?<section>[^'\"]+)['\"][^>]*)>");
    private final String html;
    private final String lowerHtml;

    private AtlasHtmlDocument(String html) {
        this.html = html == null ? "" : html;
        this.lowerHtml = this.html.toLowerCase(Locale.ROOT);
    }

    static AtlasHtmlDocument parse(String html) {
        return new AtlasHtmlDocument(html);
    }

    String html() {
        return html;
    }

    String lowerHtml() {
        return lowerHtml;
    }

    String sectionText(String startToken, String endToken) {
        String lowerStart = startToken.toLowerCase(Locale.ROOT);
        String lowerEnd = endToken.toLowerCase(Locale.ROOT);
        int start = lowerHtml.indexOf(lowerStart);
        if (start < 0) {
            return "";
        }
        int end = lowerHtml.indexOf(lowerEnd, start + lowerStart.length());
        return end < 0 ? html.substring(start) : html.substring(start, end);
    }

    String sectionByAtlasAttribute(String... attributeTokens) {
        Matcher matcher = DATA_SECTION_TAG.matcher(html);
        while (matcher.find()) {
            String attribute = matcher.group("section").toLowerCase(Locale.ROOT);
            boolean matches = true;
            for (String token : attributeTokens) {
                if (!attribute.contains(token.toLowerCase(Locale.ROOT))) {
                    matches = false;
                    break;
                }
            }
            if (!matches) {
                continue;
            }
            int end = nextDataSectionIndex(matcher.end());
            return html.substring(matcher.start(), end < 0 ? html.length() : end);
        }
        return "";
    }

    List<AtlasSectionTag> sectionTags() {
        List<AtlasSectionTag> sections = new ArrayList<>();
        Matcher matcher = DATA_SECTION_TAG.matcher(html);
        while (matcher.find()) {
            String attribute = matcher.group("section");
            String nearby = html.substring(matcher.end(), Math.min(html.length(), matcher.end() + 600));
            sections.add(new AtlasSectionTag(attribute, firstPlain(SUMMARY, nearby), matcher.start()));
        }
        return Collections.unmodifiableList(sections);
    }

    String normalizedSummaryText() {
        Matcher matcher = SUMMARY.matcher(html);
        StringBuilder summaries = new StringBuilder();
        while (matcher.find()) {
            summaries.append(plain(matcher.group(1))).append(' ');
        }
        return normalize(summaries.toString());
    }

    List<List<String>> dataRowsFrom(String htmlFragment) {
        return AtlasHtmlTableRows.dataRowsFrom(htmlFragment);
    }

    private int nextDataSectionIndex(int fromIndex) {
        Matcher matcher = DATA_SECTION_TAG.matcher(html);
        return matcher.find(fromIndex) ? matcher.start() : -1;
    }

    static String firstPlain(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? plain(matcher.group(1)) : "";
    }

    static String plain(String html) {
        return AtlasText.plainText(html);
    }

    static String normalize(String text) {
        return AtlasText.normalizeWhitespace(text).toLowerCase(Locale.ROOT);
    }

    static int firstIndexOf(String text, String... tokens) {
        int first = -1;
        for (String token : tokens) {
            int index = text.indexOf(token);
            if (index >= 0 && (first < 0 || index < first)) {
                first = index;
            }
        }
        return first;
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    static final class AtlasSectionTag {
        private final String attribute;
        private final String summary;
        private final int index;
    }
}
