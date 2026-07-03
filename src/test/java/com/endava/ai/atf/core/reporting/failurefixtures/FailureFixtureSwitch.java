package com.endava.ai.atf.core.reporting.failurefixtures;

import org.testng.Reporter;

public final class FailureFixtureSwitch {

    public static final String ENABLED_PROPERTY = "atf.failure.fixtures.enabled";

    private FailureFixtureSwitch() {
    }

    static boolean isEnabled() {
        if (Boolean.getBoolean(ENABLED_PROPERTY)) {
            return true;
        }
        org.testng.ITestResult currentResult = Reporter.getCurrentTestResult();
        if (currentResult == null || currentResult.getTestContext() == null) {
            return false;
        }
        String suiteParameter = currentResult.getTestContext()
                .getCurrentXmlTest()
                .getParameter(ENABLED_PROPERTY);
        return Boolean.parseBoolean(suiteParameter);
    }
}
