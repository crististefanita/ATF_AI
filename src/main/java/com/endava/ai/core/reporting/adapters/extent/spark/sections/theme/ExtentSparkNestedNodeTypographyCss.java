package com.endava.ai.core.reporting.adapters.extent.spark.sections.theme;

import static com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_FONT_FAMILY;
import static com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_FONT_WEIGHT_REGULAR;
import static com.endava.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_RUNTIME_TEXT_FONT_SIZE;

public final class ExtentSparkNestedNodeTypographyCss {

    private static final String CSS = ""
                    + ".test-contents .card-header .card-title,"
                    + ".test-contents .card-header .card-title a.node,"
                    + ".test-contents .card-header .card-title a.node span {"
                    + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
                    + "font-size: " + EXTENT_RUNTIME_TEXT_FONT_SIZE + " !important;"
                    + "line-height: 1.34 !important;"
                    + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
                    + "letter-spacing: 0 !important;"
                    + "}"
                    + ".test-contents .card .card .card-header .card-title,"
                    + ".test-contents .card .card .card-header .card-title a.node,"
                    + ".test-contents .card .card .card-header .card-title a.node span {"
                    + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
                    + "font-size: " + EXTENT_RUNTIME_TEXT_FONT_SIZE + " !important;"
                    + "line-height: 1.34 !important;"
                    + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
                    + "letter-spacing: 0 !important;"
                    + "}"
                    + ".test-contents .card .card .card .card-header .card-title,"
                    + ".test-contents .card .card .card .card-header .card-title a.node,"
                    + ".test-contents .card .card .card .card-header .card-title a.node span {"
                    + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
                    + "font-size: " + EXTENT_RUNTIME_TEXT_FONT_SIZE + " !important;"
                    + "line-height: 1.34 !important;"
                    + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
                    + "letter-spacing: 0 !important;"
                    + "}"
                    + ".test-contents .card-header a.node span.extent-atf-arrow,"
                    + ".test-contents .card-header .node span.extent-atf-arrow {"
                    + "font-size: 0 !important;"
                    + "line-height: 0 !important;"
                    + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
                    + "color: transparent !important;"
                    + "}"
;

    private ExtentSparkNestedNodeTypographyCss() {
    }

    public static String css() {
        return CSS;
    }
}
