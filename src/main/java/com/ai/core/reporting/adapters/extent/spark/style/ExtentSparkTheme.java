package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.spark.sections.ExtentSparkTreeCollapseCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkNestedNodeTypographyCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkSideNavSelectionCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkTestViewSurfaceCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeShellCss;

public final class ExtentSparkTheme {

    private static final String SHARED_DARK_THEME_CSS = ExtentSparkThemeShellCss.css()
            + ExtentSparkSideNavSelectionCss.css()
            + ExtentSparkDropdownCss.css()
            + ExtentSparkTreeCollapseCss.css()
            + ExtentSparkContentTypographyCss.css()
            + ExtentSparkNestedNodeTypographyCss.css()
            + ExtentSparkTestViewSurfaceCss.css()
            + ExtentSparkDashboardCss.css()
            + ExtentSparkRuntimeAuditCss.css();

    private ExtentSparkTheme() {
    }

    public static String sharedDarkThemeCss() {
        return SHARED_DARK_THEME_CSS;
    }
}
