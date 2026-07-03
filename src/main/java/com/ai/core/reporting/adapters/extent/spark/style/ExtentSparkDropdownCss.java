package com.ai.core.reporting.adapters.extent.spark.style;

final class ExtentSparkDropdownCss {
    private static final String EXTENT_BACKGROUND_COLOR = ExtentSparkVisualTokens.BACKGROUND_COLOR;
    private static final String EXTENT_ACTIVE_BACKGROUND_COLOR = ExtentSparkVisualTokens.ACTIVE_BACKGROUND_COLOR;
    private static final String EXTENT_TEXT_COLOR = ExtentSparkVisualTokens.TEXT_COLOR;
    private static final String EXTENT_FONT_WEIGHT_REGULAR = ExtentSparkVisualTokens.FONT_WEIGHT_REGULAR;
    private static final String EXTENT_RUNTIME_BORDER_WIDTH = ExtentSparkVisualTokens.RUNTIME_BORDER_WIDTH;
    private static final String EXTENT_RUNTIME_BORDER_COLOR = ExtentSparkVisualTokens.RUNTIME_BORDER_COLOR;

    private static final String CSS = ""
            + "#tag-toggle.dropdown-menu {"
            + "position: fixed !important;"
            + "top: 7.75rem !important;"
            + "left: 4.75rem !important;"
            + "right: auto !important;"
            + "transform: none !important;"
            + "width: min(36rem, calc(100vw - 6rem)) !important;"
            + "max-height: calc(100vh - 9rem) !important;"
            + "overflow-y: auto !important;"
            + "overflow-x: hidden !important;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR
            + " !important;"
            + "border-radius: 2px !important;"
            + "box-shadow: 0 14px 32px rgba(0,0,0,0.38) !important;"
            + "padding: 0.35rem 0 !important;"
            + "z-index: 1100 !important;"
            + "}"
            + "#tag-toggle.dropdown-menu .dropdown-item {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "white-space: normal !important;"
            + "overflow-wrap: anywhere !important;"
            + "line-height: 1.25 !important;"
            + "padding: 0.52rem 0.85rem !important;"
            + "border-bottom: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid "
            + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "}"
            + "#tag-toggle.dropdown-menu .dropdown-item:hover,"
            + "#tag-toggle.dropdown-menu .dropdown-item:focus {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "background: " + EXTENT_ACTIVE_BACKGROUND_COLOR + " !important;"
            + "}"
            + "#status-toggle.dropdown-menu {"
            + "position: fixed !important;"
            + "top: 7.75rem !important;"
            + "left: 20.5rem !important;"
            + "right: auto !important;"
            + "transform: none !important;"
            + "min-width: 14.5rem !important;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "border: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid " + EXTENT_RUNTIME_BORDER_COLOR
            + " !important;"
            + "border-radius: 2px !important;"
            + "box-shadow: 0 14px 32px rgba(0,0,0,0.38) !important;"
            + "padding: 0.35rem 0 !important;"
            + "z-index: 1100 !important;"
            + "}"
            + "#status-toggle.dropdown-menu .atf-status-filter-label {"
            + "display: block !important;"
            + "padding: 0.35rem 0.85rem 0.45rem 0.85rem !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "font-size: 0.74rem !important;"
            + "font-weight: " + EXTENT_FONT_WEIGHT_REGULAR + " !important;"
            + "letter-spacing: 0.045em !important;"
            + "text-transform: uppercase !important;"
            + "border-bottom: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid "
            + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "}"
            + "#status-toggle.dropdown-menu .dropdown-item {"
            + "display: flex !important;"
            + "align-items: center !important;"
            + "justify-content: space-between !important;"
            + "gap: 1.5rem !important;"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "background: " + EXTENT_BACKGROUND_COLOR + " !important;"
            + "line-height: 1.25 !important;"
            + "padding: 0.58rem 0.85rem !important;"
            + "border-bottom: " + EXTENT_RUNTIME_BORDER_WIDTH + " solid "
            + EXTENT_RUNTIME_BORDER_COLOR + " !important;"
            + "}"
            + "#status-toggle.dropdown-menu .dropdown-item:hover,"
            + "#status-toggle.dropdown-menu .dropdown-item:focus {"
            + "color: " + EXTENT_TEXT_COLOR + " !important;"
            + "background: " + EXTENT_ACTIVE_BACKGROUND_COLOR + " !important;"
            + "}";

    private ExtentSparkDropdownCss() {
    }

    static String css() {
        return CSS;
    }
}
