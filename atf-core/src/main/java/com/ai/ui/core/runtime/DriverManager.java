package com.ai.ui.core.runtime;

import com.ai.core.reporting.attachment.FinalStateAttachmentRegistry;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UIEngineFactory;
import com.ai.ui.core.reporting.UiScreenshotFailureHandler;

/**
 * ThreadLocal engine lifecycle; hides engine specifics.
 */
public final class DriverManager {
    private static final ThreadLocal<UIEngine> ENGINE = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void initEngine() {
        if (ENGINE.get() != null) {
            return;
        }

        ENGINE.set(UIEngineFactory.create());

        UiScreenshotFailureHandler screenshotHandler = new UiScreenshotFailureHandler();
        FailureAttachmentRegistry.register(screenshotHandler);
        FinalStateAttachmentRegistry.register(screenshotHandler);
    }

    public static UIEngine getEngine() {
        UIEngine e = ENGINE.get();
        if (e == null) {
            throw new IllegalStateException(
                    "UI engine is not initialized. Ensure BaseTestUI setup ran."
            );
        }
        return e;
    }

    public static boolean hasActiveEngine() {
        return ENGINE.get() != null;
    }

    public static void quitEngine() {
        UIEngine e = ENGINE.get();
        if (e != null) {
            try {
                e.quit();
            } finally {
                ENGINE.remove();
            }
        }
    }
}
