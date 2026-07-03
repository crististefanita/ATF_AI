package com.endava.ai.core.reporting.adapters.extent.patch.component;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ExtentComponentStatusExtractor {
    private static final String TEST_CASE_LEVEL_NOUNS = alternatives(
            ExtentDashboardLabels.TEST_CASE_NOUN,
            ExtentDashboardLabels.TEST_CLASSES.toLowerCase(java.util.Locale.ROOT)
    );
    private static final String TEST_METHOD_LEVEL_NOUNS = alternatives(
            ExtentDashboardLabels.TEST_NOUN,
            ExtentDashboardLabels.CONFIGURATION_METHODS.toLowerCase(java.util.Locale.ROOT)
    );
    private static final Pattern TEST_CASES_PASSED = scoreLinePattern(TEST_CASE_LEVEL_NOUNS, "passed");
    private static final Pattern TEST_CASES_FAILED = scoreLinePattern(TEST_CASE_LEVEL_NOUNS, "failed");
    private static final Pattern TESTS_PASSED = scoreLinePattern(TEST_METHOD_LEVEL_NOUNS, "passed");
    private static final Pattern TESTS_FAILED = scoreLinePattern(TEST_METHOD_LEVEL_NOUNS, "failed");
    private static final Pattern SKIPPED_AFTER_TEST_CASES = Pattern.compile(
            "<b>\\d+</b>\\s*" + TEST_CASE_LEVEL_NOUNS + " failed,\\s*<b>(\\d+)</b>\\s*skipped"
    );
    private static final Pattern SKIPPED_AFTER_TESTS = Pattern.compile(
            "<b>\\d+</b>\\s*" + TEST_METHOD_LEVEL_NOUNS + " failed,\\s*<b>(\\d+)</b>\\s*skipped"
    );

    private ExtentComponentStatusExtractor() {
    }

    static ExtentComponentStatus from(String html) {
        int cardTestPassed = firstNumber(TESTS_PASSED, html);
        int cardTestFailed = firstNumber(TESTS_FAILED, html);
        int cardTestSkipped = firstNumber(SKIPPED_AFTER_TESTS, html);
        ExtentComponentStatus fromCards = new ExtentComponentStatus(
                firstNumber(TEST_CASES_PASSED, html),
                firstNumber(TEST_CASES_FAILED, html),
                firstNumber(SKIPPED_AFTER_TEST_CASES, html),
                Math.max(cardTestPassed, statusGroupValue(html, "passChild")),
                Math.max(cardTestFailed, statusGroupValue(html, "failChild")),
                Math.max(cardTestSkipped, statusGroupValue(html, "skipChild"))
        );
        ExtentComponentStatus fromStatusGroup = new ExtentComponentStatus(
                fromCards.classPassed(),
                fromCards.classFailed(),
                fromCards.classSkipped(),
                statusGroupValue(html, "passChild"),
                statusGroupValue(html, "failChild"),
                statusGroupValue(html, "skipChild")
        );
        return fromStatusGroup.testTotal() > fromCards.testTotal() ? fromStatusGroup : fromCards;
    }

    private static int statusGroupValue(String html, String key) {
        return firstNumber(Pattern.compile(Pattern.quote(key) + ":\\s*(\\d+)"), html);
    }

    private static int firstNumber(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static Pattern scoreLinePattern(String nounAlternatives, String status) {
        return Pattern.compile("<b>(\\d+)</b>\\s*" + nounAlternatives + " " + Pattern.quote(status));
    }

    private static String alternatives(String... values) {
        return "(?:"
                + java.util.Arrays.stream(values)
                .map(Pattern::quote)
                .collect(java.util.stream.Collectors.joining("|"))
                + ")";
    }
}
