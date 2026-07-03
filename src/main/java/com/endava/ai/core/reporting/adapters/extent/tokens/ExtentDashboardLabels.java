package com.endava.ai.core.reporting.adapters.extent.tokens;

import java.util.List;

public final class ExtentDashboardLabels {
    public static final String TEST_CASES = "Test cases";
    public static final String TEST_CASE_NOUN = "test cases";
    public static final String TESTS = "Tests";
    public static final String TEST_NOUN = "tests";
    public static final String TEST_CLASSES = "Test classes";
    public static final String TEST_PACKAGES = "Test packages";
    public static final String TEST_METHODS_BY_CLASS = "Test methods by class";
    public static final String TEST_METHODS_BY_PACKAGE = "Test methods by package";
    public static final String OTHER_ANNOTATIONS = "Other annotations / tags";
    public static final String CONFIGURATION_METHODS = "Configuration methods";
    public static final String TESTNG_INVOCATIONS = "TestNG invocations";
    public static final String STEPS = "Steps";
    public static final String STARTED = "Started";
    public static final String ENDED = "Ended";
    public static final String DURATION = "Duration";
    public static final String TESTS_PASSED = "Tests Passed";
    public static final String TESTS_FAILED = "Tests Failed";
    public static final String LEGACY_TAGS = "Tags";
    public static final List<String> FORBIDDEN_OVERVIEW_SCORE_CARDS = List.of(
            TEST_CLASSES,
            TESTNG_INVOCATIONS,
            STEPS,
            CONFIGURATION_METHODS
    );

    private ExtentDashboardLabels() {
    }
}
