package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.spark.sections.collapse.ExtentSparkCollapseScriptSections;

public final class ExtentSparkCollapsePolicy {

    private static final String COLLAPSE_TEST_NODES_BY_DEFAULT_JS = ExtentSparkCollapseScriptSections.script();

    private ExtentSparkCollapsePolicy() {
    }

    public static String defaultCollapseScript() {
        return COLLAPSE_TEST_NODES_BY_DEFAULT_JS;
    }
}
