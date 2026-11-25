package com.ai.core.reporting.adapters.extent.patch.model;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ExtentHtmlDocument {
    private final String html;

    private ExtentHtmlDocument(String html) {
        this.html = html == null ? "" : html;
    }

    public static ExtentHtmlDocument of(String html) {
        return new ExtentHtmlDocument(html);
    }

    public boolean contains(String token) {
        return token != null && !token.isBlank() && html.contains(token);
    }

    public boolean blank() {
        return html.isBlank();
    }
}
