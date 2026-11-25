package com.ai.ui.core.engine.playwright.element.readers;

import com.ai.ui.core.engine.playwright.element.selectors.PlaywrightLocatorResolver;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class PlaywrightReadActions {
    private final Page page;
    private final PlaywrightLocatorResolver locatorResolver;

    public PlaywrightReadActions(Page page, PlaywrightLocatorResolver locatorResolver) {
        this.page = page;
        this.locatorResolver = locatorResolver;
    }

    public String getText(String cssSelector) {
        Locator locator = page.locator(cssSelector);
        if (locator.count() == 0) {
            return "";
        }
        return locator.allInnerTexts().stream()
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    public List<String> getTexts(String cssSelector) {
        return page.locator(cssSelector).allInnerTexts().stream()
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getVisibleTexts(String cssSelector) {
        Object raw = page.locator(cssSelector).evaluateAll(PlaywrightDomScripts.VISIBLE_TEXTS);
        if (!(raw instanceof List<?>)) {
            return Collections.emptyList();
        }
        return ((List<?>) raw).stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public String getValue(String cssSelector) {
        return locatorResolver.first(cssSelector).inputValue();
    }

    public String getAttribute(String cssSelector, String attributeName) {
        return locatorResolver.first(cssSelector).getAttribute(attributeName);
    }

    public boolean isVisible(String cssSelector) {
        return locatorResolver.visible(cssSelector).count() > 0;
    }

    public boolean isEnabled(String cssSelector) {
        return locatorResolver.first(cssSelector).isEnabled();
    }
}
