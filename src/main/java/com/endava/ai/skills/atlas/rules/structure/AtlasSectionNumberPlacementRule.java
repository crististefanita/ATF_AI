package com.endava.ai.skills.atlas.rules.structure;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSectionNumberPlacementRule implements AtlasValidationRule {
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern SUMMARY_TITLE_PATTERN = Pattern.compile(
            "(?is)<span\\b[^>]*class=\"[^\"]*summary-title[^\"]*\"[^>]*>(.*?)</span>");
    private static final Pattern SUMHINT_PATTERN = Pattern.compile(
            "(?is)<span\\b[^>]*class=\"[^\"]*sumhint[^\"]*\"[^>]*>(.*?)</span>");
    private static final Pattern NUMBERED_HINT_PATTERN = Pattern.compile("(?i)^\\s*\\d+(?:\\.\\d+)?\\.\\s+");

    private static final List<SectionLabel> REQUIRED_TITLE_LABELS = List.of(
            new SectionLabel("1. executive context", "executive context"),
            new SectionLabel("2. business flow graph", "business flow graph"),
            new SectionLabel("3. business flow understanding", "business flow understanding"),
            new SectionLabel("4. business evidence tables", "business evidence tables"),
            new SectionLabel("5. source-to-business trace", "source-to-business trace"),
            new SectionLabel("6. atlas test audit", "atlas test audit"));

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
            String plainSummary = AtlasText.plainText(summary);
            requireNumberInTitle(context, result, title, hint, plainSummary);
            requireHintIsNotNumbered(context, result, title, hint);
        }
    }

    private static void requireNumberInTitle(AtlasContext context,
                                             AtlasValidationResult result,
                                             String title,
                                             String hint,
                                             String plainSummary) {
        for (SectionLabel section : REQUIRED_TITLE_LABELS) {
            boolean belongsToSection = title.contains(section.name()) || hint.contains(section.numberedName());
            if (!belongsToSection && title.isBlank() && hint.isBlank()
                    && plainSummary.contains(section.numberedName())) {
                result.add(idStatic(), "Top-level section is missing summary-title/sumhint shell: " + section.numberedName(),
                        context.atlasHtml(),
                        "Use <summary><span class=\"summary-title\">" + capitalize(section.numberedName())
                                + "</span><span class=\"sumhint\">descriptive purpose</span></summary>.");
                continue;
            }
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
        return AtlasText.plainText(matcher.group(1));
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

    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SectionLabel {
        private final String numberedName;
        private final String name;
    }
}
