package com.endava.ai.atf.core.support.contract;

import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import com.endava.ai.ui.core.engine.window.WindowMode;
import org.testng.Assert;

public final class AtfWindowContractAssertions {

    private AtfWindowContractAssertions() {
    }

    public static void assertWindowConfiguration(
            UIWindowConfiguration actual,
            WindowMode expectedMode,
            int expectedWidth,
            int expectedHeight,
            int expectedZoomPercent
    ) {
        Assert.assertEquals(actual.mode(), expectedMode, "Resolved window mode");
        Assert.assertEquals(actual.width(), expectedWidth, "Resolved window width");
        Assert.assertEquals(actual.height(), expectedHeight, "Resolved window height");
        Assert.assertEquals(actual.zoomPercent(), expectedZoomPercent, "Resolved zoom percent");
    }
}
