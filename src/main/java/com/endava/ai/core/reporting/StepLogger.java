package com.endava.ai.core.reporting;

import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.step.StepConsolePublisher;
import com.endava.ai.core.reporting.step.StepExecutionContext;
import com.endava.ai.core.reporting.step.StepReportDelegate;
import com.endava.ai.core.reporting.step.StepScope;

import static com.endava.ai.core.support.config.ConfigManager.require;
import static com.endava.ai.core.reporting.utils.StacktraceFormatter.format;

public final class StepLogger {

    private static final StepExecutionContext CONTEXT = new StepExecutionContext();
    private static final StepConsolePublisher CONSOLE = new StepConsolePublisher(CONTEXT);
    private static final StepReportDelegate REPORT = new StepReportDelegate();

    private StepLogger() {
    }

    public static void setDelegate(ReportLogger logger) {
        REPORT.set(logger);
    }

    public static void clearDelegate() {
        REPORT.clear();
    }

    public static void clear() {
        CONTEXT.clear();
        REPORT.clear();
    }

    public static boolean testHasFailed() {
        return CONTEXT.testHasFailed();
    }

    public static boolean isExpectedFailureLogging() {
        return CONTEXT.expectedFailureLogging();
    }

    public static void withExpectedFailureLogging(Runnable action) {
        CONTEXT.withExpectedFailureLogging(action);
    }

    public static void withConsoleMuted(Runnable action) {
        CONTEXT.withConsoleMuted(action);
    }

    public static void startStep(String title) {
        CONTEXT.openStep();
        CONSOLE.step(title);
        REPORT.startStep(title);
    }

    public static StepScope step(String title) {
        return StepScope.open(title);
    }

    public static void logDetail(String detail) {
        CONTEXT.requireActiveStep();
        REPORT.logDetail(detail);
        if (getBoolean(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY)) {
            CONSOLE.detail(detail);
        }
    }

    public static void info(String detail) {
        logDetail(detail);
    }

    public static void logCodeBlock(String content) {
        CONTEXT.requireActiveStep();
        REPORT.logCodeBlock(content);
    }

    public static void pass(String message) {
        CONTEXT.requireActiveStep();
        CONSOLE.pass(message);
        REPORT.pass(message);
        CONTEXT.closeStep();
    }

    public static void fail(String message, Throwable t) {
        fail(message, format(t));
    }

    public static void fail(String message, String stacktraceAsText) {
        CONTEXT.requireActiveStep();
        if (CONTEXT.expectedFailureLogging()) {
            CONSOLE.expectedFailure(message);
            REPORT.controlledExpectedFailure(message, stacktraceAsText);
            CONTEXT.closeStep();
            return;
        }

        CONTEXT.markFailed();
        CONSOLE.fail(message);
        REPORT.fail(message, stacktraceAsText);
        CONTEXT.closeStep();
    }

    public static void failUnhandledOutsideStep(Throwable t) {
        if (testHasFailed()) return;

        if (CONTEXT.expectedFailureLogging()) {
            REPORT.controlledExpectedFailureOutsideStep(t, format(t));
            return;
        }

        CONTEXT.markFailed();
        REPORT.fail("Unhandled exception outside step", format(t));
        CONSOLE.fail("Unhandled exception outside step");
    }

    public static void closeOpenStepForScope() {
        CONTEXT.closeStep();
    }

    @SuppressWarnings("ALL")
    private static boolean getBoolean(String key) {
        return Boolean.parseBoolean(require(key));
    }
}
