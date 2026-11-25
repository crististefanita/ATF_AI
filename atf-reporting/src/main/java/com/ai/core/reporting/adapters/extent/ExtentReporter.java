package com.ai.core.reporting.adapters.extent;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.ai.core.reporting.ReportText;

import java.nio.file.Path;

public final class ExtentReporter {

    private final ExtentReportSession session;
    private boolean reportContentCreated;

    public ExtentReporter() {
        this.session = ExtentReportSession.fromRuntime();
    }

    ExtentTest createTest(String name, String description) {
        reportContentCreated = true;
        return session.createTest(name, description);
    }

    ExtentTest createClassNode(String className) {
        reportContentCreated = true;
        return session.createTest(
                ReportText.simpleClassName(className),
                className != null ? className : ""
        );
    }

    void assignTestMetadata(ExtentTest test, String className) {
        if (test == null || className == null || className.isBlank()) {
            return;
        }
        test.assignCategory(ReportText.packageName(className));
        test.assignCategory(ReportText.simpleClassName(className));
    }

    public void attachScreenshot(ExtentTest test, String base64, String title) {
        if (test == null || base64 == null || base64.isBlank()) {
            return;
        }

        String label = title != null && !title.isBlank() ? title : "Screenshot";
        test.info(MediaEntityBuilder.createScreenCaptureFromBase64String(base64, label).build());
    }

    public void logCodeBlock(ExtentTest target, String content) {
        target.info(MarkupHelper.createCodeBlock(content != null ? content : ""));
    }

    public void fail(ExtentTest target, String message, String stacktrace) {
        if (target == null) {
            return;
        }

        String content = (stacktrace != null && !stacktrace.isBlank())
                ? stacktrace
                : message;

        if (content != null && !content.isBlank()) {
            target.fail(MarkupHelper.createCodeBlock(content));
        }
    }

    public void flush() {
        session.flushAndStandardize();
    }

    public Path reportPath() {
        return session.reportPath();
    }

    public boolean hasReportContent() {
        return reportContentCreated;
    }

}
