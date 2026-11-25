package com.ai.core.reporting.adapters.extent.spark.sections.failure;

public final class ExtentSparkFailureTriageSections {

    private static final String CSS = ExtentSparkFailureOverviewCss.css()
            + ExtentSparkFailureSignatureCss.css()
            + ExtentSparkFailureDrilldownCss.css()
            + ExtentSparkFailureResponsiveCss.css();

    private ExtentSparkFailureTriageSections() {
    }

    public static String css() {
        return CSS;
    }
}