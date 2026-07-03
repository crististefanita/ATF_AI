package com.endava.ai.atf.core.reporting.adapters;

import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.support.contract.wait.AtfTestWaiter;
import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportSettings;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterSettings;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;

import static com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.countExtentHtmlReports;
import static com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.currentExtentTest;
import static com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.deleteRecursively;
import static com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.extentReportContaining;
import static com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.uniqueMarker;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_ADAPTER)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentAdapterTest {
    private static final Path CUSTOM_EXTENT_ROOT = AtfSourcePaths.TARGET_ROOT.resolve("custom-extent");
    private static final String ATLAS_EXTENT_REPORT_FILE = "html-ex_atlas-tests-extent-report.html";
    private AtfRuntimeConfigSandbox config;
    private Path contractReportsDir;

    @BeforeMethod
    public void setUpExtentContext() throws IOException {
        contractReportsDir = Files.createTempDirectory("atf-extent-adapter-contract-");
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, contractReportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED);
        ExtentAdapter.getInstance().resetForTests();
    }

    @AfterMethod
    public void tearDownExtentContext() throws IOException {
        if (config != null) {
            config.close();
            config = null;
        }
        ExtentAdapter.getInstance().resetForTests();
        if (contractReportsDir != null) {
            deleteRecursively(contractReportsDir);
            contractReportsDir = null;
        }
    }

    @Test
    public void extent_adapter_escapes_html_like_step_details() throws IOException {
        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String marker = uniqueMarker("mcp-html-escape-contract");

        adapter.startTest(marker, "HTML-like MCP details must remain text");
        adapter.startStep("Expected <details> contract");
        adapter.logDetail("Expected: <details id=\"executive-context\"><summary>Runtime</summary>");
        adapter.logDetail("Forbidden: <style>.broken{display:block}</style>");
        adapter.pass("Observed <style> and <details> as text");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        String html = Files.readString(extentReportContaining(marker));
        assertTrue(html.contains("&lt;details id=&quot;executive-context&quot;&gt;"));
        assertTrue(html.contains("&lt;style&gt;.broken{display:block}&lt;/style&gt;"));
        assertFalse(html.contains("<details id=\"executive-context\"><summary>Runtime</summary>"));
        assertFalse(html.contains("<style>.broken{display:block}</style>"));
    }

    @Test
    public void extent_adapter_generates_single_html_without_local_spark_assets() throws IOException {
        Path localSparkAssets = contractReportsDir.resolve("spark");
        deleteRecursively(localSparkAssets);

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String marker = uniqueMarker("extent-single-html-no-local-spark-contract");

        adapter.startTest(marker, "Runtime Extent output must be a single HTML file");
        adapter.startStep("Generated report shape");
        adapter.logDetail("Expected: one ExtentReport.html fixture only; no target/reports/spark folder.");
        adapter.logDetail("Expected: no local href/src references to spark/* assets.");
        adapter.pass("Runtime report shape recorded");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        Path htmlReport = extentReportContaining(marker);
        String html = Files.readString(htmlReport);

        assertTrue(html.contains(marker));
        assertTrue(
                html.contains("extent-atf-collapsed"),
                "Extent report must collapse test nodes by default so class grouping remains readable"
        );
        assertFalse(Files.exists(localSparkAssets),
                "Extent runtime report must not generate a target/reports/spark asset folder");
        assertFalse(html.contains("href=\"spark/"),
                "Extent runtime report must not depend on local spark CSS beside the HTML");
        assertFalse(html.contains("src=\"spark/"),
                "Extent runtime report must not depend on local spark JS/image assets beside the HTML");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_COMPONENT_OVERVIEW)
    public void extent_adapter_flush_standardizes_component_overview() throws IOException {
        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String marker = uniqueMarker("extent-component-overview-standard-contract");

        adapter.startTest("com.endava.ai.atf.core.reporting.adapters.ComponentOverviewContract", marker,
                "Small component Extent reports must still use the ATF Overview shell.");
        adapter.startStep("Overview standard");
        adapter.logDetail("Expected: component mode marker and ATF tag tables after flush.");
        adapter.pass("Component report content recorded");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        String html = Files.readString(extentReportContaining(marker));

        assertTrue(html.contains("id=\"" + ExtentMarkers.REPORT_MODE + "\"")
                        && html.contains("\"mode\":\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\"")
                        && html.contains("\"runtimeAudit\":false")
                        && html.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"false\""),
                "Component Extent reports must be mode-marked during flush, not left as raw Spark HTML.");
        assertTrue(html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                "Component Extent Overview must publish the ATF component tag audit.");
        assertTrue(html.contains("collapseExtentTreeByDefault")
                        && html.contains("extent-atf-collapsed")
                        && html.contains("extent-atf-collapse-ready"),
                "Persisted Extent reports must carry the executable ATF collapse runtime, not only collapse CSS tokens.");
        assertTrue(html.contains(ExtentMarkers.NAVIGATION_GUARD),
                "Persisted Extent reports must pass through the shared finalizer so tab selection and active nav state stay uniform.");
        assertEquals(count(html, ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()), 3,
                "Component Extent Overview must expose classes, packages, and annotations tag tables.");
        assertFalse(html.contains("<h6 class=\"card-title\"></h6>"),
                "Component Extent Overview must not expose Spark's blank grandchild score card.");
        assertFalse(html.contains("<div class=\"card-header\"><p>Tags</p></div>"),
                "Component Extent Overview must replace Spark's native mixed Tags card with ATF tag tables.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_DEFAULT_REPORT_DIR)
    public void extent_default_report_directory_is_target_reports() throws IOException {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(ConfigManager.FRAMEWORK_PROPERTIES_SOURCE)) {
            properties.load(input);
        }

        assertEquals(
                properties.getProperty(ReportingConfigKeys.REPORTS_DIR),
                ReportingConfigKeys.DEFAULT_REPORTS_DIR,
                "Extent users open target/reports/ExtentReport_*.html; the default must not point at target/allure."
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_FIXTURE_SANDBOX)
    public void extent_adapter_contract_fixtures_use_sandbox_reports_directory() {
        assertTrue(
                contractReportsDir.toAbsolutePath().toString().contains("atf-extent-adapter-contract-"),
                "Contract fixture reports must use a temporary sandbox directory."
        );
        assertFalse(
                contractReportsDir.toAbsolutePath().toString().contains("target" + java.io.File.separator + "reports"),
                "Contract fixture reports must stay outside the final runtime reports directory."
        );
        assertFalse(
                contractReportsDir.toAbsolutePath().toString().contains("target" + java.io.File.separator + "contract-reports"),
                "Contract fixture reports must not leave secondary Extent artifacts under target/contract-reports."
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_STABLE_ARTIFACT)
    public void extent_report_settings_resolve_stable_non_timestamped_artifact() {
        ExtentReportSettings settings = ExtentReportSettings.fromValues(
                AtfSourcePaths.unixPath(CUSTOM_EXTENT_ROOT),
                false,
                "yyyy-MM-dd_HH-mm-ss",
                new Date(0),
                "Doc",
                "Name"
        );

        assertEquals(
                settings.reportPath(),
                CUSTOM_EXTENT_ROOT.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME)
        );
        assertEquals(settings.documentTitle(), "Doc");
        assertEquals(settings.reportName(), "Name");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_TIMESTAMPED_ARTIFACT)
    public void extent_report_settings_resolve_timestamped_artifact_from_runtime_format() {
        ExtentReportSettings settings = ExtentReportSettings.fromValues(
                AtfSourcePaths.unixPath(CUSTOM_EXTENT_ROOT),
                true,
                "yyyyMMdd",
                new Date(0),
                ExtentReportSettings.DEFAULT_DOCUMENT_TITLE,
                ExtentReportSettings.DEFAULT_REPORT_NAME
        );

        assertEquals(
                settings.reportPath(),
                CUSTOM_EXTENT_ROOT.resolve(ExtentReportFiles.fileName(true, "yyyyMMdd", new Date(0)))
        );
        assertEquals(settings.documentTitle(), ExtentReportSettings.DEFAULT_DOCUMENT_TITLE);
        assertEquals(settings.reportName(), ExtentReportSettings.DEFAULT_REPORT_NAME);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_EXPLICIT_FILE_NAME)
    public void extent_report_settings_resolve_explicit_report_file_name_from_runtime_config() {
        config.setConfigAndSystemProperty(
                ReportingConfigKeys.REPORTS_FILE_NAME,
                ATLAS_EXTENT_REPORT_FILE
        );

        ExtentReportSettings settings = ExtentReportSettings.fromRuntime(new Date(0));

        assertEquals(
                settings.reportPath(),
                contractReportsDir.resolve(ATLAS_EXTENT_REPORT_FILE)
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_FILE_NAMES_HELPER)
    public void extent_report_file_names_are_canonicalized_in_one_helper() {
        assertEquals(
                ExtentReportFiles.fileName(false, "yyyyMMdd", new Date(0)),
                ExtentReportFiles.STABLE_REPORT_FILE_NAME
        );
        assertEquals(
                ExtentReportFiles.fileName(true, "yyyyMMdd", new Date(0)),
                ExtentReportFiles.TIMESTAMP_FILE_PREFIX + "19700101" + ExtentReportFiles.REPORT_FILE_EXTENSION
        );
        assertTrue(ExtentReportFiles.isExtentHtmlReport(ExtentReportFiles.STABLE_REPORT_FILE_NAME));
        assertTrue(ExtentReportFiles.isExtentHtmlReport("ExtentReport_2026-06-27_10-16-08.html"));
        assertFalse(ExtentReportFiles.isExtentHtmlReport("index.html"));
        assertFalse(ExtentReportFiles.isExtentHtmlReport("ExtentReport_2026-06-27.txt"));
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_RUNTIME_SETTINGS)
    public void extent_report_settings_read_active_runtime_configuration() {
        ExtentReportSettings settings = ExtentReportSettings.fromRuntime(new Date(0));

        assertEquals(settings.reportPath(), contractReportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME));
        assertEquals(settings.documentTitle(), ExtentReportSettings.DEFAULT_DOCUMENT_TITLE);
        assertEquals(settings.reportName(), ExtentReportSettings.DEFAULT_REPORT_NAME);
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_RUNTIME_SETTINGS)
    public void extent_adapter_reset_rebinds_reporter_to_current_runtime_configuration() throws IOException {
        ExtentAdapter adapter = ExtentAdapter.getInstance();
        Path originalPath = adapter.currentReportPath();
        Path reboundReportsDir = Files.createTempDirectory("atf-extent-adapter-rebound-contract-");
        String marker = uniqueMarker("extent-runtime-rebind-contract");

        try {
            config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reboundReportsDir.toString());
            adapter.resetForTests();

            adapter.startTest(marker, "Extent adapter must honor the current runtime reports.dir after reset.");
            adapter.endTest(TestStatus.PASS);
            adapter.flush();

            Path reboundPath = reboundReportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME);
            assertEquals(adapter.currentReportPath(), reboundPath,
                    "Reset must recreate the Extent reporter from the active runtime configuration.");
            assertTrue(Files.readString(reboundPath).contains(marker),
                    "The new runtime report must contain the test body written after rebinding.");
            assertFalse(originalPath.equals(reboundPath),
                    "A rebinding contract must prove the adapter did not keep the old report path.");
        } finally {
            deleteRecursively(reboundReportsDir);
        }
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_RUN_SCOPED_TIMESTAMP)
    public void extent_timestamped_runtime_reuses_one_report_artifact_across_adapter_recreation()
            throws Exception {
        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_ENABLED)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_FORMAT, "yyyy-MM-dd_HH-mm-ss");
        ExtentAdapter adapter = ExtentAdapter.getInstance();

        adapter.resetForTests();
        adapter.startTest(
                "com.endava.ai.atf.core.reporting.lifecycle.FirstLifecycleContract",
                "first_lifecycle_contract",
                "First class in the same runtime"
        );
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        AtfTestWaiter.pause(Duration.ofMillis(1_200));

        adapter.resetForTests();
        adapter.startTest(
                "com.endava.ai.atf.core.reporting.failurefixtures.SecondLifecycleContract",
                "second_lifecycle_contract",
                "Second class in the same runtime"
        );
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        assertEquals(
                countExtentHtmlReports(),
                1,
                "Timestamped Extent reporting must produce one report artifact for the active runtime."
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_ADAPTER_SPARK_REPORTER_SETTINGS)
    public void extent_spark_reporter_settings_preserve_report_configuration() {
        ExtentReportSettings reportSettings = ExtentReportSettings.fromValues(
                AtfSourcePaths.unixPath(CUSTOM_EXTENT_ROOT),
                false,
                "yyyyMMdd",
                new Date(0),
                "Document",
                "Report"
        );

        ExtentSparkReporterSettings sparkSettings =
                ExtentSparkReporterSettings.fromReportSettings(reportSettings);

        assertEquals(
                sparkSettings.reportPath(),
                CUSTOM_EXTENT_ROOT.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME)
        );
        assertEquals(sparkSettings.documentTitle(), "Document");
        assertEquals(sparkSettings.reportName(), "Report");
    }

    @Test
    public void extent_adapter_groups_tests_under_class_nodes_by_default() throws Exception {
        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String className = "com.endava.ai.atf.core.reporting.adapters.ExtentAdapterClassGroupingContract";
        String methodName = "method_is_child_of_class";

        adapter.startTest(className, methodName, "Default Extent hierarchy must be class -> method");
        adapter.startStep("Hierarchy contract");
        adapter.logDetail("Expected: class node owns method node in the Extent test tree.");
        adapter.pass("Hierarchy contract recorded");

        com.aventstack.extentreports.ExtentTest current = currentExtentTest(adapter);
        assertTrue(
                current.getModel().getName().contains(methodName),
                "Current Extent node must represent the method, not the class"
        );
        assertTrue(
                current.getModel().getParent().getName().contains("ExtentAdapterClassGroupingContract"),
                "Method node must be grouped under the simple class name"
        );

        adapter.endTest(TestStatus.PASS);
        adapter.flush();
    }

    private static int count(String text, String needle) {
        int matches = 0, next = text.indexOf(needle);
        while (next >= 0) {
            matches++;
            next = text.indexOf(needle, next + needle.length());
        }
        return matches;
    }
}
