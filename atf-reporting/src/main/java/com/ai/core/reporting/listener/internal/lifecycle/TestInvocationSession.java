package com.ai.core.reporting.listener.internal.lifecycle;

import com.ai.core.reporting.listener.internal.InvocationController;
import com.ai.core.reporting.listener.internal.LifecycleFlusher;
import com.ai.core.reporting.listener.internal.step.StepBufferRegistry;
import com.ai.core.reporting.listener.internal.TestExecutionState;

public final class TestInvocationSession {

    private final StepBufferRegistry buffers;
    private final LifecycleFlusher flusher;
    private final ThreadLocal<TestExecutionState> state;
    private final ThreadLocal<InvocationController> invocations;

    public TestInvocationSession(StepBufferRegistry buffers, LifecycleFlusher flusher) {
        this.buffers = buffers;
        this.flusher = flusher;
        this.state = ThreadLocal.withInitial(TestExecutionState::new);
        this.invocations = ThreadLocal.withInitial(() ->
                new InvocationController(buffers, flusher, state.get())
        );
    }

    public TestExecutionState state() {
        return state.get();
    }

    public InvocationController invocationController() {
        return invocations.get();
    }

    public void clear() {
        state.remove();
        invocations.remove();
    }
}
