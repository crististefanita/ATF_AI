package com.ai.core.reporting.spi;

import com.ai.core.reporting.ReportLogger;

/** Optional reporting implementation discovered independently of browser engines. */
public interface ReportingBackend {

    void beginSuite();

    void endSuite();

    ReportLogger getLogger(boolean required);

    void setLoggerForTests(ReportLogger logger);

    ReportLogger testLoggerForTests();

    void reset();

    boolean isSuiteActive();

    long generation();

    ReportingLifecycleListener createListener();

    void resetListenerStateForTests();
}
