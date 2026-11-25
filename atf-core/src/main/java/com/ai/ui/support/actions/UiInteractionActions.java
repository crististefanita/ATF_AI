package com.ai.ui.support.actions;

import com.ai.ui.core.runtime.DriverManager;

import java.nio.file.Path;
import java.util.regex.Pattern;

final class UiInteractionActions {
    private static final Pattern SENSITIVE_INPUT = Pattern.compile(
            "(?:^|[^a-z0-9])(?:password|passwd|pwd|secret|token|credential|credentials|passcode|pin)(?:$|[^a-z0-9])",
            Pattern.CASE_INSENSITIVE);

    private UiInteractionActions() {
    }

    static void click(String cssSelector, String description) {
        UiActionStep.run(
                "Click on: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().click(cssSelector);
                },
                "Clicked",
                "Failed clicking: " + description
        );
    }

    static void clickVisible(String cssSelector, String description, int oneBasedIndex) {
        UiActionStep.run(
                "Click visible match on: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail("visibleIndex=" + oneBasedIndex);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().clickVisible(cssSelector, oneBasedIndex);
                },
                "Clicked visible match",
                "Failed clicking visible match: " + description
        );
    }

    static void type(String cssSelector, String description, String value) {
        UiActionStep.run(
                "Type into element: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail(UiActionStep.DETAIL_VALUE
                            + (sensitiveInput(cssSelector, description) ? "[REDACTED]" : value));
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().type(cssSelector, value);
                },
                "Typed",
                "Failed typing into: " + description
        );
    }

    static void pressKey(String cssSelector, String description, String key) {
        UiActionStep.run(
                "Press key on element: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail("key=" + key);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().pressKey(cssSelector, key);
                },
                "Key pressed",
                "Failed pressing key on: " + description
        );
    }

    private static boolean sensitiveInput(String cssSelector, String description) {
        String hint = (description + " " + cssSelector)
                .replaceAll("([A-Z])([A-Z][a-z])", "$1 $2")
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return SENSITIVE_INPUT.matcher(hint).find();
    }

    static void select(String cssSelector, String description, String value) {
        UiActionStep.run(
                "Select option: " + description,
                () -> {
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail(UiActionStep.DETAIL_VALUE + value);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().select(cssSelector, value);
                },
                "Selected",
                "Failed selecting: " + description
        );
    }

    static void uploadFile(String cssSelector, String description, String path) {
        UiActionStep.run(
                "Upload file into element: " + description,
                () -> {
                    String absolutePath = Path.of(path).toAbsolutePath().toString();
                    UiActionStep.detail(UiActionStep.DETAIL_LOCATOR + cssSelector);
                    UiActionStep.detail("path=" + absolutePath);
                    UiActionWait.visibleIfEngineRequires(cssSelector);
                    DriverManager.getEngine().uploadFile(cssSelector, absolutePath);
                },
                "Uploaded",
                "Failed uploading file into: " + description
        );
    }

    static void clearSession() {
        UiActionStep.run(
                "Clear browser session",
                () -> DriverManager.getEngine().clearSession(),
                "Session cleared",
                "Failed clearing browser session"
        );
    }
}
