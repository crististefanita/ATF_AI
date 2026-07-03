package com.ai.atf.core.reporting.adapters;

import com.ai.atf.core.reporting.adapters.support.ExtentSparkPresentationDomSupport;
import com.ai.atf.core.reporting.adapters.support.ExtentSparkVisualContractTokens;

public abstract class ExtentSparkPresentationContractSupport extends ExtentSparkPresentationDomSupport {

    protected static final String ONE_PIXEL_PNG = ExtentSparkVisualContractTokens.ONE_PIXEL_PNG;
    protected static final String EXTENT_BACKGROUND_HEX = ExtentSparkVisualContractTokens.EXTENT_BACKGROUND_HEX;
    protected static final String EXTENT_BACKGROUND_RGB = ExtentSparkVisualContractTokens.EXTENT_BACKGROUND_RGB;
    protected static final String EXTENT_ACTIVE_BACKGROUND_HEX =
            ExtentSparkVisualContractTokens.EXTENT_ACTIVE_BACKGROUND_HEX;
    protected static final String EXTENT_ACTIVE_BACKGROUND_RGB =
            ExtentSparkVisualContractTokens.EXTENT_ACTIVE_BACKGROUND_RGB;
    protected static final String EXTENT_TEXT_HEX = ExtentSparkVisualContractTokens.EXTENT_TEXT_HEX;
    protected static final String EXTENT_TEXT_RGB = ExtentSparkVisualContractTokens.EXTENT_TEXT_RGB;
    protected static final String EXTENT_FONT_FAMILY = ExtentSparkVisualContractTokens.EXTENT_FONT_FAMILY;
    protected static final String EXTENT_RUNTIME_TIME_RGB =
            ExtentSparkVisualContractTokens.EXTENT_RUNTIME_TIME_RGB;
    protected static final String EXTENT_RUNTIME_BORDER_RGB =
            ExtentSparkVisualContractTokens.EXTENT_RUNTIME_BORDER_RGB;

    public static final class ExtentHierarchyFixture {
    }

    public static final class SecondaryExtentHierarchyFixture {
    }
}

