package com.ai.core.reporting.step;

public final class StepExecutionContext {
    private final ThreadLocal<Integer> stepDepth = ThreadLocal.withInitial(() -> 0);
    private final ThreadLocal<Boolean> testFailed = new ThreadLocal<>();
    private final ThreadLocal<Boolean> expectedFailureLogging = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Boolean> consoleMuted = ThreadLocal.withInitial(() -> false);

    public StepExecutionContext() {
    }

    public void clear() {
        stepDepth.remove();
        testFailed.remove();
        expectedFailureLogging.remove();
        consoleMuted.remove();
    }

    public boolean testHasFailed() {
        return Boolean.TRUE.equals(testFailed.get());
    }

    public void markFailed() {
        testFailed.set(Boolean.TRUE);
    }

    public boolean expectedFailureLogging() {
        return Boolean.TRUE.equals(expectedFailureLogging.get());
    }

    public void withExpectedFailureLogging(Runnable action) {
        boolean previous = expectedFailureLogging();
        expectedFailureLogging.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            expectedFailureLogging.set(previous);
        }
    }

    public boolean consoleMuted() {
        return Boolean.TRUE.equals(consoleMuted.get());
    }

    public void withConsoleMuted(Runnable action) {
        boolean previous = consoleMuted();
        consoleMuted.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            consoleMuted.set(previous);
        }
    }

    public void openStep() {
        stepDepth.set(stepDepth.get() + 1);
    }

    public void closeStep() {
        requireActiveStep();
        stepDepth.set(stepDepth.get() - 1);
    }

    public void requireActiveStep() {
        if (stepDepth.get() <= 0) {
            throw new IllegalStateException("No active step.");
        }
    }
}
