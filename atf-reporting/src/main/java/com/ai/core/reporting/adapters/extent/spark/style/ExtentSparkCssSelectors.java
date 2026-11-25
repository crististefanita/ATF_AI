package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;

final class ExtentSparkCssSelectors {
    private static final String CLASS_PREFIX = ".";
    private static final String SELECTOR_SEPARATOR = ",";

    private ExtentSparkCssSelectors() {
    }

    static String dashboard(String suffix) {
        return CLASS_PREFIX + ExtentDomIds.DASHBOARD_VIEW + suffix;
    }

    static String category(String suffix) {
        return CLASS_PREFIX + ExtentDomIds.CATEGORY_VIEW + suffix;
    }

    static String dashboardAndCategory(String suffix) {
        return dashboard(suffix) + SELECTOR_SEPARATOR + category(suffix);
    }
}
