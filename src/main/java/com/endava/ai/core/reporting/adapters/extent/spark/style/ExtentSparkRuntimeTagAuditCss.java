package com.endava.ai.core.reporting.adapters.extent.spark.style;

final class ExtentSparkRuntimeTagAuditCss {
    private static final String RUNTIME_TAGS = " .atf-runtime-tags";
    private static final String RUNTIME_TAGS_HEADING = " .atf-runtime-tags-heading";
    private static final String RUNTIME_TAG_GRID = " .atf-runtime-tag-grid";
    private static final String RUNTIME_TAG_CARD_ANNOTATIONS = " .atf-runtime-tag-card-annotations";
    private static final String RUNTIME_TAG_CARD_HEADER = " .atf-runtime-tag-card .card-header";
    private static final String RUNTIME_TAG_CARD_HEADER_TEXT = " .atf-runtime-tag-card .card-header p";
    private static final String RUNTIME_TAG_CARD_BODY = " .atf-runtime-tag-card .card-body";
    private static final String RUNTIME_TAG_SCROLL = " .atf-runtime-tag-scroll";
    private static final String RUNTIME_TAG_TABLE = " .atf-runtime-tag-table";
    private static final String RUNTIME_TAG_TABLE_BODY_ODD = " .atf-runtime-tag-table tbody tr:nth-child(odd)";
    private static final String RUNTIME_TAG_TABLE_BODY_HOVER = " .atf-runtime-tag-table tbody tr:hover";
    private static final String RUNTIME_TAG_TABLE_NUMERIC_HEADER = " .atf-runtime-tag-table th:nth-child(n+2)";
    private static final String RUNTIME_TAG_TABLE_NUMERIC_CELL = " .atf-runtime-tag-table td:nth-child(n+2)";
    private static final String RUNTIME_TAG_TABLE_HEAD = " .atf-runtime-tag-table thead th";
    private static final String RUNTIME_TAG_TOTAL_HEADER = " .atf-runtime-tag-total th";
    private static final String RUNTIME_TAG_TOTAL_CELL = " .atf-runtime-tag-total td";

    private static final String EXTENT_BACKGROUND_COLOR = ExtentSparkVisualTokens.BACKGROUND_COLOR;
    private static final String EXTENT_TEXT_COLOR = ExtentSparkVisualTokens.TEXT_COLOR;
    private static final String EXTENT_FONT_WEIGHT_REGULAR = ExtentSparkVisualTokens.FONT_WEIGHT_REGULAR;
    private static final String EXTENT_RUNTIME_BORDER_WIDTH = ExtentSparkVisualTokens.RUNTIME_BORDER_WIDTH;
    private static final String EXTENT_RUNTIME_BORDER_COLOR = ExtentSparkVisualTokens.RUNTIME_BORDER_COLOR;

    private static final String CSS = ""
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAGS) + " {"
            + "flex: 0 0 100%;"
            + "max-width: 100%;"
            + "box-sizing: border-box;"
            + "padding: 0.75rem 0 1rem 0;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAGS_HEADING) + " {"
            + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR + ";"
            + "border-radius: 2px;"
            + "padding: 1rem 1.25rem;"
            + "margin-bottom: 1rem;"
            + "background: " + EXTENT_BACKGROUND_COLOR + ";"
            + "box-shadow: none;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAGS_HEADING + " h3") + " {"
            + "margin: 0 0 0.25rem 0;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-size: 1rem;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + ";"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAGS_HEADING + " p") + " {"
            + "margin: 0;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-size: 0.88rem;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_GRID) + " {"
            + "display: grid;"
            + "grid-template-columns: repeat(3, minmax(322px, 1fr));"
            + "gap: 1.25rem;"
            + "align-items: start;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_CARD_ANNOTATIONS) + " {"
            + "grid-column: auto;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_CARD_HEADER) + " {"
            + "border-bottom: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid "
            + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "padding: 0.95rem 1rem;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_CARD_HEADER_TEXT) + " {"
            + "margin: 0;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + ";"
            + "letter-spacing: 0.01em;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_CARD_BODY) + " {"
            + "padding: 0;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_SCROLL) + " {"
            + "max-height: 340px;"
            + "overflow: auto;"
            + "border-radius: 0 0 10px 10px;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_TABLE) + " {"
            + "margin-bottom: 0 !important;"
            + "font-size: 0.86rem;"
            + "line-height: 1.48;"
            + "table-layout: fixed;"
            + "}"
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TABLE + " th") + ","
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TABLE + " td") + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TABLE + " th") + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TABLE + " td") + " {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "border-color: " + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "vertical-align: top;"
            + "padding: 0.48rem 0.72rem !important;"
            + "word-break: break-word;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_TABLE_BODY_ODD) + " {"
            + "background: rgba(255,255,255,0.025) !important;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_TABLE_BODY_HOVER) + " {"
            + "background: rgba(255,255,255,0.06) !important;"
            + "}"
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TABLE_NUMERIC_HEADER) + ","
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TABLE_NUMERIC_CELL) + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TABLE_NUMERIC_HEADER) + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TABLE_NUMERIC_CELL) + " {"
            + "text-align: right;"
            + "width: 5.5rem;"
            + "white-space: nowrap;"
            + "}"
            + ExtentSparkCssSelectors.dashboardAndCategory(RUNTIME_TAG_TABLE_HEAD) + " {"
            + "position: sticky;"
            + "top: 0;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "z-index: 1;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-size: 0.74rem;"
            + "text-transform: uppercase;"
            + "letter-spacing: 0.045em;"
            + "}"
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TOTAL_HEADER) + ","
            + ExtentSparkCssSelectors.dashboard(RUNTIME_TAG_TOTAL_CELL) + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TOTAL_HEADER) + ","
            + ExtentSparkCssSelectors.category(RUNTIME_TAG_TOTAL_CELL) + " {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + ";"
            + "background: rgba(255,255,255,0.06) !important;"
            + "}";

    private ExtentSparkRuntimeTagAuditCss() {
    }

    static String css() {
        return CSS;
    }
}
