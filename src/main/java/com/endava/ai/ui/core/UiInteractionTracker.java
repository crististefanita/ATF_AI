package com.endava.ai.ui.core;

/**
 * Tracks whether the current test actually attempted a UI operation.
 */
public final class UiInteractionTracker {
    private static final ThreadLocal<Boolean> UI_TOUCHED = ThreadLocal.withInitial(() -> false);

    private UiInteractionTracker() {
    }

    public static void markUiInteraction() {
        UI_TOUCHED.set(true);
    }

    public static boolean hasUiInteraction() {
        return Boolean.TRUE.equals(UI_TOUCHED.get());
    }

    public static void reset() {
        UI_TOUCHED.set(false);
    }

    public static void clear() {
        UI_TOUCHED.remove();
    }
}
