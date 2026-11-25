package com.ai.atf.core.reporting.screenshot;

import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.step.StepScope;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UIEngineFactory;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_BROWSER_WINDOW_CONTRACT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_BROWSER_WINDOW_CONTRACT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
public class UiEngineConfiguredRuntimeParityContractTest {
    private static final String CONFIGURED_ZOOM = "60";
    private static final String RESOLVED_ZOOM = "67%";

    private AtfRuntimeConfigSandbox config;

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }
    }

    @Test
    public void configured_engine_opens_single_browser_target() throws Exception {
        UIEngine engine = createConfiguredEngine();
        try {
            try (StepScope step = StepLogger.step("Open local parity probe")) {
                StepLogger.logDetail("configured.runtime.engine=" + configuredEngineName());
                Assert.assertEquals(countBrowserTargets(engine), 1, "Engine must start with one browser target.");
                engine.open(localProbeUrl());
                Assert.assertEquals(countBrowserTargets(engine), 1, "Engine must keep one browser target after open.");
                Assert.assertTrue(engine.getCurrentUrl().startsWith("data:text/html"));
                step.pass("Configured engine kept one browser target");
            }
        } finally {
            engine.quit();
        }
    }

    @Test
    public void configured_engine_captures_non_blank_screenshot() {
        UIEngine engine = createConfiguredEngine();
        try {
            try (StepScope step = StepLogger.step("Capture configured engine screenshot")) {
                StepLogger.logDetail("configured.runtime.engine=" + configuredEngineName());
                engine.open(localProbeUrl());
                String screenshot = engine.captureScreenshotAsBase64();
                Assert.assertNotNull(screenshot);
                Assert.assertFalse(screenshot.isBlank(), "Configured engine screenshot must not be blank.");
                step.pass("Configured engine screenshot captured");
            }
        } finally {
            engine.quit();
        }
    }

    @Test
    public void configured_engine_applies_resolved_headless_zoom() throws Exception {
        UIEngine engine = createConfiguredEngine();
        try {
            try (StepScope step = StepLogger.step("Inspect configured engine zoom")) {
                StepLogger.logDetail("configured.runtime.engine=" + configuredEngineName());
                engine.open(localProbeUrl());
                Assert.assertEquals(documentZoom(engine), RESOLVED_ZOOM);
                engine.captureScreenshotAsBase64();
                Assert.assertEquals(documentZoom(engine), RESOLVED_ZOOM);
                step.pass("Configured engine kept resolved zoom before and after screenshot");
            }
        } finally {
            engine.quit();
        }
    }

    private UIEngine createConfiguredEngine() {
        String engineName = configuredEngineName();
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY, UiRuntimeOptions.WINDOW_MODE_FULLSCREEN)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .setConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, CONFIGURED_ZOOM)
                .setConfig(UiRuntimeOptions.UI_RUNTIME_METADATA_REPORTING_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "10");
        return UIEngineFactory.create();
    }

    private static String configuredEngineName() {
        return UiEngineType.fromConfig(System.getProperty(
                UiRuntimeOptions.UI_ENGINE_KEY,
                UiEngineType.PLAYWRIGHT.configValue()
        )).configValue();
    }

    private static String documentZoom(UIEngine engine) throws Exception {
        Object zoom;
        if (engine.getClass().getName().contains("SeleniumEngine")) {
            WebDriver driver = (WebDriver) readField(engine, "driver");
            zoom = ((JavascriptExecutor) driver).executeScript("return document.documentElement.style.zoom;");
        } else {
            Page page = (Page) readField(engine, "page");
            zoom = page.evaluate("() => document.documentElement.style.zoom");
        }
        return String.valueOf(zoom);
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

    private static Object readField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static String localProbeUrl() {
        String html = "<!doctype html><html><body>"
                + "<main id='atf-runtime-parity-probe'>ATF runtime parity probe</main>"
                + "</body></html>";
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
