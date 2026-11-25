package com.ai.atf.core.reporting.support;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.config.UiScreenshotConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.ai.core.reporting.internal.ReportingPaths;
import com.ai.ui.core.runtime.UiInteractionTracker;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UIEngineFactory;
import com.ai.ui.core.reporting.UiScreenshotFailureHandler;
import io.qameta.allure.model.Status;
import org.testng.ITestResult;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.ai.atf.core.reporting.support.UiReportingTestSupport.resetReportingState;
import static com.ai.atf.core.reporting.support.UiReportingTestSupport.setEngineForCurrentThread;
import static com.ai.atf.core.reporting.support.UiReportingTestSupport.successResult;

public final class AllureZoomEvidenceFixture {

    private final AtfRuntimeConfigSandbox config;
    private final Class<?> testClass;

    public AllureZoomEvidenceFixture(AtfRuntimeConfigSandbox config, Class<?> testClass) {
        this.config = config;
        this.testClass = testClass;
    }

    public Path captureZoomEvidence(String uiEngine, String zoomPercent, String methodName)
            throws Exception {
        configureRuntime(uiEngine, zoomPercent);
        resetReportingState();

        try (AllureResultFixture allure = AllureResultFixture.open(
                "allure-zoom-visual-" + uiEngine + "-" + zoomPercent
        )) {
            config.setSystemProperty(ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY, allure.resultsDir().toString());
            FailureAttachmentRegistry.register(new UiScreenshotFailureHandler());

            String uuid = allure.startRawTestCase(methodName);
            runUiFlow(methodName);
            allure.writeTestCase(uuid, Status.PASSED);

            return copyScreenshotArtifact(allure.firstFileWithSuffix(".png"), uiEngine, zoomPercent);
        }
    }

    private void configureRuntime(String uiEngine, String zoomPercent) {
        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, uiEngine)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, "true")
                .setConfig(UiScreenshotConfigKeys.SCREENSHOTS_ENABLED, "true")
                .setConfig(UiScreenshotConfigKeys.FAILURE_ONLY, "false")
                .setConfig(UiScreenshotConfigKeys.CAPTURE_FINAL_STATE, "true")
                .setConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, zoomPercent)
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "2");
    }

    private void runUiFlow(String methodName) {
        TestListener listener = new TestListener();
        ITestResult result = successResult(testClass, methodName);
        UIEngine engine = UIEngineFactory.create();

        try {
            setEngineForCurrentThread(engine);
            listener.onTestStart(result);
            engine.open(zoomEvidenceUrl());
            UiInteractionTracker.markUiInteraction();

            StepLogger.startStep("success");
            StepLogger.logDetail("ui success");
            StepLogger.pass("ok");

            new UiScreenshotFailureHandler().captureFinalStateIfEligible();
            listener.onTestSuccess(result);
            listener.onFinish((org.testng.ISuite) null);
        } finally {
            engine.quit();
            setEngineForCurrentThread(null);
        }
    }

    private static String zoomEvidenceUrl() {
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(
                        "<!doctype html><html><body>"
                                + "<main style='width:900px;height:520px;padding:48px;"
                                + "background:linear-gradient(135deg,#123,#58a);color:white;font-size:42px;'>"
                                + "<h1>Allure zoom evidence</h1>"
                                + "<p>Final screenshot must preserve real browser zoom.</p>"
                                + "</main></body></html>",
                        StandardCharsets.UTF_8
                ).replace("+", "%20");
    }

    private static Path copyScreenshotArtifact(Path screenshot, String uiEngine, String zoomPercent)
            throws IOException {
        Path artifactDir = AllureResultFixture.fixtureRoot().resolve("zoom-comparison");
        Files.createDirectories(artifactDir);
        Path target = artifactDir.resolve(uiEngine + "-" + zoomPercent + ".png");
        Files.copy(screenshot, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }
}
