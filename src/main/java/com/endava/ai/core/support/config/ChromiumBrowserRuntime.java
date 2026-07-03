package com.endava.ai.core.support.config;

public final class ChromiumBrowserRuntime {
    public static final String APPEARANCE_SETTINGS_URL = "chrome://settings/appearance";
    public static final String SETTINGS_PRIVATE_UNAVAILABLE =
            "chrome.settingsPrivate is not available for browser zoom configuration";
    public static final String ZOOM_NOT_APPLIED = "Browser zoom was not applied correctly";

    private static final double ZOOM_TOLERANCE = 0.01d;

    private ChromiumBrowserRuntime() {
    }

    public static boolean matchesExpectedZoom(Object actualZoom, double expectedZoomFactor) {
        return actualZoom instanceof Number
                && Math.abs(((Number) actualZoom).doubleValue() - expectedZoomFactor) <= ZOOM_TOLERANCE;
    }
}
