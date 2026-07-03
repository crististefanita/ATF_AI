package com.endava.ai.ui.core.engine.selenium.element.actions;

import com.endava.ai.ui.core.wait.UiSleeper;

public final class SeleniumRetryPause {
    private final long millis;

    public SeleniumRetryPause(long millis) {
        this.millis = millis;
    }

    public void sleepBriefly() {
        UiSleeper.pauseMillis(millis);
    }
}
