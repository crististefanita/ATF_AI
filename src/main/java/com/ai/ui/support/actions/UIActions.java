package com.ai.ui.support.actions;

import com.ai.ui.core.engine.UiNetworkResponse;

import java.util.List;

public final class UIActions {
    private UIActions() {
    }

    public static void navigateToRelative(String relativePath) {
        UiNavigationActions.navigateToRelative(relativePath);
    }

    public static void openRelativeWithoutUrlWait(String relativePath) {
        UiNavigationActions.openRelativeWithoutUrlWait(relativePath);
    }

    public static void click(String cssSelector, String description) {
        UiInteractionActions.click(cssSelector, description);
    }

    public static void clickVisible(String cssSelector, String description, int oneBasedIndex) {
        UiInteractionActions.clickVisible(cssSelector, description, oneBasedIndex);
    }

    public static void type(String cssSelector, String description, String value) {
        UiInteractionActions.type(cssSelector, description, value);
    }

    public static void pressKey(String cssSelector, String description, String key) {
        UiInteractionActions.pressKey(cssSelector, description, key);
    }

    @SuppressWarnings("unused")
    public static void select(String cssSelector, String description, String value) {
        UiInteractionActions.select(cssSelector, description, value);
    }

    public static void uploadFile(String cssSelector, String description, String path) {
        UiInteractionActions.uploadFile(cssSelector, description, path);
    }

    public static String getText(String cssSelector, String description) {
        return UiReadActions.getText(cssSelector, description);
    }

    public static String getValue(String cssSelector, String description) {
        return UiReadActions.getValue(cssSelector, description);
    }

    public static List<String> getTexts(String cssSelector, String description) {
        return UiReadActions.getTexts(cssSelector, description);
    }

    public static List<String> getVisibleTexts(String cssSelector, String description) {
        return UiReadActions.getVisibleTexts(cssSelector, description);
    }

    public static String getAttribute(String cssSelector, String description, String attributeName) {
        return UiReadActions.getAttribute(cssSelector, description, attributeName);
    }

    public static UiNetworkResponse waitForResponse(String description, String urlContains, String method, Runnable trigger) {
        return UiNetworkActions.waitForResponse(description, urlContains, method, trigger);
    }

    public static void clearSession() {
        UiInteractionActions.clearSession();
    }
}
