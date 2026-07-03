package com.endava.ai.ui.core.engine.playwright.element.actions;

import com.endava.ai.ui.core.engine.playwright.element.selectors.PlaywrightLocatorResolver;
import com.endava.ai.ui.core.engine.zoom.UiZoomPhase;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class PlaywrightFormActions {
    private final PlaywrightLocatorResolver locatorResolver;
    private final Consumer<UiZoomPhase> afterAction;

    public PlaywrightFormActions(PlaywrightLocatorResolver locatorResolver, Consumer<UiZoomPhase> afterAction) {
        this.locatorResolver = locatorResolver;
        this.afterAction = afterAction;
    }

    public void select(String cssSelector, String valueOrText) {
        Locator locator = locatorResolver.first(cssSelector);
        for (PlaywrightSelectStrategy strategy : PlaywrightSelectStrategy.values()) {
            if (strategy.select(locator, valueOrText)) {
                afterAction.accept(UiZoomPhase.AFTER_SELECT);
                return;
            }
        }

        throw new PlaywrightException("No option found for: " + valueOrText);
    }

    public void uploadFile(String cssSelector, String absolutePath) {
        locatorResolver.first(cssSelector).setInputFiles(Path.of(absolutePath));
    }
}
