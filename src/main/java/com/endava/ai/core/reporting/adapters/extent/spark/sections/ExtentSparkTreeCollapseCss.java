package com.endava.ai.core.reporting.adapters.extent.spark.sections;

import com.endava.ai.core.reporting.adapters.extent.spark.style.ExtentSparkVisualTokens;
import com.endava.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkSpacingTokens;

/**
 * Tree-collapse CSS for Extent Spark test nodes.
 * The runtime collapse script owns behavior; this section owns only the visual policy.
 */
public final class ExtentSparkTreeCollapseCss {

    private static final String EXTENT_TEXT_COLOR = ExtentSparkVisualTokens.TEXT_COLOR;
    private static final String EXTENT_FONT_FAMILY = ExtentSparkVisualTokens.FONT_FAMILY;
    private static final String TREE_ARROW_MARGIN_RIGHT = ExtentSparkSpacingTokens.TREE_ARROW_MARGIN_RIGHT;
    private static final String TREE_ARROW_BORDER_VERTICAL = ExtentSparkSpacingTokens.TREE_ARROW_BORDER_VERTICAL;
    private static final String TREE_ARROW_BORDER_LEFT = ExtentSparkSpacingTokens.TREE_ARROW_BORDER_LEFT;
    private static final String TREE_ARROW_TRANSITION = ExtentSparkSpacingTokens.TREE_ARROW_TRANSITION;

    private static final String CSS =
            ".test-contents .card.extent-atf-collapsed > .accordion,"
                    + ".test-contents .card.extent-atf-collapsed > .collapse {"
                    + "display: none !important;"
                    + "}"
                    + ".test-contents a.node:before,"
                    + ".test-contents a.node:after,"
                    + ".test-contents .card-header .card-title > a.node:before {"
                    + "content: none !important;"
                    + "display: none !important;"
                    + "}"
                    + ".test-contents .extent-atf-arrow {"
                    + "display: inline-block;"
                    + "width: 0;"
                    + "height: 0;"
                    + "margin-right: " + TREE_ARROW_MARGIN_RIGHT + ";"
                    + "vertical-align: middle;"
                    + "border-top: " + TREE_ARROW_BORDER_VERTICAL + " solid transparent;"
                    + "border-bottom: " + TREE_ARROW_BORDER_VERTICAL + " solid transparent;"
                    + "border-left: " + TREE_ARROW_BORDER_LEFT + " solid " + EXTENT_TEXT_COLOR + ";"
                    + "opacity: 0.9;"
                    + "transform-origin: center;"
                    + "transition: " + TREE_ARROW_TRANSITION + ";"
                    + "font-size: 0 !important;"
                    + "}"
                    + ".test-contents .card.extent-atf-collapsed > .card-header .extent-atf-arrow:before {"
                    + "content: none !important;"
                    + "}"
                    + ".test-contents .card.extent-atf-collapse-ready:not(.extent-atf-collapsed) "
                    + "> .card-header .extent-atf-arrow:before {"
                    + "content: none !important;"
                    + "}"
                    + ".test-contents .card.extent-atf-collapse-ready:not(.extent-atf-collapsed) "
                    + "> .card-header .extent-atf-arrow {"
                    + "transform: rotate(90deg);"
                    + "}"
                    + ".test-contents .card.extent-atf-collapsed > .card-header .extent-atf-arrow {"
                    + "transform: none;"
                    + "}"
                    + ".test-contents .card.extent-atf-leaf > .card-header .extent-atf-arrow {"
                    + "visibility: hidden !important;"
                    + "border-left-color: transparent !important;"
                    + "}"
                    + ".test-contents .node-attr {"
                    + "display: none !important;"
                    + "}"
                    + ".test-contents .card.extent-atf-root-node > .card-header .node-attr {"
                    + "display: block !important;"
                    + "}"
                    + ".test-contents > .mt-4 > .accordion > .card > .card-header .node-attr,"
                    + ".test-contents > .accordion > .card > .card-header .node-attr,"
                    + ".test-content .detail-body > .mt-4 > .accordion > .card > .card-header .node-attr {"
                    + "display: block !important;"
                    + "}"
                    + ".test-contents .card.extent-atf-sub-node > .card-header .node-attr {"
                    + "display: none !important;"
                    + "}"
                    + ".test-contents .card .card > .card-header .node-attr,"
                    + ".test-content .detail-body .card .card > .card-header .node-attr,"
                    + ".test-content .detail-body .accordion .accordion .card > .card-header .node-attr {"
                    + "display: none !important;"
                    + "}";

    private ExtentSparkTreeCollapseCss() {
    }

    public static String css() {
        return CSS;
    }
}
