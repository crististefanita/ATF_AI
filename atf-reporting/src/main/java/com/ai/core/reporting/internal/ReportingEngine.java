package com.ai.core.reporting.internal;

import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;
import java.util.Locale;

/** Selects the maintained Extent reporting implementation. */
public enum ReportingEngine {
    EXTENT;

    public ReportLogger createLogger() { return ExtentAdapter.getInstance(); }

    public static ReportingEngine from(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException(ReportingConfigKeys.REPORTING_ENGINE + " is not set");
        try { return valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException(ReportingConfigKeys.REPORTING_ENGINE + " supports extent; observed: " + raw, invalid);
        }
    }

    public static ReportingEngine current() {
        return from(ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, EXTENT.name()));
    }

    public boolean isExtent() { return this == EXTENT; }
}
