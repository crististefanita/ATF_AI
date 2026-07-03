package com.endava.ai.skills.atlas.rules.structure;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.report.AtlasReportModel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSectionNumberingRule implements AtlasValidationRule {
    private static final Pattern BUSINESS_SUBGRAPH_SUMMARY = Pattern.compile("\\b3\\.\\d+\\b");

    private static final List<String> REQUIRED_TOP_LEVEL_LABELS = List.of(
            "1. executive context",
            "2. business flow graph",
            "3. business flow understanding",
            "4. business evidence tables",
            "5. source-to-business trace");

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
        AtlasReportModel report = AtlasReportModel.parse(context.atlasText());
        String html = context.lowerAtlasText();
        String summaryText = report.normalizedSummaryText();
        List<String> sectionAttributes = report.sectionAttributes();
        for (String label : REQUIRED_TOP_LEVEL_LABELS) {
            if (!summaryText.contains(label)) {
                result.add(id(), "Missing numbered top-level atlas summary: " + label, context.atlasHtml(),
                        "Show navigation numbers in section summaries, not only in prose; keep the visible sequence 1 through 5.");
            }
        }
        int subgraphSummaries = countBusinessSubgraphSummaries(summaryText);
        if (subgraphSummaries < 4) {
            result.add(id(), "Too few numbered business subgraph summaries: " + subgraphSummaries,
                    context.atlasHtml(),
                    "Number important business flow slices as 3.x under Business Flow Understanding, without hardcoding any application-specific flow names.");
        }
        for (String label : REQUIRED_TOP_LEVEL_LABELS) {
            if (!sectionAttributes.contains(label)) {
                result.add(id(), "Missing numbered atlas section: " + label, context.atlasHtml(),
                        "Number only major sections and important flow slices, for example 1. Executive Context, 2. Business Flow Graph, 3. Business Flow Understanding, and generic 3.x business slices.");
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

    private static int countBusinessSubgraphSummaries(String summaryText) {
        Matcher matcher = BUSINESS_SUBGRAPH_SUMMARY.matcher(summaryText);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
