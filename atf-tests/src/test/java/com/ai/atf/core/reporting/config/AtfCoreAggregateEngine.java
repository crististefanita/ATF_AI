package com.ai.atf.core.reporting.config;

import java.util.Locale;

public enum AtfCoreAggregateEngine {
    EXTENT, NONE;

    public boolean extentEnabled() { return this == EXTENT; }
    public String configValue() { return name().toLowerCase(Locale.ROOT); }

    public static AtfCoreAggregateEngine parse(String raw) {
        if (raw == null || raw.isBlank()) return EXTENT;
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if ("extent".equals(normalized)) return EXTENT;
        if ("none".equals(normalized) || "off".equals(normalized) || "false".equals(normalized)) return NONE;
        throw new IllegalArgumentException("atf.core.aggregate.report.engine supports extent or none; observed: " + raw);
    }
}
