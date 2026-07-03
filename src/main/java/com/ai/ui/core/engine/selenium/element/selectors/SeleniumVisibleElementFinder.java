package com.ai.ui.core.engine.selenium.element.selectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public final class SeleniumVisibleElementFinder {
    private final WebDriver driver;
    private final SeleniumSelectorResolver selectorResolver;
    private final int timeoutSeconds;

    public SeleniumVisibleElementFinder(WebDriver driver, SeleniumSelectorResolver selectorResolver, int timeoutSeconds) {
        this.driver = driver;
        this.selectorResolver = selectorResolver;
        this.timeoutSeconds = timeoutSeconds;
    }

    public WebElement visibleElementAt(String selector, int oneBasedIndex) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(ignored -> {
            List<WebElement> visibleElements = selectorResolver.findAll(selector).stream()
                    .filter(WebElement::isDisplayed)
                    .collect(Collectors.toList());
            return visibleElements.size() >= oneBasedIndex
                    ? visibleElements.get(oneBasedIndex - 1)
                    : null;
        });
    }
}
