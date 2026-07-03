package com.endava.ai.ui.core.engine.playwright.element;

import com.endava.ai.ui.core.engine.playwright.element.actions.PlaywrightClickActions;
import com.endava.ai.ui.core.engine.playwright.element.actions.PlaywrightFormActions;
import com.endava.ai.ui.core.engine.playwright.element.actions.PlaywrightTypingActions;
import com.endava.ai.ui.core.engine.playwright.element.readers.PlaywrightReadActions;
import com.endava.ai.ui.core.engine.playwright.element.selectors.PlaywrightLocatorResolver;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.function.Consumer;

public final class PlaywrightElementActions {
    private final PlaywrightLocatorResolver locatorResolver;
    private final PlaywrightClickActions clickActions;
    private final PlaywrightTypingActions typingActions;
    private final PlaywrightFormActions formActions;
    private final PlaywrightReadActions readActions;

    public PlaywrightElementActions(Page page, Consumer<UiZoomPhase> afterAction) {
        this.locatorResolver = new PlaywrightLocatorResolver(page);
        this.clickActions = new PlaywrightClickActions(locatorResolver, afterAction);
        this.typingActions = new PlaywrightTypingActions(afterAction);
        this.formActions = new PlaywrightFormActions(locatorResolver, afterAction);
        this.readActions = new PlaywrightReadActions(page, locatorResolver);
    }

    public void click(String cssSelector) {
        clickActions.click(cssSelector);
    }

    public void type(String cssSelector, String text) {
        typingActions.type(locatorResolver.first(cssSelector), text);
    }

    public void pressKey(String cssSelector, String key) {
        typingActions.pressKey(locatorResolver.first(cssSelector), key);
    }

    public void select(String cssSelector, String valueOrText) {
        formActions.select(cssSelector, valueOrText);
    }

    public void uploadFile(String cssSelector, String absolutePath) {
        formActions.uploadFile(cssSelector, absolutePath);
    }

    public String getText(String cssSelector) {
        return readActions.getText(cssSelector);
    }

    public List<String> getTexts(String cssSelector) {
        return readActions.getTexts(cssSelector);
    }

    public List<String> getVisibleTexts(String cssSelector) {
        return readActions.getVisibleTexts(cssSelector);
    }

    public String getValue(String cssSelector) {
        return readActions.getValue(cssSelector);
    }

    public String getAttribute(String cssSelector, String attributeName) {
        return readActions.getAttribute(cssSelector, attributeName);
    }

    public boolean isVisible(String cssSelector) {
        return readActions.isVisible(cssSelector);
    }

    public boolean isEnabled(String cssSelector) {
        return readActions.isEnabled(cssSelector);
    }

    public void waitForVisible(String cssSelector, int seconds) {
        locatorResolver.waitForVisible(cssSelector, seconds);
    }
}
