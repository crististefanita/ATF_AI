package com.endava.ai.atlas.rules.structure;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSectionNumberPlacementRule implements AtlasValidationRule {
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern SUMMARY_TITLE_PATTERN = Pattern.compile(
            "(?is)<span\\b[^>]*class=\"[^\"]*summary-title[^\"]*\"[^>]*>(.*?)</span>");
    private static final Pattern SUMHINT_PATTERN = Pattern.compile(
            "(?is)<span\\b[^>]*class=\"[^\"]*sumhint[^\"]*\"[^>]*>(.*?)</span>");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
    private static final Pattern NUMBERED_HINT_PATTERN = Pattern.compile("(?i)^\\s*\\d+(?:\\.\\d+)?\\.\\s+");

    private static final List<SectionLabel> REQUIRED_TITLE_LABELS = List.of(
            new SectionLabel("1. executive context", "executive context"),
            new SectionLabel("2. business flow graph", "business flow graph"),
            new SectionLabel("3. business flow understanding", "business flow understanding"),
            new SectionLabel("4. business evidence tables", "business evidence tables"),
            new SectionLabel("5. source-to-business trace", "source-to-business trace"));

    @Override
    public String id() {
        return "atlas.candidate.sections.number-placement";
    }

    @Override
    public String expected() {
        return "Section numbers belong in summary-title labels, while sumhint stays descriptive and unnumbered.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher matcher = SUMMARY_PATTERN.matcher(context.lowerAtlasText());
        while (matcher.find()) {
            String summary = matcher.group(1);
            String title = extract(summary, SUMMARY_TITLE_PATTERN);
            String hint = extract(summary, SUMHINT_PATTERN);
            requireNumberInTitle(context, result, title, hint);
            requireHintIsNotNumbered(context, result, title, hint);
        }
    }

    private static void requireNumberInTitle(AtlasContext context,
                                             AtlasValidationResult result,
                                             String title,
                                             String hint) {
        for (SectionLabel section : REQUIRED_TITLE_LABELS) {
            boolean belongsToSection = title.contains(section.name()) || hint.contains(section.numberedName());
            if (!belongsToSection) {
                continue;
            }
            if (!title.contains(section.numberedName())) {
                result.add(idStatic(), "Section number is not in summary-title: " + section.numberedName(),
                        context.atlasHtml(),
                        "Move the number into summary-title, for example <span class=\"summary-title\">"
                                + capitalize(section.numberedName()) + "</span>; keep sumhint descriptive only.");
            }
        }
    }

    private static void requireHintIsNotNumbered(AtlasContext context,
                                                 AtlasValidationResult result,
                                                 String title,
                                                 String hint) {
        if (hint.isBlank()) {
            return;
        }
        if (NUMBERED_HINT_PATTERN.matcher(hint).find()) {
            result.add(idStatic(), "Section hint/pill is numbered instead of descriptive: " + hint,
                    context.atlasHtml(),
                    "Remove navigation numbers from sumhint. The hint should explain scope, rhythm or purpose, not repeat the section number/title.");
        }
        for (SectionLabel section : REQUIRED_TITLE_LABELS) {
            if (hint.contains(section.numberedName())) {
                result.add(idStatic(), "Section hint repeats numbered title: " + section.numberedName(),
                        context.atlasHtml(),
                        "Keep the numbered title once in summary-title; do not duplicate it inside the pill/hint.");
            }
        }
    }

    private static String extract(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return TAG_PATTERN.matcher(matcher.group(1)).replaceAll(" ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String capitalize(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean nextUpper = true;
        for (char current : value.toCharArray()) {
            if (Character.isWhitespace(current) || current == '.' || current == '-') {
                nextUpper = true;
                result.append(current);
            } else if (nextUpper) {
                result.append(Character.toUpperCase(current));
                nextUpper = false;
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }

    private static String idStatic() {
        return "atlas.candidate.sections.number-placement";
    }

    private static final class SectionLabel {
        private final String numberedName;
        private final String name;

        private SectionLabel(String numberedName, String name) {
            this.numberedName = numberedName;
            this.name = name;
        }

        private String numberedName() {
            return numberedName;
        }

        private String name() {
            return name;
        }
    }
}
