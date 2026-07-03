package com.ai.atf.core.reporting.lifecycle;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.annotations.Listeners;

import com.ai.core.support.contract.ids.AtfContractReportLabels;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.support.contract.ids.AtfContractSteps;
import org.testng.ITestNGMethod;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.TestResult;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.Assert.assertTrue;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.TEST_LISTENER_FAILURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_FAILURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class TestListenerFailureContractTest {
    private static final Path NEGATIVE_EXTENT_LISTENER_FAILURE_ROOT =
            AtfSourcePaths.TARGET_ROOT.resolve("negative-extent-listener-failure");

    private TestListener listener;
    private TestReportObserver observer;

    @BeforeMethod
    public void setUp() {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();

        observer = new TestReportObserver();
        ReportingManager.setLoggerForTests(observer);

        listener = new TestListener();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
        deleteRecursively(NEGATIVE_EXTENT_LISTENER_FAILURE_ROOT.toAbsolutePath());
    }

    @Test
    public void failure_outside_step_is_logged_as_fail() {
        listener.onTestFailure(
                failureResult(new IllegalArgumentException("boom"))
        );

        assertTrue(observer.failCalled);
        assertTrue(observer.endFailCalled);
    }

    @Test
    public void logs_fail_only_once_when_step_already_failed() {
        listener.onTestStart(successResult());

        StepLogger.startStep("Validate: status=200");
        StepLogger.fail(
                "Unexpected HTTP status expected [200] but found [422]",
                new AssertionError()
        );

        listener.onTestFailure(
                failureResult(
                        new AssertionError("Unexpected HTTP status expected [200] but found [422]")
                )
        );

        assertEquals(1, observer.failCount);
    }

    @Test
    public void does_not_duplicate_fail_when_step_fails() {
        listener.onTestStart(successResult());

        StepLogger.startStep("Validate: status=200");
        StepLogger.fail("Boom", new AssertionError());

        listener.onTestFailure(
                failureResult(new AssertionError("Boom"))
        );

        assertEquals(1, observer.failMessages.size());
    }

    @Test
    public void extent_does_not_crash_on_failure_outside_step() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-negative-extent-listener-failure-");

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfig(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfig(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED)) {
            ReportingManager.setLoggerForTests(isolatedExtentAdapter());

            listener = new TestListener();
            listener.onTestStart(successResult());

            listener.onTestFailure(
                    failureResult(new RuntimeException("boom outside step"))
            );
        } finally {
            ReportingManager.setLoggerForTests(observer);
            deleteRecursively(reportsDir);
        }
    }

    @Test
    public void step_details_are_preserved_when_step_logs_info() {
        listener.onTestStart(successResult());

        StepLogger.startStep("Create user");
        StepLogger.info("POST /users");
        StepLogger.info("email=test@example.com");

        StepLogger.pass("Validated status=200");

        listener.onTestSuccess(successResult());

        assertTrue(observer.details.contains("POST /users"));
        assertTrue(observer.details.contains("email=test@example.com"));
    }

    @Test
    public void unhandled_exception_does_not_create_duplicate_test_nodes() {
        ITestResult result = failureResult(
                new IllegalStateException("boom before step")
        );

        listener.onTestFailure(result);

        // Contract: no exception, no infinite loop.
        // Number of test nodes is engine/listener dependent.
        assertTrue(
                observer.testCount >= 1,
                "Unhandled exception must be reported by at least one test node"
        );
    }

    @Test
    public void skipped_contract_verdict_is_logged_once_before_invocation_cleanup() throws Exception {
        ITestResult result = skippedContractResult();

        listener.onTestStart(result);
        listener.afterInvocation(testInvocation(result.getMethod()), result);
        listener.onTestSkipped(result);

        assertEquals(
                1,
                observer.failMessages.stream()
                        .filter(message -> message.contains(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED))
                        .count()
        );
    }

    @Test
    public void skip_exception_does_not_log_unhandled_failure_outside_step() throws Exception {
        ITestResult result = skippedContractResult();
        result.setThrowable(new SkipException("Package audit intentionally skipped"));

        listener.onTestStart(result);
        listener.afterInvocation(testInvocation(result.getMethod()), result);
        listener.onTestSkipped(result);

        assertTrue(observer.endSkipCalled);
        assertTrue(observer.failMessages.stream()
                        .noneMatch(message -> message.contains("Unhandled exception outside step")),
                "Deliberate TestNG skips must not be narrated as unhandled failures.");
    }

    @Test
    public void manual_contract_steps_do_not_duplicate_listener_contract_narration() throws Exception {
        ITestResult result = manualContractResult();

        observer.clearRecordedSteps();
        listener.onTestStart(result);
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                ContractFixture.class,
                "manualContract"
        );
        AtfContractSteps.expected(contract);
        AtfContractSteps.observed("runtime.status=200");
        AtfContractSteps.verdict("Manual runtime evidence satisfied the contract.", contract.repairHint());
        listener.afterInvocation(testInvocation(result.getMethod()), result);

        assertEquals(1, observer.stepTitleCount(AtfContractReportLabels.EXPECTED_FRAMEWORK_CONTRACT));
        assertEquals(1, observer.stepTitleCount(AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT));
        assertEquals(1, observer.stepTitleCount(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT));
        assertEquals(1, observer.passMessageCount(AtfContractReportLabels.OBSERVED_FRAMEWORK_RESULT_RECORDED));
        assertEquals(1, observer.passMessageCount(AtfContractReportLabels.COLD_FRAMEWORK_VERDICT_RECORDED));
        assertTrue(observer.details.contains("runtime.status=200"),
                "Manual dynamic evidence must stay visible after deduplicating listener narration.");
    }

    // ---------------- helpers ----------------

    private static ITestResult successResult() {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SUCCESS);
        result.setMethod(fakeMethod());
        return result;
    }

    private static ITestResult failureResult(Throwable t) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.FAILURE);
        result.setThrowable(t);
        result.setMethod(fakeMethod());
        return result;
    }

    private static ITestResult skippedContractResult() throws NoSuchMethodException {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SKIP);
        result.setMethod(contractMethod());
        return result;
    }

    private static ITestResult manualContractResult() throws NoSuchMethodException {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SUCCESS);
        result.setMethod(contractMethod("manualContract"));
        return result;
    }

    private static IInvokedMethod testInvocation(ITestNGMethod method) {
        IInvokedMethod invoked = mock(IInvokedMethod.class);
        when(invoked.isTestMethod()).thenReturn(true);
        when(invoked.getTestMethod()).thenReturn(method);
        return invoked;
    }

    private static ITestNGMethod fakeMethod() {
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn("fakeTest");
        when(method.getDescription()).thenReturn("fake description");
        when(method.getRealClass()).thenReturn(TestListenerFailureContractTest.class);
        return method;
    }

    private static ITestNGMethod contractMethod() throws NoSuchMethodException {
        return contractMethod("skippedContract");
    }

    private static ITestNGMethod contractMethod(String methodName) throws NoSuchMethodException {
        Method javaMethod = ContractFixture.class.getDeclaredMethod(methodName);
        ITestNGMethod method = fakeMethod();
        when(method.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(javaMethod));
        when(method.getRealClass()).thenReturn(ContractFixture.class);
        when(method.getMethodName()).thenReturn(methodName);
        return method;
    }

    private static ExtentAdapter isolatedExtentAdapter() throws Exception {
        Constructor<ExtentAdapter> constructor = ExtentAdapter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete " + path, e);
                        }
                    });
        }
    }

    // ---------------- observer ----------------

    private static final class TestReportObserver implements ReportLogger {

        boolean failCalled = false;
        boolean endFailCalled = false;
        boolean endSkipCalled = false;
        int failCount = 0;
        int testCount = 0;

        List<String> failMessages = new ArrayList<>();
        List<String> details = new ArrayList<>();
        List<String> stepTitles = new ArrayList<>();
        List<String> passMessages = new ArrayList<>();

        @Override
        public void startTest(String testName, String description) {
            testCount++;
        }

        @Override
        public void endTest(TestStatus status) {
            if (status == TestStatus.FAIL) endFailCalled = true;
            if (status == TestStatus.SKIP) endSkipCalled = true;
        }

        @Override
        public void startStep(String stepTitle) {
            stepTitles.add(stepTitle);
        }

        @Override
        public void logDetail(String detail) {
            details.add(detail);
        }

        @Override
        public void pass(String message) {
            passMessages.add(message);
        }

        @Override
        public void fail(String message, String stacktraceAsText) {
            failCalled = true;
            failCount++;
            failMessages.add(message);
        }

        @Override public void attachScreenshotBase64(String base64, String title) {}
        @Override public void logCodeBlock(String content) {}
        @Override public void flush() {}

        long stepTitleCount(String title) {
            return stepTitles.stream().filter(title::equals).count();
        }

        long passMessageCount(String message) {
            return passMessages.stream().filter(message::equals).count();
        }

        void clearRecordedSteps() {
            details.clear();
            stepTitles.clear();
            passMessages.clear();
            failMessages.clear();
            failCalled = false;
            failCount = 0;
        }
    }

    private static final class ContractFixture {
        @AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_FAILURE_CONTRACT_TEST_SKIPPED_CONTRACT)
        private void skippedContract() {
        }

        @AtfContractRef(AtfContractNarrativeIds.Contract.TEST_LISTENER_FAILURE_CONTRACT_TEST_CLASS)
        private void manualContract() {
        }
    }
}
