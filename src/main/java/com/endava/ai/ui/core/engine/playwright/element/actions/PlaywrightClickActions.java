package com.endava.ai.ui.core.engine.playwright.element.actions;

import com.endava.ai.ui.core.engine.playwright.element.selectors.PlaywrightLocatorResolver;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;

import java.util.function.Consumer;

public final class PlaywrightClickActions {
    private final PlaywrightLocatorResolver locatorResolver;
    private final Consumer<UiZoomPhase> afterAction;

    public PlaywrightClickActions(PlaywrightLocatorResolver locatorResolver, Consumer<UiZoomPhase> afterAction) {
        this.locatorResolver = locatorResolver;
        this.afterAction = afterAction;
    }

    public void click(String cssSelector) {
        Locator locator = locatorResolver.first(cssSelector);
        locator.scrollIntoViewIfNeeded();
        try {
            locator.click();
        } catch (PlaywrightException firstFailure) {
            locator.click(new Locator.ClickOptions().setForce(true));
        } finally {
            afterAction.accept(UiZoomPhase.AFTER_CLICK);
        }
    }
}
