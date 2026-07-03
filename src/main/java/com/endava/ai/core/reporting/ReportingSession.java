package com.endava.ai.core.reporting;

import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.internal.ReportingEngine;

import java.util.function.Supplier;

final class ReportingSession {

    private ReportLogger logger;
    private ReportLogger testLogger;
    private boolean suiteActive;
    private long generation;

    synchronized void beginSuite() {
        logger = null;
        testLogger = null;
        ExtentAdapter.getInstance().resetForTests();
        suiteActive = true;
        generation++;
    }

    synchronized void endSuite() {
        suiteActive = false;
    }

    synchronized ReportLogger getOrCreate(Supplier<String> engineSupplier) {
        if (logger != null) {
            return logger;
        }

        String raw = engineSupplier.get();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        logger = withExtentMirror(ReportingEngine.from(raw).createLogger());
        return logger;
    }

    synchronized void setLoggerForTests(ReportLogger testLogger) {
        this.testLogger = testLogger;
        logger = withExtentMirror(testLogger);
    }

    synchronized ReportLogger testLoggerForTests() {
        return testLogger;
    }

    synchronized void reset() {
        logger = null;
        testLogger = null;
        suiteActive = false;
    }

    synchronized boolean isSuiteActive() {
        return suiteActive;
    }

    synchronized long generation() {
        return generation;
    }

    private static ReportLogger withExtentMirror(ReportLogger primary) {
        if (primary == null) {
            return null;
        }
        if (!Boolean.parseBoolean(System.getProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, "false"))) {
            return primary;
        }
        if (primary == ExtentAdapter.getInstance()) {
            return primary;
        }
        return CompositeReportLogger.of(primary, ExtentAdapter.getInstance());
    }
}
