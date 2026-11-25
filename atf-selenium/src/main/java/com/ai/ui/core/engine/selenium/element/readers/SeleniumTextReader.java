package com.ai.ui.core.engine.selenium.element.readers;

import com.ai.ui.core.engine.selenium.element.actions.SeleniumRetryPause;
import com.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class SeleniumTextReader {
    private static final int TEXT_READ_ATTEMPTS = 4;

    private final SeleniumSelectorResolver selectorResolver;
    private final SeleniumRetryPause retryPause;

    SeleniumTextReader(SeleniumSelectorResolver selectorResolver, SeleniumRetryPause retryPause) {
        this.selectorResolver = selectorResolver;
        this.retryPause = retryPause;
    }

    String getText(String cssSelector) {
        if ("body".equalsIgnoreCase(cssSelector.trim())) {
            return selectorResolver.documentBodyText();
        }
        for (int attempt = 0; attempt < TEXT_READ_ATTEMPTS; attempt++) {
            try {
                List<WebElement> elements = selectorResolver.findAll(cssSelector);
                if (elements.isEmpty()) {
                    return "";
                }
                if (elements.size() == 1) {
                    return elements.get(0).getText();
                }
                return joinedVisibleText(elements);
            } catch (StaleElementReferenceException stale) {
                if (attempt == TEXT_READ_ATTEMPTS - 1) {
                    throw stale;
                }
                retryPause.sleepBriefly();
            }
        }
        return "";
    }

    List<String> getTexts(String cssSelector) {
        return readTexts(cssSelector, false);
    }

    List<String> getVisibleTexts(String cssSelector) {
        return readTexts(cssSelector, true);
    }

    private List<String> readTexts(String cssSelector, boolean visibleOnly) {
        for (int attempt = 0; attempt < TEXT_READ_ATTEMPTS; attempt++) {
            try {
                List<String> values = new ArrayList<>();
                for (WebElement element : selectorResolver.findAll(cssSelector)) {
                    if (visibleOnly && !element.isDisplayed()) {
                        continue;
                    }
                    String text = element.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        values.add(text.trim());
                    }
                }
                return values;
            } catch (StaleElementReferenceException stale) {
                if (attempt == TEXT_READ_ATTEMPTS - 1) {
                    throw stale;
                }
                retryPause.sleepBriefly();
            }
        }
        return List.of();
    }

    private static String joinedVisibleText(List<WebElement> elements) {
        return elements.stream()
                .map(WebElement::getText)
                .filter(text -> text != null && !text.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining("\n"));
    }
}
