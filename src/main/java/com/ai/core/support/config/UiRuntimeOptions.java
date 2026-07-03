package com.ai.core.support.config;

import lombok.Getter;
import lombok.experimental.Accessors;

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
    private static final String DEFAULT_HEADLESS = "true";
    private static final String DEFAULT_WAIT_TIMEOUT_SECONDS = "10";
    private static final String DEFAULT_DISABLED = "false";

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
        return new UiRuntimeOptions(
                ConfigManager.get(UI_ENGINE_KEY, DEFAULT_ENGINE).toLowerCase(),
                Boolean.parseBoolean(ConfigManager.get(UI_HEADLESS_KEY, DEFAULT_HEADLESS)),
                readWaitTimeoutSeconds(),
                Boolean.parseBoolean(ConfigManager.get(CONSOLE_DETAILS_ENABLED_KEY, DEFAULT_DISABLED)),
                Boolean.parseBoolean(ConfigManager.get(UI_RUNTIME_METADATA_REPORTING_KEY, DEFAULT_DISABLED)),
                ConfigManager.get(UI_PAGE_ZOOM_PERCENT_KEY, "100")
        );
    }

    public UiEngineType engineType() {
        return UiEngineType.fromConfig(engineName);
    }

    private static int readWaitTimeoutSeconds() {
        String value = ConfigManager.get(
                UI_WAIT_TIMEOUT_SECONDS_KEY,
                ConfigManager.get(LEGACY_WAIT_TIMEOUT_SECONDS_KEY, DEFAULT_WAIT_TIMEOUT_SECONDS)
        );
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid integer config key: " + UI_WAIT_TIMEOUT_SECONDS_KEY, e);
        }
    }
}
