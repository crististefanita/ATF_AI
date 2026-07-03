package com.endava.ai.ui.support.actions;

import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.support.wait.WaitUtils;

final class UiNavigationActions {
    private static final String BASE_URL_DETAIL = "base.url=";
    private static final String RELATIVE_PATH_DETAIL = "relativePath=";

    private UiNavigationActions() {
    }

    static void navigateToRelative(String relativePath) {
        String baseUrl = ConfigManager.require("base.url");
        String fullUrl = baseUrl + relativePath;

        UiActionStep.run(
                "Navigate to URL: " + fullUrl,
                () -> {
                    UiActionStep.detail(BASE_URL_DETAIL + baseUrl);
                    UiActionStep.detail(RELATIVE_PATH_DETAIL + relativePath);
                    DriverManager.getEngine().open(fullUrl);
                    WaitUtils.waitForUrlContains(relativePath);
                },
                "Navigated",
                "Failed navigating to: " + fullUrl
        );
    }

    static void openRelativeWithoutUrlWait(String relativePath) {
        String baseUrl = ConfigManager.require("base.url");
        String fullUrl = baseUrl + relativePath;

        UiActionStep.run(
                "Open URL: " + fullUrl,
                () -> {
                    UiActionStep.detail(BASE_URL_DETAIL + baseUrl);
                    UiActionStep.detail(RELATIVE_PATH_DETAIL + relativePath);
                    DriverManager.getEngine().open(fullUrl);
                },
                "Opened",
                "Failed opening: " + fullUrl
        );
    }
}
