package com.ai.core.support.contract.wait;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public final class AtfTestWaiter {
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);

    private AtfTestWaiter() {
    }

    public static void pause(Duration duration) {
        sleep(duration);
    }

    public static boolean awaitTrue(Duration timeout, BooleanSupplier condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(DEFAULT_POLL_INTERVAL);
        }
        return condition.getAsBoolean();
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting in ATF test support", exception);
        }
    }
}
