package com.ai.ui.support.actions;

import com.ai.ui.core.engine.UiNetworkResponse;
import com.ai.ui.core.runtime.DriverManager;

final class UiNetworkActions {
    private UiNetworkActions() {
    }

    static UiNetworkResponse waitForResponse(String description, String urlContains, String method, Runnable trigger) {
        return UiActionStep.call(
                "Wait for network response: " + description,
                () -> {
                    UiActionStep.detail("urlContains=" + urlContains);
                    UiActionStep.detail("method=" + method);
                    UiNetworkResponse result = DriverManager.getEngine().waitForResponse(urlContains, method, trigger);
                    UiActionStep.detail("responseUrl=" + result.getUrl());
                    UiActionStep.detail("responseMethod=" + result.getMethod());
                    UiActionStep.detail("status=" + result.getStatus());
                    UiActionStep.detail("requestPreviewLength=" + result.getRequestPreview().length());
                    if (!result.getRequestPreview().isBlank()) {
                        UiActionStep.detail("requestPreview=" + result.getRequestPreview());
                    }
                    UiActionStep.detail("bodyPreviewLength=" + result.getBodyPreview().length());
                    if (!result.getBodyPreview().isBlank()) {
                        UiActionStep.detail("bodyPreview=" + result.getBodyPreview());
                    }
                    return result;
                },
                "Network response captured",
                "Failed waiting for network response: " + description
        );
    }
}
