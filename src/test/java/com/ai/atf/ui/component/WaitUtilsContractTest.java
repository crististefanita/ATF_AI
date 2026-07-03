package com.ai.atf.ui.component;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.atf.ui.component.support.StubUiEngine;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.ui.support.wait.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.WAIT_UTILS_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.WAIT_UTILS_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class WaitUtilsContractTest {

    @AfterMethod
    public void tearDown() {
        StepLogger.clear();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
    }

    @Test
    public void wait_until_returns_first_matching_value_before_timeout() {
        AtomicInteger attempts = new AtomicInteger();

        Integer value = WaitUtils.waitUntil(
                attempts::incrementAndGet,
                current -> current >= 3,
                Duration.ofMillis(50),
                Duration.ofMillis(1)
        );

        Assert.assertEquals(value, Integer.valueOf(3));
        Assert.assertEquals(attempts.get(), 3);
    }

    @Test
    public void wait_until_returns_last_observed_value_when_timeout_expires() {
        AtomicInteger attempts = new AtomicInteger();

        Integer value = WaitUtils.waitUntil(
                attempts::incrementAndGet,
                current -> false,
                Duration.ofMillis(20),
                Duration.ofMillis(1)
        );

        Assert.assertTrue(attempts.get() >= 1, "Expected waitUntil to evaluate the supplier at least once");
        Assert.assertEquals(value, Integer.valueOf(attempts.get()));
    }

    @Test
    public void wait_for_input_value_retries_until_predicate_matches() {
        SequencedValueUiEngine engine = new SequencedValueUiEngine("", "ready@example.com");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        WaitUtils.waitForInputValue(
                "#email",
                "email field",
                value -> value.contains("@"),
                Duration.ofMillis(600)
        );

        Assert.assertEquals(engine.getValueCalls, 2);
    }

    @Test
    public void wait_for_condition_returns_when_boolean_supplier_eventually_matches() {
        AtomicInteger attempts = new AtomicInteger();

        WaitUtils.waitForCondition(
                "counter reaches two",
                () -> attempts.incrementAndGet() >= 2,
                "Expected counter to reach two"
        );

        Assert.assertEquals(attempts.get(), 2);
    }

    @Test
    public void wait_for_condition_throws_assertion_error_with_custom_message_when_condition_never_matches() {
        AssertionError error = Assert.expectThrows(
                AssertionError.class,
                () -> WaitUtils.waitForCondition(
                        "condition never matches",
                        () -> false,
                        "Expected condition to match",
                        Duration.ofSeconds(1)
                )
        );

        Assert.assertEquals(error.getMessage(), "Expected condition to match");
    }

    @Test
    public void wait_helpers_read_current_runtime_timeout_from_config() {
        TimeoutRecordingUiEngine engine = new TimeoutRecordingUiEngine();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .clearSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY)
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "1")) {

            WaitUtils.waitForVisible("#ready");
            Assert.assertEquals(engine.lastVisibleTimeoutSeconds, 1);

            ConfigManager.set(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "2");
            WaitUtils.waitForUrlContains("/dashboard");
            Assert.assertEquals(engine.lastUrlTimeoutSeconds, 2);
        }
    }

    @Test
    public void pause_preserves_interrupted_status() {
        try {
            Thread.currentThread().interrupt();
            WaitUtils.pause(Duration.ofMillis(1));
            Assert.assertTrue(Thread.currentThread().isInterrupted(),
                    "UI wait pause must preserve interrupted status through the shared sleeper policy.");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void wait_for_input_value_throws_assertion_error_when_predicate_never_matches() {
        SequencedValueUiEngine engine = new SequencedValueUiEngine("", "");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        AssertionError error = Assert.expectThrows(
                AssertionError.class,
                () -> WaitUtils.waitForInputValue(
                        "#email",
                        "email field",
                        value -> value.contains("@"),
                        Duration.ofMillis(10)
                )
        );

        Assert.assertTrue(
                error.getMessage().contains("Timed out waiting for input value: email field"),
                "Expected failure message to mention the timed out field"
        );
        Assert.assertTrue(engine.getValueCalls >= 1, "Expected at least one value read before timing out");
    }

    private static final class SequencedValueUiEngine extends StubUiEngine {
        private final Deque<String> values = new ArrayDeque<>();
        private String lastValue = "";
        private int getValueCalls;

        private SequencedValueUiEngine(String... values) {
            for (String value : values) {
                this.values.addLast(value);
                this.lastValue = value;
            }
        }

        @Override
        public String getValue(String cssSelector) {
            getValueCalls++;
            if (values.size() > 1) {
                lastValue = values.removeFirst();
                return lastValue;
            }
            if (!values.isEmpty()) {
                lastValue = values.peekFirst();
            }
            return lastValue;
        }

    }

    private static final class TimeoutRecordingUiEngine extends StubUiEngine {
        private int lastVisibleTimeoutSeconds;
        private int lastUrlTimeoutSeconds;

        @Override
        public void waitForVisible(String cssSelector, int seconds) {
            lastVisibleTimeoutSeconds = seconds;
        }

        @Override
        public void waitForUrlContains(String fragment, int seconds) {
            lastUrlTimeoutSeconds = seconds;
        }
    }
}
