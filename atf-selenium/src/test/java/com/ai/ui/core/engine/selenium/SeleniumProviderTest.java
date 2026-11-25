package com.ai.ui.core.engine.selenium;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UiEngineRegistry;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.devtools.CdpInfo;
import org.openqa.selenium.devtools.CdpVersionFinder;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Set;

public class SeleniumProviderTest {
    @Test
    public void adapterClasspathDiscoversOnlyItsOwnEngineWithoutStartingBrowser() {
        Assert.assertEquals(UiEngineRegistry.defaults().supportedTypes(), Set.of(UiEngineType.SELENIUM));
        Assert.assertEquals(UiEngineRegistry.defaults().supportedValues(), "selenium");
        Assert.expectThrows(ClassNotFoundException.class,
                () -> Class.forName("com.microsoft.playwright.Page"));
    }

    @Test
    public void typedDevToolsExactlyMatchesThePinnedChromeMajorWithoutStartingBrowser() {
        Assert.assertEquals(new BuildInfo().getReleaseLabel(), "4.54.0");
        CdpInfo cdp = new CdpVersionFinder().match("158.0.8127.88")
                .orElseThrow(() -> new AssertionError("The pinned Chrome 158 requires a typed CDP provider."));
        Assert.assertEquals(cdp.getMajorVersion(), 158,
                "A nearest-version CDP fallback does not satisfy the pinned compatibility matrix.");
        Assert.assertEquals(cdp.getClass().getName(), "org.openqa.selenium.devtools.v158.v158CdpInfo");
    }
}
