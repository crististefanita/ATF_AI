package com.endava.ai.core.reporting.listener.internal;

import com.endava.ai.core.reporting.ReportLogger;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.TestStatus;
import lombok.RequiredArgsConstructor;
import org.testng.ITestResult;

@RequiredArgsConstructor
public final class TestLifecycleController {

    private final TestContext context;

    public void startTest(ITestResult result) {
        ReportLogger logger = ReportingManager.tryGetLogger();

        context.markFirstTestConsumed();
        context.setLastTest(result);
        context.markTestStarted();

        if (logger == null) return;

        resetPreviousTestIfNeeded(logger);
        ensureTestStarted(result, logger);
        StepLogger.setDelegate(logger);
    }

    public void failTest(ITestResult result) {
        ReportLogger logger = ReportingManager.tryGetLogger();

        if (logger == null) {
            context.markTestEnded();
            return;
        }

        ensureTestContext(result, logger);
        reportFailure(result, logger);

        logger.endTest(TestStatus.FAIL);
        context.markTestEnded();
    }

    public void endSuite() {
        ReportLogger logger = ReportingManager.tryGetLogger();

        if (logger != null && context.getLastTest() != null) {
            if (context.isTestStarted()) {
                logger.endTest(StepLogger.testHasFailed() ? TestStatus.FAIL : TestStatus.PASS);
            }
            logger.flush();
        }

        StepLogger.clear();
        context.clear();
    }

    private void resetPreviousTestIfNeeded(ReportLogger logger) {
        if (!context.isFirstTest() && context.isTestStarted()) {
            logger.endTest(TestStatus.RESET);
        }
        context.markFirstTestConsumed();
    }

    private void ensureTestContext(ITestResult result, ReportLogger logger) {
        if (context.hasTestStarted()) return;

        context.markTestStarted();
        ensureTestStarted(result, logger);
        StepLogger.setDelegate(logger);
    }

    private void ensureTestStarted(ITestResult result, ReportLogger logger) {
        Class<?> realClass = result.getMethod().getRealClass();
        logger.ensureTestStarted(
                realClass == null ? "" : realClass.getName(),
                TestDisplayName.from(result),
                result.getMethod().getDescription()
        );
    }

    private static void reportFailure(ITestResult result, ReportLogger logger) {
        if (StepLogger.testHasFailed()) return;

        logger.fail(
                "Test failed",
                result.getThrowable() != null
                        ? result.getThrowable().toString()
                        : "null"
        );
    }
}
