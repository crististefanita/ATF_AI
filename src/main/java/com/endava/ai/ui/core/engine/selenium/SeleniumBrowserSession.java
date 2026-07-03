package com.endava.ai.ui.core.engine.selenium;

import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.reporting.utils.ConsoleLogger;
import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

final class SeleniumBrowserSession {
    private static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(0);

    private final WebDriver driver;
    private final UiRuntimeOptions runtimeOptions;

    SeleniumBrowserSession(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        this.runtimeOptions = runtimeOptions;
        this.driver = new SeleniumChromeDriverFactory(runtimeOptions).createDriver();
        this.driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);
        applyWindowConfiguration(windowConfiguration);
        assertSingleStartupTarget("after init");
    }

    WebDriver driver() {
        return driver;
    }

    void setWindowSize(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
    }

    void assertSingleStartupTarget(String phase) {
        int handleCount = driver.getWindowHandles().size();
        logStartupDebug("Selenium window handles " + phase + ": " + handleCount);
        if (handleCount != 1) {
            throw new IllegalStateException("Expected exactly one Selenium window " + phase + " but found " + handleCount);
        }
    }

    void close() {
        driver.quit();
    }

    private void applyWindowConfiguration(UIWindowConfiguration windowConfiguration) {
        if (windowConfiguration.isFullscreen() && !runtimeOptions.isHeadless()) {
            driver.manage().window().maximize();
            return;
        }

        setWindowSize(windowConfiguration.width(), windowConfiguration.height());
    }

    private void logStartupDebug(String message) {
        if (runtimeOptions.isConsoleDetailsEnabled()) {
            ConsoleLogger.console("  \u2022 " + message);
        }
    }
}
