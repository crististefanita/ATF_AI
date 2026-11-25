package com.ai.atf.core.reporting.window;

import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.internal.ReportingPaths;
import com.ai.core.reporting.step.StepScope;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UIEngineFactory;
import com.ai.ui.core.engine.window.UIWindowSizeResolver;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.model.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.appNavigationSourceUrl;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.deleteRecursively;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.documentZoom;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.evaluate;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.extentReportContaining;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.prepareDirectory;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.readAllTextFiles;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.waitUntilUrlContains;
import static com.ai.atf.core.reporting.window.ZoomRuntimeReportingTestSupport.zoomMetadataProbeUrl;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ZOOM_RUNTIME)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_CLASS)
@Listeners(TestListener.class)
public class UIZoomRuntimeReportingContractTest {

    private static final String CONFIGURED_ZOOM            = "60";
    private static final int RESOLVED_ZOOM                 = 67;
    private static final String HEADLESS                   = Boolean.TRUE.toString();

    private AtfRuntimeConfigSandbox config;
    private AllureLifecycle previousAllureLifecycle;
    private Path extentZoomReportsDir;

    @AfterMethod
    public void tearDown() throws IOException {
        if (config != null) {
            config.close();
            config = null;
        }
        if (previousAllureLifecycle != null) {
            Allure.setLifecycle(previousAllureLifecycle);
        }

        ReportingManager.reset();
        StepLogger.clear();
        if (extentZoomReportsDir != null) {
            deleteRecursively(extentZoomReportsDir);
        }
        deleteRecursively(Path.of("target", "extent-zoom-contract").toAbsolutePath());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClassArtifacts() throws IOException {
        deleteRecursively(Path.of("target", "extent-zoom-contract").toAbsolutePath());
    }

    @DataProvider
    public Object[][] zoomEngines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue()},
                {UiEngineType.PLAYWRIGHT.configValue()}
        };
    }

    @Test(dataProvider = "zoomEngines")
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_PLAYWRIGHT_DOCUMENT)
    public void headless_engine_applies_resolved_zoom_to_loaded_document(String engineName) throws Exception {
        configureZoomedHeadless(engineName);

        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "headless_engine_applies_resolved_zoom_to_loaded_document"
        );
        AtfContractSteps.expected(contract);

        int resolvedZoom = UIWindowSizeResolver.resolve().zoomPercent();
        String appliedZoom;
        UIEngine engine = UIEngineFactory.create();
        try {
            try (StepScope step = StepLogger.step("Open zoom probe page")) {
                engine.open(zoomMetadataProbeUrl());
                appliedZoom = documentZoom(engine);
                step.pass("Zoom probe page inspected");
            }
        } finally {
            engine.quit();
        }

        AtfContractSteps.observed("configured.zoom=" + CONFIGURED_ZOOM
                + " | resolved.zoom=" + resolvedZoom
                + " | engine=" + engineName
                + " | applied.documentElement.style.zoom=" + appliedZoom);

        Assert.assertEquals(resolvedZoom, RESOLVED_ZOOM);
        Assert.assertEquals(appliedZoom, RESOLVED_ZOOM + "%");
        AtfContractSteps.verdict(
                "Runtime applies the normalized zoom before UI interactions are evaluated.",
                contract.repairHint()
        );
    }

    @Test(dataProvider = "zoomEngines")
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_NAVIGATION_SCREENSHOT)
    public void headless_engine_reapplies_resolved_zoom_after_app_driven_navigation_and_screenshot(
            String engineName
    ) throws Exception {
        configureZoomedHeadless(engineName);

        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "headless_engine_reapplies_resolved_zoom_after_app_driven_navigation_and_screenshot"
        );
        AtfContractSteps.expected(contract);

        String initialZoom;
        String afterAppNavigationZoom;
        String afterScreenshotZoom;
        UIEngine engine = UIEngineFactory.create();
        try {
            try (StepScope step = StepLogger.step("Open zoom navigation screenshot flow")) {
                engine.open(appNavigationSourceUrl());
                initialZoom = documentZoom(engine);

                engine.click("#next");
                waitUntilUrlContains(engine, "zoom-target");
                afterAppNavigationZoom = documentZoom(engine);

                evaluate(engine, "document.documentElement.style.zoom = '';");
                engine.captureScreenshotAsBase64();
                afterScreenshotZoom = documentZoom(engine);
                step.pass("Zoom navigation screenshot flow inspected");
            }
        } finally {
            engine.quit();
        }

        AtfContractSteps.observed("configured.zoom=" + CONFIGURED_ZOOM
                + " | resolved.zoom=" + RESOLVED_ZOOM
                + " | engine=" + engineName
                + " | initial.document.zoom=" + initialZoom
                + " | after.app.navigation.document.zoom=" + afterAppNavigationZoom
                + " | after.screenshot.document.zoom=" + afterScreenshotZoom);

        Assert.assertEquals(initialZoom, RESOLVED_ZOOM + "%");
        Assert.assertEquals(afterAppNavigationZoom, RESOLVED_ZOOM + "%");
        Assert.assertEquals(afterScreenshotZoom, RESOLVED_ZOOM + "%");
        AtfContractSteps.verdict(
                "Zoom survives page-owned navigation and final screenshot capture uses the current resolved zoom.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_EXTENT_METADATA)
    public void extent_report_exposes_zoom_runtime_metadata() throws Exception {
        configureZoomedHeadless(UiEngineType.PLAYWRIGHT.configValue());

        assertExtentReportExposesZoomRuntimeMetadata(UiEngineType.PLAYWRIGHT.configValue());
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_EXTENT_METADATA)
    public void extent_report_exposes_selenium_zoom_runtime_metadata() throws Exception {
        configureZoomedHeadless(UiEngineType.SELENIUM.configValue());

        assertExtentReportExposesZoomRuntimeMetadata(UiEngineType.SELENIUM.configValue());
    }

    private void assertExtentReportExposesZoomRuntimeMetadata(String engineName) throws Exception {

        Path reportsDir = prepareDirectory(Files.createTempDirectory("atf-extent-zoom-contract-"));
        extentZoomReportsDir = reportsDir;
        ensureSandbox()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED);
        ExtentAdapter.getInstance().resetForTests();

        String marker = "extentZoomRuntimeMetadata-" + engineName;
        generateReportBackedZoomedFlow(marker);

        Path report = extentReportContaining(marker, reportsDir);
        String html = Files.readString(report).toLowerCase(Locale.ROOT);

        assertZoomMetadata(html, "Extent report", report, engineName);
        deleteRecursively(reportsDir);
    }

    @Test(groups = AtfTestGroups.ALLURE_ONLY)
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ZOOM_RUNTIME_ALLURE_METADATA)
    public void allure_result_exposes_zoom_runtime_metadata() throws Exception {
        configureZoomedHeadless(UiEngineType.PLAYWRIGHT.configValue());

        Path resultsDir = prepareDirectory(Files.createTempDirectory("atf-allure-zoom-contract-"));
        try {
            ensureSandbox()
                    .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_ALLURE)
                    .setSystemProperty(ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY, resultsDir.toString());
            previousAllureLifecycle = Allure.getLifecycle();
            Allure.setLifecycle(new AllureLifecycle(new FileSystemResultsWriter(resultsDir)));

            String uuid = UUID.randomUUID().toString();
            Allure.getLifecycle().scheduleTestCase(
                    new io.qameta.allure.model.TestResult()
                            .setUuid(uuid)
                            .setName("allureZoomRuntimeMetadata")
            );
            Allure.getLifecycle().startTestCase(uuid);
            generateReportBackedZoomedFlow("allureZoomRuntimeMetadata");
            Allure.getLifecycle().updateTestCase(uuid, testResult -> testResult.setStatus(Status.PASSED));
            Allure.getLifecycle().stopTestCase(uuid);
            Allure.getLifecycle().writeTestCase(uuid);

            String resultPayload = readAllTextFiles(resultsDir).toLowerCase(Locale.ROOT);
            assertZoomMetadata(resultPayload, "Allure result", resultsDir, UiEngineType.PLAYWRIGHT.configValue());
        } finally {
            deleteRecursively(resultsDir);
        }
    }

    private void generateReportBackedZoomedFlow(String testName) throws Exception {
        ReportingManager.reset();
        StepLogger.clearDelegate();
        ReportLogger logger = ReportingManager.getLogger();
        logger.startTest(testName, "Zoom runtime metadata contract");
        StepLogger.setDelegate(logger);

        UIEngine engine = UIEngineFactory.create();
        try {
            StepLogger.startStep("Open zoomed UI page");
            StepLogger.logDetail("metadata.marker=" + testName);
            engine.open(zoomMetadataProbeUrl());
            StepLogger.pass("Zoomed UI page opened");
            logger.endTest(TestStatus.PASS);
            logger.flush();
        } finally {
            engine.quit();
            StepLogger.clearDelegate();
        }
    }

    private static void assertZoomMetadata(String payload, String artifactName, Path artifact, String engineName) {
        Assert.assertTrue(
                payload.contains(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY),
                artifactName + " must include the configured zoom key: "
                        + UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY + " | artifact=" + artifact
        );
        Assert.assertTrue(
                payload.contains(CONFIGURED_ZOOM),
                artifactName + " must include configured zoom=" + CONFIGURED_ZOOM + " | artifact=" + artifact
        );
        Assert.assertTrue(
                payload.contains("resolved") && payload.contains(Integer.toString(RESOLVED_ZOOM)),
                artifactName + " must include resolved zoom=" + RESOLVED_ZOOM + " | artifact=" + artifact
        );
        Assert.assertTrue(
                payload.contains(engineName),
                artifactName + " must include ui.engine=" + engineName + " | artifact=" + artifact
        );
        Assert.assertTrue(
                payload.contains("headless") && payload.contains(HEADLESS),
                artifactName + " must include ui.headless=" + HEADLESS + " | artifact=" + artifact
        );
    }

    private void configureZoomedHeadless(String engineName) {
        ensureSandbox()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, HEADLESS)
                .setConfig(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, CONFIGURED_ZOOM)
                .setConfig(UiRuntimeOptions.UI_RUNTIME_METADATA_REPORTING_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WINDOW_MODE_KEY, UiRuntimeOptions.WINDOW_MODE_FULLSCREEN)
                .clearConfig(UiRuntimeOptions.UI_WINDOW_SIZE_KEY)
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "10");
    }

    private AtfRuntimeConfigSandbox ensureSandbox() {
        if (config == null) {
            config = AtfRuntimeConfigSandbox.open();
        }
        return config;
    }

}
