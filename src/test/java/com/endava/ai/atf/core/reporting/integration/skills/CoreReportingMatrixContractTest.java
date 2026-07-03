package com.endava.ai.atf.core.reporting.integration.skills;

import com.endava.ai.core.support.contract.AtfContractArea;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractProfile;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.endava.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import com.endava.ai.core.reporting.aggregate.model.AggregateSummary;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentSparkReportInspector;
import com.endava.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import com.endava.ai.core.reporting.adapters.extent.synthetic.SyntheticRuntimeInvocation;
import io.qameta.allure.model.Status;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CORE_REPORTING_MATRIX)
@AtfContractRef(AtfContractNarrativeIds.Contract.CORE_REPORTING_MATRIX)
public class CoreReportingMatrixContractTest {
    private static final Path CORE_REPORTING_MATRIX_XML =
            AtfSourcePaths.testResource("atf/core/suites/reporting")
                    .resolve("testng-atf-core-reporting-matrix.xml");
    private static final Path AGENTIC_REPORTING_SANITY_XML =
            AtfSourcePaths.testResource("atf/core/reporting").resolve("testng-reporting-integration.xml");
    private static final Path REPORTING_CONSUMER_SMOKE_XML =
            AtfSourcePaths.testResource("atf/core/reporting").resolve("testng-reporting-consumer-smoke.xml");
    private static final String MATRIX_CLASS = CoreReportingMatrixContractTest.class.getName();
    private static final String BUSINESS_GRAPH_CONSUMER_BRIDGE_CLASS =
            "com.endava.ai.atf.core.reporting.integration.skills.ReportingBusinessGraphConsumerBridgeContractTest";
    private static final String BUSINESS_GRAPH_MCP_COMPATIBILITY_CLASS =
            "com.endava.ai.atf.core.reporting.integration.skills.ReportingBusinessGraphMcpCompatibilityContractTest";
    private static final String ATLAS_PROFILE_CLASS =
            "com.endava.ai.skills.atlas.tests.profile.AtlasTestNgProfileContractTest";
    private static final String ATLAS_CATALOG_CLASS =
            "com.endava.ai.skills.atlas.tests.catalog.AtlasRuleCatalogContractTest";
    private static final String MCP_PROFILE_CLASS =
            "com.endava.ai.skills.mcp.tests.core.profiles.McpTestNgProfileContractTest";
    private static final String MCP_SHAPE_CLASS =
            "com.endava.ai.skills.mcp.tests.core.shape.McpPackageShapeContractTest";
    private static final String RUNTIME_CLASS = "com.endava.ai.atf.core.reporting.matrix.RuntimeMatrixFixture";
    private static final String AGGREGATE_CLASS = "com.endava.ai.atf.core.reporting.matrix.AggregateMatrixFixture";

    @Test
    public void coreReportingMatrixProfileIsSmallAndYamlBacked() throws Exception {
        List<String> classes = classes(CORE_REPORTING_MATRIX_XML);

        Assert.assertEquals(classes, List.of(MATRIX_CLASS),
                "Core reporting matrix should stay a tiny executable contract, not a broad profile clone.");
        String profiles = Files.readString(Path.of("src/main/resources/atf-core/contracts/profiles/profiles.yml"));
        Assert.assertTrue(profiles.contains("id: atf-core-reporting-matrix"),
                "profiles.yml must document the core reporting matrix profile.");
        Assert.assertTrue(profiles.contains(AtfSourcePaths.unixPath(CORE_REPORTING_MATRIX_XML)),
                "profiles.yml must point to the executable TestNG matrix XML.");
    }

    @Test
    public void agenticReportingSanityMixesCoreAtlasAndMcpWithoutBecomingFullSweep() throws Exception {
        List<String> classes = classes(AGENTIC_REPORTING_SANITY_XML);

        Assert.assertEquals(classes, List.of(
                "com.endava.ai.atf.core.reporting.integration.skills.DiagnosticHtmlReportShellContractTest",
                MATRIX_CLASS,
                "com.endava.ai.atf.core.reporting.integration.skills.ExtentSparkCanonicalShellContractTest",
                "com.endava.ai.atf.core.reporting.integration.skills.ExtentFailurePlacementContractTest"
        ));
        Assert.assertTrue(classes.stream().anyMatch(name -> name.contains(".atf.")), "Missing ATF core sample.");
        Assert.assertTrue(classes.stream().anyMatch(name -> name.contains(".integration.skills.")),
                "Missing explicit reporting integration package.");
        Assert.assertTrue(classes.size() <= 6,
                "Reporting integration sanity must remain small enough to run before/after shell refactors.");
    }

