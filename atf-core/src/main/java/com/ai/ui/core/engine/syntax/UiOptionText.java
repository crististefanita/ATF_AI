package com.ai.ui.core.engine.syntax;

import java.util.Locale;

public final class UiOptionText {
    private static final String NON_ALPHANUMERIC_PATTERN = "[^a-z0-9]+";

    private UiOptionText() {
    }

    public static String normalized(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll(NON_ALPHANUMERIC_PATTERN, "");
    }
}
