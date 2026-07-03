package com.endava.ai.ui.core.engine.playwright.element.actions;

import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.endava.ai.ui.core.wait.UiSleeper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;

import java.time.Duration;
import java.util.function.Consumer;

public final class PlaywrightTypingActions {
    private static final int FILL_STABILITY_POLLS = 5;
    private static final Duration FILL_STABILITY_POLL_INTERVAL = Duration.ofMillis(50);

    private final Consumer<UiZoomPhase> afterAction;

    public PlaywrightTypingActions(Consumer<UiZoomPhase> afterAction) {
        this.afterAction = afterAction;
    }

    public void type(Locator locator, String text) {
        try {
            String targetText = text != null ? text : "";
            if (targetText.equals(currentInputValue(locator))) {
                return;
            }
            locator.fill(targetText);
            stabilizeInputValue(locator, targetText);
        } finally {
            afterAction.accept(UiZoomPhase.AFTER_TYPE);
        }
    }

    public void pressKey(Locator locator, String key) {
        try {
            locator.press(key);
        } finally {
            afterAction.accept(UiZoomPhase.AFTER_KEY_PRESS);
        }
    }

    private static void stabilizeInputValue(Locator locator, String targetText) {
        for (int poll = 0; poll < FILL_STABILITY_POLLS; poll++) {
            if (!UiSleeper.pause(FILL_STABILITY_POLL_INTERVAL)) {
                throw new IllegalStateException("Interrupted while stabilizing Playwright input value.");
            }
            if (!targetText.equals(currentInputValue(locator))) {
                locator.fill(targetText);
            }
        }
    }

    private static String currentInputValue(Locator locator) {
        try {
            String value = locator.inputValue();
            return value == null ? "" : value;
        } catch (PlaywrightException ignored) {
            return "";
        }
    }
}
