package com.endava.ai.atf.core.reporting.integration.skills;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContract;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture;
import com.endava.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture;
import com.endava.ai.atf.core.reporting.failurefixtures.FailureFixtureSwitch;
import com.endava.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.endava.ai.skills.atlas.contract.AtlasSuiteIds;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_FAILURE_PLACEMENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_FAILURE_PLACEMENT_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentFailurePlacementContractTest {

    private AtfRuntimeConfigSandbox config;
    private Path reportsDir;

    @BeforeMethod
    public void setUp() throws IOException {
        reportsDir = Files.createTempDirectory("atf-extent-failure-placement-");
        config = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_DISABLED);
        resetRuntime();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        if (config != null) {
            config.close();
            config = null;
        }
        resetRuntime();
        if (reportsDir != null) {
            deleteRecursively(reportsDir);
            reportsDir = null;
        }
    }

    @Test
    public void extent_report_distinguishes_before_test_body_and_after_failures() throws IOException {
        TestListenerAdapter observed = runFailureFixtures();
        String html = Files.readString(
                reportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME),
                StandardCharsets.UTF_8
        );

        Assert.assertTrue(
                html.contains("fixture-before-method-failure"),
                "A @BeforeMethod failure must be visible in the generated Extent report."
        );
        Assert.assertTrue(
                html.contains("fixture-test-body-failure"),
                "A test-body failure must be visible in the generated Extent report."
        );
        Assert.assertTrue(
                html.contains("fixture-after-method-failure"),
                "An @AfterMethod failure must be visible in the generated Extent report."
        );
        Assert.assertEquals(
                observed.getFailedTests().size(),
                1,
                "The fixture must keep exactly one failed @Test invocation while setup and teardown fail as configuration events."
        );
        Assert.assertTrue(
                observed.getConfigurationFailures().size() >= 2,
                "The fixture must prove setup and teardown failures through TestNG configuration failures."
        );
    }

    @Test
    public void extent_timestamped_failure_fixture_run_writes_one_report_artifact() throws IOException {
        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_ENABLED)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_FORMAT, "yyyy-MM-dd_HH-mm-ss");
        resetRuntime();

        TestListenerAdapter observed = runFailureFixtures();
        List<Path> reports = extentReports();

        Assert.assertEquals(
                reports.size(),
                1,
                "A mixed fixture run with setup, test-body, and teardown failures must still produce one Extent report."
        );
        String html = Files.readString(reports.get(0), StandardCharsets.UTF_8);
        Assert.assertTrue(
                html.contains("fixture-before-method-failure")
                        && html.contains("fixture-test-body-failure")
                        && html.contains("fixture-after-method-failure"),
                "The single timestamped report must retain every controlled failure signal."
        );
        Assert.assertEquals(
                observed.getFailedTests().size(),
                1,
                "The fixture must keep exactly one failed @Test invocation in the timestamped run."
        );
    }

    @Test
    public void extent_report_keeps_test_body_invocation_detail_for_silent_passing_tests() throws IOException {
        TestListenerAdapter observed = runFixtures(SilentPassingFixture.class);
        String html = Files.readString(
                reportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME),
                StandardCharsets.UTF_8
        );

        Assert.assertEquals(
                observed.getPassedTests().size(),
                1,
                "The fixture must keep one silent passing invocation."
        );
        Assert.assertTrue(
                html.contains("TEST BODY"),
                "The generated Extent report must keep the TEST BODY node for silent passing tests."
        );
        Assert.assertTrue(
                html.contains("Test body invocation: "
                        + SilentPassingFixture.class.getName()
                        + "#silentPassingTestBody"),
                "A passing test without StepLogger calls must still expose an auditable body invocation detail."
        );
        Assert.assertTrue(
                html.contains("Expected framework contract")
                        && html.contains("Silent contract fixture")
                        && html.contains("Expected invariant: Silent passing contracts still publish body context."),
                "Silent @AtfContract tests must publish expected contract metadata inside the report body."
        );
    }

    @Test
    public void extent_report_keeps_mcp_check_ref_details_for_silent_passing_tests() throws IOException {
        runFixtures(SilentMcpCheckFixture.class);
        String html = Files.readString(
                reportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME),
                StandardCharsets.UTF_8
        );

        Assert.assertTrue(
                html.contains("MCP check ref: mcp.gate.final-closeout.origin-command-visible"),
                "A silent @McpCheckRef method must publish the referenced YAML check id."
        );
        Assert.assertTrue(
                html.contains("MCP expected check: Final report hyperlinks validation/mcp/extent-origin.json")
                        && html.contains("MCP asserted artifacts: analysis/langgraph-business-understanding.html")
                        && html.contains("MCP repair hint: Show mvn, testng-mcp-package-closeout.xml"),
                "The report body must include YAML-backed MCP expected/artifact/repair details, not only the method name."
        );
        Assert.assertTrue(
                html.contains("Observed validator result: MCP YAML-backed validator completed for "
                        + "mcp.gate.final-closeout.origin-command-visible.")
                        && html.contains("Cold framework verdict")
                        && html.contains("Repair hint if this fails: Show mvn, testng-mcp-package-closeout.xml"),
                "A non-smoke MCP contract test must publish observed result, cold verdict and repair hint."
        );
    }

    @Test
    public void extent_report_keeps_atlas_suite_details_for_silent_contract_tests() throws IOException {
        runFixtures(SilentAtlasSuiteFixture.class);
        String html = Files.readString(
                reportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME),
                StandardCharsets.UTF_8
        );

        Assert.assertTrue(
                html.contains("Atlas suite ref: " + AtlasSuiteIds.YAML_SUITE_CATALOG)
                        && html.contains("Atlas profile: BASE")
                        && html.contains("Atlas artifact: docs/atlas/business-test-atlas.html"),
                "A silent Atlas contract class must publish YAML-backed suite metadata."
        );
        Assert.assertTrue(
                html.contains("Observed validator result: Atlas YAML-backed suite contract completed for "
                        + AtlasSuiteIds.YAML_SUITE_CATALOG + ".")
                        && html.contains("Cold framework verdict")
                        && html.contains("Repair Atlas source or generated atlas artifact declared by "
                        + AtlasSuiteIds.YAML_SUITE_CATALOG),
                "A non-smoke Atlas contract test must publish observed result, cold verdict and repair hint."
        );
    }

    private TestListenerAdapter runFailureFixtures() {
        return runFixtures(
                BeforeMethodFailureFixture.class,
                TestBodyFailureFixture.class,
                AfterMethodFailureFixture.class
        );
    }

    private TestListenerAdapter runFixtures(Class<?>... fixtureClasses) {
        XmlSuite suite = new XmlSuite();
        suite.setName("ATF reporting lifecycle fixture suite");

        XmlTest test = new XmlTest(suite);
        test.setName("reporting-lifecycle-fixtures");
        test.setXmlClasses(List.of(fixtureClasses).stream()
                .map(XmlClass::new)
                .collect(Collectors.toList()));

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(FailureFixtureSwitch.ENABLED_PROPERTY, "true")) {
            TestListenerAdapter observed = new TestListenerAdapter();
            TestNG testng = new TestNG();
            testng.setXmlSuites(List.of(suite));
            testng.setUseDefaultListeners(false);
            testng.setOutputDirectory(reportsDir.resolve("testng-output").toString());
            testng.addListener(new TestListener());
            testng.addListener(observed);
            testng.setVerbose(0);
            testng.run();

            ExtentAdapter.getInstance().flush();
            ExtentRuntimeAuditRegistry.publishIfExtent();
            return observed;
        }
    }

    private static void resetRuntime() {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
        ExtentRuntimeAuditRegistry.clear();
        ExtentAdapter.getInstance().resetForTests();
    }

    private List<Path> extentReports() throws IOException {
        try (var files = Files.list(reportsDir)) {
            return files
                    .filter(ExtentFailurePlacementContractTest::isExtentReport)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static boolean isExtentReport(Path path) {
        return ExtentReportFiles.isExtentHtmlReport(path.getFileName().toString());
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    public static final class SilentPassingFixture {
        @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_FAILURE_PLACEMENT_CONTRACT_TEST_SILENT_PASSING_TEST_BODY)
        @Test
        public void silentPassingTestBody() {
            Assert.assertTrue(true);
        }
    }

    public static final class SilentMcpCheckFixture {
        @Test
        @McpCheckRef("mcp.gate.final-closeout.origin-command-visible")
        public void silentMcpCheckBody() {
            Assert.assertTrue(true);
        }
    }

    @AtlasContractSuiteRef(AtlasSuiteIds.YAML_SUITE_CATALOG)
    public static final class SilentAtlasSuiteFixture {
        @Test
        public void silentAtlasContractBody() {
            Assert.assertTrue(true);
        }
    }
}
