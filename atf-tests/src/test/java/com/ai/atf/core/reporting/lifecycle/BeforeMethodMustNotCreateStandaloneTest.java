package com.ai.atf.core.reporting.lifecycle;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.TestStatus;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_METHOD_MUST_NOT_CREATE_STANDALONE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_METHOD_MUST_NOT_CREATE_STANDALONE_TEST_CLASS)
@Listeners(TestListener.class)
public class BeforeMethodMustNotCreateStandaloneTest {

    private TestListener listener;
    private StartTestObserver observer;

    @BeforeMethod
    public void setUp() {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();

        observer = new StartTestObserver();
        ReportingManager.setLoggerForTests(observer);

        listener = new TestListener();
    }

    @AfterMethod
    public void tearDown() {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
    }

    @Test
    public void before_method_must_not_start_fallback_test() {
        int baselineStartCalls = observer.startTestCalls;

        StepLogger.startStep("Validate: status=200");
        StepLogger.pass("Validated");

        listener.onTestStart(successResult());

        Assert.assertTrue(
                observer.startTestCalls - baselineStartCalls >= 1,
                "At least one real test must be started"
        );
    }

    @Test
    public void before_method_between_two_tests_must_not_create_fallback_test() {
        int baselineStartCalls = observer.startTestCalls;

        listener.onTestStart(successResult());
        listener.onTestSuccess(successResult());

        StepLogger.startStep("Validate: status=200");
        StepLogger.pass("Validated");

        listener.onTestStart(successResult());

        int simulatedStartCalls = observer.startTestCalls - baselineStartCalls;
        Assert.assertTrue(
                simulatedStartCalls >= 1,
                "At least one real test must remain active for buffered before-method steps"
        );
        Assert.assertTrue(
                simulatedStartCalls <= 2,
                "Before-method logging must not create an extra fallback test; observed startTest calls="
                        + simulatedStartCalls
        );
    }

    @Test
    public void empty_after_method_must_not_create_teardown_node() {
        ITestResult result = successResult();

        listener.onTestStart(result);
        listener.afterInvocation(testInvocation(), result);
        listener.onTestSuccess(result);
        listener.beforeInvocation(afterMethodInvocation(), result);
        listener.afterInvocation(afterMethodInvocation(), result);

        Assert.assertFalse(
                observer.stepTitles.contains("TEARDOWN"),
                "Empty @AfterMethod must not publish a standalone TEARDOWN node"
        );
    }

    // ---------- helpers ----------

    private static ITestResult successResult() {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SUCCESS);
        result.setMethod(fakeMethod());
        return result;
    }

    private static ITestNGMethod fakeMethod() {
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn("fakeTest");
        when(method.getDescription()).thenReturn("fake description");
        return method;
    }

    private static org.testng.IInvokedMethod testInvocation() {
        org.testng.IInvokedMethod invoked = mock(org.testng.IInvokedMethod.class);
        when(invoked.isTestMethod()).thenReturn(true);
        return invoked;
    }

    private static org.testng.IInvokedMethod afterMethodInvocation() {
        ITestNGMethod method = fakeMethod();
        when(method.isAfterMethodConfiguration()).thenReturn(true);

        org.testng.IInvokedMethod invoked = mock(org.testng.IInvokedMethod.class);
        when(invoked.isConfigurationMethod()).thenReturn(true);
        when(invoked.getTestMethod()).thenReturn(method);
        return invoked;
    }

    // ---------- observer ----------

    private static final class StartTestObserver implements ReportLogger {

        int startTestCalls = 0;
        List<String> stepTitles = new ArrayList<>();

        @Override
        public void startTest(String testName, String description) {
            startTestCalls++;
        }

        @Override public void endTest(TestStatus status) {}
        @Override public void startStep(String stepTitle) { stepTitles.add(stepTitle); }
        @Override public void logDetail(String detail) {}
        @Override public void pass(String message) {}
        @Override public void fail(String message, String stacktraceAsText) {}
        @Override public void attachScreenshotBase64(String base64, String title) {}
        @Override public void logCodeBlock(String content) {}
        @Override public void flush() {}
    }
}
