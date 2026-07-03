package com.endava.ai.atf.core.reporting.regression;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.endava.ai.core.reporting.ReportLogger;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.step.StepScope;
import com.endava.ai.core.reporting.utils.ConsoleLogger;
import com.endava.ai.core.reporting.utils.StacktraceFormatter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.STEP_LOGGER_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.STEP_LOGGER_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class StepLoggerContractTest {

    @BeforeMethod
    public void setUp() {
        StepLogger.clear();
    }

    @AfterMethod
    public void tearDown() {
        StepLogger.clear();
        ConsoleLogger.resetSinkForTests();
    }

    @Test
    public void happy_path_single_step_does_not_throw() {
        StepLogger.startStep("A");
        StepLogger.logDetail("x");
        StepLogger.pass("ok");

        // contract: no exception
        Assert.assertTrue(true);
    }

    @Test
    public void nested_steps_are_allowed() {
        StepLogger.startStep("A");
        StepLogger.startStep("B");

        StepLogger.pass("ok B");
        StepLogger.pass("ok A");

        // contract: nesting allowed, no exception
        Assert.assertTrue(true);
    }

    @Test
    public void step_logger_publishes_formatted_console_messages() {
        List<String> console = new ArrayList<>();
        ConsoleLogger.setSinkForTests(console::add);

        StepLogger.startStep("console proof");
        StepLogger.pass("visible pass");

        Assert.assertEquals(console, List.of(
                "\u25B6 console proof",
                "  \u2705 PASS: visible pass"
        ), "StepLogger must preserve the canonical ATF console format.");
    }

    @Test
    public void muted_console_suppresses_infrastructure_steps_without_breaking_lifecycle() {
        List<String> console = new ArrayList<>();
        ConsoleLogger.setSinkForTests(console::add);

        StepLogger.withConsoleMuted(() -> {
            StepLogger.startStep("infrastructure proof");
            StepLogger.pass("hidden pass");
        });
        StepLogger.startStep("business proof");
        StepLogger.pass("visible pass");

        Assert.assertEquals(console, List.of(
                "\u25B6 business proof",
                "  \u2705 PASS: visible pass"
        ), "Infrastructure steps may stay in reports without polluting the baseline console output.");
    }

    @Test
    public void step_scope_recovers_depth_when_code_throws_before_pass_or_fail() {
        List<String> details = new ArrayList<>();
        StepLogger.setDelegate(new RecordingLogger(details));

        try {
            try (StepScope ignored = StepLogger.step("scoped step")) {
                StepLogger.logDetail("before boom");
                throw new IllegalStateException("boom");
            }
        } catch (IllegalStateException expected) {
            Assert.assertEquals(expected.getMessage(), "boom");
        }

        Assert.assertEquals(
                details,
                List.of("before boom", "Step scope closed without explicit pass/fail"),
                "An implicitly closed StepScope must leave an auditable report detail before depth cleanup."
        );

        IllegalStateException noActiveStep = Assert.expectThrows(
                IllegalStateException.class,
                () -> StepLogger.logDetail("must not leak into previous scope")
        );
        Assert.assertEquals(noActiveStep.getMessage(), "No active step.");
    }

    @Test
    public void unhandled_exception_outside_step_is_idempotent() {
        StepLogger.withExpectedFailureLogging(() -> {
            StepLogger.failUnhandledOutsideStep(new RuntimeException("boom"));
            StepLogger.failUnhandledOutsideStep(new RuntimeException("boom again"));
        });

        Assert.assertFalse(
                StepLogger.testHasFailed(),
                "Controlled expected-failure logging must not turn a passing StepLogger contract into a real Extent failure."
        );
    }

    @Test
    public void stacktrace_formatter_preserves_failure_chain_and_root_detail() {
        RuntimeException failure = new RuntimeException(
                "outer failure",
                new IllegalStateException("inner root\nhidden second line")
        );

        String formatted = StacktraceFormatter.format(failure);

        Assert.assertTrue(formatted.contains("Failure chain:"),
                "Formatted failure must keep the chain header.");
        Assert.assertTrue(formatted.contains("- RuntimeException: outer failure"),
                "Formatted failure must include the outer failure.");
        Assert.assertTrue(formatted.contains("caused by: IllegalStateException: inner root"),
                "Formatted failure must include the root cause first line.");
        Assert.assertTrue(formatted.contains("Root cause detail:"),
                "Formatted failure must keep the root cause section.");
        Assert.assertTrue(formatted.contains("hidden second line"),
                "Root cause detail must keep the full exception message for debugging.");
    }

    @Test
    public void clearRemovesDelegateAndOpenStepDepthBetweenRepeatedInvocations() {
        List<String> firstRunDetails = new ArrayList<>();
        StepLogger.setDelegate(new RecordingLogger(firstRunDetails));
        StepLogger.startStep("leaky step");
        StepLogger.logDetail("first run detail");

        StepLogger.clear();

        IllegalStateException noActiveStep = Assert.expectThrows(
                IllegalStateException.class,
                () -> StepLogger.logDetail("second run must not inherit first run depth")
        );
        Assert.assertEquals(noActiveStep.getMessage(), "No active step.");

        StepLogger.startStep("second run");
        StepLogger.logDetail("delegate must be gone");
        StepLogger.pass("done");

        Assert.assertEquals(
                firstRunDetails,
                List.of("first run detail"),
                "StepLogger.clear() must remove delegate state before the next invocation."
        );
    }

    @Test
    public void parallelThreadsDoNotShareDelegatesOrStepDepth() throws Exception {
        List<String> mainDetails = new ArrayList<>();
        List<String> workerDetails = new ArrayList<>();
        AtomicReference<Throwable> workerFailure = new AtomicReference<>();
        CountDownLatch workerReady = new CountDownLatch(1);
        CountDownLatch releaseWorker = new CountDownLatch(1);

        StepLogger.setDelegate(new RecordingLogger(mainDetails));
        StepLogger.startStep("main step");
        StepLogger.logDetail("main-before-worker");

        Thread worker = new Thread(() -> {
            try {
                StepLogger.setDelegate(new RecordingLogger(workerDetails));
                StepLogger.startStep("worker step");
                StepLogger.logDetail("worker-only");
                workerReady.countDown();
                releaseWorker.await();
                StepLogger.pass("worker done");
            } catch (Throwable failure) {
                workerFailure.set(failure);
            } finally {
                StepLogger.clear();
            }
        }, "step-logger-parallel-contract");

        worker.start();
        workerReady.await();
        StepLogger.logDetail("main-after-worker");
        StepLogger.pass("main done");
        releaseWorker.countDown();
        worker.join();

        Assert.assertNull(workerFailure.get(), "Worker step logging must not fail.");
        Assert.assertEquals(mainDetails, List.of("main-before-worker", "main-after-worker"));
        Assert.assertEquals(workerDetails, List.of("worker-only"));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class RecordingLogger implements ReportLogger {
        private final List<String> details;

        @Override
        public void startTest(String testName, String description) {
        }

        @Override
        public void endTest(TestStatus status) {
        }

        @Override
        public void startStep(String stepTitle) {
        }

        @Override
        public void logDetail(String detail) {
            details.add(detail);
        }

        @Override
        public void pass(String message) {
        }

        @Override
        public void fail(String message, String stacktraceAsText) {
        }

        @Override
        public void attachScreenshotBase64(String base64, String title) {
        }

        @Override
        public void logCodeBlock(String content) {
        }

        @Override
        public void flush() {
        }
    }
}
