package com.ai.ui.core.engine.playwright;

import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.utils.ConsoleLogger;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;

import java.nio.file.Path;
import java.util.List;

final class PlaywrightBrowserSession {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final UiRuntimeOptions runtimeOptions;
    private final Path userDataDir;

    PlaywrightBrowserSession(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        this.runtimeOptions = runtimeOptions;
        boolean headless = runtimeOptions.isHeadless();
        this.playwright = Playwright.create();
        this.userDataDir = requiresPersistentContextForZoom(windowConfiguration, headless)
                ? PlaywrightUserDataDirectory.createTemporary()
                : null;

        if (userDataDir == null) {
            this.browser = playwright.chromium().launch(
                    PlaywrightLaunchOptionsFactory.launchOptions(windowConfiguration, headless)
            );
            this.context = browser.newContext(
                    PlaywrightLaunchOptionsFactory.contextOptions(windowConfiguration, headless)
            );
        } else {
            this.context = playwright.chromium()
                    .launchPersistentContext(
                            userDataDir,
                            PlaywrightLaunchOptionsFactory.persistentContextOptions(windowConfiguration, headless)
                    );
            this.browser = context.browser();
        }

        this.page = acquireStartupPage(context);
        applyWindowConfiguration(windowConfiguration, headless);
        assertSingleStartupTarget("after init");
    }

    Page page() {
        return page;
    }

    BrowserContext context() {
        return context;
    }

    void setWindowSize(int width, int height) {
        page.setViewportSize(width, height);
    }

    void assertSingleStartupTarget(String phase) {
        int pageCount = context.pages().size();
        logStartupDebug("Playwright pages " + phase + ": " + pageCount);
        if (pageCount != 1) {
            throw new IllegalStateException("Expected exactly one Playwright page " + phase + " but found " + pageCount);
        }
    }

    void close() {
        try {
            context.close();
        } finally {
            try {
                if (browser != null && userDataDir == null) {
                    browser.close();
                }
            } finally {
                try {
                    playwright.close();
                } finally {
                    PlaywrightUserDataDirectory.deleteQuietly(userDataDir);
                }
            }
        }
    }

    private void applyWindowConfiguration(UIWindowConfiguration windowConfiguration, boolean headless) {
        if (windowConfiguration.isFullscreen() && !headless) {
            return;
        }
        setWindowSize(windowConfiguration.width(), windowConfiguration.height());
    }

    private static Page acquireStartupPage(BrowserContext context) {
        List<Page> pages = context.pages();
        if (pages.isEmpty()) {
            return context.newPage();
        }

        Page primaryPage = pages.get(0);
        closeExtraPages(pages);
        return primaryPage;
    }

    private static void closeExtraPages(List<Page> pages) {
        for (int i = 1; i < pages.size(); i++) {
            try {
                pages.get(i).close();
            } catch (PlaywrightException ignored) {
            }
        }
    }

    private static boolean requiresPersistentContextForZoom(UIWindowConfiguration windowConfiguration, boolean headless) {
        return !headless && windowConfiguration.zoomPercent() != UIWindowConfiguration.DEFAULT_ZOOM_PERCENT;
    }

    private void logStartupDebug(String message) {
        if (runtimeOptions.isConsoleDetailsEnabled()) {
            ConsoleLogger.console("  \u2022 " + message);
        }
    }
}
