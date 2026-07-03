package com.endava.ai.atf.core.reporting.screenshot;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.support.config.UiScreenshotConfigKeys;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.ReportLogger;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.core.runtime.UiInteractionTracker;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.reporting.ScreenshotManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SCREENSHOT_ARTIFACT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SCREENSHOT_ARTIFACT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentScreenshotArtifactSmokeTest {

    private AtfRuntimeConfigSandbox config;

    @AfterMethod
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }

        setEngineForCurrentThread(null);
        FailureAttachmentRegistry.clearForTests();
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
    }

    @Test
    public void extent_screenshot_policy_matrix_does_not_publish_fake_screenshots() {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setSystemProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, Boolean.FALSE.toString())
                .setConfig(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, Boolean.TRUE.toString())
                .setConfig(UiScreenshotConfigKeys.FAILURE_ONLY, Boolean.FALSE.toString())
                .setConfig(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, Boolean.TRUE.toString());

        ScreenshotObserver observer = new ScreenshotObserver();
        FailureAttachmentRegistry.clearForTests();
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
        ReportingManager.setLoggerForTests(observer);

        for (String uiEngine : new String[]{
                UiEngineType.SELENIUM.configValue(),
                UiEngineType.PLAYWRIGHT.configValue()
        }) {
            config.setConfig(UiRuntimeOptions.UI_ENGINE_KEY, uiEngine);
            runSuccessfulUiFlow(uiEngine, observer);
            runFailingUiFlow(uiEngine, observer);
        }

        Assert.assertEquals(observer.countScreenshots("Final Screenshot"), 2);
        Assert.assertEquals(observer.countScreenshots("Failure Screenshot"), 2);
        publishObserverOnlyMatrixSummary(observer);
        assertMatrixScreenshotsAreNotPublishedInExtent();
    }

    private void runSuccessfulUiFlow(String uiEngine, ScreenshotObserver observer) {
        setEngineForCurrentThread(new FakeUiEngine("extent-success-" + uiEngine));
        UiInteractionTracker.reset();
        ScreenshotManager.resetForTest();
        UiInteractionTracker.markUiInteraction();
        Assert.assertTrue(DriverManager.hasActiveEngine(), "Synthetic UI engine must be active before final screenshot policy is evaluated");
        Assert.assertTrue(UiInteractionTracker.hasUiInteraction(), "UI interaction marker must be active before final screenshot policy is evaluated");
        Assert.assertSame(ReportingManager.testLoggerForTests(), observer, "Screenshot policy must publish through the isolated observer logger");
        ScreenshotManager.attachOnce("Final Screenshot");
        setEngineForCurrentThread(null);
    }

    private void runFailingUiFlow(String uiEngine, ScreenshotObserver observer) {
        setEngineForCurrentThread(new FakeUiEngine("extent-failure-" + uiEngine));
        UiInteractionTracker.reset();
        ScreenshotManager.resetForTest();
        UiInteractionTracker.markUiInteraction();
        Assert.assertTrue(DriverManager.hasActiveEngine(), "Synthetic UI engine must be active before failure screenshot policy is evaluated");
        Assert.assertTrue(UiInteractionTracker.hasUiInteraction(), "UI interaction marker must be active before failure screenshot policy is evaluated");
        Assert.assertSame(ReportingManager.testLoggerForTests(), observer, "Screenshot policy must publish through the isolated observer logger");
        ScreenshotManager.attachOnce("Failure Screenshot");
        setEngineForCurrentThread(null);
    }

    private static void setEngineForCurrentThread(UIEngine engine) {
        try {
            Field field = DriverManager.class.getDeclaredField("ENGINE");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ThreadLocal<UIEngine> engines = (ThreadLocal<UIEngine>) field.get(null);

            if (engine == null) {
                engines.remove();
            } else {
                engines.set(engine);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set UI engine for tests", e);
        }
    }

    private static void publishObserverOnlyMatrixSummary(ScreenshotObserver observer) {
        StepLogger.setDelegate(ExtentAdapter.getInstance());
        StepLogger.startStep("Extent screenshot policy matrix");
        StepLogger.logDetail("Matrix scope: Selenium + Playwright");
        StepLogger.logDetail("Observed final screenshots: " + observer.countScreenshots("Final Screenshot"));
        StepLogger.logDetail("Observed failure screenshots: " + observer.countScreenshots("Failure Screenshot"));
        StepLogger.logDetail("Report artifact policy: matrix screenshots stay in the observer and do not pollute Extent.");
        StepLogger.pass("Screenshot matrix verified without publishing fake image noise");
        ExtentAdapter.getInstance().flush();
    }

    private static void assertMatrixScreenshotsAreNotPublishedInExtent() {
        Path report = ExtentAdapter.getInstance().currentReportPath();
        Assert.assertNotNull(report, "Extent report path must be available for screenshot matrix audit.");
        try {
            String html = Files.readString(report);
            for (String payload : List.of(
                    "extent-success-selenium",
                    "extent-failure-selenium",
                    "extent-success-playwright",
                    "extent-failure-playwright"
            )) {
                String encodedPayload = Base64.getEncoder()
                        .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                Assert.assertFalse(
                        html.contains(encodedPayload),
                        "Observer-only screenshot matrix must not publish fake screenshot payload to Extent: " + payload
                );
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to inspect Extent report for screenshot matrix noise: " + report, e);
        }
    }

    private static final class FakeUiEngine implements UIEngine {

        private final String payload;

        private FakeUiEngine(String payload) {
            this.payload = Base64.getEncoder().encodeToString(payload.getBytes());
        }

        @Override public boolean supportsAutoWait() { return true; }
        @Override public void open(String url) {}
        @Override public void click(String cssSelector) {}
        @Override public void type(String cssSelector, String text) {}
        @Override public void select(String cssSelector, String valueOrText) {}
        @Override public String getText(String cssSelector) { return ""; }
        @Override public String getValue(String cssSelector) { return ""; }
        @Override public boolean isVisible(String cssSelector) { return true; }
        @Override public void waitForVisible(String cssSelector, int seconds) {}
        @Override public void waitForUrlContains(String fragment, int seconds) {}
        @Override public String getCurrentUrl() { return "about:blank"; }
        @Override public void clearSession() {}
        @Override public String captureScreenshotAsBase64() { return payload; }
        @Override public void quit() {}
        @Override public void setWindowSize(int width, int height) {}
    }

    private static final class ScreenshotObserver implements ReportLogger {

        private final List<String> screenshotTitles = new CopyOnWriteArrayList<>();
        private int countScreenshots(String title) {
            int count = 0;
            for (String screenshotTitle : screenshotTitles) {
                if (title.equals(screenshotTitle)) {
                    count++;
                }
            }
            return count;
        }

        @Override public void startTest(String testName, String description) {}

        @Override public void endTest(TestStatus status) {}

        @Override public void startStep(String stepTitle) {}
        @Override public void logDetail(String detail) {}
        @Override public void pass(String message) {}
        @Override public void fail(String message, String stacktraceAsText) {}

        @Override
        public void attachScreenshotBase64(String base64, String title) {
            if (base64 != null && !base64.isBlank()) {
                screenshotTitles.add(title);
            }
        }

        @Override public void logCodeBlock(String content) {}
        @Override public void flush() {}
    }
}
