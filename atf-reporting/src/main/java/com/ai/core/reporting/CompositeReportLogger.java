package com.ai.core.reporting;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

public final class CompositeReportLogger implements ReportLogger {

    @Getter
    @Accessors(fluent = true)
    private final ReportLogger primary;
    private final ReportLogger secondary;

    private CompositeReportLogger(ReportLogger primary, ReportLogger secondary) {
        this.primary = Objects.requireNonNull(primary, "primary");
        this.secondary = Objects.requireNonNull(secondary, "secondary");
    }

    public static ReportLogger of(ReportLogger primary, ReportLogger secondary) {
        if (primary == secondary || primary instanceof CompositeReportLogger) {
            return primary;
        }
        return new CompositeReportLogger(primary, secondary);
    }

    @Override
    public void startTest(String testName, String description) {
        primary.startTest(testName, description);
        secondary.startTest(testName, description);
    }

    @Override
    public void startTest(String className, String testName, String description) {
        primary.startTest(className, testName, description);
        secondary.startTest(className, testName, description);
    }

    @Override
    public void ensureTestStarted(String testName, String description) {
        primary.ensureTestStarted(testName, description);
        secondary.ensureTestStarted(testName, description);
    }

    @Override
    public void ensureTestStarted(String className, String testName, String description) {
        primary.ensureTestStarted(className, testName, description);
        secondary.ensureTestStarted(className, testName, description);
    }

    @Override
    public void endTest(TestStatus status) {
        primary.endTest(status);
        secondary.endTest(secondaryStatus(status));
    }

    @Override
    public void startStep(String stepTitle) {
        primary.startStep(stepTitle);
        secondary.startStep(stepTitle);
    }

    @Override
    public void logDetail(String detail) {
        primary.logDetail(detail);
        secondary.logDetail(detail);
    }

    @Override
    public void pass(String message) {
        primary.pass(message);
        secondary.pass(message);
    }

    @Override
    public void fail(String message, String stacktraceAsText) {
        primary.fail(message, stacktraceAsText);
        if (StepLogger.isExpectedFailureLogging()) {
            secondary.logDetail("Controlled expected failure observed: " + message);
            secondary.logCodeBlock(stacktraceAsText);
            secondary.pass("Controlled expected failure kept Extent-green");
            return;
        }
        secondary.fail(message, stacktraceAsText);
    }

    @Override
    public void attachScreenshotBase64(String base64, String title) {
        primary.attachScreenshotBase64(base64, title);
        secondary.attachScreenshotBase64(base64, title);
    }

    @Override
    public void logCodeBlock(String content) {
        primary.logCodeBlock(content);
        secondary.logCodeBlock(content);
    }

    @Override
    public void flush() {
        primary.flush();
        secondary.flush();
    }

    private static TestStatus secondaryStatus(TestStatus status) {
        if (status == TestStatus.FAIL && StepLogger.isExpectedFailureLogging()) {
            return TestStatus.PASS;
        }
        return status;
    }
}
