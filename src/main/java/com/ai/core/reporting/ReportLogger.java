package com.ai.core.reporting;

public interface ReportLogger {
    void startTest(String testName, String description);
    void endTest(TestStatus status);
    void startStep(String stepTitle);
    void logDetail(String detail);
    void pass(String message);
    void fail(String message, String stacktraceAsText);
    void attachScreenshotBase64(String base64, String title);
    void logCodeBlock(String content);
    void flush();

    default void ensureTestStarted(String testName, String description) {
        startTest(testName, description);
    }

    default void ensureTestStarted(String className, String testName, String description) {
        ensureTestStarted(testName, description);
    }

    default void startTest(String className, String testName, String description) {
        startTest(testName, description);
    }
}
