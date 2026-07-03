package com.ai.atf.core.reporting.adapters.dom;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Path;

public final class ExtentSparkBrowserSession {

    private ExtentSparkBrowserSession() {
    }

    public static void openReport(Path report, int width, int height, PageWork work) throws Exception {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(width, height));
            navigateToReport(page, report);
            work.accept(page);
        }
    }

    public static void navigateToReport(Page page, Path report) {
        page.navigate(report.toUri().toString());
        page.waitForLoadState();
        ExtentSparkDomAssertions.waitForPreparedExtentTree(page);
    }

    @FunctionalInterface
    public interface PageWork {
        void accept(Page page) throws Exception;
    }
}
