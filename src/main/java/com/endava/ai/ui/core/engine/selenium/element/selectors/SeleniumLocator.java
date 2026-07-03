package com.endava.ai.ui.core.engine.selenium.element.selectors;

import com.endava.ai.ui.core.engine.syntax.UiSelectorSyntax;
import org.openqa.selenium.By;

final class SeleniumLocator {
    private SeleniumLocator() {
    }

    static By by(String selector) {
        if (UiSelectorSyntax.isXpath(selector)) {
            return By.xpath(UiSelectorSyntax.xpathExpression(selector));
        }
        return By.cssSelector(selector);
    }
}
