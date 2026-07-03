package com.ai.core.reporting.adapters.extent.overview;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.support.html.HtmlText;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtentOverviewTopCards {
    private static final String DASHBOARD_VIEW = ExtentHtmlStructure.DASHBOARD_CONTAINER;
    private static final String ROW_START = "<div class=\"row\">";
    private static final String TEST_CASES_ROW = "<div class=\"row atf-runtime-dashboard-tests-row\">";
    private static final String SPARK_TOP_CARD_COLUMN = "<div class=\"col-md-3\">";
    private static final String STANDARD_TOP_CARD_COLUMN = "<div class=\"col-md atf-aggregate-top-card\">";
    private static final String DURATION_CARD_TOKEN =
            "<p class=\"m-b-0\">" + ExtentDashboardLabels.DURATION + "</p>";
    private static final String TESTS_PASSED_CARD =
            ExtentHtmlStructure.topCardLabel("pass", ExtentDashboardLabels.TESTS_PASSED)
                    + "\\s*<h3[^>]*>.*?</h3>";
    private static final String TESTS_FAILED_CARD =
            ExtentHtmlStructure.topCardLabel("fail", ExtentDashboardLabels.TESTS_FAILED)
                    + "\\s*<h3[^>]*>.*?</h3>";
    private static final String STARTED_CARD = plainTopCard(ExtentDashboardLabels.STARTED);
    private static final String ENDED_CARD = plainTopCard(ExtentDashboardLabels.ENDED);
    private static final String ENDED_CARD_COLUMN =
            "(<div class=\"(?:col-md-3|col-md atf-aggregate-top-card)\">\\s*"
                    + "<div class=\"card\">\\s*"
                    + "(?:<div class=\"card-header\">.*?</div>\\s*)?"
                    + "<div class=\"card-body\">\\s*"
                    + plainTopCard(ExtentDashboardLabels.ENDED)
                    + "\\s*</div>\\s*</div>\\s*</div>)";
    private static final String HEADER_ENDED_CARD_COLUMN =
            "(<div class=\"(?:col-md-3|col-md atf-aggregate-top-card)\">\\s*"
                    + "<div class=\"card\">\\s*"
                    + "<div class=\"card-header\">\\s*"
                    + ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.ENDED)
                    + ".*?</div>\\s*"
                    + "<div class=\"card-body\">\\s*<h3[^>]*>.*?</h3>\\s*</div>\\s*</div>\\s*</div>)";

    private ExtentOverviewTopCards() {
    }

    public static String reconcileStatus(String html, long passed, long failed) {
        String updated = replaceFirst(
                html,
                TESTS_PASSED_CARD,
                ExtentHtmlStructure.topCardLabel("pass", ExtentDashboardLabels.TESTS_PASSED)
                        + "<h3>" + passed + "</h3>"
        );
        return replaceFirst(
                updated,
                TESTS_FAILED_CARD,
                ExtentHtmlStructure.topCardLabel("fail", ExtentDashboardLabels.TESTS_FAILED)
                        + "<h3>" + failed + "</h3>"
        );
    }

    public static String reconcileTiming(String html, String startedAt, String endedAt, String duration) {
        if (startedAt == null || endedAt == null || duration == null
                || startedAt.isBlank() || endedAt.isBlank() || duration.isBlank()) {
            return html;
        }
        String updated = replaceFirst(
                html,
                STARTED_CARD,
                plainTopCardHtml(ExtentDashboardLabels.STARTED, startedAt)
        );
        updated = replaceFirst(
                updated,
                ENDED_CARD,
                plainTopCardHtml(ExtentDashboardLabels.ENDED, endedAt)
        );
        return alignWithDuration(updated, duration);
    }

    public static String alignWithExistingTiming(String html) {
        return durationFromExistingCards(html)
                .map(duration -> alignWithDuration(html, duration))
                .orElseGet(() -> normalizeTopCardColumns(normalizeTopCardLayout(html)));
    }

    private static String alignWithDuration(String html, String duration) {
        String updated = replaceFirst(
                html,
                plainTopCard(ExtentDashboardLabels.DURATION),
                plainTopCardHtml(ExtentDashboardLabels.DURATION, duration)
        );
        return normalizeTopCardColumns(normalizeTopCardLayout(ensureDurationScoreCard(updated, duration)));
    }

    private static Optional<String> durationFromExistingCards(String html) {
        Optional<String> started = topCardValue(html, ExtentDashboardLabels.STARTED);
        Optional<String> ended = topCardValue(html, ExtentDashboardLabels.ENDED);
        return started.isPresent() && ended.isPresent()
                ? ExtentOverviewTiming.durationBetween(started.get(), ended.get())
                : Optional.empty();
    }

    private static Optional<String> topCardValue(String html, String label) {
        Optional<String> bodyLabelValue = topCardValueFromPattern(
                html,
                "(?s)<p class=\"m-b-0(?: text-[^\"]+)?\">\\s*"
                        + Pattern.quote(label)
                        + "\\s*</p>\\s*<h3[^>]*>(.*?)</h3>"
        );
        if (bodyLabelValue.isPresent()) {
            return bodyLabelValue;
        }
        return topCardValueFromPattern(
                html,
                "(?s)<h6 class=\"card-title\">\\s*"
                        + Pattern.quote(label)
                        + "\\s*</h6>.*?<div class=\"card-body\">\\s*<h3[^>]*>(.*?)</h3>"
        );
    }

    private static String ensureDurationScoreCard(String html, String duration) {
        if (html.contains(DURATION_CARD_TOKEN)) {
            return html;
        }
        String updated = html.replaceFirst("(?s)" + ENDED_CARD_COLUMN,
                "$1" + Matcher.quoteReplacement(durationScoreCard(duration)));
        if (!updated.equals(html)) {
            return updated;
        }
        return html.replaceFirst("(?s)" + HEADER_ENDED_CARD_COLUMN,
                "$1" + Matcher.quoteReplacement(durationScoreCard(duration)));
    }

    private static String durationScoreCard(String duration) {
        return STANDARD_TOP_CARD_COLUMN + "<div class=\"card\"><div class=\"card-body\">"
                + plainTopCardHtml(ExtentDashboardLabels.DURATION, duration)
                + "</div></div></div>";
    }

    private static String normalizeTopCardLayout(String html) {
        int dashboardStart = html.indexOf(DASHBOARD_VIEW);
        if (dashboardStart < 0) {
            return html;
        }
        int topRowStart = html.indexOf(ROW_START, dashboardStart);
        int testCasesRowStart = html.indexOf(TEST_CASES_ROW, topRowStart);
        if (topRowStart < 0 || testCasesRowStart <= topRowStart) {
            return html;
        }
        String before = html.substring(0, topRowStart);
        String topCards = html.substring(topRowStart, testCasesRowStart)
                .replace(SPARK_TOP_CARD_COLUMN, STANDARD_TOP_CARD_COLUMN);
        String after = html.substring(testCasesRowStart);
        return before + topCards + after;
    }

    private static String normalizeTopCardColumns(String html) {
        String updated = html;
        updated = normalizeTopCardColumn(updated, ExtentDashboardLabels.STARTED);
        updated = normalizeTopCardColumn(updated, ExtentDashboardLabels.ENDED);
        updated = normalizeTopCardColumn(updated, ExtentDashboardLabels.DURATION);
        updated = normalizeTopCardColumn(updated, ExtentDashboardLabels.TESTS_PASSED);
        return normalizeTopCardColumn(updated, ExtentDashboardLabels.TESTS_FAILED);
    }

    private static String normalizeTopCardColumn(String html, String label) {
        String escapedLabel = Pattern.quote(label);
        String regex = "(?s)<div class=\"col-md-3\">(\\s*<div class=\"card\"><div class=\"card-body\">\\s*"
                + "<p class=\"m-b-0(?: text-pass| text-fail)?\">"
                + escapedLabel
                + "</p>\\s*<h3[^>]*>.*?</h3>\\s*</div></div>\\s*</div>)";
        return html.replaceFirst(regex, Matcher.quoteReplacement(STANDARD_TOP_CARD_COLUMN) + "$1");
    }

    private static String plainTopCard(String label) {
        return "<p class=\"m-b-0\">" + Pattern.quote(label) + "</p>\\s*<h3[^>]*>.*?</h3>";
    }

    private static String plainTopCardHtml(String label, String value) {
        return "<p class=\"m-b-0\">"
                + HtmlText.html(label)
                + "</p><h3>"
                + HtmlText.html(value)
                + "</h3>";
    }

    private static String replaceFirst(String html, String regex, String replacement) {
        return html.replaceFirst("(?s)" + regex, Matcher.quoteReplacement(replacement));
    }

    private static Optional<String> topCardValueFromPattern(String html, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(plainText(matcher.group(1)).replaceAll("\\s+", " ").trim());
    }

    private static String plainText(String value) {
        return value.replaceAll("(?s)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

}
