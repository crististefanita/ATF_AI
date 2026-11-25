package com.ai.ui.core.wait;

import java.time.Duration;

/**
 * Single UI wait boundary for fixed pauses.
 *
 * <p>Engine adapters and support waits may need a short deterministic pause,
 * but interrupt handling must stay consistent and auditable.</p>
 */
public final class UiSleeper {
    private UiSleeper() {
    }

    public static boolean pause(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return true;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static boolean pauseMillis(long millis) {
        return pause(Duration.ofMillis(millis));
    }
}
