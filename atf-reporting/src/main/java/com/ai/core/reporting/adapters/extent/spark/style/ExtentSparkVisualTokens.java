package com.ai.core.reporting.adapters.extent.spark.style;

import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkBorderTokens;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkPaletteTokens;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkTypographyTokens;

/**
 * Canonical visual tokens for the Extent Spark report skin.
 * Renderers should compose CSS from these values instead of redefining palette or typography literals.
 */
public final class ExtentSparkVisualTokens {

    public static final String BACKGROUND_COLOR = ExtentSparkPaletteTokens.BACKGROUND_COLOR;
    public static final String ACTIVE_BACKGROUND_COLOR = ExtentSparkPaletteTokens.ACTIVE_BACKGROUND_COLOR;
    public static final String SIDE_NAV_SELECTED_BACKGROUND_COLOR =
            ExtentSparkPaletteTokens.SIDE_NAV_SELECTED_BACKGROUND_COLOR;
    public static final String SIDE_NAV_SELECTED_RAIL_COLOR = ExtentSparkPaletteTokens.SIDE_NAV_SELECTED_RAIL_COLOR;
    public static final String TEXT_COLOR = ExtentSparkPaletteTokens.TEXT_COLOR;
    public static final String RUNTIME_TIME_COLOR = ExtentSparkPaletteTokens.MUTED_TEXT_COLOR;
    public static final String FONT_FAMILY = ExtentSparkTypographyTokens.FONT_FAMILY;
    public static final String CODE_FONT_FAMILY = ExtentSparkTypographyTokens.CODE_FONT_FAMILY;
    public static final String FONT_WEIGHT_LIGHT = ExtentSparkTypographyTokens.FONT_WEIGHT_LIGHT;
    public static final String FONT_WEIGHT_REGULAR = ExtentSparkTypographyTokens.FONT_WEIGHT_REGULAR;
    public static final String RUNTIME_LABEL_FONT_SIZE = ExtentSparkTypographyTokens.RUNTIME_LABEL_FONT_SIZE;
    public static final String RUNTIME_TEXT_FONT_SIZE = ExtentSparkTypographyTokens.RUNTIME_TEXT_FONT_SIZE;
    public static final String RUNTIME_META_FONT_SIZE = ExtentSparkTypographyTokens.RUNTIME_META_FONT_SIZE;
    public static final String RUNTIME_BADGE_FONT_SIZE = ExtentSparkTypographyTokens.RUNTIME_BADGE_FONT_SIZE;
    public static final String RUNTIME_TABLE_HEADER_FONT_SIZE =
            ExtentSparkTypographyTokens.RUNTIME_TABLE_HEADER_FONT_SIZE;
    public static final String RUNTIME_TABLE_ROW_FONT_SIZE = ExtentSparkTypographyTokens.RUNTIME_TABLE_ROW_FONT_SIZE;
    public static final String CODE_FONT_SIZE = ExtentSparkTypographyTokens.CODE_FONT_SIZE;
    public static final String CODE_FONT_WEIGHT = ExtentSparkTypographyTokens.CODE_FONT_WEIGHT;
    public static final String RUNTIME_BORDER_WIDTH = ExtentSparkBorderTokens.RUNTIME_BORDER_WIDTH;
    public static final String RUNTIME_BORDER_COLOR = ExtentSparkBorderTokens.RUNTIME_BORDER_COLOR;
    public static final String RUNTIME_BORDER_COLOR_STRONG = ExtentSparkBorderTokens.RUNTIME_BORDER_COLOR_STRONG;

    private ExtentSparkVisualTokens() {
    }
}
