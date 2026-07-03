package com.ai.core.reporting;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;

public final class ReportingManager {

    private static final ReportingSession SESSION = new ReportingSession();

    private ReportingManager() {
    }

    public static void beginSuite() {
        SESSION.beginSuite();
    }

    public static void endSuite() {
        SESSION.endSuite();
    }

    public static synchronized ReportLogger getLogger() {
        return SESSION.getOrCreate(() -> ConfigManager.require(ReportingConfigKeys.REPORTING_ENGINE));
    }

    public static synchronized ReportLogger tryGetLogger() {
        return SESSION.getOrCreate(() -> ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, null));
    }

    public static void setLoggerForTests(ReportLogger testLogger) {
        SESSION.setLoggerForTests(testLogger);
    }

    public static ReportLogger testLoggerForTests() {
        return SESSION.testLoggerForTests();
    }

    public static void reset() {
        SESSION.reset();
    }

    public static boolean isSuiteActiveForTests() {
        return SESSION.isSuiteActive();
    }

    public static long suiteGenerationForTests() {
        return SESSION.generation();
    }
}
