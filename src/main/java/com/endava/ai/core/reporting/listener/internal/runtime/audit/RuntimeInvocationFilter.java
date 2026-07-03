package com.endava.ai.core.reporting.listener.internal.runtime.audit;

import org.testng.ITestResult;

public final class RuntimeInvocationFilter {

    private static final String FAILURE_FIXTURES_ENABLED_PROPERTY = "atf.failure.fixtures.enabled";

    private RuntimeInvocationFilter() {
    }

    public static boolean isReportable(ITestResult result, String className, String methodName) {
        if (className == null || className.isBlank()) {
            return false;
        }
        if (methodName == null || methodName.isBlank()) {
            return false;
        }
        if ((className.contains(".atf.reporting.failurefixtures.")
                || className.contains(".atf.core.reporting.failurefixtures."))
                && !failureFixturesEnabled(result)) {
            return false;
        }
        if (className.contains("$")) {
            return false;
        }
        return !methodName.startsWith("fake");
    }

    private static boolean failureFixturesEnabled(ITestResult result) {
        if (Boolean.getBoolean(FAILURE_FIXTURES_ENABLED_PROPERTY)) {
            return true;
        }
        if (result == null || result.getTestContext() == null || result.getTestContext().getCurrentXmlTest() == null) {
            return false;
        }
        String parameter = result.getTestContext().getCurrentXmlTest().getParameter(FAILURE_FIXTURES_ENABLED_PROPERTY);
        return Boolean.parseBoolean(parameter);
    }
}
