package com.endava.ai.core.support.config;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Locale;

@Getter
@Accessors(fluent = true)
public enum UiEngineType {
    SELENIUM("selenium"),
    PLAYWRIGHT("playwright");

    private static final String DEFAULT_ALIAS = "default";

    private final String configValue;

    UiEngineType(String configValue) {
        this.configValue = configValue;
    }

    public static UiEngineType fromConfig(String rawEngineName) {
        String normalized = normalize(rawEngineName);
        if (DEFAULT_ALIAS.equals(normalized)) {
            return SELENIUM;
        }
        for (UiEngineType type : values()) {
            if (type.configValue.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported ui.engine: " + rawEngineName);
    }

    private static String normalize(String rawEngineName) {
        if (rawEngineName == null || rawEngineName.trim().isEmpty()) {
            return DEFAULT_ALIAS;
        }
        return rawEngineName.trim().toLowerCase(Locale.ROOT);
    }
}
