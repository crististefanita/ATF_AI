package com.endava.ai.core.reporting.adapters.allure;

import java.util.Base64;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AllureAttachmentPublisher {

    private final AllureReporter reporter;

    public void attachScreenshotBase64(String base64, String title) {
        if (isBlank(base64)) {
            return;
        }
        reporter.attachPng(
                title != null ? title : "Failure Screenshot",
                Base64.getDecoder().decode(base64)
        );
    }

    public void attachJsonPayload(String content) {
        if (isBlank(content)) {
            return;
        }
        reporter.attachText("Payload", "application/json", ".json", content);
    }

    public void attachFailureDetails(String stacktrace) {
        if (isBlank(stacktrace)) {
            return;
        }
        reporter.attachText("Step failure details", "text/plain", ".txt", stacktrace);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
