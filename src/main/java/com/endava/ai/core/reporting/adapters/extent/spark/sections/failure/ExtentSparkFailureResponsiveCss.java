package com.endava.ai.core.reporting.adapters.extent.spark.sections.failure;

final class ExtentSparkFailureResponsiveCss {

    private static final String CSS = ""
                    + "@media (max-width: 1100px) {"
                    + ".test-wrapper.row.view.test-view > .test-list,"
                    + ".test-wrapper.row.view.test-view > .test-content {flex: 0 0 100% !important; max-width: 100% !important;}"
                    + ".dashboard-view .atf-runtime-tag-grid,"
                    + ".category-view .atf-runtime-tag-grid {grid-template-columns: 1fr;}"
                    + ".category-view .atf-runtime-failure-details {margin-left: 0;}"
                    + ".dashboard-view .atf-runtime-tag-card-annotations,"
                    + ".category-view .atf-runtime-tag-card-annotations {grid-column: auto;}"
                    + "}";

    private ExtentSparkFailureResponsiveCss() {
    }

    static String css() {
        return CSS;
    }
}
