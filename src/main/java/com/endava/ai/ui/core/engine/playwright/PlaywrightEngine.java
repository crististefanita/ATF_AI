package com.endava.ai.ui.core.engine.playwright;

import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.endava.ai.ui.core.engine.playwright.element.PlaywrightElementActions;
import com.endava.ai.ui.core.engine.playwright.element.actions.PlaywrightVisibleClickWaiter;
import com.endava.ai.ui.core.engine.timing.UiEngineTiming;
import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.List;

public final class PlaywrightEngine implements UIEngine {
    public static final UIEngine.Capabilities CAPABILITIES = UIEngine.Capabilities.of(
            UiEngineType.PLAYWRIGHT.configValue(),
            UIEngine.Capability.AUTO_WAIT,
            UIEngine.Capability.VISIBLE_INDEX_CLICK,
            UIEngine.Capability.KEY_PRESS,
            UIEngine.Capability.FILE_UPLOAD,
            UIEngine.Capability.ATTRIBUTE_LOOKUP,
            UIEngine.Capability.NETWORK_RESPONSE_WAIT,
            UIEngine.Capability.BROWSER_ZOOM,
            UIEngine.Capability.SESSION_CLEAR,
            UIEngine.Capability.SCREENSHOT_CAPTURE,
            UIEngine.Capability.WINDOW_RESIZE
    );

    private final PlaywrightBrowserSession session;
    private final Page page;
    private final PlaywrightNetworkWaiter networkWaiter;
    private final PlaywrightVisibleClickWaiter visibleClickWaiter;
    private final PlaywrightScreenshotCapture screenshotCapture;
    private final PlaywrightZoomController zoomController;
    private final PlaywrightSessionCleaner sessionCleaner;
    private final PlaywrightElementActions elementActions;

    public PlaywrightEngine(UIWindowConfiguration windowConfiguration) {
        this(windowConfiguration, UiRuntimeOptions.load());
    }

    public PlaywrightEngine(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        boolean headless = runtimeOptions.isHeadless();
        this.session = new PlaywrightBrowserSession(windowConfiguration, runtimeOptions);
        this.page = session.page();
        int waitTimeoutSeconds = runtimeOptions.waitTimeoutSeconds();
        this.zoomController = new PlaywrightZoomController(page, windowConfiguration, runtimeOptions, headless);
        this.networkWaiter = new PlaywrightNetworkWaiter(page);
        this.sessionCleaner = new PlaywrightSessionCleaner(session.context(), page);
        this.elementActions = new PlaywrightElementActions(
                page,
                zoomController::reapplyHeadlessPageZoomFallback
        );
        this.visibleClickWaiter = new PlaywrightVisibleClickWaiter(
                page,
                waitTimeoutSeconds,
                () -> zoomController.reapplyHeadlessPageZoomFallback(UiZoomPhase.AFTER_VISIBLE_CLICK)
        );
        this.screenshotCapture = new PlaywrightScreenshotCapture(page);

        int timeoutMs = UiEngineTiming.secondsToMillis(waitTimeoutSeconds);
        page.setDefaultTimeout(timeoutMs);
        page.setDefaultNavigationTimeout(timeoutMs);
    }

    @Override
    public boolean supportsAutoWait() {
        return true;
    }

    @Override
    public UIEngine.Capabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public void setWindowSize(int width, int height) {
        session.setWindowSize(width, height);
    }

    @Override
    public void open(String url) {
        if (!zoomController.usesHeadlessZoomFallback()) {
            zoomController.ensureBrowserZoomConfigured();
        }
        zoomController.publishZoomMetadata(UiZoomPhase.BEFORE_NAVIGATION);
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        zoomController.reapplyHeadlessPageZoomFallback(UiZoomPhase.AFTER_NAVIGATION);
        session.assertSingleStartupTarget("after open");
    }

    @Override
    public void click(String cssSelector) {
        elementActions.click(cssSelector);
    }

    @Override
    public void clickVisible(String cssSelector, int oneBasedIndex) {
        visibleClickWaiter.click(cssSelector, oneBasedIndex);
    }

    @Override
    public void type(String cssSelector, String text) {
        elementActions.type(cssSelector, text);
    }

    @Override
    public void pressKey(String cssSelector, String key) {
        elementActions.pressKey(cssSelector, key);
    }

    @Override
    public void select(String cssSelector, String valueOrText) {
        elementActions.select(cssSelector, valueOrText);
    }

    @Override
    public void uploadFile(String cssSelector, String absolutePath) {
        elementActions.uploadFile(cssSelector, absolutePath);
    }

    @Override
    public String getText(String cssSelector) {
        return elementActions.getText(cssSelector);
    }

    @Override
    public List<String> getTexts(String cssSelector) {
        return elementActions.getTexts(cssSelector);
    }

    @Override
    public List<String> getVisibleTexts(String cssSelector) {
        return elementActions.getVisibleTexts(cssSelector);
    }

    @Override
    public String getValue(String cssSelector) {
        return elementActions.getValue(cssSelector);
    }

    @Override
    public String getAttribute(String cssSelector, String attributeName) {
        return elementActions.getAttribute(cssSelector, attributeName);
    }

    @Override
    public UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
        return networkWaiter.waitForResponse(urlContains, method, trigger);
    }

    @Override
    public boolean isVisible(String cssSelector) {
        return elementActions.isVisible(cssSelector);
    }

    @Override
    public boolean isEnabled(String cssSelector) {
        return elementActions.isEnabled(cssSelector);
    }

    @Override
    public void waitForVisible(String cssSelector, int seconds) {
        elementActions.waitForVisible(cssSelector, seconds);
    }

    @Override
    public void waitForUrlContains(String fragment, int seconds) {
        page.waitForURL(
                "**" + fragment + "**",
                new Page.WaitForURLOptions().setTimeout(UiEngineTiming.secondsToMillisDouble(seconds))
        );
        zoomController.reapplyHeadlessPageZoomFallback(UiZoomPhase.AFTER_URL_WAIT);
    }

    @Override
    public String getCurrentUrl() {
        return page.url();
    }

    @Override
    public void clearSession() {
        sessionCleaner.clearSession();
    }

    @Override
    public String captureScreenshotAsBase64() {
        zoomController.reapplyHeadlessPageZoomFallback(UiZoomPhase.BEFORE_SCREENSHOT);
        return screenshotCapture.captureFullPageAsBase64();
    }

    @Override
    public void quit() {
        session.close();
    }
}
