package com.endava.ai.core.reporting.step;

import com.endava.ai.core.reporting.ReportLogger;

public final class StepReportDelegate {
    private final ThreadLocal<ReportLogger> delegate = new ThreadLocal<>();

    public StepReportDelegate() {
    }

    public void set(ReportLogger logger) {
        delegate.set(logger);
    }

    public void clear() {
        delegate.remove();
    }

    public void startStep(String title) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.startStep(title);
        }
    }

    public void logDetail(String detail) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.logDetail(detail);
        }
    }

    public void logCodeBlock(String content) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.logCodeBlock(content);
        }
    }

    public void pass(String message) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.pass(message);
        }
    }

    public void fail(String message, String stacktraceAsText) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.fail(message, stacktraceAsText);
        }
    }

    public void controlledExpectedFailure(String message, String stacktraceAsText) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.logDetail("WARN / controlled expected failure: " + message);
            logger.logCodeBlock(stacktraceAsText);
            logger.pass("Controlled expected failure observed");
        }
    }

    public void controlledExpectedFailureOutsideStep(Throwable throwable, String stacktraceAsText) {
        ReportLogger logger = delegate.get();
        if (logger != null) {
            logger.startStep("Controlled expected failure outside step");
            logger.logDetail("WARN / controlled expected failure outside step: " + throwable.getMessage());
            logger.logCodeBlock(stacktraceAsText);
            logger.pass("Controlled expected failure outside step observed");
        }
    }
}
