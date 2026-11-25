package com.ai.atf.core.reporting.adapters.dom;

import com.ai.core.reporting.adapters.extent.inspection.ExtentRenderedStateParser;
import org.testng.Assert;

public final class ExtentRenderedState {

    private ExtentRenderedState() {
    }

    public static double numericValue(String state, String key) {
        try {
            return ExtentRenderedStateParser.parse(state).numericValue(key);
        } catch (IllegalArgumentException e) {
            Assert.fail("State must expose numeric value for " + key + ": " + state, e);
            return Double.NaN;
        }
    }
}
