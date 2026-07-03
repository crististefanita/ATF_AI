package com.ai.core.reporting.adapters.extent.spark.sections.theme;

import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkVisualTokens;

public final class ExtentSparkContentTextCss {
    private static final String EXTENT_TEXT_COLOR = ExtentSparkVisualTokens.TEXT_COLOR;
    private static final String EXTENT_FONT_FAMILY = ExtentSparkVisualTokens.FONT_FAMILY;
    private static final String EXTENT_CODE_FONT_FAMILY = ExtentSparkVisualTokens.CODE_FONT_FAMILY;
    private static final String EXTENT_FONT_WEIGHT_LIGHT = ExtentSparkVisualTokens.FONT_WEIGHT_LIGHT;
    private static final String EXTENT_FONT_WEIGHT_REGULAR = ExtentSparkVisualTokens.FONT_WEIGHT_REGULAR;
    private static final String EXTENT_RUNTIME_LABEL_FONT_SIZE = ExtentSparkVisualTokens.RUNTIME_LABEL_FONT_SIZE;
    private static final String EXTENT_RUNTIME_TEXT_FONT_SIZE = ExtentSparkVisualTokens.RUNTIME_TEXT_FONT_SIZE;
    private static final String EXTENT_RUNTIME_META_FONT_SIZE = ExtentSparkVisualTokens.RUNTIME_META_FONT_SIZE;
    private static final String EXTENT_RUNTIME_TIME_COLOR = ExtentSparkVisualTokens.RUNTIME_TIME_COLOR;
    private static final String EXTENT_RUNTIME_TABLE_HEADER_FONT_SIZE =
            ExtentSparkVisualTokens.RUNTIME_TABLE_HEADER_FONT_SIZE;
    private static final String EXTENT_RUNTIME_TABLE_ROW_FONT_SIZE = ExtentSparkVisualTokens.RUNTIME_TABLE_ROW_FONT_SIZE;
    private static final String EXTENT_CODE_FONT_SIZE = ExtentSparkVisualTokens.CODE_FONT_SIZE;
    private static final String EXTENT_CODE_FONT_WEIGHT = ExtentSparkVisualTokens.CODE_FONT_WEIGHT;

    private static final String CSS = ""
            + ".test-list .test-detail .name {"
            + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
            + "font-size: " + EXTENT_RUNTIME_TEXT_FONT_SIZE + " !important;"
            + "line-height: 1.34 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "letter-spacing: 0 !important;"
            + "}"
            + ".test-list .test-detail .text-sm,"
            + ".test-list .test-detail .text-sm span {"
            + "font-size: " + EXTENT_RUNTIME_META_FONT_SIZE + " !important;"
            + "line-height: 1.25 !important;"
            + "}"
            + ".test-content h5.test-status {"
            + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
            + "font-size: " + EXTENT_RUNTIME_LABEL_FONT_SIZE + " !important;"
            + "line-height: 1.34 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "letter-spacing: 0 !important;"
            + "}"
            + ".test-content > .p-v-10 > .m-t-10.m-l-5 {"
            + "display: none !important;"
            + "}"
            + ".test-contents .card-header a.node span,"
            + ".test-contents .card-header .node span {"
            + "font-family: " + EXTENT_FONT_FAMILY + " !important;"
            + "font-size: " + EXTENT_RUNTIME_TEXT_FONT_SIZE + " !important;"
            + "line-height: 1.34 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "letter-spacing: 0 !important;"
            + "}"
            + ".test-contents .card-header .node-attr,"
            + ".test-contents .card-header .node-attr .badge,"
            + ".test-contents .node-attr,"
            + ".test-contents .node-attr .badge {"
            + "font-size: " + EXTENT_RUNTIME_META_FONT_SIZE + " !important;"
            + "line-height: 1.16 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_LIGHT + " !important;"
            + "color: " + EXTENT_RUNTIME_TIME_COLOR + " !important;"
            + "border-color: rgba(184,194,207,0.42) !important;"
            + "background: " + ExtentSparkVisualTokens.BACKGROUND_COLOR + " !important;"
            + "background-color: " + ExtentSparkVisualTokens.BACKGROUND_COLOR + " !important;"
            + "}"
            + ".test-view textarea.code-block,"
            + ".test-view .code-block,"
            + ".test-view pre,"
            + ".test-view code,"
            + ".exception-view textarea.code-block,"
            + ".exception-view .code-block,"
            + ".exception-view pre,"
            + ".exception-view code,"
            + ".category-view textarea.code-block,"
            + ".category-view .code-block,"
            + ".category-view pre,"
            + ".category-view code {"
            + "font-family: " + EXTENT_CODE_FONT_FAMILY + " !important;"
            + "font-size: " + EXTENT_CODE_FONT_SIZE + " !important;"
            + "line-height: 1.38 !important;"
            + "font-weight: " + EXTENT_CODE_FONT_WEIGHT + " !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "}"
            + ".test-view table th {"
            + "font-size: " + EXTENT_RUNTIME_TABLE_HEADER_FONT_SIZE + " !important;"
            + "line-height: 1.28 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_LIGHT + " !important;"
            + "}"
            + ".test-view table td,"
            + ".test-view table td span,"
            + ".test-view table td div,"
            + ".test-view table td p {"
            + "font-size: " + EXTENT_RUNTIME_TABLE_ROW_FONT_SIZE + " !important;"
            + "line-height: 1.34 !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_LIGHT + " !important;"
            + "}";

    private ExtentSparkContentTextCss() {
    }

    public static String css() {
        return CSS;
    }
}
