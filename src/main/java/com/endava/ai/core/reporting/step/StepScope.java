package com.endava.ai.core.reporting.step;

import com.endava.ai.core.reporting.StepLogger;

public final class StepScope implements AutoCloseable {

    private boolean closed;

    private StepScope(String title) {
        StepLogger.startStep(title);
    }

    public static StepScope open(String title) {
        return new StepScope(title);
    }

    public void pass(String message) {
        if (closed) {
            return;
        }
        StepLogger.pass(message);
        closed = true;
    }

    public void fail(String message, Throwable failure) {
        if (closed) {
            return;
        }
        StepLogger.fail(message, failure);
        closed = true;
    }

    @Override
    public void close() {
        if (!closed) {
            StepLogger.logDetail("Step scope closed without explicit pass/fail");
            StepLogger.closeOpenStepForScope();
            closed = true;
        }
    }
}
