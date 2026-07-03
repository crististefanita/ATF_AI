package com.endava.ai.atf.core.reporting.adapters.support;

import com.endava.ai.atf.core.reporting.adapters.html.ExtentSparkReportHtmlAssertions;

public abstract class ExtentSparkPresentationHtmlSupport extends ExtentSparkPresentationFixtureSupport {

    protected static int countOccurrences(String value, String needle) {
        return ExtentSparkReportHtmlAssertions.countOccurrences(value, needle);
    }

    protected static String dashboardFragment(String html) {
        return ExtentSparkReportHtmlAssertions.dashboardFragment(html);
    }

    protected static String dashboardTopCount(String html, String label) {
        return ExtentSparkReportHtmlAssertions.dashboardTopCount(html, label);
    }

    protected static String dashboardSummaryCount(String html, String title) {
        return ExtentSparkReportHtmlAssertions.dashboardSummaryCount(html, title);
    }

    protected static String dashboardTestsCardCount(String html) {
        return ExtentSparkReportHtmlAssertions.dashboardTestsCardCount(html);
    }

    protected static String dashboardTestsCard(String html) {
        return ExtentSparkReportHtmlAssertions.dashboardTestsCard(html);
    }

    protected static int runtimeTagTableCountSum(String html, String tableKind) {
        return ExtentSparkReportHtmlAssertions.runtimeTagTableCountSum(html, tableKind);
    }

    protected static String categoryFragment(String html) {
        return ExtentSparkReportHtmlAssertions.categoryFragment(html);
    }

    protected static void assertEqualRuntimeTagCardWidths(String state) {
        ExtentSparkReportHtmlAssertions.assertEqualRuntimeTagCardWidths(state);
    }

    protected static void assertComputedColor(String state, String key, String expectedColor) {
        ExtentSparkReportHtmlAssertions.assertComputedColor(state, key, expectedColor);
    }
}
