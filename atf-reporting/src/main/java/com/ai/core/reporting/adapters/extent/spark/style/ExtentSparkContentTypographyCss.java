package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentBadgeCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentSurfaceCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentTextCss;

final class ExtentSparkContentTypographyCss {

    private ExtentSparkContentTypographyCss() {
    }

    static String css() {
        return ExtentSparkContentSurfaceCss.css()
                + ExtentSparkContentTextCss.css()
                + ExtentSparkContentBadgeCss.css();
    }
}
