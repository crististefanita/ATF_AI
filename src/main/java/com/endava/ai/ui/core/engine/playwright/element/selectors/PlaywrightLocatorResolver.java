package com.endava.ai.ui.core.engine.playwright.element.selectors;

import com.endava.ai.ui.core.engine.syntax.UiSelectorSyntax;
import com.endava.ai.ui.core.engine.timing.UiEngineTiming;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

public final class PlaywrightLocatorResolver {
    private final Page page;

    public PlaywrightLocatorResolver(Page page) {
        this.page = page;
    }

    public Locator first(String cssSelector) {
        return firstVisibleOrFirst(cssSelector);
    }

    public Locator visible(String cssSelector) {
        if (UiSelectorSyntax.isNonCssSelector(cssSelector) || UiSelectorSyntax.hasVisiblePseudo(cssSelector)) {
            return page.locator(cssSelector);
        }
        try {
            return page.locator(UiSelectorSyntax.withVisiblePseudo(cssSelector));
        } catch (PlaywrightException ignored) {
            return page.locator(cssSelector);
        }
    }

    public void waitForVisible(String cssSelector, int seconds) {
        firstVisibleOrFirst(cssSelector).waitFor(new Locator.WaitForOptions()
                .setTimeout(UiEngineTiming.secondsToMillisDouble(seconds))
                .setState(WaitForSelectorState.VISIBLE));
    }

    private Locator firstVisibleOrFirst(String cssSelector) {
        Locator visible = visible(cssSelector);
        if (visible.count() > 0) {
            return visible.first();
        }
        return page.locator(cssSelector).first();
    }
}
