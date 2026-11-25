package com.ai.core.reporting.adapters;

import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.allure.AllureAttachmentPublisher;
import com.ai.core.reporting.adapters.allure.AllureParameterFactory;
import com.ai.core.reporting.adapters.allure.AllureReporter;
import com.ai.core.reporting.adapters.allure.AllureStepBuffer;
import com.ai.core.reporting.adapters.allure.AllureStepContext;
import com.ai.core.reporting.adapters.allure.AllureStepPublisher;
import com.ai.core.reporting.internal.ReportingEngine;

public final class AllureAdapter implements ReportLogger {

    private static final AllureAdapter INSTANCE = new AllureAdapter();

    private final AllureReporter reporter = new AllureReporter();
    private final AllureStepContext stepContext = new AllureStepContext(reporter);
    private final AllureStepBuffer stepBuffer = new AllureStepBuffer();
    private final AllureStepPublisher stepPublisher = new AllureStepPublisher(reporter, stepContext, stepBuffer);
    private final AllureAttachmentPublisher attachments = new AllureAttachmentPublisher(reporter);

    private AllureAdapter() {}

    public static AllureAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public void startTest(String testName, String description) {
        stepContext.clear();
    }

    @Override
    public void endTest(TestStatus status) {
        if (isInactive()) return;
        stepPublisher.flushBufferedStepsIfPossible();
        stepPublisher.clear();
    }

    @Override
    public void startStep(String title) {
        if (isInactive()) return;
        stepPublisher.startStep(title);
    }

    @Override
    public void logDetail(String detail) {
        if (isInactive() || isBlank(detail)) return;
        stepPublisher.addParameter(AllureParameterFactory.fromDetail(detail));
    }

    @Override
    public void pass(String message) {
        if (isInactive()) return;
        stepPublisher.passCurrentStep();
    }

    @Override
    public void fail(String message, String stacktrace) {
        if (isInactive()) return;

        stepPublisher.failCurrentStep(isBlank(message) ? null : AllureParameterFactory.param("error", message));
        attachments.attachFailureDetails(stacktrace);
    }

    @Override
    public void attachScreenshotBase64(String base64, String title) {
        if (isInactive() || isBlank(base64)) return;
        attachments.attachScreenshotBase64(base64, title);
    }

    @Override
    public void logCodeBlock(String content) {
        if (isInactive() || isBlank(content)) return;
        attachments.attachJsonPayload(content);
    }

    @Override
    public void flush() {
    }

    private boolean isInactive() {
        return !ReportingEngine.current().isAllure();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
