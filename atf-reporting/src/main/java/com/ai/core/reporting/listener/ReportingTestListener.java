package com.ai.core.reporting.listener;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.listener.internal.FlowPolicy;
import com.ai.core.reporting.listener.internal.LifecycleFlusher;
import com.ai.core.reporting.listener.internal.step.StepBufferRegistry;
import com.ai.core.reporting.listener.internal.TestContext;
import com.ai.core.reporting.listener.internal.TestExecutionState;
import com.ai.core.reporting.listener.internal.TestLifecycleController;
import com.ai.core.reporting.listener.internal.lifecycle.AtfContractNarrationHandler;
import com.ai.core.reporting.listener.internal.lifecycle.ConfigurationFailureHandler;
import com.ai.core.reporting.listener.internal.lifecycle.LifecycleCommand;
import com.ai.core.reporting.listener.internal.lifecycle.LifecycleCommandRunner;
import com.ai.core.reporting.listener.internal.lifecycle.TestGroupFlowHandler;
import com.ai.core.reporting.listener.internal.lifecycle.TestInvocationSession;
import com.ai.core.reporting.listener.internal.lifecycle.TestResultOutcomeHandler;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.spi.ReportingLifecycleListener;
import com.ai.core.reporting.attachment.FinalStateAttachmentRegistry;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.ai.core.reporting.internal.ReportingEngineCleanup;
import com.ai.core.reporting.internal.ReportingPaths;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestResult;

import java.util.List;

public final class ReportingTestListener implements ReportingLifecycleListener {

    private final TestContext context = new TestContext();
    private final StepBufferRegistry buffers = new StepBufferRegistry();
    private final TestLifecycleController lifecycle = new TestLifecycleController(context);
    private final FlowPolicy flowPolicy = new FlowPolicy();
    private final LifecycleFlusher flusher = new LifecycleFlusher(buffers);
    private final TestInvocationSession session = new TestInvocationSession(buffers, flusher);
    private final ConfigurationFailureHandler configurationFailures =
            new ConfigurationFailureHandler(context);
    private final AtfContractNarrationHandler contractNarration = new AtfContractNarrationHandler();
    private final TestGroupFlowHandler groupFlow = new TestGroupFlowHandler(flowPolicy, flusher);

    private final TestResultOutcomeHandler outcomes =
            new TestResultOutcomeHandler(lifecycle, groupFlow, session::state);

    static {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(ReportingTestListener::shutdownQuietly));
    }

    @Override
    public void onStart(ISuite suite) {
        LifecycleCommandRunner.executeAll(List.of(
                LifecycleCommand.named("allureResults.ensureDirectory", ReportingPaths::ensureAllureResultsDirectory),
                LifecycleCommand.named("reporting.beginSuite", ReportingManager::beginSuite),
                LifecycleCommand.named("context.clear", context::clear),
                LifecycleCommand.named("buffers.clear", buffers::clear),
                LifecycleCommand.named("stepLogger.clear", StepLogger::clear),
                LifecycleCommand.named("flowPolicy.clear", flowPolicy::clear),
                LifecycleCommand.named("session.clear", session::clear),
                LifecycleCommand.named("runtimeAudit.clear", ExtentRuntimeAuditRegistry::clear)
        ));
    }

    @Override
    public void onFinish(ISuite suite) {
        LifecycleCommandRunner.executeAll(List.of(
                LifecycleCommand.named("lifecycle.endSuite", lifecycle::endSuite),
                LifecycleCommand.named("runtimeAudit.publishIfExtent", ExtentRuntimeAuditRegistry::publishIfExtent),
                LifecycleCommand.named("reporting.endSuite", ReportingManager::endSuite),
                LifecycleCommand.named("session.clear", session::clear)
        ));
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult tr) {
        if (isReportingDisabled(method, tr)) return;
        ReportingPaths.ensureAllureResultsDirectory();
        session.invocationController().beforeInvocation(method);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult tr) {
        if (isReportingDisabled(method, tr)) return;
        if (method.isTestMethod() && tr != null && tr.getStatus() == ITestResult.SUCCESS) {
            FinalStateAttachmentRegistry.onTestSuccess();
        }
        if (method.isTestMethod() && tr != null) {
            contractNarration.afterTestInvocation(tr);
        }
        session.invocationController().afterInvocation(method, tr);
        ReportingPaths.ensureAllureResultsDirectory();
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (isReportingDisabled(result)) return;
        ReportingPaths.ensureAllureResultsDirectory();
        StepLogger.clear();
        FailureAttachmentRegistry.onTestStart();

        lifecycle.startTest(result);

        ReportLogger logger = ReportingManager.tryGetLogger();
        if (logger == null) return;

        // Canonical reporting delegate wiring for the active test invocation.
        StepLogger.setDelegate(logger);

        Class<?> cls = result.getMethod().getRealClass();
        String method = result.getMethod().getMethodName();

        TestExecutionState st = session.state();
        boolean firstForClass = context.isFirstMethodForClass(cls);

        if (firstForClass) {
            st.reset();
            if (flowPolicy.isFlowClass(result.getTestContext(), cls))
                st.enableFlowMode();
        } else if (!st.isFlowMode()) {
            st.reset();
        }

        groupFlow.start(logger, cls, method, firstForClass, st);
        contractNarration.beforeTestInvocation(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (isReportingDisabled(result)) {
            return;
        }
        outcomes.onSuccess(result);
        context.markTestEnded();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (isReportingDisabled(result)) {
            return;
        }
        outcomes.onFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (isReportingDisabled(result)) {
            return;
        }
        outcomes.onSkipped(result);
        context.markTestEnded();
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        if (isReportingDisabled(result)) return;
        configurationFailures.onConfigurationFailure(result);
    }

    public static void resetForTests() {
        ReportingManager.reset();
        StepLogger.clear();
        ExtentRuntimeAuditRegistry.clear();
    }

    private static void shutdownQuietly() {
        try {
            ExtentRuntimeAuditRegistry.publishIfExtent();
        } catch (Throwable shutdownPublishFailure) {
            // JVM shutdown can run after Surefire has already torn down the test classloader.
            // At this point diagnostics themselves may be unavailable, so shutdown must stay best-effort.
        }
        try {
            ReportingEngineCleanup.onShutdown();
        } catch (Throwable shutdownCleanupFailure) {
            // Same best-effort boundary as above: never turn shutdown cleanup into a provider failure.
        }
    }

    private static boolean isReportingDisabled(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return false;
        }
        Class<?> realClass = result.getMethod().getRealClass();
        return realClass != null && realClass.isAnnotationPresent(NoFrameworkReporting.class);
    }

    private static boolean isReportingDisabled(IInvokedMethod method, ITestResult result) {
        if (isReportingDisabled(result)) {
            return true;
        }
        if (method == null || method.getTestMethod() == null) {
            return false;
        }
        Class<?> realClass = method.getTestMethod().getRealClass();
        return realClass != null && realClass.isAnnotationPresent(NoFrameworkReporting.class);
    }

}
