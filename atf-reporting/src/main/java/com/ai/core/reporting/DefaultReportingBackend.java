package com.ai.core.reporting;

import com.ai.core.reporting.listener.ReportingTestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.spi.ReportingBackend;
import com.ai.core.reporting.spi.ReportingLifecycleListener;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;

/** Reporting module service provider; all concrete report state stays here. */
public final class DefaultReportingBackend implements ReportingBackend {

    private final ReportingSession session = new ReportingSession();

    @Override
    public void beginSuite() {
        session.beginSuite();
    }

    @Override
    public void endSuite() {
        session.endSuite();
    }

    @Override
    public ReportLogger getLogger(boolean required) {
        return session.getOrCreate(() -> required
                ? ConfigManager.require(ReportingConfigKeys.REPORTING_ENGINE)
                : ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, null));
    }

    @Override
    public void setLoggerForTests(ReportLogger logger) {
        session.setLoggerForTests(logger);
    }

    @Override
    public ReportLogger testLoggerForTests() {
        return session.testLoggerForTests();
    }

    @Override
    public void reset() {
        session.reset();
    }

    @Override
    public boolean isSuiteActive() {
        return session.isSuiteActive();
    }

    @Override
    public long generation() {
        return session.generation();
    }

    @Override
    public ReportingLifecycleListener createListener() {
        return new ReportingTestListener();
    }

    @Override
    public void resetListenerStateForTests() {
        ExtentRuntimeAuditRegistry.clear();
    }
}
