package com.ai.core.reporting;

import com.ai.core.reporting.spi.ReportingBackend;
import com.ai.core.reporting.spi.ReportingBackendLoader;
import com.ai.core.reporting.spi.ReportingLifecycleListener;

public final class ReportingManager {

    private static final ReportingBackend BACKEND = ReportingBackendLoader.load()
            .orElseGet(ConsoleOnlyBackend::new);

    private ReportingManager() {
    }

    public static void beginSuite() {
        BACKEND.beginSuite();
    }

    public static void endSuite() {
        BACKEND.endSuite();
    }

    public static synchronized ReportLogger getLogger() {
        return BACKEND.getLogger(true);
    }

    public static synchronized ReportLogger tryGetLogger() {
        return BACKEND.getLogger(false);
    }

    public static void setLoggerForTests(ReportLogger testLogger) {
        BACKEND.setLoggerForTests(testLogger);
    }

    public static ReportLogger testLoggerForTests() {
        return BACKEND.testLoggerForTests();
    }

    public static void reset() {
        BACKEND.reset();
    }

    public static boolean isSuiteActiveForTests() {
        return BACKEND.isSuiteActive();
    }

    public static long suiteGenerationForTests() {
        return BACKEND.generation();
    }

    public static ReportingLifecycleListener createTestListener() {
        return BACKEND.createListener();
    }

    public static void resetListenerStateForTests() {
        BACKEND.resetListenerStateForTests();
    }

    /** Keeps Core usable when no report implementation is on the classpath. */
    private static final class ConsoleOnlyBackend implements ReportingBackend {

        private ReportLogger testLogger;
        private boolean suiteActive;
        private long generation;

        @Override
        public synchronized void beginSuite() {
            testLogger = null;
            suiteActive = true;
            generation++;
        }

        @Override
        public synchronized void endSuite() {
            suiteActive = false;
        }

        @Override
        public synchronized ReportLogger getLogger(boolean required) {
            if (testLogger == null && required) {
                throw new IllegalStateException(
                        "No reporting backend is installed. Add the atf-reporting module to the runtime classpath.");
            }
            return testLogger;
        }

        @Override
        public synchronized void setLoggerForTests(ReportLogger logger) {
            testLogger = logger;
        }

        @Override
        public synchronized ReportLogger testLoggerForTests() {
            return testLogger;
        }

        @Override
        public synchronized void reset() {
            testLogger = null;
            suiteActive = false;
        }

        @Override
        public synchronized boolean isSuiteActive() {
            return suiteActive;
        }

        @Override
        public synchronized long generation() {
            return generation;
        }

        @Override
        public ReportingLifecycleListener createListener() {
            return new ReportingLifecycleListener() { };
        }

        @Override
        public void resetListenerStateForTests() {
        }
    }
}
