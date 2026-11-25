package com.ai.ui.core.engine.selenium.element.selectors;

import com.ai.ui.core.engine.syntax.UiSelectorSyntax;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public final class SeleniumSelectorResolver {
    private final WebDriver driver;

    public SeleniumSelectorResolver(WebDriver driver) {
        this.driver = driver;
    }

    public List<WebElement> findAll(String selector) {
        if (UiSelectorSyntax.isXpath(selector)) {
            return driver.findElements(By.xpath(UiSelectorSyntax.xpathExpression(selector)));
        }
        Object raw = ((JavascriptExecutor) driver).executeScript(SeleniumSelectorScripts.FIND_MATCHES, selector);
        if (!(raw instanceof List<?>)) {
            return List.of();
        }
        List<WebElement> elements = new ArrayList<>();
        for (Object item : (List<?>) raw) {
            if (item instanceof WebElement) {
                elements.add((WebElement) item);
            }
        }
        return elements;
    }

    public WebElement first(String selector) {
        List<WebElement> matches = findAll(selector);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("No element found for selector: " + selector);
        }

        for (WebElement match : matches) {
            if (match.isDisplayed() && isHitTestVisible(match)) {
                return match;
            }
        }

        for (WebElement match : matches) {
            if (match.isDisplayed()) {
                return match;
            }
        }

        return matches.get(0);
    }

    public String documentBodyText() {
        Object raw = ((JavascriptExecutor) driver).executeScript(SeleniumSelectorScripts.BODY_TEXT);
        return raw == null ? "" : raw.toString();
    }

    private boolean isHitTestVisible(WebElement element) {
        Object raw = ((JavascriptExecutor) driver).executeScript(SeleniumSelectorScripts.HIT_TEST_VISIBLE, element);
        return Boolean.TRUE.equals(raw);
    }
}
