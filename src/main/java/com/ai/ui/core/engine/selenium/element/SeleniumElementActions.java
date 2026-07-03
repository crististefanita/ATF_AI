package com.ai.ui.core.engine.selenium.element;

import com.ai.ui.core.engine.timing.UiEngineTiming;
import com.ai.ui.core.engine.selenium.element.actions.SeleniumClickActions;
import com.ai.ui.core.engine.selenium.element.actions.SeleniumFormActions;
import com.ai.ui.core.engine.selenium.element.actions.SeleniumRetryPause;
import com.ai.ui.core.engine.selenium.element.actions.SeleniumTypingActions;
import com.ai.ui.core.engine.selenium.element.readers.SeleniumElementReader;
import com.ai.ui.core.engine.selenium.element.scripts.SeleniumScriptExecutor;
import com.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import com.ai.ui.core.engine.selenium.element.selectors.SeleniumVisibleElementFinder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public final class SeleniumElementActions {
    private final WebDriver driver;
    private final SeleniumSelectorResolver selectorResolver;
    private final SeleniumElementReader elementReader;
    private final SeleniumClickActions clickActions;
    private final SeleniumTypingActions typingActions;
    private final SeleniumFormActions formActions;

    public SeleniumElementActions(WebDriver driver, int timeoutSeconds, long retrySleepMillis) {
        this.driver = driver;
        this.selectorResolver = new SeleniumSelectorResolver(driver);
        SeleniumRetryPause retryPause = new SeleniumRetryPause(retrySleepMillis);
        SeleniumScriptExecutor scripts = new SeleniumScriptExecutor(driver);
        SeleniumVisibleElementFinder visibleElementFinder =
                new SeleniumVisibleElementFinder(driver, selectorResolver, timeoutSeconds);
        this.elementReader = new SeleniumElementReader(selectorResolver, retryPause);
        this.clickActions = new SeleniumClickActions(
                driver,
                selectorResolver,
                visibleElementFinder,
                retryPause,
                scripts,
                timeoutSeconds
        );
        this.typingActions = new SeleniumTypingActions(selectorResolver, retryPause, clickActions, scripts);
        this.formActions = new SeleniumFormActions(selectorResolver, clickActions, scripts);
    }

    public void click(String cssSelector) {
        clickActions.click(cssSelector);
    }

    public void clickVisible(String cssSelector, int oneBasedIndex) {
        clickActions.clickVisible(cssSelector, oneBasedIndex);
    }

    public void type(String cssSelector, String text) {
        typingActions.type(cssSelector, text);
    }

    public void pressKey(String cssSelector, String key) {
        typingActions.pressKey(cssSelector, key);
    }

    public void select(String cssSelector, String valueOrText) {
        formActions.select(cssSelector, valueOrText);
    }

    public void uploadFile(String cssSelector, String absolutePath) {
        formActions.uploadFile(cssSelector, absolutePath);
    }

    public String getText(String cssSelector) {
        return elementReader.getText(cssSelector);
    }

    public List<String> getTexts(String cssSelector) {
        return elementReader.getTexts(cssSelector);
    }

    public List<String> getVisibleTexts(String cssSelector) {
        return elementReader.getVisibleTexts(cssSelector);
    }

    public String getValue(String cssSelector) {
        return elementReader.getValue(cssSelector);
    }

    public String getAttribute(String cssSelector, String attributeName) {
        return elementReader.getAttribute(cssSelector, attributeName);
    }

    public boolean isVisible(String cssSelector) {
        return elementReader.isVisible(cssSelector);
    }

    public boolean isEnabled(String cssSelector) {
        return elementReader.isEnabled(cssSelector);
    }

    public void waitForVisible(String cssSelector, int seconds) {
        new WebDriverWait(driver, UiEngineTiming.durationSeconds(seconds)).until(driver -> {
            WebElement element = selectorResolver.first(cssSelector);
            return element.isDisplayed() ? element : null;
        });
    }
}
