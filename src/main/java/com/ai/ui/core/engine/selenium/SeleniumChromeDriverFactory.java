package com.ai.ui.core.engine.selenium;

import com.ai.core.reporting.utils.ConsoleLogger;
import com.ai.core.support.config.UiRuntimeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

final class SeleniumChromeDriverFactory {
    private static final String CHROME_DRIVER_PROPERTY = "webdriver.chrome.driver";
    private static final String HEADLESS_CHROME_ARG = "--headless=new";
    private static final String NO_SANDBOX_ARG = "--no-sandbox";
    private static final String DISABLE_DEV_SHM_USAGE_ARG = "--disable-dev-shm-usage";
    private static final String CHROME_LOGGING_PREFS_CAPABILITY = "goog:loggingPrefs";

    private final UiRuntimeOptions runtimeOptions;
    private final SeleniumChromeDriverCache chromeDriverCache;

    SeleniumChromeDriverFactory(UiRuntimeOptions runtimeOptions) {
        this(runtimeOptions, new SeleniumChromeDriverCache());
    }

    SeleniumChromeDriverFactory(UiRuntimeOptions runtimeOptions, SeleniumChromeDriverCache chromeDriverCache) {
        this.runtimeOptions = runtimeOptions;
        this.chromeDriverCache = chromeDriverCache;
    }

    ChromeDriver createDriver() {
        ChromeOptions options = chromeOptions();
        if (hasExplicitChromeDriver()) {
            return new ChromeDriver(options);
        }

        Optional<Path> cachedDriver = chromeDriverCache.latestChromeDriverExecutable();
        if (cachedDriver.isPresent()) {
            try {
                return createDriverWithCachedBinary(options, cachedDriver.get());
            } catch (RuntimeException cachedDriverFailure) {
                System.clearProperty(CHROME_DRIVER_PROPERTY);
                try {
                    return new ChromeDriver(options);
                } catch (RuntimeException seleniumManagerFailure) {
                    seleniumManagerFailure.addSuppressed(cachedDriverFailure);
                    throw seleniumManagerFailure;
                }
            }
        }

        try {
            return new ChromeDriver(options);
        } catch (RuntimeException seleniumManagerFailure) {
            return createDriverWithCachedBinaryFallback(options, seleniumManagerFailure);
        }
    }

    private ChromeDriver createDriverWithCachedBinaryFallback(
            ChromeOptions options,
            RuntimeException seleniumManagerFailure
    ) {
        Optional<Path> cachedDriver = chromeDriverCache.latestChromeDriverExecutable();
        if (cachedDriver.isEmpty()) {
            throw seleniumManagerFailure;
        }

        try {
            return createDriverWithCachedBinary(options, cachedDriver.get());
        } catch (RuntimeException cachedDriverFailure) {
            cachedDriverFailure.addSuppressed(seleniumManagerFailure);
            throw cachedDriverFailure;
        }
    }

    private ChromeDriver createDriverWithCachedBinary(ChromeOptions options, Path cachedDriver) {
        System.setProperty(CHROME_DRIVER_PROPERTY, cachedDriver.toString());
        logDriverFallback(cachedDriver);
        return new ChromeDriver(options);
    }

    private ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (runtimeOptions.isHeadless()) {
            options.addArguments(HEADLESS_CHROME_ARG);
        }
        options.addArguments(NO_SANDBOX_ARG);
        options.addArguments(DISABLE_DEV_SHM_USAGE_ARG);
        options.setCapability(CHROME_LOGGING_PREFS_CAPABILITY, performanceLoggingPreferences());
        return options;
    }

    private static LoggingPreferences performanceLoggingPreferences() {
        LoggingPreferences preferences = new LoggingPreferences();
        preferences.enable(LogType.PERFORMANCE, Level.ALL);
        return preferences;
    }

    private static boolean hasExplicitChromeDriver() {
        String configuredDriver = System.getProperty(CHROME_DRIVER_PROPERTY);
        return configuredDriver != null && !configuredDriver.isBlank();
    }

    private void logDriverFallback(Path cachedDriver) {
        if (runtimeOptions.isConsoleDetailsEnabled()) {
            ConsoleLogger.console("  \u2022 Selenium cached chromedriver: " + cachedDriver);
        }
    }
}
