package com.endava.ai.core.reporting.adapters.extent.spark.sections;

import com.endava.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageSections;

/**
 * Failure-triage CSS used by the Extent Spark runtime audit tab.
 * The runtime audit shell composes this section after timeline and tag-audit styling.
 */
public final class ExtentSparkFailureTriageCss {

    private static final String CSS = ExtentSparkFailureTriageSections.css();

    private ExtentSparkFailureTriageCss() {
    }

    public static String css() {
        return CSS;
    }
}