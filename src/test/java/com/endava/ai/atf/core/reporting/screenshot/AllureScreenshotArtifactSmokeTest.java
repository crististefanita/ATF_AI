package com.endava.ai.atf.core.reporting.screenshot;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.support.AllureResultFixture;
import com.endava.ai.atf.core.reporting.support.ScreenshotPayloadUiEngine;
import com.endava.ai.atf.core.reporting.support.UiReportingTestSupport;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.support.config.UiScreenshotConfigKeys;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.endava.ai.ui.core.runtime.UiInteractionTracker;
import com.endava.ai.ui.core.reporting.UiScreenshotFailureHandler;
import io.qameta.allure.model.Status;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.endava.ai.atf.core.reporting.support.UiReportingTestSupport.failureResult;
import static com.endava.ai.atf.core.reporting.support.UiReportingTestSupport.resetReportingState;
import static com.endava.ai.atf.core.reporting.support.UiReportingTestSupport.setEngineForCurrentThread;
import static com.endava.ai.atf.core.reporting.support.UiReportingTestSupport.successResult;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ALLURE_SCREENSHOT_ARTIFACT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_SCREENSHOT_ARTIFACT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.ALLURE_ONLY)
public class AllureScreenshotArtifactSmokeTest {

    @AfterMethod
    public void tearDown() {
        resetReportingState();
    }

    @DataProvider
    public Object[][] uiEngines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue()},
                {UiEngineType.PLAYWRIGHT.configValue()}
        };
    }

    @Test(dataProvider = "uiEngines")
    public void allure_receives_final_and_failure_screenshots_without_duplicates(String uiEngine) throws IOException {
        try (AllureResultFixture allure = AllureResultFixture.open("allure-final-failure-screenshot-" + uiEngine)) {
            allure.config()
                    .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
                    .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, uiEngine)
                    .setConfig(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, Boolean.TRUE.toString())
                    .setConfig(UiScreenshotConfigKeys.FAILURE_ONLY, Boolean.FALSE.toString())
                    .setConfig(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, Boolean.TRUE.toString());

            resetReportingState();
            UiScreenshotFailureHandler screenshotHandler = new UiScreenshotFailureHandler();
            FailureAttachmentRegistry.register(screenshotHandler);

            runSuccessfulUiFlow(allure, uiEngine, screenshotHandler);
            runFailingUiFlow(allure, uiEngine);

            Assert.assertEquals(allure.countOccurrences("Final Screenshot"), 1);
            Assert.assertEquals(allure.countOccurrences("Failure Screenshot"), 1);
            Assert.assertTrue(allure.countFiles(".png") >= 2);
        }
    }

    private void runSuccessfulUiFlow(
            AllureResultFixture allure,
            String uiEngine,
            UiScreenshotFailureHandler screenshotHandler
    ) {
        String uuid = allure.startRawTestCase("allure-success");

        TestListener listener = new TestListener();
        ITestResult result = successResult(AllureScreenshotArtifactSmokeTest.class, "allureSuccess");

        setEngineForCurrentThread(new ScreenshotPayloadUiEngine("allure-success-" + uiEngine));
        listener.onTestStart(result);
        UiInteractionTracker.markUiInteraction();

        StepLogger.startStep("success");
        StepLogger.logDetail("ui success");
        StepLogger.pass("ok");

        screenshotHandler.captureFinalStateIfEligible();
        listener.onTestSuccess(result);
        listener.onFinish((org.testng.ISuite) null);

        allure.writeTestCase(uuid, Status.PASSED);
        setEngineForCurrentThread(null);
    }

    private void runFailingUiFlow(AllureResultFixture allure, String uiEngine) {
        String uuid = allure.startRawTestCase("allure-failure");

        TestListener listener = new TestListener();
        ITestResult result = failureResult(
                AllureScreenshotArtifactSmokeTest.class,
                "allureFailure",
                new AssertionError("expected failure")
        );

        setEngineForCurrentThread(new ScreenshotPayloadUiEngine("allure-failure-" + uiEngine));
        listener.onTestStart(result);
        UiInteractionTracker.markUiInteraction();
        StepLogger.withExpectedFailureLogging(() ->
                listener.onTestFailure(result)
        );
        listener.onFinish((org.testng.ISuite) null);

        allure.writeTestCase(uuid, Status.FAILED);
        setEngineForCurrentThread(null);
    }
}
