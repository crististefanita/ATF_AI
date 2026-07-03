package com.ai.skills.atlas.rules.structure;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTopLevelSectionContinuityRule implements AtlasValidationRule {
    private static final Pattern DETAILS_WITH_SUMMARY = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern SUMMARY_TITLE_PATTERN = Pattern.compile(
            "(?is)<span\\b[^>]*class=\"[^\"]*summary-title[^\"]*\"[^>]*>(.*?)</span>");
    private static final Pattern NAVIGATION_NUMBER_PATTERN = Pattern.compile("(?i)^\\s*\\d+(?:\\.\\d+)?\\.?\\s+");

    @Override
    public String id() {
        return "atlas.candidate.sections.top-level-continuity";
    }

    @Override
    public String expected() {
        return "Top-level atlas sections are a continuous numbered navigation spine; secondary analysis blocks are nested or numbered as subsections.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        List<String> topLevelSummaries = topLevelSummaries(context.lowerAtlasText());
        requireEveryTopLevelSectionNumbered(context, result, topLevelSummaries);
        requireNoUnnumberedBlocksBetweenFourAndFive(context, result, topLevelSummaries);
    }

    private static void requireEveryTopLevelSectionNumbered(AtlasContext context,
                                                            AtlasValidationResult result,
                                                            List<String> summaries) {
        for (String summary : summaries) {
            if (summary.equals("legend")) {
                continue;
            }
            if (!NAVIGATION_NUMBER_PATTERN.matcher(summary).find()) {
                result.add(idStatic(), "Top-level atlas section is not numbered: " + summary,
                        context.atlasHtml(),
                        "Number top-level sections or nest support evidence inside an existing numbered section. Do not leave standalone evidence blocks between 4 and 5.");
            }
        }
    }

    private static void requireNoUnnumberedBlocksBetweenFourAndFive(AtlasContext context,
                                                                    AtlasValidationResult result,
                                                                    List<String> summaries) {
        int coverageIndex = indexOfPrefix(summaries, "4. ");
        int mappingIndex = indexOfPrefix(summaries, "5. ");
        if (coverageIndex < 0 || mappingIndex < 0 || coverageIndex >= mappingIndex) {
            return;
        }
        for (int index = coverageIndex + 1; index < mappingIndex; index++) {
            String summary = summaries.get(index);
            if (!summary.startsWith("4.")) {
                result.add(idStatic(), "Unnumbered or misplaced top-level section between 4 and 5: " + summary,
                        context.atlasHtml(),
                        "Move this block under 4. Business Evidence Tables as a collapsed subsection, or number it as 4.x. The visible navigation spine must not jump from 4 to unnumbered sections to 5.");
            }
        }
    }

    private static List<String> topLevelSummaries(String html) {
        Matcher matcher = DETAILS_WITH_SUMMARY.matcher(html);
        List<String> summaries = new ArrayList<>();
        while (matcher.find()) {
            String detailsAttributes = matcher.group(1);
            if (detailsAttributes.contains("subdetails")) {
                continue;
            }
            summaries.add(summaryTitleOrText(matcher.group(2)));
        }
        return summaries;
    }

    private static String summaryTitleOrText(String summaryHtml) {
        Matcher titleMatcher = SUMMARY_TITLE_PATTERN.matcher(summaryHtml);
        String selected = titleMatcher.find() ? titleMatcher.group(1) : summaryHtml;
        return AtlasText.plainText(selected);
    }

    private static int indexOfPrefix(List<String> summaries, String prefix) {
        for (int index = 0; index < summaries.size(); index++) {
            if (summaries.get(index).startsWith(prefix)) {
                return index;
            }
        }
        return -1;
    }

    private static String idStatic() {
        return "atlas.candidate.sections.top-level-continuity";
    }
}
