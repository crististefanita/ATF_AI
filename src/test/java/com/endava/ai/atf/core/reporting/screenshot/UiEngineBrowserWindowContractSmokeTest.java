package com.endava.ai.atf.core.reporting.screenshot;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.listener.TestListener;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.UIEngineFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_BROWSER_WINDOW_CONTRACT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_BROWSER_WINDOW_CONTRACT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
public class UiEngineBrowserWindowContractSmokeTest {

    private AtfRuntimeConfigSandbox config;

    @AfterMethod
    public void tearDown() {
        closeConfig();
    }

    @DataProvider
    public Object[][] engines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue()},
                {UiEngineType.PLAYWRIGHT.configValue()}
        };
    }

    @DataProvider
    public Object[][] headlessZoomEngines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue()},
                {UiEngineType.PLAYWRIGHT.configValue()}
        };
    }

    @Test(dataProvider = "headlessZoomEngines")
    public void configured_headless_browser_zoom_applies_resolved_document_zoom(String engineName) throws Exception {
        String zoomed = observeDocumentZoom(engineName, "60");

        Assert.assertEquals(
                zoomed,
                "67%",
                "Headless browser zoom must be applied as resolved document zoom for " + engineName
                        + " when ui.page.zoom.percent=60; actual=" + zoomed
        );
    }

    @Test(dataProvider = "engines")
    public void engine_keeps_single_browser_target_during_startup_and_navigation(String engineName) throws Exception {
        configureEngine(engineName, "60");

        UIEngine engine = UIEngineFactory.create();
        try {
            Assert.assertEquals(countBrowserTargets(engine), 1, "Engine must start with a single browser target");
            engine.open(localProbeUrl());
            Assert.assertEquals(countBrowserTargets(engine), 1, "Engine must keep a single browser target after open");
            Assert.assertTrue(engine.getCurrentUrl().startsWith("data:text/html"));
        } finally {
            engine.quit();
        }
    }

    private String observeDocumentZoom(String engineName, String zoomPercent) throws Exception {
        configureEngine(engineName, zoomPercent);

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localProbeUrl());
            return readDocumentZoom(engine);
        } finally {
            engine.quit();
            closeConfig();
        }
    }

    private static String localProbeUrl() {
        String html = "<!doctype html><html><body><main id='atf-window-probe'>ATF window probe</main></body></html>";
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String readDocumentZoom(UIEngine engine) throws Exception {
        Object documentZoom;
        if (engine.getClass().getName().contains("SeleniumEngine")) {
            WebDriver driver = (WebDriver) readField(engine, "driver");
            documentZoom = ((JavascriptExecutor) driver).executeScript("return document.documentElement.style.zoom;");
        } else {
            Page page = (Page) readField(engine, "page");
            documentZoom = page.evaluate("() => document.documentElement.style.zoom");
        }
        return String.valueOf(documentZoom);
    }

    private static int countBrowserTargets(UIEngine engine) throws Exception {
        if (engine.getClass().getName().contains("SeleniumEngine")) {
            WebDriver driver = (WebDriver) readField(engine, "driver");
            return driver.getWindowHandles().size();
        }

        Object session = readField(engine, "session");
        BrowserContext context = (BrowserContext) readField(session, "context");
        return context.pages().size();
    }

    private void configureEngine(String engineName, String zoomPercent) {
        closeConfig();
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY, UiRuntimeOptions.WINDOW_MODE_FULLSCREEN)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .setConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, zoomPercent)
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "2");
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void closeConfig() {
        if (config != null) {
            config.close();
            config = null;
        }
    }
}
