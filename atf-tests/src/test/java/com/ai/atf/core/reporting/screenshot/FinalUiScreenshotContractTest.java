package com.ai.atf.core.reporting.screenshot;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import org.testng.annotations.Listeners;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.config.UiScreenshotConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.attachment.FinalStateAttachmentRegistry;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.ai.ui.core.runtime.UiInteractionTracker;
import com.ai.ui.core.reporting.UiScreenshotFailureHandler;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.ai.atf.core.reporting.screenshot.FinalScreenshotContractSupport.failureResult;
import static com.ai.atf.core.reporting.screenshot.FinalScreenshotContractSupport.setEngineForCurrentThread;
import static com.ai.atf.core.reporting.screenshot.FinalScreenshotContractSupport.successResult;
import static com.ai.atf.core.reporting.screenshot.FinalScreenshotContractSupport.testInvocation;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.FINAL_UI_SCREENSHOT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.FINAL_UI_SCREENSHOT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class FinalUiScreenshotContractTest {

    private final UiScreenshotFailureHandler handler = new UiScreenshotFailureHandler();

    private ScreenshotObserver observer;
    private TestListener listener;
    private AtfRuntimeConfigSandbox config;

    @BeforeMethod
    public void setUp() {
        config = AtfRuntimeConfigSandbox.open();

        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
        FailureAttachmentRegistry.clearForTests();

        observer = new ScreenshotObserver();
        FailureAttachmentRegistry.register(handler);
        FinalStateAttachmentRegistry.register(handler);
        listener = new TestListener();

        configureScreenshots(
                UiEngineType.PLAYWRIGHT.configValue(),
                ReportingConfigKeys.ENGINE_EXTENT,
                true,
                false,
                true
        );
        installObserverBesideActiveReporter();
        setEngineForCurrentThread(null);
    }

    @AfterMethod
    public void tearDown() {
        setEngineForCurrentThread(null);
        if (config != null) {
            config.close();
            config = null;
        }

        FailureAttachmentRegistry.clearForTests();
        FinalStateAttachmentRegistry.clearForTests();
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
    }

    @DataProvider
    public Object[][] screenshotMatrix() {
        return new Object[][]{
                {UiEngineType.PLAYWRIGHT.configValue(), ReportingConfigKeys.ENGINE_EXTENT, true, true, false, true},
                {UiEngineType.PLAYWRIGHT.configValue(), ReportingConfigKeys.ENGINE_EXTENT, false, true, false, false},
                {UiEngineType.PLAYWRIGHT.configValue(), ReportingConfigKeys.ENGINE_EXTENT, true, false, false, false},
                {UiEngineType.PLAYWRIGHT.configValue(), ReportingConfigKeys.ENGINE_EXTENT, true, true, true, false},
                {UiEngineType.SELENIUM.configValue(), ReportingConfigKeys.ENGINE_EXTENT, true, true, false, true}
        };
    }

    @DataProvider
    public Object[][] screenshotConfigMatrix() {
        Object[][] matrix = screenshotMatrix();
        Object[][] configOnly = new Object[matrix.length][5];

        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, configOnly[i], 0, 5);
        }

        return configOnly;
    }

    @Test(dataProvider = "screenshotMatrix")
    public void successful_ui_tests_follow_final_screenshot_contract(
            String uiEngine,
            String reportingEngine,
            boolean enabled,
            boolean captureFinalState,
            boolean failureOnly,
        boolean expectFinalScreenshot
    ) {
        configureScreenshots(uiEngine, reportingEngine, enabled, failureOnly, captureFinalState);
        installObserverBesideActiveReporter();

        FakeUiEngine engine = new FakeUiEngine(uiEngine + "-success");
        setEngineForCurrentThread(engine);

        ITestResult result = successResult();
        listener.onTestStart(result);
        listener.afterInvocation(testInvocation(), result);
        listener.onTestSuccess(result);

        assertTitles(expectFinalScreenshot ? List.of("Final Screenshot") : List.of());
        Assert.assertEquals(engine.captureCalls(), expectFinalScreenshot ? 1 : 0);
    }

    @Test(dataProvider = "screenshotConfigMatrix")
    public void failed_ui_tests_preserve_existing_failure_screenshot_contract(
            String uiEngine,
            String reportingEngine,
            boolean enabled,
            boolean captureFinalState,
        boolean failureOnly
    ) {
        configureScreenshots(uiEngine, reportingEngine, enabled, failureOnly, captureFinalState);
        installObserverBesideActiveReporter();

        FakeUiEngine engine = new FakeUiEngine(uiEngine + "-failure");
        setEngineForCurrentThread(engine);

        ITestResult result = failureResult(new AssertionError("boom"));
        listener.onTestStart(result);
        UiInteractionTracker.markUiInteraction();
        StepLogger.withExpectedFailureLogging(() ->
                listener.onTestFailure(result)
        );

        assertTitles(enabled ? List.of("Failure Screenshot") : List.of());
        Assert.assertEquals(engine.captureCalls(), enabled ? 1 : 0);
    }

    @Test(dataProvider = "screenshotConfigMatrix")
    public void non_ui_success_flow_never_attempts_ui_screenshots(
            String uiEngine,
            String reportingEngine,
            boolean enabled,
            boolean captureFinalState,
        boolean failureOnly
    ) {
        configureScreenshots(uiEngine, reportingEngine, enabled, failureOnly, captureFinalState);
        installObserverBesideActiveReporter();

        ITestResult result = successResult();
        listener.onTestStart(result);
        listener.onTestSuccess(result);

        assertTitles(List.of());
    }

    @Test(dataProvider = "screenshotConfigMatrix")
    public void non_ui_failure_flow_never_attempts_ui_screenshots(
            String uiEngine,
            String reportingEngine,
            boolean enabled,
            boolean captureFinalState,
        boolean failureOnly
    ) {
        configureScreenshots(uiEngine, reportingEngine, enabled, failureOnly, captureFinalState);
        installObserverBesideActiveReporter();

        ITestResult result = failureResult(new AssertionError("boom"));
        listener.onTestStart(result);
        StepLogger.withExpectedFailureLogging(() ->
                listener.onTestFailure(result)
        );

        assertTitles(List.of());
    }

    @Test
    public void final_and_failure_capture_do_not_duplicate_screenshots() {
        installObserverBesideActiveReporter();
        FakeUiEngine engine = new FakeUiEngine("dedupe");
        setEngineForCurrentThread(engine);

        handler.onTestStart();
        UiInteractionTracker.markUiInteraction();
        handler.captureFinalStateIfEligible();
        handler.onTestFailure();

        Assert.assertEquals(observer.screenshotTitles(), List.of("Final Screenshot"));
        Assert.assertEquals(engine.captureCalls(), 1);
    }

    @Test
    public void final_capture_is_thread_safe_in_parallel_execution() throws Exception {
        installObserverBesideActiveReporter();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(() -> runParallelCapture("parallel-1"));
            Future<Integer> second = executor.submit(() -> runParallelCapture("parallel-2"));

            Assert.assertEquals((int) first.get(10, TimeUnit.SECONDS), 1);
            Assert.assertEquals((int) second.get(10, TimeUnit.SECONDS), 1);
            Assert.assertEquals(observer.screenshotTitles().size(), 2);
            Assert.assertEquals(
                    countScreenshotsWithTitle("Final Screenshot"),
                    2
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private int runParallelCapture(String name) {
        FakeUiEngine engine = new FakeUiEngine(name);
        setEngineForCurrentThread(engine);

        try {
            handler.onTestStart();
            UiInteractionTracker.markUiInteraction();
            handler.captureFinalStateIfEligible();
            return engine.captureCalls();
        } finally {
            setEngineForCurrentThread(null);
        }
    }

    private int countScreenshotsWithTitle(String title) {
        int count = 0;
        for (String value : observer.screenshotTitles()) {
            if (title.equals(value)) {
                count++;
            }
        }
        return count;
    }

    private void assertTitles(List<String> expected) {
        Assert.assertEquals(observer.screenshotTitles(), expected);
    }

    private void installObserverBesideActiveReporter() {
        ReportingManager.reset();
        ReportingManager.setLoggerForTests(new ObserverMirrorLogger(ReportingManager.getLogger(), observer));
    }

    private void configureScreenshots(
            String uiEngine,
            String reportingEngine,
            boolean enabled,
            boolean failureOnly,
            boolean captureFinalState
    ) {
        config.setConfig(UiRuntimeOptions.UI_ENGINE_KEY, uiEngine)
                .setConfig(ReportingConfigKeys.REPORTING_ENGINE, reportingEngine)
                .setConfig(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, Boolean.toString(enabled))
                .setConfig(UiScreenshotConfigKeys.FAILURE_ONLY, Boolean.toString(failureOnly))
                .setConfig(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, Boolean.toString(captureFinalState));
    }

}
