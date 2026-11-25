package com.ai.ui.core.engine.playwright.element.actions;

import com.ai.ui.core.engine.timing.UiEngineTiming;
import com.ai.ui.core.wait.UiSleeper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.time.Duration;

public final class PlaywrightVisibleClickWaiter {
    private static final long POLL_MILLIS = 100L;

    private final Page page;
    private final int timeoutSeconds;
    private final Runnable afterClick;

    public PlaywrightVisibleClickWaiter(Page page, int timeoutSeconds, Runnable afterClick) {
        this.page = page;
        this.timeoutSeconds = timeoutSeconds;
        this.afterClick = afterClick;
    }

    public void click(String cssSelector, int oneBasedIndex) {
        if (oneBasedIndex < 1) {
            throw new IllegalArgumentException("Visible click index is 1-based.");
        }

        long deadlineMillis = System.currentTimeMillis() + UiEngineTiming.secondsToMillisLong(timeoutSeconds);
        PlaywrightException lastFailure = null;
        while (System.currentTimeMillis() <= deadlineMillis) {
            try {
                clickNow(cssSelector, oneBasedIndex);
                return;
            } catch (PlaywrightException failure) {
                lastFailure = failure;
                sleepBeforeRetry();
            }
        }
        throw lastFailure != null
                ? lastFailure
                : new PlaywrightException(noVisibleMatchMessage(cssSelector, oneBasedIndex));
    }

    private void clickNow(String cssSelector, int oneBasedIndex) {
        Locator locator = page.locator(cssSelector);
        int count = locator.count();
        int visibleIndex = 0;
        for (int i = 0; i < count; i++) {
            Locator nth = locator.nth(i);
            if (!nth.isVisible()) {
                continue;
            }
            visibleIndex++;
            if (visibleIndex == oneBasedIndex) {
                try {
                    nth.click();
                } finally {
                    afterClick.run();
                }
                return;
            }
        }
        throw new PlaywrightException(noVisibleMatchMessage(cssSelector, oneBasedIndex));
    }

    private static void sleepBeforeRetry() {
        if (!UiSleeper.pause(Duration.ofMillis(POLL_MILLIS))) {
            throw new PlaywrightException("Interrupted while waiting for visible element click");
        }
    }

    private static String noVisibleMatchMessage(String cssSelector, int oneBasedIndex) {
        return "No visible match found for index " + oneBasedIndex + " using selector: " + cssSelector;
    }
}
