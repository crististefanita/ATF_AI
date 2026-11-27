package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.CompositeReportLogger;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.TestStatus;
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
        for (String value : java.util.List.of("false", "", "true ", "unsupported")) {
            try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                    .setSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, value)) {
                ReportingManager.reset();
                RecordingLogger direct = new RecordingLogger();
                ReportingManager.setLoggerForTests(direct);
                Assert.assertSame(ReportingManager.tryGetLogger(), direct,
                        "The compatibility mirror uses Boolean.parseBoolean; unsupported or padded values disable mirroring");
                Assert.assertSame(ReportingManager.testLoggerForTests(), direct);
            }
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
        RecordingLogger observer = new RecordingLogger();
        ReportingManager.setLoggerForTests(observer);
        ReportLogger cached = ReportingManager.tryGetLogger();

        ReportingManager.endSuite();

        Assert.assertFalse(ReportingManager.isSuiteActiveForTests(),
                "endSuite must mark the reporting session inactive without clearing generation history.");
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1,
                "endSuite must not advance generation.");
        Assert.assertSame(ReportingManager.tryGetLogger(), cached,
                "endSuite only marks the boundary; it must not replace the cached logger behind listener callbacks.");
        Assert.assertSame(ReportingManager.testLoggerForTests(), observer);
        ReportingManager.endSuite();
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1,
                "Repeated endSuite calls must not create another suite generation.");

        ReportingManager.beginSuite();
        Assert.assertNull(ReportingManager.testLoggerForTests(), "A new suite must discard the previous observer.");
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 2);
        ReportingManager.reset();

        Assert.assertFalse(ReportingManager.isSuiteActiveForTests(),
                "reset must leave the session inactive.");
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 2,
                "reset clears activity and caches, not the observed generation history.");
    }

    @Test
    public void cachedObserverIdentitySurvivesConfigurationChangesUntilReset() {
        RecordingLogger observer = new RecordingLogger();
        try (AtfRuntimeConfigSandbox settings = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, "false")) {
            ReportingManager.setLoggerForTests(observer);
            ReportLogger cached = ReportingManager.getLogger();
            settings.setSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, "unrecognized-report-engine");

            Assert.assertSame(ReportingManager.getLogger(), cached,
                    "An already selected session logger must not be recreated from changing global configuration.");
            Assert.assertSame(ReportingManager.tryGetLogger(), cached);
            Assert.assertSame(ReportingManager.testLoggerForTests(), observer);

            ReportingManager.reset();
            Assert.assertNull(ReportingManager.testLoggerForTests());
            Assert.expectThrows(IllegalArgumentException.class, ReportingManager::getLogger);
        }
    }

    @Test
    public void invalidEngineFailsWithoutCachingABogusLoggerAndCanBeCorrected() {
        for (String invalidEngine : new String[]{"unrecognized-report-engine", "allure", "both"}) {
            ReportingManager.reset();
            try (AtfRuntimeConfigSandbox settings = AtfRuntimeConfigSandbox.open()
                    .setSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, invalidEngine)
                    .setSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, "false")) {
                IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class, ReportingManager::getLogger);
                Assert.assertTrue(failure.getMessage().contains(invalidEngine),
                        "The original invalid configured value must remain visible in the diagnostic.");
                Assert.assertTrue(failure.getMessage().contains(ReportingConfigKeys.REPORTING_ENGINE),
                        "The diagnostic must identify the configuration property to repair.");
                Assert.assertNull(ReportingManager.testLoggerForTests(), "No fake observer may hide an invalid engine.");
                Assert.expectThrows(IllegalArgumentException.class, ReportingManager::tryGetLogger);

                settings.setSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT);
                ReportLogger recovered = ReportingManager.getLogger();
                Assert.assertNotNull(recovered, "Correcting configuration after a failed selection must allow a real logger.");
                Assert.assertSame(ReportingManager.tryGetLogger(), recovered);
            }
        }
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
