package com.endava.ai.atf.core.reporting.support;

import com.endava.ai.ui.core.engine.UIEngine;

import java.util.Base64;

public final class ScreenshotPayloadUiEngine implements UIEngine {

    private final String payload;

    public ScreenshotPayloadUiEngine(String payload) {
        this.payload = Base64.getEncoder().encodeToString(payload.getBytes());
    }

    @Override public boolean supportsAutoWait() { return true; }
    @Override public void open(String url) {}
    @Override public void click(String cssSelector) {}
    @Override public void type(String cssSelector, String text) {}
    @Override public void select(String cssSelector, String valueOrText) {}
    @Override public String getText(String cssSelector) { return ""; }
    @Override public String getValue(String cssSelector) { return ""; }
    @Override public boolean isVisible(String cssSelector) { return true; }
    @Override public void waitForVisible(String cssSelector, int seconds) {}
    @Override public void waitForUrlContains(String fragment, int seconds) {}
    @Override public String getCurrentUrl() { return "about:blank"; }
    @Override public void clearSession() {}
    @Override public String captureScreenshotAsBase64() { return payload; }
    @Override public void quit() {}
    @Override public void setWindowSize(int width, int height) {}
}
