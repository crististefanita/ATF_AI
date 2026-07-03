package com.endava.ai.skills.business.parse;

import com.endava.ai.skills.business.taxonomy.*;

import com.endava.ai.skills.business.model.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BusinessHtmlArtifactReducer {
    private static final Pattern LINK_PATTERN = Pattern.compile("(?is)<a\\s+[^>]*href\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern SECTION_PATTERN = Pattern.compile("(?is)<(h[1-6]|summary)[^>]*>(.*?)</\\1>");
    private static final Pattern SCORE_CARD_PATTERN = Pattern.compile("(?is)<article[^>]*score-card[^>]*>(.*?)</article>");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(100|[1-9]?\\d)\\b");
    private static final Pattern SCRIPT_OR_STYLE_PATTERN = Pattern.compile("(?is)<(script|style)[^>]*>.*?</\\1>");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");

    private BusinessHtmlArtifactReducer() {
    }

    static HtmlArtifact reduce(String html) {
        return new HtmlArtifact(
                normalizeVisibleText(stripHtml(html)),
                extractSections(html),
                extractLinks(html),
                extractScoreValues(html)
        );
    }

    static String normalizeText(String text) {
        return normalizeVisibleText(text);
    }

    private static List<String> extractSections(String html) {
        LinkedHashSet<String> sections = new LinkedHashSet<>();
        Matcher matcher = SECTION_PATTERN.matcher(html);
        while (matcher.find()) {
            String text = normalizeVisibleText(stripHtml(matcher.group(2)));
            if (!text.isBlank()) {
                sections.add(text);
            }
        }
        return List.copyOf(sections);
    }

    private static List<String> extractLinks(String html) {
        LinkedHashSet<String> links = new LinkedHashSet<>();
        Matcher matcher = LINK_PATTERN.matcher(html);
        while (matcher.find()) {
            String link = htmlDecode(matcher.group(1)).trim();
            if (!link.isBlank()) {
                links.add(link);
            }
        }
        return List.copyOf(links);
    }

    private static List<Integer> extractScoreValues(String html) {
        List<Integer> scores = new ArrayList<>();
        Matcher cardMatcher = SCORE_CARD_PATTERN.matcher(html);
        while (cardMatcher.find()) {
            Matcher numberMatcher = NUMBER_PATTERN.matcher(stripHtml(cardMatcher.group(1)));
            if (numberMatcher.find()) {
                scores.add(Integer.parseInt(numberMatcher.group(1)));
            }
        }
        return List.copyOf(scores);
    }

    private static String stripHtml(String html) {
        return TAG_PATTERN.matcher(SCRIPT_OR_STYLE_PATTERN.matcher(html).replaceAll(" ")).replaceAll(" ");
    }

    private static String normalizeVisibleText(String text) {
        return htmlDecode(text).replaceAll("\\s+", " ").trim();
    }

    private static String htmlDecode(String text) {
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    static final class HtmlArtifact {
        private final String visibleText;
        private final List<String> sections;
        private final List<String> links;
        private final List<Integer> scoreValues;

        private HtmlArtifact(String visibleText,
                             List<String> sections,
                             List<String> links,
                             List<Integer> scoreValues) {
            this.visibleText = visibleText;
            this.sections = List.copyOf(sections);
            this.links = List.copyOf(links);
            this.scoreValues = List.copyOf(scoreValues);
        }

        String visibleText() {
            return visibleText;
        }

        List<String> sections() {
            return sections;
        }

        List<String> links() {
            return links;
        }

        List<Integer> scoreValues() {
            return scoreValues;
        }
    }
}
