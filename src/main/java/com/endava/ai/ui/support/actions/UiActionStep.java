package com.endava.ai.ui.support.actions;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.core.runtime.UiInteractionTracker;

import java.util.function.Supplier;

final class UiActionStep {
    static final String DETAIL_LOCATOR = "locator=";
    static final String DETAIL_VALUE = "value=";

    private UiActionStep() {
    }

    static void detail(String detail) {
        StepLogger.logDetail(detail);
    }

    static void run(String title, Runnable action, String successMessage, String failureMessage) {
        call(
                title,
                () -> {
                    action.run();
                    return null;
                },
                successMessage,
                failureMessage
        );
    }

    static <T> T call(String title, Supplier<T> action, String successMessage, String failureMessage) {
        StepLogger.startStep(title);
        try {
            UiInteractionTracker.markUiInteraction();
            T result = action.get();
            StepLogger.pass(successMessage);
            return result;
        } catch (Throwable e) {
            StepLogger.fail(failureMessage, e);
            throw new AssertionError("UI Action failed: " + title, e);
        }
    }
}
