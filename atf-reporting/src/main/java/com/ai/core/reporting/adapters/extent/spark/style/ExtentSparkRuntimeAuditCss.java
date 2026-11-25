package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.spark.sections.ExtentSparkFailureTriageCss;

/**
 * Runtime-audit CSS for the customized Extent Spark overview and failure triage tabs.
 * Keeping this separate from the base Spark shell makes the report skin easier to audit and evolve.
 */
final class ExtentSparkRuntimeAuditCss {

    private static final String CSS = ExtentSparkRuntimeTimelineCss.css()
                    + ExtentSparkRuntimeTagAuditCss.css()
                    + ExtentSparkFailureTriageCss.css();

    private ExtentSparkRuntimeAuditCss() {
    }

    static String css() {
        return CSS;
    }
}
