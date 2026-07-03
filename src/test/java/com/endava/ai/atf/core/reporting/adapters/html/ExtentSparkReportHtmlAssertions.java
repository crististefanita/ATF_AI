package com.endava.ai.atf.core.reporting.adapters.html;

import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentSparkReportInspector;
import org.testng.Assert;

public final class ExtentSparkReportHtmlAssertions {

    private ExtentSparkReportHtmlAssertions() {
    }

    public static int countOccurrences(String value, String needle) {
        return ExtentSparkReportInspector.countOccurrences(value, needle);
    }

    public static String dashboardFragment(String html) {
        return ExtentSparkReportInspector.inspect(html).dashboardFragment();
    }

    public static String dashboardTopCount(String html, String label) {
        return ExtentSparkReportInspector.inspect(html).dashboardTopCount(label);
    }

    public static String dashboardSummaryCount(String html, String title) {
        return ExtentSparkReportInspector.inspect(html).dashboardSummaryCount(title);
    }

    public static String dashboardTestsCardCount(String html) {
        return ExtentSparkReportInspector.inspect(html).dashboardTestsCardCount();
    }

    public static String dashboardTestsCard(String html) {
        return ExtentSparkReportInspector.inspect(html).dashboardTestsCard();
    }

    public static int runtimeTagTableCountSum(String html, String tableKind) {
        return ExtentSparkReportInspector.inspect(html).runtimeTagTableCountSum(tableKind);
    }

    public static String categoryFragment(String html) {
        return ExtentSparkReportInspector.inspect(html).categoryFragment();
    }

    public static void assertEqualRuntimeTagCardWidths(String state) {
        int[] widths = ExtentSparkReportInspector.inspect("").runtimeTagCardWidths(state);
        int classWidth = widths[0];
        int packageWidth = widths[1];
        int annotationWidth = widths[2];
        Assert.assertTrue(
                Math.abs(classWidth - packageWidth) <= 2 && Math.abs(classWidth - annotationWidth) <= 2,
                "Runtime tag cards must have equal width. State: " + state
        );
    }

    public static void assertComputedColor(String state, String key, String expectedColor) {
        String actualColor = ExtentSparkReportInspector.inspect("").renderedStateValue(state, key);
        Assert.assertEquals(
                actualColor,
                expectedColor,
                "Unexpected computed background color for " + key + ". State: " + state
        );
    }
}
