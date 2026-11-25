package com.ai.ui.core.engine.timing;

import java.time.Duration;

public final class UiEngineTiming {
    public static final int MILLIS_PER_SECOND = 1000;

    private UiEngineTiming() {
    }

    public static int secondsToMillis(int seconds) {
        return Math.multiplyExact(seconds, MILLIS_PER_SECOND);
    }

    public static double secondsToMillisDouble(int seconds) {
        return secondsToMillis(seconds);
    }

    public static long secondsToMillisLong(int seconds) {
        return Math.multiplyExact((long) seconds, MILLIS_PER_SECOND);
    }

    public static Duration durationSeconds(int seconds) {
        return Duration.ofSeconds(seconds);
    }
}
