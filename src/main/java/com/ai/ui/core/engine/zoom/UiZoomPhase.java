package com.ai.ui.core.engine.zoom;

public enum UiZoomPhase {
    BEFORE_NAVIGATION("before navigation"),
    AFTER_NAVIGATION("after navigation"),
    AFTER_CLICK("after click"),
    AFTER_VISIBLE_CLICK("after visible click"),
    AFTER_TYPE("after type"),
    AFTER_KEY_PRESS("after key press"),
    AFTER_SELECT("after select"),
    AFTER_URL_WAIT("after URL wait"),
    BEFORE_SCREENSHOT("before screenshot");

    private final String label;

    UiZoomPhase(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
