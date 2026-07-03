package com.ai.ui.core.engine.selenium.element.actions;

import org.openqa.selenium.Keys;

enum SeleniumKeyboard {
    TAB("Tab", Keys.TAB),
    ENTER("Enter", Keys.ENTER),
    ARROW_RIGHT("ArrowRight", Keys.ARROW_RIGHT),
    ARROW_LEFT("ArrowLeft", Keys.ARROW_LEFT),
    BACKSPACE("Backspace", Keys.BACK_SPACE),
    DELETE("Delete", Keys.DELETE),
    ESCAPE("Escape", Keys.ESCAPE),
    CONTROL_A("Control+A", Keys.chord(Keys.CONTROL, "a")),
    CTRL_A("Ctrl+A", Keys.chord(Keys.CONTROL, "a"));

    private final String apiName;
    private final CharSequence mappedKey;

    SeleniumKeyboard(String apiName, CharSequence mappedKey) {
        this.apiName = apiName;
        this.mappedKey = mappedKey;
    }

    static CharSequence map(String key) {
        for (SeleniumKeyboard candidate : values()) {
            if (candidate.apiName.equalsIgnoreCase(key)) {
                return candidate.mappedKey;
            }
        }
        throw new IllegalArgumentException("Unsupported key for Selenium engine: " + key);
    }
}
