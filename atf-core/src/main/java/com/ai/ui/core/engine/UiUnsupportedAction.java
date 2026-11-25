package com.ai.ui.core.engine;

public enum UiUnsupportedAction {
    VISIBLE_INDEX_CLICK("Visible-index click"),
    KEY_PRESS("Key press"),
    FILE_UPLOAD("File upload"),
    ATTRIBUTE_LOOKUP("Attribute lookup"),
    NETWORK_RESPONSE_WAIT("Network response waiting");

    private static final String MESSAGE_SUFFIX = " is not supported by this UI engine.";

    private final String label;

    UiUnsupportedAction(String label) {
        this.label = label;
    }

    public String message() {
        return label + MESSAGE_SUFFIX;
    }

    public UnsupportedOperationException exception() {
        return new UnsupportedOperationException(message());
    }
}
