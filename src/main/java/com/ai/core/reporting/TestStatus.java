package com.ai.core.reporting;

import java.util.Locale;

public enum TestStatus {
    PASS,
    FAIL,
    SKIP,
    RESET;

    public String wireValue() {
        return name();
    }

    public static TestStatus from(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Test status must not be blank");
        }
        return valueOf(status.trim().toUpperCase(Locale.ROOT));
    }
}
