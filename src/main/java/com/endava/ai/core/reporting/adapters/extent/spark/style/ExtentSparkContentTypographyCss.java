package com.endava.ai.core.reporting.adapters.extent.spark.style;

import com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentBadgeCss;
import com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentSurfaceCss;
import com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentTextCss;

final class ExtentSparkContentTypographyCss {

    private ExtentSparkContentTypographyCss() {
    }

    static String css() {
        return ExtentSparkContentSurfaceCss.css()
                + ExtentSparkContentTextCss.css()
                + ExtentSparkContentBadgeCss.css();
    }
}
