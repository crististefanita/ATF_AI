package com.ai.ui.core.reporting;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.config.UiScreenshotConfigKeys;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.attachment.FinalStateAttachmentHandler;
import com.ai.core.reporting.attachment.FailureAttachmentHandler;
import com.ai.ui.core.runtime.UiInteractionTracker;

public final class UiScreenshotFailureHandler
        implements FailureAttachmentHandler, FinalStateAttachmentHandler {

    private static final String FAILURE_SCREENSHOT = "Failure Screenshot";
    private static final String FINAL_SCREENSHOT = "Final Screenshot";

    @Override
    public void onTestStart() {
        UiInteractionTracker.reset();
        ScreenshotManager.resetForTest();
    }

    @Override
    public void onTestFailure() {
        if (!isFailureScreenshotEnabled()) return;
        ScreenshotManager.attachOnce(FAILURE_SCREENSHOT);
    }

    @Override
    public void onTestSuccess() {
        if (!isFinalStateCaptureEnabled()) return;

        if (ScreenshotManager.attachOnce(FINAL_SCREENSHOT)) {
            StepLogger.withConsoleMuted(() -> {
                StepLogger.startStep("Attach final UI screenshot");
                StepLogger.pass("Final UI screenshot attached");
            });
        }
    }

    public void captureFinalStateIfEligible() {
        onTestSuccess();
    }

    private boolean isFailureScreenshotEnabled() {
        return isUiSessionConfigured() && isScreenshotsEnabled();
    }

    private boolean isFinalStateCaptureEnabled() {
        return isFailureScreenshotEnabled()
                && !isFailureOnlyMode()
                && getBoolean(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, false);
    }

    private boolean isUiSessionConfigured() {
        String engine = ConfigManager.get(UiRuntimeOptions.UI_ENGINE_KEY, "");
        return engine != null && !engine.isBlank();
    }

    private boolean isScreenshotsEnabled() {
        return getBoolean(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, true);
    }

    private boolean isFailureOnlyMode() {
        return getBoolean(UiScreenshotConfigKeys.FAILURE_ONLY, false);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        return ConfigManager.getBoolean(key, defaultValue);
    }
}
