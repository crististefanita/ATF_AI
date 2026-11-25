package com.ai.ui.core.engine.playwright;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UiEngineRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Set;

public class PlaywrightProviderTest {
    @Test
    public void adapterClasspathDiscoversOnlyItsOwnEngineWithoutStartingBrowser() {
        Assert.assertEquals(UiEngineRegistry.defaults().supportedTypes(), Set.of(UiEngineType.PLAYWRIGHT));
        Assert.assertEquals(UiEngineRegistry.defaults().supportedValues(), "playwright");
        Assert.expectThrows(ClassNotFoundException.class,
                () -> Class.forName("org.openqa.selenium.WebDriver"));
    }
}