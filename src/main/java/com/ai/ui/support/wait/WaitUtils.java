package com.ai.ui.support.wait;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.core.wait.UiSleeper;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class WaitUtils {

    private static final String DEFAULT_WAIT_TIMEOUT_SECONDS = "10";
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(250);

    private WaitUtils() {
    }

    private static int explicitWaitSeconds() {
        String key = UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY;
        String value = ConfigManager.get(
                UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY,
                ConfigManager.get(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY, DEFAULT_WAIT_TIMEOUT_SECONDS)
        );
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Missing or invalid integer config key: " + key, e
            );
        }
    }

    public static void waitForVisible(String cssSelector) {
        logDetailIfPossible("Wait for visible: " + cssSelector);
        DriverManager.getEngine().waitForVisible(cssSelector, explicitWaitSeconds());
    }

    public static void waitForUrlContains(String fragment) {
        logDetailIfPossible("Wait for URL contains: " + fragment);
        DriverManager.getEngine().waitForUrlContains(fragment, explicitWaitSeconds());
    }

    public static void waitForCondition(String description,
                                        BooleanSupplier condition,
                                        String failureMessage) {
        waitForCondition(
                description,
                condition,
                failureMessage,
                defaultTimeout()
        );
    }

    public static void waitForCondition(String description,
                                        BooleanSupplier condition,
                                        String failureMessage,
                                        Duration timeout) {
        logDetailIfPossible("Wait for condition: " + description);

        Boolean matched = waitUntil(
                condition::getAsBoolean,
                Boolean.TRUE::equals,
                timeout,
                DEFAULT_POLL_INTERVAL
        );

        if (!Boolean.TRUE.equals(matched)) {
            throw new AssertionError(failureMessage);
        }
    }

    public static void waitForInputValue(String cssSelector,
                                         String description,
                                         Predicate<String> condition) {
        waitForInputValue(cssSelector, description, condition, defaultTimeout());
    }

    public static void waitForInputValue(String cssSelector,
                                         String description,
                                         Predicate<String> condition,
                                         Duration timeout) {
        logDetailIfPossible("Wait for input value: " + description);

        String value = waitUntil(
                () -> DriverManager.getEngine().getValue(cssSelector),
                current -> current != null && condition.test(current),
                timeout,
                DEFAULT_POLL_INTERVAL
        );

        if (value == null || !condition.test(value)) {
            throw new AssertionError("Timed out waiting for input value: "
                    + description + "; last observed value: " + value);
        }
    }

    public static <T> T waitUntil(Supplier<T> supplier,
                                  Predicate<T> condition,
                                  Duration pollInterval) {
        return waitUntil(supplier, condition, defaultTimeout(), pollInterval);
    }

    public static <T> T waitUntil(Supplier<T> supplier,
                                  Predicate<T> condition,
                                  Duration timeout,
                                  Duration pollInterval) {
        Instant end = Instant.now().plus(timeout);
        T value;

        do {
            value = supplier.get();
            if (condition.test(value)) {
                return value;
            }
            sleep(pollInterval);
        } while (Instant.now().isBefore(end));

        return value;
    }

    public static void pause(Duration duration) {
        sleep(duration);
    }

    private static void logDetailIfPossible(String detail) {
        try {
            StepLogger.logDetail(detail);
        } catch (IllegalStateException ignored) {
        }
    }

    private static Duration defaultTimeout() {
        return Duration.ofSeconds(explicitWaitSeconds());
    }

    private static void sleep(Duration d) {
        UiSleeper.pause(d);
    }
}
