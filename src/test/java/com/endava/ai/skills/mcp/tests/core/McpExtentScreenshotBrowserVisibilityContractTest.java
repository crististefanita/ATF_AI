package com.endava.ai.skills.mcp.tests.core;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentScreenshotEvidence;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-extent-screenshot-browser-visibility-contract-test")
public class McpExtentScreenshotBrowserVisibilityContractTest {

    @Test(groups = "mcp-final-report-closeout")
    @McpCheckRef("run.extent.screenshot.browser-visible")
    public void staticScreenshotVisibilityMatchesBrowserRendering() {
        String visibleHtml = htmlWithScreenshot("<a class=\"proof\" data-featherlight=\"image\" href=\"%s\">Open screenshot</a>");
        String hiddenHtml = htmlWithScreenshot("<div style=\"display:none\">"
                + "<a class=\"proof\" data-featherlight=\"image\" href=\"%s\">Hidden screenshot</a></div>");

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            assertVisibilityMatchesBrowser(browser, visibleHtml, true);
            assertVisibilityMatchesBrowser(browser, hiddenHtml, false);
        }
    }

    private static void assertVisibilityMatchesBrowser(Browser browser, String html, boolean expectedVisible) {
        List<McpExtentScreenshotEvidence.EmbeddedScreenshot> screenshots = McpExtentScreenshotEvidence.findEmbedded(html);
        Assert.assertEquals(screenshots.size(), 1, "Fixture must contain exactly one embedded screenshot media element.");
        boolean staticallyVisible = !screenshots.get(0).isHiddenIn(html);
        boolean browserVisible = browserSeesScreenshot(browser, html);

        Assert.assertEquals(staticallyVisible, expectedVisible,
                "Static MCP screenshot visibility should match the fixture expectation.");
        Assert.assertEquals(browserVisible, expectedVisible,
                "Chromium screenshot visibility should match the fixture expectation.");
        Assert.assertEquals(staticallyVisible, browserVisible,
                "Static MCP visibility and browser-real visibility must stay calibrated.");
    }

    private static boolean browserSeesScreenshot(Browser browser, String html) {
        try (Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1200, 800))) {
            page.setContent(html);
            return page.locator("a[href^='data:image/png;base64'], img[src^='data:image/png;base64']").first().isVisible();
        }
    }

    private static String htmlWithScreenshot(String bodyTemplate) {
        String image = "data:image/png;base64," + "A".repeat(900);
        return "<html><body><main>"
                + String.format(bodyTemplate, image)
                + "</main></body></html>";
    }
}
