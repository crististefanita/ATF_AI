package com.endava.ai.ui.core.engine.selenium.element.readers;

import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import org.openqa.selenium.WebElement;

final class SeleniumValueReader {
    private final SeleniumSelectorResolver selectorResolver;

    SeleniumValueReader(SeleniumSelectorResolver selectorResolver) {
        this.selectorResolver = selectorResolver;
    }

    String getValue(String cssSelector) {
        WebElement element = selectorResolver.first(cssSelector);
        String value = element.getDomProperty("value");
        return value != null ? value : element.getAttribute("value");
    }

    String getAttribute(String cssSelector, String attributeName) {
        return selectorResolver.first(cssSelector).getAttribute(attributeName);
    }
}
