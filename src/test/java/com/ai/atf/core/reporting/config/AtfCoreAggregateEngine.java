package com.ai.atf.core.reporting.config;

import java.util.Locale;

public enum AtfCoreAggregateEngine {
    EXTENT,
    ALLURE,
    BOTH,
    NONE;

    public boolean extentEnabled() {
        return this == EXTENT || this == BOTH;
    }

    public boolean allureEnabled() {
        return this == ALLURE || this == BOTH;
    }

    public String configValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AtfCoreAggregateEngine parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return BOTH;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (EXTENT.configValue().equals(normalized)) {
            return EXTENT;
        }
        if (ALLURE.configValue().equals(normalized)) {
            return ALLURE;
        }
        if (NONE.configValue().equals(normalized) || "off".equals(normalized) || "false".equals(normalized)) {
            return NONE;
        }
        return BOTH;
    }
}
