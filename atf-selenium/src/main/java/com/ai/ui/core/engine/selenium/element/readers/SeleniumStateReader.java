package com.ai.ui.core.engine.selenium.element.readers;

import com.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import org.openqa.selenium.NoSuchElementException;

final class SeleniumStateReader {
    private final SeleniumSelectorResolver selectorResolver;

    SeleniumStateReader(SeleniumSelectorResolver selectorResolver) {
        this.selectorResolver = selectorResolver;
    }

    boolean isVisible(String cssSelector) {
        try {
            return selectorResolver.first(cssSelector).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    boolean isEnabled(String cssSelector) {
        try {
            return selectorResolver.first(cssSelector).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
