package com.endava.ai.ui.core.engine.playwright;

import com.endava.ai.core.support.config.ChromiumBrowserRuntime;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.UiRuntimeEventPublisher;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

final class PlaywrightZoomController {
    private static final String HAS_SETTINGS_PRIVATE_SCRIPT =
            "() => typeof chrome !== 'undefined' && !!chrome.settingsPrivate";
    private static final String SET_DEFAULT_ZOOM_SCRIPT = "zoom => chrome.settingsPrivate.setDefaultZoom(zoom)";
    private static final String GET_DEFAULT_ZOOM_SCRIPT =
            "() => new Promise(resolve => chrome.settingsPrivate.getDefaultZoom(resolve))";
    private static final String APPLY_PAGE_ZOOM_SCRIPT =
            "zoomPercent => { document.documentElement.style.zoom = zoomPercent + '%'; }";

    private final Page page;
    private final UIWindowConfiguration windowConfiguration;
    private final UiRuntimeOptions runtimeOptions;
    private final boolean headless;
    private boolean browserZoomConfigured;

    PlaywrightZoomController(
            Page page,
            UIWindowConfiguration windowConfiguration,
            UiRuntimeOptions runtimeOptions,
            boolean headless
    ) {
        this.page = page;
        this.windowConfiguration = windowConfiguration;
        this.runtimeOptions = runtimeOptions;
        this.headless = headless;
    }

    void ensureBrowserZoomConfigured() {
        if (browserZoomConfigured || windowConfiguration.zoomPercent() == UIWindowConfiguration.DEFAULT_ZOOM_PERCENT) {
            return;
        }

        page.navigate(ChromiumBrowserRuntime.APPEARANCE_SETTINGS_URL);
        Object hasSettingsPrivate = page.evaluate(HAS_SETTINGS_PRIVATE_SCRIPT);
        if (!Boolean.TRUE.equals(hasSettingsPrivate)) {
            throw new PlaywrightException(ChromiumBrowserRuntime.SETTINGS_PRIVATE_UNAVAILABLE);
        }

        double expectedZoomFactor = windowConfiguration.zoomPercent() / 100.0d;
        page.evaluate(SET_DEFAULT_ZOOM_SCRIPT, expectedZoomFactor);
        Object configuredZoom = page.evaluate(GET_DEFAULT_ZOOM_SCRIPT);
        if (!ChromiumBrowserRuntime.matchesExpectedZoom(configuredZoom, expectedZoomFactor)) {
            throw new PlaywrightException(ChromiumBrowserRuntime.ZOOM_NOT_APPLIED);
        }

        browserZoomConfigured = true;
    }

    boolean usesHeadlessZoomFallback() {
        return headless && windowConfiguration.zoomPercent() != UIWindowConfiguration.DEFAULT_ZOOM_PERCENT;
    }

    void reapplyHeadlessPageZoomFallback(UiZoomPhase phase) {
        if (!usesHeadlessZoomFallback()) {
            return;
        }

        try {
            page.evaluate(APPLY_PAGE_ZOOM_SCRIPT, windowConfiguration.zoomPercent());
            publishZoomMetadata(phase);
        } catch (PlaywrightException ignored) {
            // App-driven navigation can transiently swap documents; the screenshot guard retries before capture.
        }
    }

    void publishZoomMetadata(UiZoomPhase phase) {
        if (windowConfiguration.zoomPercent() == UIWindowConfiguration.DEFAULT_ZOOM_PERCENT) {
            return;
        }

        UiRuntimeEventPublisher.publishZoomMetadata(
                runtimeOptions,
                UiEngineType.PLAYWRIGHT,
                headless,
                windowConfiguration.zoomPercent(),
                phase.label()
        );
    }
}
