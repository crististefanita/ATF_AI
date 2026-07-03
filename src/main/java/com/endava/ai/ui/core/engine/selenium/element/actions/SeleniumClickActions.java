package com.endava.ai.ui.core.engine.selenium.element.actions;

import com.endava.ai.ui.core.engine.timing.UiEngineTiming;
import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumDomScripts;
import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumScriptExecutor;
import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumVisibleElementFinder;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class SeleniumClickActions {
    private static final int CLICK_ATTEMPTS = 3;

    private final WebDriver driver;
    private final SeleniumSelectorResolver selectorResolver;
    private final SeleniumVisibleElementFinder visibleElementFinder;
    private final SeleniumRetryPause retryPause;
    private final SeleniumScriptExecutor scripts;
    private final int timeoutSeconds;

    public SeleniumClickActions(
            WebDriver driver,
            SeleniumSelectorResolver selectorResolver,
            SeleniumVisibleElementFinder visibleElementFinder,
            SeleniumRetryPause retryPause,
            SeleniumScriptExecutor scripts,
            int timeoutSeconds
    ) {
        this.driver = driver;
        this.selectorResolver = selectorResolver;
        this.visibleElementFinder = visibleElementFinder;
        this.retryPause = retryPause;
        this.scripts = scripts;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void click(String cssSelector) {
        for (int attempt = 0; attempt < CLICK_ATTEMPTS; attempt++) {
            try {
                clickWithFallback(waitForClickable(cssSelector));
                return;
            } catch (StaleElementReferenceException stale) {
                if (attempt == CLICK_ATTEMPTS - 1) {
                    throw stale;
                }
                retryPause.sleepBriefly();
            }
        }
    }

    public void clickVisible(String cssSelector, int oneBasedIndex) {
        if (oneBasedIndex < 1) {
            throw new IllegalArgumentException("Visible click index is 1-based.");
        }
        clickWithFallback(visibleElementFinder.visibleElementAt(cssSelector, oneBasedIndex));
    }

    void clickWithFallback(WebElement element) {
        scrollIntoView(element);
        focus(element);
        try {
            new Actions(driver).moveToElement(element).click().perform();
            return;
        } catch (WebDriverException actionFailure) {
            try {
                element.click();
                return;
            } catch (WebDriverException nativeClickFailure) {
                scripts.tryExecute(SeleniumDomScripts.CLICK, element);
            }
        }
    }

    void scrollIntoView(WebElement element) {
        scripts.tryExecute(SeleniumDomScripts.SCROLL_INTO_VIEW, element);
    }

    void focus(WebElement element) {
        scripts.tryExecute(SeleniumDomScripts.FOCUS, element);
    }

    private WebElement waitForClickable(String cssSelector) {
        return new WebDriverWait(driver, UiEngineTiming.durationSeconds(timeoutSeconds)).until(driver -> {
            WebElement element = selectorResolver.first(cssSelector);
            return isInteractable(element) ? element : null;
        });
    }

    private static boolean isInteractable(WebElement element) {
        if (!element.isDisplayed()) {
            return false;
        }
        return !requiresEnabledState(element) || element.isEnabled();
    }

    private static boolean requiresEnabledState(WebElement element) {
        String tagName = element.getTagName();
        return "button".equalsIgnoreCase(tagName)
                || "input".equalsIgnoreCase(tagName)
                || "select".equalsIgnoreCase(tagName)
                || "textarea".equalsIgnoreCase(tagName)
                || "option".equalsIgnoreCase(tagName);
    }
}
