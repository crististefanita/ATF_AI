package com.ai.ui.core.engine.playwright;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

final class PlaywrightSessionCleaner {
    private static final String CLEAR_SESSION_SCRIPT = "() => { localStorage.clear(); sessionStorage.clear(); }";

    private final BrowserContext context;
    private final Page page;

    PlaywrightSessionCleaner(BrowserContext context, Page page) {
        this.context = context;
        this.page = page;
    }

    void clearSession() {
        context.clearCookies();
        try {
            page.evaluate(CLEAR_SESSION_SCRIPT);
        } catch (PlaywrightException ignored) {
        }
    }
}
