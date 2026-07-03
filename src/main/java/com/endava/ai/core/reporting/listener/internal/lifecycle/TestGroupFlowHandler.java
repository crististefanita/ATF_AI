package com.endava.ai.core.reporting.listener.internal.lifecycle;

import com.endava.ai.core.reporting.listener.internal.FlowPolicy;
import com.endava.ai.core.reporting.listener.internal.GroupController;
import com.endava.ai.core.reporting.listener.internal.LifecycleFlusher;
import com.endava.ai.core.reporting.listener.internal.TestExecutionState;
import com.endava.ai.core.reporting.ReportLogger;
import lombok.RequiredArgsConstructor;
import org.testng.ITestResult;

@RequiredArgsConstructor
public final class TestGroupFlowHandler {

    private final FlowPolicy flowPolicy;
    private final LifecycleFlusher flusher;

    public void start(
            ReportLogger logger,
            Class<?> cls,
            String method,
            boolean firstForClass,
            TestExecutionState state
    ) {
        GroupController group = new GroupController(logger);

        if (state.isFlowMode()) {
            startFlowMethod(group, logger, cls, method, state);
            return;
        }

        flusher.flushBefore(logger, group, cls, firstForClass);

        group.open("TEST BODY");
        logTestBodyInvocation(logger, cls, method);
        state.openTest();
    }

    public void close(ITestResult result, ReportLogger logger, TestExecutionState state) {
        GroupController group = new GroupController(logger);
        Class<?> cls = result.getMethod().getRealClass();

        closeOpenTest(group, state);

        if (state.isFlowMode()) {
            closeFlowIfComplete(result, logger, group, cls, state);
            return;
        }

        closeMethodGroupIfOpen(group, state);
        closeAfterScopesIfNeeded(logger, group, cls, state);
    }

    private void startFlowMethod(
            GroupController group,
            ReportLogger logger,
            Class<?> cls,
            String method,
            TestExecutionState state
    ) {
        if (!state.isMethodGroupOpen()) {
            group.open("FLOW");
            state.openMethodGroup();

            flusher.flushBefore(logger, group, cls, true);

            group.open("TEST BODY");
        }

        group.open(method);
        logTestBodyInvocation(logger, cls, method);
        state.openTest();
    }

    private static void logTestBodyInvocation(ReportLogger logger, Class<?> cls, String method) {
        logger.logDetail("Test body invocation: " + className(cls) + "#" + method);
    }

    private static String className(Class<?> cls) {
        return cls == null ? "<unknown-test-class>" : cls.getName();
    }

    private void closeOpenTest(GroupController group, TestExecutionState state) {
        if (state.isTestOpen()) {
            group.closeCurrent();
            state.closeTest();
        }
    }

    private void closeFlowIfComplete(
            ITestResult result,
            ReportLogger logger,
            GroupController group,
            Class<?> cls,
            TestExecutionState state
    ) {
        if (!flowPolicy.isLastInFlow(result)) {
            return;
        }

        group.closeCurrent();
        group.closeCurrent();
        state.closeMethodGroup();

        state.disableFlowMode();

        if (flusher.hasAfterScopesContent(cls)) {
            group.open("TEARDOWN");
            state.openAfter();

            flusher.flushAfterScopes(logger, group, cls);

            group.closeCurrent();
            state.closeAfter();
        }
    }

    private static void closeMethodGroupIfOpen(GroupController group, TestExecutionState state) {
        if (state.isMethodGroupOpen()) {
            group.closeCurrent();
            state.closeMethodGroup();
        }
    }

    private void closeAfterScopesIfNeeded(
            ReportLogger logger,
            GroupController group,
            Class<?> cls,
            TestExecutionState state
    ) {
        if (!state.isAfterOpen() && !flusher.hasAfterScopesContent(cls)) {
            return;
        }

        if (!state.isAfterOpen()) {
            group.open("TEARDOWN");
            state.openAfter();
        }

        flusher.flushAfterScopes(logger, group, cls);

        group.closeCurrent();
        state.closeAfter();
    }
}
