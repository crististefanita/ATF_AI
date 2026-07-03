package com.endava.ai.core.reporting.htmlcontract.validation;

import com.endava.ai.core.reporting.htmlcontract.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HtmlCanonicalSectionValidator {
    private static final Pattern DETAILS_WITH_SUMMARY = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern OPEN_ATTRIBUTE = Pattern.compile("(?i)(^|\\s)open(\\s|=|$)");

    private HtmlCanonicalSectionValidator() {
    }

    static void validate(String html,
                         HtmlReportContract contract,
                         List<HtmlReportShellViolation> violations) {
        requireCanonicalSections(html, contract, violations);
        requireCollapsedSections(html, contract, violations);
        requireOnlyOnePrimarySectionOpen(html, violations);
    }

    private static void requireCanonicalSections(String html,
                                                 HtmlReportContract contract,
                                                 List<HtmlReportShellViolation> violations) {
        List<String> summaries = detailsSummaries(html);
        for (HtmlReportSectionRequirement section : contract.sections()) {
            String required = section.label();
            boolean found = summaries.stream().anyMatch(summary -> summary.contains(required))
                    || plainText(html).contains(required);
            HtmlReportValidationSupport.require(found,
                    "report.section.missing",
                    "Report is missing canonical final-report section: " + required + ".",
                    violations);
        }
    }

    private static void requireOnlyOnePrimarySectionOpen(String html, List<HtmlReportShellViolation> violations) {
        int openDetails = 0;
        Matcher matcher = DETAILS_WITH_SUMMARY.matcher(html);
        while (matcher.find()) {
            String attributes = matcher.group(1).toLowerCase(Locale.ROOT);
            if (attributes.contains("open") && !attributes.contains("subdetails")) {
                openDetails++;
            }
        }
        HtmlReportValidationSupport.require(openDetails <= 1,
                "report.section.too-many-open",
                "Only one top-level report section should be open by default.",
                violations);
    }

    private static void requireCollapsedSections(String html,
                                                 HtmlReportContract contract,
                                                 List<HtmlReportShellViolation> violations) {
        List<DetailSection> details = detailSections(html);
        for (HtmlReportSectionRequirement section : contract.collapsedSections()) {
            String required = section.label();
            DetailSection match = details.stream()
                    .filter(detail -> detail.summary().contains(required))
                    .findFirst()
                    .orElse(null);
            HtmlReportValidationSupport.require(match != null,
                    "report.section.collapsed-details.missing",
                    required + " must be rendered as a collapsed <details> section with a <summary>, not as an always-open <section>.",
                    violations);
            if (match != null) {
                HtmlReportValidationSupport.require(!OPEN_ATTRIBUTE.matcher(match.attributes()).find(),
                        "report.section.collapsed-details.open",
                        required + " must be collapsed by default; remove the open attribute from its <details> element.",
                        violations);
            }
        }
    }

    private static List<String> detailsSummaries(String html) {
        List<String> summaries = new ArrayList<>();
        for (DetailSection detail : detailSections(html)) {
            summaries.add(detail.summary());
        }
        return summaries;
    }

    private static List<DetailSection> detailSections(String html) {
        Matcher matcher = DETAILS_WITH_SUMMARY.matcher(html);
        List<DetailSection> details = new ArrayList<>();
        while (matcher.find()) {
            details.add(new DetailSection(matcher.group(1).toLowerCase(Locale.ROOT), plainText(matcher.group(2))));
        }
        return details;
    }

    private static String plainText(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static final class DetailSection {
        private final String attributes;
        private final String summary;

        private DetailSection(String attributes, String summary) {
            this.attributes = attributes;
            this.summary = summary;
        }

        private String attributes() {
            return attributes;
        }

        private String summary() {
            return summary;
        }
    }
}
