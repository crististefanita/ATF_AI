package com.endava.ai.ui.core.engine;

import lombok.Getter;

import java.util.Locale;

@Getter
public final class UiNetworkResponse {
    private static final int MAX_BODY_PREVIEW_CHARS = 2_000;

    private final String url;
    private final String method;
    private final int status;
    private final String requestPreview;
    private final String bodyPreview;

    public UiNetworkResponse(String url, String method, int status, String body) {
        this(url, method, status, "", body);
    }

    public UiNetworkResponse(String url, String method, int status, String requestBody, String body) {
        this.url = valueOrEmpty(url);
        this.method = valueOrEmpty(method).toUpperCase(Locale.ROOT);
        this.status = status;
        this.requestPreview = boundedPreview(requestBody);
        this.bodyPreview = boundedPreview(body);
    }

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public boolean urlContains(String expectedFragment) {
        return url.contains(valueOrEmpty(expectedFragment));
    }

    private static String boundedPreview(String body) {
        String normalized = valueOrEmpty(body)
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.length() <= MAX_BODY_PREVIEW_CHARS) {
            return normalized;
        }
        return normalized.substring(0, MAX_BODY_PREVIEW_CHARS) + "...";
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
