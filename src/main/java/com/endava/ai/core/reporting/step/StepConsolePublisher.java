package com.endava.ai.core.reporting.step;

import com.endava.ai.core.reporting.utils.ConsoleLogger;

import java.util.Objects;

public final class StepConsolePublisher {
    private static final String CONSOLE_STEP_PREFIX = "\u25B6 ";
    private static final String CONSOLE_DETAIL_PREFIX = "  \u2022 ";
    private static final String CONSOLE_PASS_PREFIX = "  \u2705 PASS: ";
    private static final String CONSOLE_FAIL_PREFIX = "  \u274C FAIL: ";
    private static final String CONSOLE_EXPECTED_FAILURE_PREFIX = "  \u26A0 WARN / EXPECTED FAILURE: ";

    private final StepExecutionContext context;

    public StepConsolePublisher(StepExecutionContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public void step(String title) {
        publish(CONSOLE_STEP_PREFIX + title);
    }

    public void detail(String detail) {
        publish(CONSOLE_DETAIL_PREFIX + detail);
    }

    public void pass(String message) {
        publish(CONSOLE_PASS_PREFIX + message);
    }

    public void fail(String message) {
        publish(CONSOLE_FAIL_PREFIX + message);
    }

    public void expectedFailure(String message) {
        publish(CONSOLE_EXPECTED_FAILURE_PREFIX + message);
    }

    private void publish(String message) {
        if (!context.consoleMuted()) {
            ConsoleLogger.console(message);
        }
    }
}
