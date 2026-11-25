package com.ai.core.reporting.listener;

import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.spi.ReportingLifecycleListener;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

/** Stable TestNG entry point that delegates to the optional reporting module. */
public final class TestListener implements ReportingLifecycleListener {

    private final ReportingLifecycleListener delegate = ReportingManager.createTestListener();

    @Override
    public void onStart(ISuite suite) {
        delegate.onStart(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        delegate.onFinish(suite);
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
        delegate.beforeInvocation(method, result);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        delegate.afterInvocation(method, result);
    }

    @Override
    public void onTestStart(ITestResult result) {
        delegate.onTestStart(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        delegate.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        delegate.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        delegate.onTestSkipped(result);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        delegate.onConfigurationFailure(result);
    }

    public static void resetForTests() {
        ReportingManager.reset();
        StepLogger.clear();
        ReportingManager.resetListenerStateForTests();
    }
}
