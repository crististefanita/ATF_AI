package com.ai.core.reporting.adapters.extent.spark.sections.theme;

import static com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeTokens.EXTENT_BACKGROUND_COLOR;

final class ExtentSparkTestViewBackgroundCss {

    private static final String CSS = ""
            + ".test-view .test-contents,"
            + ".test-view .test-contents .accordion,"
            + ".test-view .test-contents .collapse,"
            + ".test-view .test-contents .card,"
            + ".test-view .test-contents .card-header,"
            + ".test-view .test-contents .card-body,"
            + ".test-view .test-contents .card-header .card-title,"
            + ".test-view .test-contents .card-header .node,"
            + ".test-view .test-contents .card-header > ul,"
            + ".test-view .test-contents .card-header > ul li,"
            + ".test-view .test-contents .node-attr,"
            + ".test-view .test-contents .node-attr .badge,"
            + ".test-content .test-time-info,"
            + ".test-content .test-time-info span,"
            + ".test-content .test-time-info .badge,"
            + ".test-content .test-time-info .uri-anchor,"
            + ".test-content > span.badge,"
            + ".test-view .test-contents .atf-runtime-tag-selected-item,"
            + ".test-view .test-contents .mt-4,"
            + ".test-view .test-contents .m-t-15 {"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "background-color: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "}";

    private ExtentSparkTestViewBackgroundCss() {
    }

    static String css() {
        return CSS;
    }
}
