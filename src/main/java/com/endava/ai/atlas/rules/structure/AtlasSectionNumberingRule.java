package com.endava.ai.atlas.rules.structure;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSectionNumberingRule implements AtlasValidationRule {
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");

    private static final List<String> REQUIRED_TOP_LEVEL_LABELS = List.of(
            "1. executive context",
            "2. business flow graph",
            "3. business flow understanding",
            "4. business evidence tables",
            "5. source-to-business trace");

    private static final List<String> REQUIRED_SUBGRAPH_LABELS = List.of(
            "3.1 checkout to invoice lifecycle",
            "3.2 purchase history reopen lifecycle",
            "3.3 support message lifecycle",
            "3.4 profile account memory",
            "3.5 invoice boundary diagnostic");

    private static final List<String> OVER_NUMBERED_LABELS = List.of(
            "legend 1.",
            "1. legend",
            "2. legend",
            "3. legend",
            "4. legend",
            "5. legend",
            "score-card 1.",
            "1. business flow clarity");

    @Override
    public String id() {
        return "atlas.candidate.sections.numbered";
    }

    @Override
    public String expected() {
        return "Major atlas sections and important business subgraphs are numbered for navigation, while legends and micro-cards stay unnumbered.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        String summaryText = normalizedSummaryText(html);
        for (String label : REQUIRED_TOP_LEVEL_LABELS) {
            if (!summaryText.contains(label)) {
                result.add(id(), "Missing numbered top-level atlas summary: " + label, context.atlasHtml(),
                        "Show navigation numbers in section summaries, not only in prose; keep the visible sequence 1 through 5.");
            }
        }
        for (String label : REQUIRED_SUBGRAPH_LABELS) {
            if (!summaryText.contains(label)) {
                result.add(id(), "Missing numbered business subgraph summary: " + label, context.atlasHtml(),
                        "Number important business flow slices as 3.x under Business Flow Understanding, for example 3.1 Checkout to invoice lifecycle.");
            }
        }
        for (String label : REQUIRED_TOP_LEVEL_LABELS) {
            if (!html.contains(label)) {
                result.add(id(), "Missing numbered atlas section: " + label, context.atlasHtml(),
                        "Number only major sections and important flow slices, for example 1. Executive Context, 2. Business Flow Graph, 3. Business Flow Understanding, 3.1 Checkout to invoice lifecycle.");
            }
        }
        requireOrderedSummaries(context, result, summaryText);
        for (String label : OVER_NUMBERED_LABELS) {
            if (html.contains(label)) {
                result.add(id(), "Atlas appears over-numbered: " + label, context.atlasHtml(),
                        "Do not number legends, score cards or micro-elements; keep numbering for navigation-level sections only.");
            }
        }
    }

    private static void requireOrderedSummaries(AtlasContext context,
                                                AtlasValidationResult result,
                                                String summaryText) {
        int previousIndex = -1;
        for (String label : REQUIRED_TOP_LEVEL_LABELS) {
            int currentIndex = summaryText.indexOf(label);
            if (currentIndex < 0) {
                continue;
            }
            if (currentIndex <= previousIndex) {
                result.add("atlas.candidate.sections.numbered",
                        "Numbered section order is not increasing at: " + label,
                        context.atlasHtml(),
                        "Keep navigation order stable: 1. Executive Context -> 2. Business Flow Graph -> 3. Business Flow Understanding -> 4. Business Evidence Tables -> 5. Source-to-Business Trace.");
            }
            previousIndex = currentIndex;
        }
    }

    private static String normalizedSummaryText(String html) {
        Matcher matcher = SUMMARY_PATTERN.matcher(html);
        StringBuilder summaries = new StringBuilder();
        while (matcher.find()) {
            String summary = TAG_PATTERN.matcher(matcher.group(1)).replaceAll(" ");
            summaries.append(summary).append(' ');
        }
        return summaries.toString().replaceAll("\\s+", " ").trim();
    }
}
