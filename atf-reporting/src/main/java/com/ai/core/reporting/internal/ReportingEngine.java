package com.ai.core.reporting.internal;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.adapters.AllureAdapter;
import com.ai.core.reporting.adapters.ExtentAdapter;

import java.util.Locale;

public enum ReportingEngine {

    EXTENT {
        @Override
        public ReportLogger createLogger() {
            return ExtentAdapter.getInstance();
        }

        @Override
        public boolean keepsAllureResults() {
            return false;
        }
    },

    ALLURE {
        @Override
        public ReportLogger createLogger() {
            return AllureAdapter.getInstance();
        }

        @Override
        public boolean keepsAllureResults() {
            return true;
        }
    };

    public abstract ReportLogger createLogger();

    public abstract boolean keepsAllureResults();

    public static ReportingEngine from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(ReportingConfigKeys.REPORTING_ENGINE + " is not set");
        }
        return ReportingEngine.valueOf(raw.toUpperCase(Locale.ROOT));
    }

    public static ReportingEngine current() {
        return from(
                ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, EXTENT.name())
        );
    }

    public boolean isAllure() {
        return this == ALLURE;
    }

    @SuppressWarnings("unused")
    public boolean isExtent() {
        return this == EXTENT;
    }
}