    @Test
    public void reportingConsumerSmokeMixesCoreAtlasAndMcpWithoutBecomingFullSweep() throws Exception {
        List<String> classes = classes(REPORTING_CONSUMER_SMOKE_XML);

        Assert.assertEquals(classes, List.of(
                MATRIX_CLASS,
                BUSINESS_GRAPH_CONSUMER_BRIDGE_CLASS,
                BUSINESS_GRAPH_MCP_COMPATIBILITY_CLASS,
                ATLAS_PROFILE_CLASS,
                ATLAS_CATALOG_CLASS,
                MCP_PROFILE_CLASS,
                MCP_SHAPE_CLASS
        ));
        Assert.assertTrue(classes.stream().anyMatch(name -> name.contains(".atf.")), "Missing ATF core sample.");
        Assert.assertTrue(classes.stream().anyMatch(name -> name.contains(".atlas.")), "Missing Atlas sample.");
        Assert.assertTrue(classes.stream().anyMatch(name -> name.contains(".mcp.")), "Missing MCP sample.");
        Assert.assertTrue(classes.size() <= 7,
                "Agentic reporting sanity must remain small enough to run before/after reporting refactors.");
    }

    @Test
    public void runtimeExtentOverviewSupportsPassFailSkipAndDuration() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-runtime-reporting-matrix-");
        String marker = ExtentAdapterContractSupport.uniqueMarker("runtime-matrix");
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())) {
            ExtentAdapterContractSupport.resetExtentRuntime();
            Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                    .test(RUNTIME_CLASS, "matrix_passes", "Matrix pass case")
                    .step("TEST BODY")
                    .detail(marker)
                    .pass("pass evidence")
                    .end(TestStatus.PASS)
                    .test(RUNTIME_CLASS, "matrix_fails", "Matrix fail case")
                    .step("TEST BODY")
                    .fail("controlled failure", "AssertionError: controlled matrix failure")
                    .end(TestStatus.FAIL)
                    .test(RUNTIME_CLASS, "matrix_skips", "Matrix skip case")
                    .step("TEST BODY")
                    .detail("skip evidence")
                    .end(TestStatus.SKIP)
                    .flushFindAndPublishRuntimeAudit(marker, List.of(
                            SyntheticRuntimeInvocation.passed(RUNTIME_CLASS, "matrix_passes", 10L),
                            SyntheticRuntimeInvocation.failed(RUNTIME_CLASS, "matrix_fails", 20L,
                                    "controlled matrix failure"),
                            SyntheticRuntimeInvocation.skipped(RUNTIME_CLASS, "matrix_skips", 30L)
                    ))
                    .report();

            ExtentSparkReportFinalizer.standardizePublishedValidation(report);
            String html = Files.readString(report);
            assertPublishableOverview(html, ExtentReportKind.RUNTIME_VALIDATION);

            ExtentSparkReportInspector inspector = ExtentSparkReportInspector.inspect(html);
            Assert.assertEquals(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_PASSED), "1");
            Assert.assertEquals(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_FAILED), "1");
            Assert.assertEquals(inspector.dashboardTestsCardCount(), "3");
            Assert.assertTrue(inspector.dashboardTestsCard().contains("skipped"),
                    "Runtime Overview Test cases card must keep skipped status evidence.");
            Assert.assertTrue(html.contains(ExtentDashboardLabels.DURATION),
                    "Runtime Overview must expose a Duration top card after standardization.");
        } finally {
            ExtentAdapterContractSupport.resetExtentRuntime();
            ExtentAdapterContractSupport.deleteRecursively(reportsDir);
        }
    }

    @Test
    public void aggregateExtentOverviewSupportsMixedStatusesAndFinalAudit() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-aggregate-reporting-matrix-");
        Path report = reportsDir.resolve("AggregateMatrixExtentReport.html");
        List<AtfCoreAggregateCase> cases = List.of(
                aggregateCase("aggregate_passes", AggregateStatus.PASSED, 100L, 110L, ""),
                aggregateCase("aggregate_fails", AggregateStatus.FAILED, 120L, 140L,
                        "AssertionError: controlled aggregate failure"),
                aggregateCase("aggregate_skips", AggregateStatus.SKIPPED, 150L, 155L, "Skipped by profile")
        );

        try {
            AggregateReportDescriptor descriptor = AggregateReportDescriptor.builder()
                    .testTitle("Core Reporting Matrix Aggregate")
                    .description("Synthetic aggregate matrix for pass, fail and skip Overview policy.")
                    .scopeLabel("core reporting matrix")
                    .fragmentationReason("Small aggregate profile proves mixed-status shell behavior.")
                    .auditShape("Pass/fail/skip cases plus final audit.")
                    .addRunCommand("mvn -q '-Dsurefire.suiteXmlFiles="
                            + AtfSourcePaths.unixPath(CORE_REPORTING_MATRIX_XML) + "' test")
                    .build();

            new AggregateExtentReportWriter().write(
                    report,
                    ReportingConfigKeys.ENGINE_EXTENT,
                    descriptor,
                    AggregateSummary.from(cases),
                    cases
            );

            String html = Files.readString(report);
            assertPublishableOverview(html, ExtentReportKind.AGGREGATE_VALIDATION);
            ExtentSparkReportInspector inspector = ExtentSparkReportInspector.inspect(html);
            Assert.assertEquals(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_PASSED), "1");
            Assert.assertEquals(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_FAILED), "1");
            Assert.assertEquals(inspector.dashboardTestsCardCount(), "3");
            Assert.assertTrue(html.contains("id=\"atf-core-final-audit\""),
                    "Aggregate reports must keep the final audit section.");
            Assert.assertTrue(html.contains("Wall-clock duration") && html.contains("Cumulative test duration"),
                    "Aggregate final audit must keep timing evidence.");
        } finally {
            ExtentAdapterContractSupport.deleteRecursively(reportsDir);
        }
    }

    private static void assertPublishableOverview(String html, ExtentReportKind kind) {
        List<String> violations = ExtentPublishedOverviewInspector.inspect(html).violations(kind);
        Assert.assertTrue(violations.isEmpty(),
                kind + " must satisfy the shared Extent Overview contract: " + violations);
        Assert.assertFalse(html.contains(ExtentDashboardLabels.TEST_CLASSES),
                "Published reporting matrix reports must not use Test classes as score-card unit.");
        Assert.assertFalse(html.contains(ExtentDashboardLabels.STEPS),
                "Published reporting matrix reports must not use Spark Steps score-card vocabulary.");
    }

    private static AtfCoreAggregateCase aggregateCase(
            String method,
            AggregateStatus status,
            long started,
            long ended,
            String stackTrace
    ) {
        Status allureStatus = status == AggregateStatus.PASSED
                ? Status.PASSED
                : status == AggregateStatus.SKIPPED ? Status.SKIPPED : Status.FAILED;
        return new AtfCoreAggregateCase(
                "Core Reporting Matrix",
                "core-reporting-matrix",
                AGGREGATE_CLASS,
                method,
                "",
                AtfContractProfile.REPORTING_CONTRACT.name(),
                "Core reporting mixed status",
                "Validate shared Extent overview and aggregate audit behavior.",
                AtfContractArea.REPORTING_ADAPTER.name(),
                "ATF core reporting",
                "Report shell is uniform for pass/fail/skip.",
                "Synthetic aggregate case generated.",
                "Keep mixed-status aggregate proof routed through AggregateExtentReportWriter.",
                allureStatus,
                started,
                ended,
                status.name(),
                stackTrace
        );
    }

    private static List<String> classes(Path suiteXml) throws Exception {
        Assert.assertTrue(Files.isRegularFile(suiteXml), "Missing TestNG suite XML: " + suiteXml);
        Document document = parseXml(suiteXml);
        NodeList nodes = document.getElementsByTagName("class");
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            names.add(nodes.item(index).getAttributes().getNamedItem("name").getNodeValue());
        }
        return List.copyOf(names);
    }

    private static Document parseXml(Path suiteXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(suiteXml.toFile());
    }
}
