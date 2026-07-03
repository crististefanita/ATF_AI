package com.ai.core.reporting.listener.internal.lifecycle;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.listener.internal.TestExecutionState;
import com.ai.core.reporting.listener.internal.TestLifecycleController;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import org.testng.ITestResult;

import java.util.function.Supplier;

public final class TestResultOutcomeHandler {

    private final TestLifecycleController lifecycle;
    private final TestGroupFlowHandler groupFlow;
    private final Supplier<TestExecutionState> state;

    public TestResultOutcomeHandler(
            TestLifecycleController lifecycle,
            TestGroupFlowHandler groupFlow,
            Supplier<TestExecutionState> state
    ) {
        this.lifecycle = lifecycle;
        this.groupFlow = groupFlow;
        this.state = state;
    }

    public void onSuccess(ITestResult result) {
        closeGroups(result);
        endCurrentTest(TestStatus.PASS);
        StepLogger.clearDelegate();
        ExtentRuntimeAuditRegistry.record(result, "PASSED");
    }

    public void onFailure(ITestResult result) {
        boolean expectedFailureSimulation = StepLogger.isExpectedFailureLogging();
        endWithThrowable(result, true, expectedFailureSimulation);
        if (!expectedFailureSimulation) {
            ExtentRuntimeAuditRegistry.record(result, "FAILED");
        }
    }

    public void onSkipped(ITestResult result) {
        closeGroups(result);
        StepLogger.clearDelegate();
        endCurrentTest(TestStatus.SKIP);
        ExtentRuntimeAuditRegistry.record(result, "SKIPPED");
    }

    private void endWithThrowable(ITestResult result, boolean failed, boolean expectedFailureSimulation) {
        Throwable throwable = result.getThrowable();

        FailureAttachmentRegistry.onTestFailure(throwable);
        if (failed && !expectedFailureSimulation) {
            lifecycle.failTest(result);
        }

        if (throwable != null) {
            StepLogger.failUnhandledOutsideStep(throwable);
        }

        closeGroups(result);
        StepLogger.clearDelegate();
    }

    private static void endCurrentTest(TestStatus status) {
        ReportLogger logger = ReportingManager.tryGetLogger();
        if (logger != null) {
            logger.endTest(status);
        }
    }

    private void closeGroups(ITestResult result) {
        ReportLogger logger = ReportingManager.tryGetLogger();
        if (logger == null) {
            return;
        }
        groupFlow.close(result, logger, state.get());
    }
}
