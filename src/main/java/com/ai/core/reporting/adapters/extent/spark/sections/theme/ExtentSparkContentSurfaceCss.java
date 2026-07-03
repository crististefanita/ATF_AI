package com.ai.core.reporting.adapters.extent.spark.sections.theme;

import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkVisualTokens;

public final class ExtentSparkContentSurfaceCss {
    private static final String EXTENT_BACKGROUND_COLOR = ExtentSparkVisualTokens.BACKGROUND_COLOR;
    private static final String EXTENT_TEXT_COLOR = ExtentSparkVisualTokens.TEXT_COLOR;
    private static final String EXTENT_RUNTIME_BORDER_WIDTH = ExtentSparkVisualTokens.RUNTIME_BORDER_WIDTH;
    private static final String EXTENT_RUNTIME_BORDER_COLOR = ExtentSparkVisualTokens.RUNTIME_BORDER_COLOR;

    private static final String CSS = ""
            + ".dashboard-view > .row:first-of-type .card,"
            + ".dashboard-view .atf-runtime-dashboard-tests-card,"
            + ".dashboard-view .atf-runtime-dashboard-summary .card,"
            + ".dashboard-view .atf-runtime-timeline-card,"
            + ".dashboard-view .atf-runtime-tag-card,"
            + ".dashboard-view .atf-core-final-audit,"
            + ".category-view .atf-runtime-tag-card,"
            + ".test-view .test-contents .card,"
            + ".category-view .card,"
            + ".exception-view .card,"
            + ".author-view .card,"
            + ".device-view .card {"
            + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid "
            + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "border-radius: 2px !important;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "box-shadow: none;"
            + "overflow: hidden;"
            + "}"
            + ".test-view .test-contents .card-header,"
            + ".category-view .card-header,"
            + ".exception-view .card-header,"
            + ".author-view .card-header,"
            + ".device-view .card-header {"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "border-color: " + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "}"
            + ".test-view .test-contents .card-body,"
            + ".category-view .card-body,"
            + ".exception-view .card-body,"
            + ".author-view .card-body,"
            + ".device-view .card-body {"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "}"
            + ".test-view .table,"
            + ".category-view .table,"
            + ".exception-view .table,"
            + ".author-view .table,"
            + ".device-view .table {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "}"
            + ".test-view table th,"
            + ".test-view table td,"
            + ".category-view table th,"
            + ".category-view table td,"
            + ".exception-view table th,"
            + ".exception-view table td,"
            + ".author-view table th,"
            + ".author-view table td,"
            + ".device-view table th,"
            + ".device-view table td {"
            + "border-color: " + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "}"
            + ".test-view table td:nth-child(2),"
            + ".test-list .text-sm span:first-child {"
            + "white-space: nowrap !important;"
            + "}"
            + ".test-view table td:nth-child(3) {"
            + "min-width: 32rem;"
            + "}"
            + ".test-view .title,"
            + ".test-content .title,"
            + ".detail-body .title {"
            + "display: inline-block !important;"
            + "white-space: nowrap !important;"
            + "max-width: none !important;"
            + "overflow-wrap: normal !important;"
            + "word-break: normal !important;"
            + "}";

    private ExtentSparkContentSurfaceCss() {
    }

    public static String css() {
        return CSS;
    }
}
