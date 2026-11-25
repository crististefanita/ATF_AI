package com.ai.core.reporting.listener.internal;

import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.listener.internal.step.StepBufferLogger;
import com.ai.core.reporting.listener.internal.step.StepBufferRegistry;
import lombok.RequiredArgsConstructor;
import org.testng.IInvokedMethod;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

@RequiredArgsConstructor
public final class InvocationController {

    private final StepBufferRegistry buffers;
    private final LifecycleFlusher flusher;
    private final TestExecutionState state;

    public void beforeInvocation(IInvokedMethod method) {
        if (!method.isConfigurationMethod()) return;

        ITestNGMethod m = method.getTestMethod();
        if (m == null) return;

        StepLogger.setDelegate(resolveDelegate(m));
    }

    public void afterInvocation(IInvokedMethod method, ITestResult tr) {
        if (method.isTestMethod()) {
            afterTestMethod(tr);
            StepLogger.clearDelegate();
            return;
        }

        if (!method.isConfigurationMethod()) return;

        ITestNGMethod m = method.getTestMethod();

        // TEARDOWN for @AfterMethod must be logged HERE (not in afterTestMethod)
        if (m != null && m.isAfterMethodConfiguration()) {
            ReportLogger logger = ReportingManager.tryGetLogger();
            if (logger != null && !state.isFlowMode() && flusher.hasAfterMethodContent()) {
                GroupController g = new GroupController(logger);

                g.open("TEARDOWN");
                g.open("@AfterMethod");
                flusher.flushAfterMethod(logger);
                g.close(); // closes @AfterMethod node
                g.close(); // closes TEARDOWN node
                state.closeAfter();
            }
        }

        StepLogger.clearDelegate();
    }

    /**
     * Runs right after the @Test method returns.
     * Responsibility: close TEST BODY so that @AfterMethod cannot end up under it.
     */
    @SuppressWarnings("unused")
    private void afterTestMethod(ITestResult tr) {
        if (state.isFlowMode()) return;

        ReportLogger logger = ReportingManager.tryGetLogger();
        if (logger == null) return;

        if (state.isTestOpen()) {
            GroupController g = new GroupController(logger);
            g.closeCurrent(); // closes TEST BODY
            state.closeTest();
        }
    }

    private ReportLogger resolveDelegate(ITestNGMethod m) {
        ReportLogger real = ReportingManager.tryGetLogger();
        if (real == null) return null;

        if (m.isBeforeMethodConfiguration()) {
            StepBufferLogger b = buffers.beforeMethod();
            if (b != null) b.clear();
            return b;
        }
        if (m.isAfterMethodConfiguration()) {
            StepBufferLogger a = buffers.afterMethod();
            if (a != null) a.clear();
            return a;
        }
        if (m.isBeforeClassConfiguration() || m.isBeforeTestConfiguration() || m.isBeforeSuiteConfiguration()) {
            return buffers.beforeFor(m);
        }
        if (m.isAfterClassConfiguration() || m.isAfterTestConfiguration() || m.isAfterSuiteConfiguration()) {
            return buffers.afterFor(m);
        }

        return real;
    }
}
