package com.ai.core.support.config;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Locale;
import com.ai.ui.core.engine.timing.UiEngineTiming;

public final class UiRuntimeOptions {
    public static final String UI_ENGINE_KEY                       = "ui.engine";
    public static final String UI_HEADLESS_KEY                     = "ui.headless";
    public static final String UI_WAIT_TIMEOUT_SECONDS_KEY         = "ui.wait.timeout.seconds";
    public static final String LEGACY_WAIT_TIMEOUT_SECONDS_KEY     = "explicit.wait.seconds";
    public static final String CONSOLE_DETAILS_ENABLED_KEY         = "console.details.enabled";
    public static final String UI_RUNTIME_METADATA_REPORTING_KEY   = "ui.runtime.metadata.reporting.enabled";
    public static final String UI_PAGE_ZOOM_PERCENT_KEY            = "ui.page.zoom.percent";
    public static final String UI_WINDOW_MODE_KEY                  = "ui.window.mode";
    public static final String UI_WINDOW_SIZE_KEY                  = "ui.window.size";
    public static final String WINDOW_MODE_FULLSCREEN              = "fullscreen";

    private static final String DEFAULT_ENGINE = "default";
    private static final boolean DEFAULT_HEADLESS = true;
    private static final String DEFAULT_WAIT_TIMEOUT_SECONDS = "10";
    private static final boolean DEFAULT_DISABLED = false;
    private static final int MAX_WAIT_TIMEOUT_SECONDS = Integer.MAX_VALUE / UiEngineTiming.MILLIS_PER_SECOND;

    @Getter
    @Accessors(fluent = true)
    private final String engineName;
    @Getter
    private final boolean headless;
    @Getter
    @Accessors(fluent = true)
    private final int waitTimeoutSeconds;
    @Getter
    private final boolean consoleDetailsEnabled;
    @Getter
    private final boolean runtimeMetadataReportingEnabled;
    @Getter
    @Accessors(fluent = true)
    private final String configuredZoomPercent;

    private UiRuntimeOptions(
            String engineName,
            boolean headless,
            int waitTimeoutSeconds,
            boolean consoleDetailsEnabled,
            boolean runtimeMetadataReportingEnabled,
            String configuredZoomPercent
    ) {
        this.engineName = engineName;
        this.headless = headless;
        this.waitTimeoutSeconds = waitTimeoutSeconds;
        this.consoleDetailsEnabled = consoleDetailsEnabled;
        this.runtimeMetadataReportingEnabled = runtimeMetadataReportingEnabled;
        this.configuredZoomPercent = configuredZoomPercent;
    }

    public static UiRuntimeOptions load() {
        String engineName = ConfigManager.get(UI_ENGINE_KEY, DEFAULT_ENGINE).toLowerCase(Locale.ROOT);
        UiEngineType.fromConfig(engineName);
        ConfigManager.getBoolean(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, true);
        ConfigManager.getBoolean(UiScreenshotConfigKeys.FAILURE_ONLY, false);
        ConfigManager.getBoolean(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, false);
        return new UiRuntimeOptions(
                engineName,
                ConfigManager.getBoolean(UI_HEADLESS_KEY, DEFAULT_HEADLESS),
                readWaitTimeoutSeconds(),
                ConfigManager.getBoolean(CONSOLE_DETAILS_ENABLED_KEY, DEFAULT_DISABLED),
                ConfigManager.getBoolean(UI_RUNTIME_METADATA_REPORTING_KEY, DEFAULT_DISABLED),
                ConfigManager.get(UI_PAGE_ZOOM_PERCENT_KEY, "100")
        );
    }

    public UiEngineType engineType() {
        return UiEngineType.fromConfig(engineName);
    }

    public static int readWaitTimeoutSeconds() {
        String key = UI_WAIT_TIMEOUT_SECONDS_KEY;
        String value = ConfigManager.get(key, null);
        if (value == null) {
            value = ConfigManager.get(LEGACY_WAIT_TIMEOUT_SECONDS_KEY, null);
            if (value == null) value = DEFAULT_WAIT_TIMEOUT_SECONDS;
            else key = LEGACY_WAIT_TIMEOUT_SECONDS_KEY;
        }
        try {
            int seconds = Integer.parseInt(value);
            if (seconds < 1 || seconds > MAX_WAIT_TIMEOUT_SECONDS) {
                throw new IllegalStateException("Config key must be between 1 and " + MAX_WAIT_TIMEOUT_SECONDS
                        + " seconds: " + key + "=" + value);
            }
            return seconds;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer config key: " + key + "=" + value, e);
        }
    }
}
