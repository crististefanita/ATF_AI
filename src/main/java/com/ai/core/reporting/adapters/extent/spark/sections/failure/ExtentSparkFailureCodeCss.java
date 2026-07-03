package com.ai.core.reporting.adapters.extent.spark.sections.failure;

import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_BACKGROUND_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_CODE_FONT_FAMILY;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_CODE_FONT_SIZE;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_CODE_FONT_WEIGHT;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_RUNTIME_BORDER_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_RUNTIME_BORDER_WIDTH;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_TEXT_COLOR;

final class ExtentSparkFailureCodeCss {

    private static final String CSS = ""
            + ".category-view .atf-runtime-failure-code {"
            + "margin: 0;"
            + "padding: 0.65rem 0.78rem;"
            + "background: " + EXTENT_BACKGROUND_COLOR + ";"
            + "border: 0;"
            + "border-top: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR + ";"
            + "border-radius: 0 0 8px 8px;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "white-space: pre-wrap;"
            + "overflow-wrap: anywhere;"
            + "font-size: " + EXTENT_CODE_FONT_SIZE + ";"
            + "line-height: 1.5;"
            + "font-weight: " + EXTENT_CODE_FONT_WEIGHT + ";"
            + "}"
            + ".category-view .atf-runtime-failure-code code {"
            + "color: inherit !important;"
            + "font-family: " + EXTENT_CODE_FONT_FAMILY + " !important;"
            + "font-size: " + EXTENT_CODE_FONT_SIZE + " !important;"
            + "line-height: 1.5 !important;"
            + "font-weight: " + EXTENT_CODE_FONT_WEIGHT + " !important;"
            + "background: transparent !important;"
            + "padding: 0 !important;"
            + "}";

    private ExtentSparkFailureCodeCss() {
    }

    static String css() {
        return CSS;
    }
}
