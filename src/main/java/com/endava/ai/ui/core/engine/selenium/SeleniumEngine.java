package com.endava.ai.ui.core.engine.selenium;

import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.timing.UiEngineTiming;
import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.endava.ai.ui.core.engine.selenium.element.SeleniumElementActions;
import com.endava.ai.ui.core.engine.selenium.network.SeleniumNetworkWaiter;
import com.endava.ai.ui.core.engine.window.UIWindowConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public final class SeleniumEngine implements UIEngine {
    public static final UIEngine.Capabilities CAPABILITIES = UIEngine.Capabilities.of(
            UiEngineType.SELENIUM.configValue(),
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

    private static final long RETRY_SLEEP_MILLIS = 150L;

    private final SeleniumBrowserSession session;
    private final WebDriver driver;
    private final SeleniumZoomController zoomController;
    private final SeleniumSessionCleaner sessionCleaner;
    private final SeleniumScreenshotCapture screenshotCapture;
    private final SeleniumElementActions elementActions;
    private final SeleniumNetworkWaiter networkWaiter;

    public SeleniumEngine(UIWindowConfiguration windowConfiguration) {
        this(windowConfiguration, UiRuntimeOptions.load());
    }

    public SeleniumEngine(UIWindowConfiguration windowConfiguration, UiRuntimeOptions runtimeOptions) {
        this.session = new SeleniumBrowserSession(windowConfiguration, runtimeOptions);
        this.driver = session.driver();
        this.zoomController = new SeleniumZoomController(driver, windowConfiguration, runtimeOptions);
        this.sessionCleaner = new SeleniumSessionCleaner(driver);
        this.screenshotCapture = new SeleniumScreenshotCapture(driver);
        this.networkWaiter = new SeleniumNetworkWaiter(driver, runtimeOptions.waitTimeoutSeconds());
        this.elementActions = new SeleniumElementActions(
                driver,
                runtimeOptions.waitTimeoutSeconds(),
                RETRY_SLEEP_MILLIS
        );
    }

    @Override
    public boolean supportsAutoWait() { return false; }

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
        zoomController.publishBeforeNavigationMetadata();
        driver.get(url);
        zoomController.reapplyHeadlessPageZoomFallbackAfterNavigation();
        session.assertSingleStartupTarget("after open");
    }

    @Override
    public void click(String cssSelector) {
        elementActions.click(cssSelector);
    }

    @Override
    public void clickVisible(String cssSelector, int oneBasedIndex) {
        elementActions.clickVisible(cssSelector, oneBasedIndex);
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
        wait(seconds).until(ExpectedConditions.urlContains(fragment));
        zoomController.reapplyHeadlessPageZoomFallbackAfterUrlWait();
    }

    @Override
    public UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
        return networkWaiter.waitForResponse(urlContains, method, trigger);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public void clearSession() {
        sessionCleaner.clearSession();
    }

    @Override
    public String captureScreenshotAsBase64() {
        zoomController.reapplyHeadlessPageZoomFallbackBeforeScreenshot();
        return screenshotCapture.captureAsBase64();
    }

    @Override
    public void quit() {
        session.close();
    }

    private WebDriverWait wait(int seconds) {
        return new WebDriverWait(driver, UiEngineTiming.durationSeconds(seconds));
    }
}
