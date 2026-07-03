package com.endava.ai.ui.core.engine.selenium;

import com.endava.ai.core.support.config.ChromiumBrowserRuntime;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.UiRuntimeEventPublisher;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

final class SeleniumZoomController {
    private static final String HAS_SETTINGS_PRIVATE_SCRIPT =
            "return typeof chrome !== 'undefined' && !!chrome.settingsPrivate;";
    private static final String SET_DEFAULT_ZOOM_SCRIPT =
            "chrome.settingsPrivate.setDefaultZoom(arguments[0]);";
    private static final String GET_DEFAULT_ZOOM_SCRIPT =
            "const done = arguments[arguments.length - 1];"
                    + "chrome.settingsPrivate.getDefaultZoom(done);";
    private static final String APPLY_PAGE_ZOOM_SCRIPT =
            "document.documentElement.style.zoom = arguments[0] + '%';";

    private final WebDriver driver;
    private final UIWindowConfiguration windowConfiguration;
    private final UiRuntimeOptions runtimeOptions;
    private final boolean headless;
    private boolean browserZoomConfigured;

    SeleniumZoomController(WebDriver driver, UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        this.driver = driver;
        this.windowConfiguration = windowConfiguration;
        this.runtimeOptions = runtimeOptions;
        this.headless = runtimeOptions.isHeadless();
    }

    void ensureBrowserZoomConfigured() {
        if (browserZoomConfigured || windowConfiguration.zoomPercent() == UIWindowConfiguration.DEFAULT_ZOOM_PERCENT) {
            return;
        }

        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        driver.get(ChromiumBrowserRuntime.APPEARANCE_SETTINGS_URL);
        Object hasSettingsPrivate = javascript.executeScript(HAS_SETTINGS_PRIVATE_SCRIPT);
        if (!Boolean.TRUE.equals(hasSettingsPrivate)) {
            throw new IllegalStateException(ChromiumBrowserRuntime.SETTINGS_PRIVATE_UNAVAILABLE);
        }

        double expectedZoomFactor = windowConfiguration.zoomPercent() / 100.0d;
        javascript.executeScript(SET_DEFAULT_ZOOM_SCRIPT, expectedZoomFactor);

        Object configuredZoom = javascript.executeAsyncScript(GET_DEFAULT_ZOOM_SCRIPT);
        if (!ChromiumBrowserRuntime.matchesExpectedZoom(configuredZoom, expectedZoomFactor)) {
            throw new IllegalStateException(ChromiumBrowserRuntime.ZOOM_NOT_APPLIED);
        }

        browserZoomConfigured = true;
    }

    boolean usesHeadlessZoomFallback() {
        return headless && windowConfiguration.zoomPercent() != UIWindowConfiguration.DEFAULT_ZOOM_PERCENT;
    }

    void publishBeforeNavigationMetadata() {
        publishZoomMetadata(UiZoomPhase.BEFORE_NAVIGATION);
    }

    void reapplyHeadlessPageZoomFallbackAfterNavigation() {
        reapplyHeadlessPageZoomFallback(UiZoomPhase.AFTER_NAVIGATION);
    }

    void reapplyHeadlessPageZoomFallbackAfterUrlWait() {
        reapplyHeadlessPageZoomFallback(UiZoomPhase.AFTER_URL_WAIT);
    }

    void reapplyHeadlessPageZoomFallbackBeforeScreenshot() {
        reapplyHeadlessPageZoomFallback(UiZoomPhase.BEFORE_SCREENSHOT);
    }

    private void reapplyHeadlessPageZoomFallback(UiZoomPhase phase) {
        if (!usesHeadlessZoomFallback()) {
            return;
        }

        try {
            ((JavascriptExecutor) driver).executeScript(APPLY_PAGE_ZOOM_SCRIPT, windowConfiguration.zoomPercent());
            publishZoomMetadata(phase);
        } catch (RuntimeException ignored) {
            // Navigation may transiently swap documents; the screenshot guard retries before capture.
        }
    }

    private void publishZoomMetadata(UiZoomPhase phase) {
        if (windowConfiguration.zoomPercent() == UIWindowConfiguration.DEFAULT_ZOOM_PERCENT) {
            return;
        }

        UiRuntimeEventPublisher.publishZoomMetadata(
                runtimeOptions,
                UiEngineType.SELENIUM,
                headless,
                windowConfiguration.zoomPercent(),
                phase.label()
        );
    }
}
