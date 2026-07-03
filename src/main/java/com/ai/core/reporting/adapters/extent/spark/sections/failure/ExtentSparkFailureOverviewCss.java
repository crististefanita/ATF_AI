package com.ai.core.reporting.adapters.extent.spark.sections.failure;

import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_BACKGROUND_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_RUNTIME_BORDER_COLOR;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_RUNTIME_BORDER_WIDTH;
import static com.ai.core.reporting.adapters.extent.spark.sections.failure.ExtentSparkFailureTriageTokens.EXTENT_TEXT_COLOR;

final class ExtentSparkFailureOverviewCss {

    private static final String CSS = ""
                    + ".category-view .atf-runtime-failure-grid {"
                    + "display: block;"
                    + "width: 100%;"
                    + "overflow: visible;"
                    + "}"
                    + ".category-view .atf-runtime-failure-empty {"
                    + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR + ";"
                    + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
                    + "color: " + EXTENT_TEXT_COLOR + " !important;"
                    + "padding: 1rem 1.2rem;"
                    + "}"
                    + ".category-view .atf-runtime-failure-empty strong {"
                    + "display: block;"
                    + "margin-bottom: 0.25rem;"
                    + "color: " + EXTENT_TEXT_COLOR + " !important;"
                    + "}"
                    + ".category-view .atf-runtime-failure-empty p {"
                    + "margin: 0;"
                    + "color: " + EXTENT_TEXT_COLOR + " !important;"
                    + "opacity: 0.82;"
                    + "}"
                    + ".category-view .atf-runtime-failure-summary {"
                    + "grid-column: 1 / -1;"
                    + "display: flex;"
                    + "align-items: center;"
                    + "gap: 0.75rem;"
                    + "padding: 0.8rem 1rem;"
                    + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR + ";"
                    + "background: " + EXTENT_BACKGROUND_COLOR + ";"
                    + "color: " + EXTENT_TEXT_COLOR + " !important;"
                    + "}"
                    + ".category-view .atf-runtime-failure-summary span:last-child {"
                    + "opacity: 0.78;"
                    + "}"
;

    private ExtentSparkFailureOverviewCss() {
    }

    static String css() {
        return CSS;
    }
}