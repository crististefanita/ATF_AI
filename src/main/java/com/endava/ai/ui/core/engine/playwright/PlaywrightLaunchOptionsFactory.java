package com.endava.ai.ui.core.engine.playwright;

import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;

import java.util.Collections;

final class PlaywrightLaunchOptionsFactory {
    private static final String START_MAXIMIZED_ARG = "--start-maximized";

    private PlaywrightLaunchOptionsFactory() {
    }

    static BrowserType.LaunchOptions launchOptions(
            UIWindowConfiguration windowConfiguration,
            boolean headless
    ) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless);
        if (windowConfiguration.isFullscreen() && !headless) {
            options.setArgs(Collections.singletonList(START_MAXIMIZED_ARG));
        }
        return options;
    }

    static Browser.NewContextOptions contextOptions(
            UIWindowConfiguration windowConfiguration,
            boolean headless
    ) {
        Browser.NewContextOptions options = new Browser.NewContextOptions();
        if (windowConfiguration.isFullscreen() && !headless) {
            options.setViewportSize(null);
        }
        return options;
    }

    static BrowserType.LaunchPersistentContextOptions persistentContextOptions(
            UIWindowConfiguration windowConfiguration,
            boolean headless
    ) {
        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setHeadless(headless);
        if (windowConfiguration.isFullscreen() && !headless) {
            options.setArgs(Collections.singletonList(START_MAXIMIZED_ARG));
            options.setViewportSize(null);
        }
        return options;
    }
}
