package com.ai.atf.core.reporting.adapters.css;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkVisualTokens;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkBorderTokens;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkPaletteTokens;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkTypographyTokens;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_VISUAL_TOKENS_COMPONENT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_VISUAL_TOKENS_COMPONENT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkVisualTokensComponentTest {

    @Test
    public void publicVisualTokensDelegateToSemanticTokenFamilies() {
        Assert.assertEquals(ExtentSparkVisualTokens.BACKGROUND_COLOR, ExtentSparkPaletteTokens.BACKGROUND_COLOR);
        Assert.assertEquals(ExtentSparkVisualTokens.ACTIVE_BACKGROUND_COLOR,
                ExtentSparkPaletteTokens.ACTIVE_BACKGROUND_COLOR);
        Assert.assertEquals(ExtentSparkVisualTokens.TEXT_COLOR, ExtentSparkPaletteTokens.TEXT_COLOR);
        Assert.assertEquals(ExtentSparkVisualTokens.RUNTIME_TIME_COLOR, ExtentSparkPaletteTokens.MUTED_TEXT_COLOR);
        Assert.assertEquals(ExtentSparkVisualTokens.FONT_FAMILY, ExtentSparkTypographyTokens.FONT_FAMILY);
        Assert.assertEquals(ExtentSparkVisualTokens.CODE_FONT_FAMILY, ExtentSparkTypographyTokens.CODE_FONT_FAMILY);
        Assert.assertEquals(ExtentSparkVisualTokens.RUNTIME_TEXT_FONT_SIZE,
                ExtentSparkTypographyTokens.RUNTIME_TEXT_FONT_SIZE);
        Assert.assertEquals(ExtentSparkVisualTokens.RUNTIME_BORDER_WIDTH,
                ExtentSparkBorderTokens.RUNTIME_BORDER_WIDTH);
        Assert.assertEquals(ExtentSparkVisualTokens.RUNTIME_BORDER_COLOR,
                ExtentSparkBorderTokens.RUNTIME_BORDER_COLOR);
        Assert.assertEquals(ExtentSparkVisualTokens.RUNTIME_BORDER_COLOR_STRONG,
                ExtentSparkBorderTokens.RUNTIME_BORDER_COLOR_STRONG);
    }
}
