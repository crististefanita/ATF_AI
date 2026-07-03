package com.endava.ai.ui.support.actions;

import com.endava.ai.ui.core.runtime.DriverManager;

import java.util.List;

final class UiReadActions {
    private UiReadActions() {
    }

    static String getText(String cssSelector, String description) {
        return UiActionStep.call(
                "Get text from: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    String result = DriverManager.getEngine().getText(cssSelector);
                    UiActionStep.detail("text=" + result);
                    return result;
                },
                "Text captured",
                "Failed getting text from: " + description
        );
    }

    static String getValue(String cssSelector, String description) {
        return UiActionStep.call(
                "Get value from: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    String result = DriverManager.getEngine().getValue(cssSelector);
                    UiActionStep.detail(UiActionStep.DETAIL_VALUE + result);
                    return result;
                },
                "Value captured",
                "Failed getting value from: " + description
        );
    }

    static List<String> getTexts(String cssSelector, String description) {
        return UiActionStep.call(
                "Get texts from: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    List<String> result = DriverManager.getEngine().getTexts(cssSelector);
                    UiActionStep.detail("texts=" + result);
                    return result;
                },
                "Texts captured",
                "Failed getting texts from: " + description
        );
    }

    static List<String> getVisibleTexts(String cssSelector, String description) {
        return UiActionStep.call(
                "Get visible texts from: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    List<String> result = DriverManager.getEngine().getVisibleTexts(cssSelector);
                    UiActionStep.detail("visibleTexts=" + result);
                    return result;
                },
                "Visible texts captured",
                "Failed getting visible texts from: " + description
        );
    }

    static String getAttribute(String cssSelector, String description, String attributeName) {
        return UiActionStep.call(
                "Get attribute from: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail("attribute=" + attributeName);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    String result = DriverManager.getEngine().getAttribute(cssSelector, attributeName);
                    UiActionStep.detail("attributeValue=" + result);
                    return result;
                },
                "Attribute captured",
                "Failed getting attribute from: " + description
        );
    }
}
