package com.endava.ai.ui.core.engine.selenium.element.actions;

import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumDomScripts;
import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumScriptExecutor;
import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

public final class SeleniumTypingActions {
    private static final int TYPE_ATTEMPTS = 2;
    private static final int FILL_STABILITY_POLLS = 5;

    private final SeleniumSelectorResolver selectorResolver;
    private final SeleniumRetryPause retryPause;
    private final SeleniumClickActions clickActions;
    private final SeleniumScriptExecutor scripts;

    public SeleniumTypingActions(
            SeleniumSelectorResolver selectorResolver,
            SeleniumRetryPause retryPause,
            SeleniumClickActions clickActions,
            SeleniumScriptExecutor scripts
    ) {
        this.selectorResolver = selectorResolver;
        this.retryPause = retryPause;
        this.clickActions = clickActions;
        this.scripts = scripts;
    }

    public void type(String cssSelector, String text) {
        for (int attempt = 0; attempt < TYPE_ATTEMPTS; attempt++) {
            try {
                WebElement element = selectorResolver.first(cssSelector);
                clickActions.scrollIntoView(element);
                fillLikePlaywright(element, text);
                return;
            } catch (StaleElementReferenceException stale) {
                if (attempt == TYPE_ATTEMPTS - 1) {
                    throw stale;
                }
                retryPause.sleepBriefly();
            }
        }
    }

    public void pressKey(String cssSelector, String key) {
        WebElement element = selectorResolver.first(cssSelector);
        clickActions.scrollIntoView(element);
        clickActions.focus(element);
        element.sendKeys(SeleniumKeyboard.map(key));
    }

    private void fillLikePlaywright(WebElement element, String text) {
        String targetText = text != null ? text : "";
        if (isValueBackedField(element)) {
            if (targetText.equals(currentValue(element))) {
                return;
            }
            replaceValue(element, targetText);
            return;
        }
        clickActions.clickWithFallback(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.DELETE);
        if (!currentValue(element).isEmpty()) {
            element.clear();
        }
        if (!targetText.isEmpty()) {
            element.sendKeys(targetText);
        }
        dispatchInput(element);
        ensureFilledValue(element, targetText);
    }

    private void replaceValue(WebElement element, String targetText) {
        clickActions.clickWithFallback(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.DELETE);
        if (!targetText.isEmpty()) {
            element.sendKeys(targetText);
        }
        dispatchInput(element);
        ensureFilledValue(element, targetText);
    }

    private void ensureFilledValue(WebElement element, String targetText) {
        if (!targetText.equals(currentValue(element))) {
            setValueWithEvents(element, targetText);
        }
        stabilizeFilledValue(element, targetText);
    }

    private void stabilizeFilledValue(WebElement element, String targetText) {
        for (int poll = 0; poll < FILL_STABILITY_POLLS; poll++) {
            retryPause.sleepBriefly();
            if (!targetText.equals(currentValue(element))) {
                setValueWithEvents(element, targetText);
            }
        }
    }

    private static boolean isValueBackedField(WebElement element) {
        String tagName = element.getTagName();
        return "input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName);
    }

    private static String currentValue(WebElement element) {
        String value = element.getDomProperty("value");
        return value != null ? value : "";
    }

    private void dispatchInput(WebElement element) {
        scripts.tryExecute(SeleniumDomScripts.DISPATCH_INPUT_EVENTS, element);
    }

    private void setValueWithEvents(WebElement element, String text) {
        scripts.tryExecute(SeleniumDomScripts.SET_VALUE, element, text);
    }
}
