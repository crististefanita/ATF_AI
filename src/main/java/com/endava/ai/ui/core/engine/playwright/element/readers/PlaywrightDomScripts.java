package com.endava.ai.ui.core.engine.playwright.element.readers;

final class PlaywrightDomScripts {
    static final String VISIBLE_TEXTS = "elements => elements"
            + ".filter(element => !!(element.offsetWidth || element.offsetHeight || element.getClientRects().length))"
            + ".map(element => element.innerText.trim())"
            + ".filter(text => text.length > 0)";

    private PlaywrightDomScripts() {
    }
}
