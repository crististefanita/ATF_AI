package com.ai.api.core.utils;

import java.time.Duration;
import java.util.Objects;

public final class ApiSleeper {

    private ApiSleeper() {
    }

    public static void sleep(Duration duration, String interruptionMessage) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Sleep duration must not be negative: " + duration);
        }

        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptionMessage, e);
        }
    }
}
