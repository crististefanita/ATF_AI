package com.ai.ui.core.engine.playwright;

import com.ai.ui.core.engine.UiNetworkResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;

final class PlaywrightNetworkWaiter {
    private final Page page;

    PlaywrightNetworkWaiter(Page page) {
        this.page = page;
    }

    UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
        Response response = page.waitForResponse(
                candidate -> candidate.url().contains(urlContains)
                        && method.equalsIgnoreCase(candidate.request().method()),
                trigger
        );
        return new UiNetworkResponse(
                response.url(),
                response.request().method(),
                response.status(),
                response.request().postData(),
                responseBodyPreview(response)
        );
    }

    private static String responseBodyPreview(Response response) {
        try {
            return response.text();
        } catch (PlaywrightException ignored) {
            return "";
        }
    }
}
