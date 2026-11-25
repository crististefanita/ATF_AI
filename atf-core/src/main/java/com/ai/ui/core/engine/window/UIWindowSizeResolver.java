package com.ai.ui.core.engine.window;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.ui.core.engine.UIEngine;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.Locale;
import java.util.function.Supplier;

public final class UIWindowSizeResolver {
    static final String DEFAULT_WINDOW_MODE = UiRuntimeOptions.WINDOW_MODE_FULLSCREEN;
    static final int DEFAULT_ZOOM_PERCENT = UIWindowConfiguration.DEFAULT_ZOOM_PERCENT;
    static final int MIN_ZOOM_PERCENT = 10;
    static final int MAX_ZOOM_PERCENT = 500;

    private UIWindowSizeResolver() {
    }

    public static UIWindowConfiguration resolve() {
        String configuredSize = ConfigManager.get(UiRuntimeOptions.UI_WINDOW_SIZE_KEY, null);
        String zoomPercent = ConfigManager.get(
                UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY,
                Integer.toString(DEFAULT_ZOOM_PERCENT)
        );
        return resolve(
                configuredSize,
                ConfigManager.get(UiRuntimeOptions.UI_WINDOW_MODE_KEY, DEFAULT_WINDOW_MODE),
                zoomPercent,
                UIWindowSizeResolver::systemWindowSize
        );
    }

    public static UIWindowConfiguration resolve(String configuredSize, String windowMode, Supplier<int[]> systemSizeSupplier) {
        return resolve(configuredSize, windowMode, null, systemSizeSupplier);
    }

    public static UIWindowConfiguration resolve(String configuredSize,
                                                String windowMode,
                                                String zoomPercent,
                                                Supplier<int[]> systemSizeSupplier) {
        int resolvedZoomPercent = resolveZoomPercent(zoomPercent);
        int[] explicitSize = parseSize(configuredSize);
        if (explicitSize != null) {
            return UIWindowConfiguration.customSize(explicitSize[0], explicitSize[1], resolvedZoomPercent);
        }

        int[] systemSize = safeSystemWindowSize(systemSizeSupplier);
        return fullscreenConfiguration(systemSize, resolvedZoomPercent);
    }

    private static int[] parseSize(String configuredSize) {
        if (configuredSize == null) {
            return null;
        }

        String[] parts = configuredSize.trim().toLowerCase(Locale.ROOT).split("x");
        if (parts.length != 2) {
            return null;
        }

        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            if (width <= 0 || height <= 0) {
                return null;
            }
            return new int[]{width, height};
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int[] safeSystemWindowSize(Supplier<int[]> systemSizeSupplier) {
        try {
            int[] size = systemSizeSupplier.get();
            if (size == null || size.length != 2 || size[0] <= 0 || size[1] <= 0) {
                return defaultWindowSize();
            }
            return size;
        } catch (RuntimeException ignored) {
            return defaultWindowSize();
        }
    }

    private static int resolveZoomPercent(String configuredZoomPercent) {
        if (configuredZoomPercent == null || configuredZoomPercent.trim().isEmpty()) {
            return DEFAULT_ZOOM_PERCENT;
        }

        try {
            int percent = Integer.parseInt(configuredZoomPercent.trim());
            if (percent < MIN_ZOOM_PERCENT || percent > MAX_ZOOM_PERCENT) {
                return DEFAULT_ZOOM_PERCENT;
            }
            return BrowserZoomLevels.nearestSupportedPercent(percent);
        } catch (NumberFormatException ignored) {
            return DEFAULT_ZOOM_PERCENT;
        }
    }

    private static UIWindowConfiguration fullscreenConfiguration(int[] size, int zoomPercent) {
        return UIWindowConfiguration.fullscreen(size[0], size[1], zoomPercent);
    }

    private static int[] systemWindowSize() {
        try {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            return new int[]{size.width, size.height};
        } catch (HeadlessException ignored) {
            return defaultWindowSize();
        }
    }

    private static int[] defaultWindowSize() {
        return new int[]{UIEngine.DEFAULT_WINDOW_WIDTH, UIEngine.DEFAULT_WINDOW_HEIGHT};
    }
}
