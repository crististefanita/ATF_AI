package com.ai.core.reporting.htmlcontract.validation;

import com.ai.core.reporting.htmlcontract.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HtmlScoreOverviewTableValidator {
    private static final String SCORE_OVERVIEW = "Score Overview";
    private static final Pattern SCORE_OVERVIEW_DETAILS = Pattern.compile(
            "(?is)<details\\b[^>]*>\\s*<summary\\b[^>]*>.*?Score Overview.*?</summary>(.*?)</details>");
    private static final Pattern TABLE = Pattern.compile("(?is)<table\\b([^>]*)>(.*?)</table>");
    private static final Pattern ROW = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern CELL = Pattern.compile("(?is)<t[hd]\\b[^>]*>(.*?)</t[hd]>");
    private static final Pattern PERCENT = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*%");
    private static final Pattern INTEGER = Pattern.compile("^\\d{1,3}$");
    private static final double EXPECTED_WEIGHT_TOTAL = 100.0;
    private static final double WEIGHT_TOLERANCE = 0.01;

    private HtmlScoreOverviewTableValidator() {
    }

    static void validate(String html,
                         HtmlReportContract contract,
                         List<HtmlReportShellViolation> violations) {
        if (!requiresScoreOverview(contract)) {
            return;
        }
        Optional<String> section = scoreOverviewBody(html);
        HtmlReportValidationSupport.require(section.isPresent(),
                "report.score-overview.section",
                "Score Overview must be a dedicated collapsible <details> section with a <summary>.",
                violations);
        if (section.isEmpty()) {
            return;
        }

        Optional<String> weightedTable = weightedScoreTable(section.get());
        HtmlReportValidationSupport.require(weightedTable.isPresent(),
                "report.score-overview.table.missing",
                "Score Overview must publish a score-table with Axis, Score, Weight and Why columns.",
                violations);
        weightedTable.ifPresent(table -> validateWeightedScoreTable(table, violations));
    }

    private static boolean requiresScoreOverview(HtmlReportContract contract) {
        return contract.sections().stream()
                .anyMatch(section -> SCORE_OVERVIEW.equals(section.label()));
    }

    private static Optional<String> scoreOverviewBody(String html) {
        Matcher matcher = SCORE_OVERVIEW_DETAILS.matcher(html);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private static Optional<String> weightedScoreTable(String sectionHtml) {
        Matcher matcher = TABLE.matcher(sectionHtml);
        while (matcher.find()) {
            String attributes = matcher.group(1).toLowerCase(Locale.ROOT);
            String table = matcher.group();
            if (attributes.contains("score-table") && hasWeightedHeaders(table)) {
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }

    private static boolean hasWeightedHeaders(String tableHtml) {
        List<List<String>> rows = rows(tableHtml);
        if (rows.isEmpty()) {
            return false;
        }
        String header = String.join(" | ", rows.get(0)).toLowerCase(Locale.ROOT);
        return containsAny(header, "axis", "axa")
                && containsAny(header, "score", "nota")
                && containsAny(header, "weight", "pondere")
                && containsAny(header, "why", "de ce");
    }

    private static void validateWeightedScoreTable(String tableHtml, List<HtmlReportShellViolation> violations) {
        List<List<String>> rows = rows(tableHtml);
        if (rows.size() < 2) {
            violations.add(HtmlReportShellViolation.of("report.score-overview.table.empty",
                    "Score Overview weighted table must contain header plus axis rows."));
            return;
        }
        List<String> header = rows.get(0);
        int expectedColumns = header.size();
        double weightTotal = 0.0;
        int weightedRows = 0;

        for (int index = 1; index < rows.size(); index++) {
            List<String> cells = rows.get(index);
            String axis = cells.isEmpty() ? "" : cells.get(0);
            String normalizedAxis = axis.toLowerCase(Locale.ROOT);
            if (normalizedAxis.contains("overall")) {
                violations.add(HtmlReportShellViolation.of("report.score-overview.overall-in-weighted-table",
                        "Overall/cap is a result, not a weighted axis row; publish it in a separate calculation or cap note outside the weighted score table."));
            }
            if (cells.size() != expectedColumns) {
                violations.add(HtmlReportShellViolation.of("report.score-overview.table.row-shape",
                        "Every Score Overview weighted-table row must have " + expectedColumns
                                + " cells like the header; malformed row starts with: " + axis + "."));
                continue;
            }
            if (!isScore(cells.get(1))) {
                violations.add(HtmlReportShellViolation.of("report.score-overview.score.numeric",
                        "Score Overview axis score must be a numeric 0-100 value: " + axis + " -> " + cells.get(1) + "."));
            }
            Optional<Double> weight = percent(cells.get(2));
            if (weight.isEmpty()) {
                violations.add(HtmlReportShellViolation.of("report.score-overview.weight.numeric",
                        "Score Overview axis weight must be a percentage: " + axis + " -> " + cells.get(2) + "."));
            } else if (!normalizedAxis.contains("overall")) {
                weightTotal += weight.get();
                weightedRows++;
            }
        }

        if (weightedRows < 5) {
            violations.add(HtmlReportShellViolation.of("report.score-overview.axis-count",
                    "Score Overview weighted table should publish the five canonical scoring axes before cap/overall."));
        }
        if (Math.abs(weightTotal - EXPECTED_WEIGHT_TOTAL) > WEIGHT_TOLERANCE) {
            violations.add(HtmlReportShellViolation.of("report.score-overview.weight-total",
                    "Score Overview weighted axis percentages must sum to 100%, observed " + format(weightTotal) + "%."));
        }
    }

    private static List<List<String>> rows(String tableHtml) {
        Matcher rowMatcher = ROW.matcher(tableHtml);
        List<List<String>> rows = new ArrayList<>();
        while (rowMatcher.find()) {
            Matcher cellMatcher = CELL.matcher(rowMatcher.group(1));
            List<String> cells = new ArrayList<>();
            while (cellMatcher.find()) {
                cells.add(plainText(cellMatcher.group(1)));
            }
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }
        return rows;
    }

    private static boolean isScore(String value) {
        String text = value.trim();
        if (!INTEGER.matcher(text).matches()) {
            return false;
        }
        int score = Integer.parseInt(text);
        return score >= 0 && score <= 100;
    }

    private static Optional<Double> percent(String value) {
        Matcher matcher = PERCENT.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(Double.parseDouble(matcher.group(1)));
    }

    private static String plainText(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String format(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
