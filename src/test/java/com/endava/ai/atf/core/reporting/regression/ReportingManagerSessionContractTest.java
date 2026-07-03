package com.endava.ai.atf.core.reporting.regression;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.CompositeReportLogger;
import com.endava.ai.core.reporting.ReportLogger;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.TestStatus;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_MANAGER_SESSION_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_MANAGER_SESSION_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ReportingManagerSessionContractTest {

    @AfterMethod(alwaysRun = true)
    public void resetReportingManager() {
        ReportingManager.reset();
    }

    @Test
    public void testLoggerIsStoredAndReturnedThroughSessionFacade() {
        RecordingLogger logger = new RecordingLogger();

        ReportingManager.setLoggerForTests(logger);

        Assert.assertSame(ReportingManager.testLoggerForTests(), logger,
                "ReportingManager must expose the raw test logger for isolated observer assertions.");
        assertActiveLoggerUsesRawObserver(ReportingManager.tryGetLogger(), logger,
                "ReportingManager must cache the session logger created from the test logger.");
    }

    @Test
    public void resetClearsCachedLoggerAndRawTestLogger() {
        ReportingManager.setLoggerForTests(new RecordingLogger());

        ReportingManager.reset();

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .clearConfig(ReportingConfigKeys.REPORTING_ENGINE)
                .clearSystemProperty(ReportingConfigKeys.REPORTING_ENGINE)
                .clearSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED)) {
            Assert.assertNull(ReportingManager.testLoggerForTests(),
                    "Reset must clear the raw test logger reference.");
            Assert.assertNull(ReportingManager.tryGetLogger(),
                    "Reset plus missing optional engine must leave tryGetLogger null.");
        }
    }

    @Test
    public void beginSuiteClearsCachedLoggerSoRuntimeReportsStartClean() {
        ReportingManager.setLoggerForTests(new RecordingLogger());

        ReportingManager.beginSuite();

        Assert.assertNull(ReportingManager.testLoggerForTests(),
                "beginSuite must not keep a stale test logger from a previous suite.");
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .clearConfig(ReportingConfigKeys.REPORTING_ENGINE)
                .clearSystemProperty(ReportingConfigKeys.REPORTING_ENGINE)
                .clearSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED)) {
            Assert.assertNull(ReportingManager.tryGetLogger(),
                    "beginSuite must force the next runtime logger to be resolved from the active configuration.");
        }
    }

    @Test
    public void extentMirrorWrapsNonExtentTestLoggerWithoutReplacingRawObserver() {
        RecordingLogger logger = new RecordingLogger();

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, "true")) {
            ReportingManager.setLoggerForTests(logger);

            ReportLogger active = ReportingManager.tryGetLogger();

            Assert.assertSame(ReportingManager.testLoggerForTests(), logger,
                    "Raw observer must stay available even when the active logger is mirrored.");
            Assert.assertTrue(active instanceof CompositeReportLogger,
                    "Mirror mode should compose the observer with the Extent adapter.");
            Assert.assertSame(((CompositeReportLogger) active).primary(), logger,
                    "The observer must remain the primary logger in mirror mode.");
        }
    }

    @Test
    public void suiteLifecycleIsExplicitAndResettable() {
        long before = ReportingManager.suiteGenerationForTests();

        ReportingManager.beginSuite();

        Assert.assertTrue(ReportingManager.isSuiteActiveForTests(),
                "beginSuite must mark the reporting session active.");
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1,
                "beginSuite must advance the observable suite generation.");

        ReportingManager.endSuite();

        Assert.assertFalse(ReportingManager.isSuiteActiveForTests(),
                "endSuite must mark the reporting session inactive without clearing generation history.");
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1,
                "endSuite must not advance generation.");

        ReportingManager.beginSuite();
        ReportingManager.reset();

        Assert.assertFalse(ReportingManager.isSuiteActiveForTests(),
                "reset must leave the session inactive.");
    }

    private static void assertActiveLoggerUsesRawObserver(
            ReportLogger active,
            RecordingLogger expectedRawObserver,
            String message
    ) {
        if (active instanceof CompositeReportLogger) {
            CompositeReportLogger composite = (CompositeReportLogger) active;
            Assert.assertSame(composite.primary(), expectedRawObserver, message);
            return;
        }
        Assert.assertSame(active, expectedRawObserver, message);
    }

    private static final class RecordingLogger implements ReportLogger {
        private TestStatus status;

        @Override
        public void startTest(String testName, String description) {
        }

        @Override
        public void endTest(TestStatus status) {
            this.status = status;
        }

        @Override
        public void startStep(String stepTitle) {
        }

        @Override
        public void logDetail(String detail) {
        }

        @Override
        public void pass(String message) {
        }

        @Override
        public void fail(String message, String stacktraceAsText) {
        }

        @Override
        public void attachScreenshotBase64(String base64, String title) {
        }

        @Override
        public void logCodeBlock(String content) {
        }

        @Override
        public void flush() {
        }
    }
}
