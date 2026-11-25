package com.ai.core.reporting.listener.internal.lifecycle;

import com.ai.core.reporting.listener.internal.TestContext;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.TestStatus;
import lombok.RequiredArgsConstructor;
import org.testng.ITestResult;

@RequiredArgsConstructor
public final class ConfigurationFailureHandler {

    private final TestContext context;

    public void onConfigurationFailure(ITestResult result) {
        ReportLogger logger = ReportingManager.tryGetLogger();
        if (logger == null || result == null || result.getMethod() == null) {
            return;
        }

        Class<?> realClass = result.getMethod().getRealClass();
        String className = realClass == null ? "" : realClass.getName();
        String methodName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        logger.ensureTestStarted(className, methodName, "Configuration failure");
        StepLogger.setDelegate(logger);
        StepLogger.startStep(configurationFailureStepName(methodName));
        StepLogger.fail(
                "Configuration failed: " + methodName,
                throwable != null ? throwable : new AssertionError("Configuration failed without throwable")
        );
        logger.endTest(TestStatus.FAIL);
        StepLogger.clearDelegate();
        context.markTestEnded();
        ExtentRuntimeAuditRegistry.record(result, "FAILED");
    }

    private static String configurationFailureStepName(String methodName) {
        return "CONFIGURATION FAILURE: " + (methodName == null || methodName.isBlank() ? "<unknown>" : methodName);
    }
}
