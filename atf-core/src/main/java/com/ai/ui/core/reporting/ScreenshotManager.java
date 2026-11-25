package com.ai.ui.core.reporting;

import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.ReportLogger;
import com.ai.ui.core.runtime.DriverManager;

public final class ScreenshotManager {

    private static final ThreadLocal<Boolean> taken = ThreadLocal.withInitial(() -> false);

    private ScreenshotManager() {
    }

    @SuppressWarnings("unused")
    public static void resetForTest() {
        taken.set(false);
    }

    public static boolean attachOnce(String label) {
        if (!taken.get() && DriverManager.hasActiveEngine()) {
            ReportLogger logger = ReportingManager.tryGetLogger();
            if (logger == null) {
                return false;
            }

            String base64 = DriverManager.getEngine()
                    .captureScreenshotAsBase64();

            logger.attachScreenshotBase64(base64, label);
            taken.set(true);

            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    static void clear() {
        taken.remove();
    }
}
