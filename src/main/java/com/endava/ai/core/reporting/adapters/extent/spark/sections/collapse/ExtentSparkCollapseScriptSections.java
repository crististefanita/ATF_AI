package com.endava.ai.core.reporting.adapters.extent.spark.sections.collapse;

public final class ExtentSparkCollapseScriptSections {

    private ExtentSparkCollapseScriptSections() {
    }

    public static String script() {
        return ExtentSparkCollapseBootstrapScript.javascript()
                + ExtentSparkCollapseMenuScript.javascript()
                + ExtentSparkCollapseSelectionScript.javascript()
                + ExtentSparkCollapseCardScript.javascript()
                + ExtentSparkCollapseEventScript.javascript();
    }
}
