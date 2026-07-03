package com.endava.ai.ui.core.engine.playwright;

import com.microsoft.playwright.Page;

import java.util.Base64;

final class PlaywrightScreenshotCapture {
    private final Page page;

    PlaywrightScreenshotCapture(Page page) {
        this.page = page;
    }

    String captureFullPageAsBase64() {
        byte[] bytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        return Base64.getEncoder().encodeToString(bytes);
    }
}
