package com.ai.core.reporting.listener.internal.runtime.render;



import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;

import java.util.List;

public final class ExtentRuntimeDashboardAnchors {
    public static final String TIMELINE_CANVAS = "<canvas id=\"timeline\"";
    public static final String STATUS_GROUP_TOKEN = "var statusGroup";
    public static final List<String> ROW_START_MARKERS = List.of(
            "<div class=\"row atf-runtime-dashboard-tests-row\">",
            "<div class=\"row\">"
    );
    public static final List<String> SCRIPT_END_MARKERS = List.of("</script>");
    public static final List<String> TIMELINE_ROW_START_MARKERS = List.of(
            "<div class=\"row atf-runtime-timeline-row\">",
            "<div class=\"row\"><div class=\"col-md-12\">",
            "<div class=\"row\">\n<div class=\"col-md-12\">",
            "<div class=\"row\">\r\n<div class=\"col-md-12\">"
    );

    private static final String TESTS_FAILED_LABEL = "<p class=\"m-b-0 text-fail\">"
            + ExtentDashboardLabels.TESTS_FAILED
            + "</p>";
    private static final String RUNTIME_TAG_AUDIT_ROW = "<div class=\"row atf-runtime-overview-tags\">";
    private static final String SPARK_TAGS_CARD_HEADER = "<div class=\"card-header\"><p>Tags</p></div>";
    private static final List<String> TAG_ROW_START_MARKERS = List.of(
            "<div class=\"row\"><div class=\"col-lg-6 col-md-12 category-container\">",
            "<div class=\"row\">\n<div class=\"col-lg-6 col-md-12 category-container\">",
            "<div class=\"row\">\r\n<div class=\"col-lg-6 col-md-12 category-container\">"
    );

    private ExtentRuntimeDashboardAnchors() {
    }

    public static int testsFailedLabel(String html) {
        return html.indexOf(TESTS_FAILED_LABEL);
    }

    public static int runtimeTagAuditRow(String html) {
        return html.indexOf(RUNTIME_TAG_AUDIT_ROW);
    }

    public static int statusGroupScriptStart(String html, int fromIndex) {
        int safeFromIndex = Math.max(0, fromIndex);
        int statusGroupStart = html.indexOf(STATUS_GROUP_TOKEN, safeFromIndex);
        if (statusGroupStart < 0) {
            return -1;
        }
        int scriptStart = html.lastIndexOf("<script", statusGroupStart);
        return scriptStart >= safeFromIndex ? scriptStart : -1;
    }

    public static int firstTagRowStart(String html) {
        return TAG_ROW_START_MARKERS.stream()
                .mapToInt(html::indexOf)
                .filter(index -> index >= 0)
                .min()
                .orElse(-1);
    }

    public static int sparkTagsRowStart(String html) {
        int tagsHeader = html.indexOf(SPARK_TAGS_CARD_HEADER);
        if (tagsHeader < 0) {
            return -1;
        }
        return TAG_ROW_START_MARKERS.stream()
                .mapToInt(marker -> html.lastIndexOf(marker, tagsHeader))
                .filter(index -> index >= 0)
                .max()
                .orElse(-1);
    }
}
