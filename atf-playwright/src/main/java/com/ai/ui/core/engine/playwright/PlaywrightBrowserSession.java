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
import java.util.Objects;
import java.util.function.Supplier;

final class PlaywrightBrowserSession {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private final UiRuntimeOptions runtimeOptions;
    private final Path userDataDir;

    PlaywrightBrowserSession(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        this(windowConfiguration, runtimeOptions, Playwright::create);
    }

    PlaywrightBrowserSession(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions,
                             Supplier<Playwright> driverFactory) {
        this.runtimeOptions = runtimeOptions;
        boolean headless = runtimeOptions.isHeadless();
        Playwright acquiredPlaywright = null;
        Browser acquiredBrowser = null;
        BrowserContext acquiredContext = null;
        Path acquiredDirectory = null;
        try {
            acquiredPlaywright = Objects.requireNonNull(driverFactory.get(), "playwright");
            acquiredDirectory = requiresPersistentContextForZoom(windowConfiguration, headless)
                    ? PlaywrightUserDataDirectory.createTemporary() : null;
            if (acquiredDirectory == null) {
                acquiredBrowser = acquiredPlaywright.chromium().launch(
                        PlaywrightLaunchOptionsFactory.launchOptions(windowConfiguration, headless));
                acquiredContext = acquiredBrowser.newContext(
                        PlaywrightLaunchOptionsFactory.contextOptions(windowConfiguration, headless));
            } else {
                acquiredContext = acquiredPlaywright.chromium().launchPersistentContext(acquiredDirectory,
                        PlaywrightLaunchOptionsFactory.persistentContextOptions(windowConfiguration, headless));
                acquiredBrowser = acquiredContext.browser();
            }
            this.playwright = acquiredPlaywright;
            this.browser = acquiredBrowser;
            this.context = acquiredContext;
            this.userDataDir = acquiredDirectory;
            this.page = acquireStartupPage(acquiredContext);
            applyWindowConfiguration(windowConfiguration, headless);
            assertSingleStartupTarget("after init");
        } catch (RuntimeException | Error primary) {
            release(acquiredContext, acquiredBrowser, acquiredPlaywright, acquiredDirectory, primary);
            throw primary;
        }
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
        release(context, browser, playwright, userDataDir, null);
    }

    private static void release(BrowserContext context, Browser browser, Playwright playwright,
                                Path directory, Throwable primary) {
        Throwable failure = primary;
        if (context != null) failure = closeResource(failure, context::close);
        if (browser != null && directory == null) failure = closeResource(failure, browser::close);
        if (playwright != null) failure = closeResource(failure, playwright::close);
        failure = closeResource(failure, () -> PlaywrightUserDataDirectory.deleteQuietly(directory));
        if (primary == null) {
            if (failure instanceof RuntimeException) throw (RuntimeException) failure;
            if (failure instanceof Error) throw (Error) failure;
        }
    }

    private static Throwable closeResource(Throwable primary, Runnable close) {
        try {
            close.run();
        } catch (RuntimeException | Error secondary) {
            if (primary == null) return secondary;
            if (primary != secondary) {
                for (Throwable existing : primary.getSuppressed()) if (existing == secondary) return primary;
                primary.addSuppressed(secondary);
            }
        }
        return primary;
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
