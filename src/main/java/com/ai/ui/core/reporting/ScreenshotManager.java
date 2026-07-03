package com.ai.ui.core.reporting;

import com.ai.core.reporting.ReportingManager;
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

            taken.set(true);

            String base64 = DriverManager.getEngine()
                    .captureScreenshotAsBase64();

            ReportingManager.getLogger()
                    .attachScreenshotBase64(base64, label);

            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    static void clear() {
        taken.remove();
    }
}
