package com.ai.core.reporting.adapters.extent.spark.sections.theme;

import static com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_SIDE_NAV_SELECTED_BACKGROUND_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_SIDE_NAV_SELECTED_RAIL_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_BACKGROUND_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_TEXT_COLOR;

public final class ExtentSparkSideNavSelectionCss {

    private static final String CSS =
            ".side-nav-menu .nav-item,"
                    + ".side-nav .nav-item,"
                    + ".side-nav li {"
                    + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
                    + "background-color: " + EXTENT_BACKGROUND_COLOR + " !important;"
                    + "}"
                    + ".side-nav-menu .nav-item > a,"
                    + ".side-nav-menu .nav-item i,"
                    + ".side-nav .nav-item > a,"
                    + ".side-nav .nav-item i,"
                    + ".side-nav li > a,"
                    + ".side-nav li i {"
                    + "opacity: 0.62 !important;"
                    + "}"
                    + ".side-nav-menu .nav-item.active,"
                    + ".side-nav .nav-item.active,"
                    + ".side-nav li.active {"
                    + "background: " + EXTENT_SIDE_NAV_SELECTED_BACKGROUND_COLOR + " !important;"
                    + "background-color: " + EXTENT_SIDE_NAV_SELECTED_BACKGROUND_COLOR + " !important;"
                    + "box-shadow: inset 3px 0 0 " + EXTENT_SIDE_NAV_SELECTED_RAIL_COLOR + " !important;"
                    + "}"
                    + ".side-nav-menu .nav-item.active > a,"
                    + ".side-nav-menu .nav-item.active i,"
                    + ".side-nav .nav-item.active > a,"
                    + ".side-nav .nav-item.active i,"
                    + ".side-nav li.active > a,"
                    + ".side-nav li.active i {"
                    + "color: " + EXTENT_TEXT_COLOR + " !important;"
                    + "opacity: 1 !important;"
                    + "}"
                    + ".side-nav-menu .nav-item.active:hover,"
                    + ".side-nav .nav-item.active:hover,"
                    + ".side-nav li.active:hover {"
                    + "background: " + EXTENT_SIDE_NAV_SELECTED_BACKGROUND_COLOR + " !important;"
                    + "background-color: " + EXTENT_SIDE_NAV_SELECTED_BACKGROUND_COLOR + " !important;"
                    + "box-shadow: inset 3px 0 0 " + EXTENT_SIDE_NAV_SELECTED_RAIL_COLOR + " !important;"
                    + "}";

    private ExtentSparkSideNavSelectionCss() {
    }

    public static String css() {
        return CSS;
    }
}
