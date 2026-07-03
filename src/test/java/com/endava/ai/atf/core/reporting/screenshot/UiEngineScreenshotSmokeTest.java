package com.endava.ai.atf.core.reporting.screenshot;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.UIEngineFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_SCREENSHOT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_SCREENSHOT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
public class UiEngineScreenshotSmokeTest {

    private AtfRuntimeConfigSandbox config;

    @AfterMethod
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }
    }

    @DataProvider
    public Object[][] engines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue(), true},
                {UiEngineType.PLAYWRIGHT.configValue(), true}
        };
    }

    @Test(dataProvider = "engines")
    public void engine_can_open_base_url_and_capture_screenshot(String engineName, boolean headless) {
        configureEngine(engineName, headless)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .clearConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY);

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localScreenshotContractUrl());
            String screenshot = engine.captureScreenshotAsBase64();

            Assert.assertNotNull(screenshot);
            Assert.assertFalse(
                    screenshot.isBlank(),
                    "Screenshot must not be blank for " + engineName + " headless=" + headless
            );
        } finally {
            engine.quit();
        }
    }

    @Test(dataProvider = "engines")
    public void engine_accepts_custom_window_size_configuration(String engineName, boolean headless) {
        configureEngine(engineName, headless)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY)
                .setConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY, "1920x1080")
                .clearConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY);

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localScreenshotContractUrl());
            String screenshot = engine.captureScreenshotAsBase64();

            Assert.assertNotNull(screenshot);
            Assert.assertFalse(
                    screenshot.isBlank(),
                    "Screenshot must not be blank for custom ui.window.size on "
                            + engineName + " headless=" + headless
            );
        } finally {
            engine.quit();
        }
    }

    @Test(dataProvider = "engines")
    public void engine_accepts_fullscreen_window_mode_when_window_size_is_missing(String engineName, boolean headless) {
        configureEngine(engineName, headless)
                .setConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY, UiRuntimeOptions.WINDOW_MODE_FULLSCREEN)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .clearConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY);

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localScreenshotContractUrl());
            String screenshot = engine.captureScreenshotAsBase64();

            Assert.assertNotNull(screenshot);
            Assert.assertFalse(
                    screenshot.isBlank(),
                    "Screenshot must not be blank for ui.window.mode=fullscreen on "
                            + engineName + " headless=" + headless
            );
        } finally {
            engine.quit();
        }
    }

    @Test(dataProvider = "engines")
    public void engine_accepts_custom_page_zoom_percent(String engineName, boolean headless) {
        configureEngine(engineName, headless)
                .setConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY, UiRuntimeOptions.WINDOW_MODE_FULLSCREEN)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .setConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, "67");

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localScreenshotContractUrl());
            String screenshot = engine.captureScreenshotAsBase64();

            Assert.assertNotNull(screenshot);
            Assert.assertFalse(
                    screenshot.isBlank(),
                    "Screenshot must not be blank for ui.page.zoom.percent=67 on "
                            + engineName + " headless=" + headless
            );
        } finally {
            engine.quit();
        }
    }

    private AtfRuntimeConfigSandbox configureEngine(String engineName, boolean headless) {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.toString(headless))
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "10");
        return config;
    }

    private static String localScreenshotContractUrl() {
        String html = "<!doctype html><html><body>"
                + "<main style='font-family:sans-serif;padding:32px'>"
                + "<h1>ATF screenshot contract</h1>"
                + "<p>Stable local page for UI engine screenshot evidence.</p>"
                + "</main></body></html>";
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
