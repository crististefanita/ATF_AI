package com.ai.atf.core.reporting.aggregate;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportSectionIds;
import com.ai.core.reporting.adapters.extent.inspection.ExtentSparkReportInspector;
import com.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.atf.core.reporting.aggregate.AtfCoreAggregateExtentFixtures.aggregateFixture;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateExtentContractAssertions.assertRuntimeAuditedAggregateMode;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateExtentContractAssertions.assertRuntimeAuditedAggregateShell;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateExtentContractAssertions.assertRuntimeOverviewTotals;
import static com.ai.atf.core.reporting.aggregate.assertions.AggregateExtentContractAssertions.containsAcrossMarkup;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.clearRuntimeCases;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.invokeAggregateAppend;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.invokeAggregateExtentWriter;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.invokeLatestExtentReport;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.recordRuntimeCase;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.resetHtmlAuditFlag;
import static com.ai.atf.core.reporting.aggregate.listener.AtfCoreAggregateReportListenerHarness.result;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class AtfCoreAggregateExtentContractTest {
    @Test
    public void aggregate_extent_dashboard_top_cards_are_reconciled_from_runtime_ledger() {
        String sparkHtml = ""
                + "<html><body>"
                + "<p class=\"m-b-0 text-pass\">" + ExtentDashboardLabels.TESTS_PASSED + "</p>"
                + "<h3 class=\"text-white\">110</h3>"
                + "<p class=\"m-b-0 text-fail\">" + ExtentDashboardLabels.TESTS_FAILED + "</p>"
                + "<h3 class=\"text-white\">7</h3>"
                + "</body></html>";

        String reconciled = AtfCoreAggregateReportListener.reconcileSparkTopCards(sparkHtml, 90, 1);

        Assert.assertTrue(
                reconciled.contains("<p class=\"m-b-0 text-pass\">" + ExtentDashboardLabels.TESTS_PASSED
                        + "</p><h3>90</h3>"),
                "Aggregate Extent final writer must publish runtime passed test cases, not Spark node totals."
        );
        Assert.assertTrue(
                reconciled.contains("<p class=\"m-b-0 text-fail\">" + ExtentDashboardLabels.TESTS_FAILED
                        + "</p><h3>1</h3>"),
                "Aggregate Extent final writer must publish runtime failed test cases, not Spark node totals."
        );
        Assert.assertFalse(
                reconciled.contains("<h3 class=\"text-white\">110</h3>"),
                "Stale Spark top-card pass count must be replaced."
        );
        Assert.assertFalse(
                reconciled.contains("<h3 class=\"text-white\">7</h3>"),
                "Stale Spark top-card fail count must be replaced."
        );
    }

    @Test
    public void aggregate_extent_final_writer_preserves_runtime_ledger_as_dashboard_authority() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-aggregate-extent-contract-");
        Path[] report = new Path[1];

        resetHtmlAuditFlag();
        clearRuntimeCases();
        ExtentRuntimeAuditRegistry.clear();
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())) {
            ExtentAdapterContractSupport.resetExtentRuntime();
            String marker = "aggregate_runtime_ledger_authority_contract";
            report[0] = ExtentSyntheticReportBuilder.in(reportsDir)
                    .test(
                            aggregateFixture("AggregateRuntimeLedgerFixture"),
                            marker,
                            "Synthetic standard Spark report for aggregate runtime ledger reconciliation."
                    )
                    .step("TEST BODY")
                    .pass("standard Spark shell fixture completed")
                    .end(TestStatus.PASS)
                    .flushAndFind(marker)
                    .report();

            ITestResult first = result("runtime_one", AtfCoreAggregateExtentContractTest.class, ITestResult.SUCCESS);
            ITestResult second = result("runtime_two", AtfCoreAggregateExtentContractTest.class, ITestResult.SUCCESS);
            ExtentRuntimeAuditRegistry.record(first, "PASSED");
            ExtentRuntimeAuditRegistry.record(second, "PASSED");
            recordRuntimeCase(first, io.qameta.allure.model.Status.PASSED);
            recordRuntimeCase(second, io.qameta.allure.model.Status.PASSED);

            invokeAggregateAppend();
        } finally {
            ExtentRuntimeAuditRegistry.clear();
            clearRuntimeCases();
            resetHtmlAuditFlag();
        }

        String html = Files.readString(report[0]);
        Assert.assertEquals(dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED), "2");
        Assert.assertEquals(dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED), "0");
        Assert.assertTrue(
                html.contains("<th>Reportable Extent test cases</th><td>2</td>"),
                "Final audit must expose the same runtime ledger total as the reconciled dashboard cards."
        );
        assertRuntimeAuditedAggregateMode(html);
        assertRuntimeOverviewTotals(html, 2);
        Assert.assertFalse(
                html.contains("<th>Reportable Extent test cases</th><td>110</td>"),
                "Final audit must not fall back to stale aggregate/Spark totals when runtime ledger rows exist."
        );
        Assert.assertTrue(
                html.contains("id=\"atf-core-final-audit\" class=\"atf-core-final-audit\""),
                "Final audit must be emitted as a semantic block styled by ExtentSparkTheme."
        );
        Assert.assertTrue(
                html.contains("dashboard.appendChild(audit)"),
                "Final audit must relocate into the Overview dashboard instead of rendering below the Tests view."
        );
        Assert.assertTrue(
                html.contains("class=\"atf-core-final-audit-table\"")
                        && html.contains("class=\"atf-core-final-audit-code\""),
                "Final audit table and inventory must use reusable CSS classes, not local inline visual rules."
        );
        Assert.assertFalse(
                html.contains("style=\"margin:24px")
                        || html.contains("font-family:Arial")
                        || html.contains("background:#101b29")
                        || html.contains("border:1px solid #40546a"),
                "Final audit must not bring back the old inline theme that drifted from the agreed Extent skin."
        );
    }

    @Test
    public void aggregate_extent_final_writer_uses_configured_reports_dir_not_stale_target_reports() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-aggregate-configured-reports-");
        Path configuredReport = timestampedExtentReport(reportsDir, "2000-01-01_00-00-00");
        Files.writeString(configuredReport, "<html><body>configured</body></html>", StandardCharsets.UTF_8);

        Path targetReports = Path.of(ReportingConfigKeys.DEFAULT_REPORTS_DIR);
        Files.createDirectories(targetReports);
        Path staleTargetReport = timestampedExtentReport(targetReports, "2999-01-01_00-00-00");
        Files.writeString(staleTargetReport, "<html><body>stale target</body></html>", StandardCharsets.UTF_8);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, reportsDir.toString())) {
            Path selected = invokeLatestExtentReport();

            Assert.assertEquals(
                    selected,
                    configuredReport.toAbsolutePath().normalize(),
                    "Extent HTML audit must target the configured reports.dir, not a newer stale target/reports file."
            );
        } finally {
            Files.deleteIfExists(staleTargetReport);
            Files.deleteIfExists(configuredReport);
            Files.deleteIfExists(reportsDir);
        }
    }

    @Test
    public void aggregate_extent_report_uses_standard_extent_shell_and_runtime_audit_layout() throws Exception {
        Path reportsDir = Files.createTempDirectory("atf-aggregate-standard-shell-");
        Path report = reportsDir.resolve(AtfCoreAggregateArtifacts.EXTENT_REPORT_FILE);

        try {
            invokeAggregateExtentWriter(report);

            String html = Files.readString(report);
            Assert.assertTrue(
                    html.contains("<title>Test Report</title>"),
                    "Aggregate Extent report must use the same document title as the standard Extent shell."
            );
            Assert.assertTrue(
                    html.contains("Automation Framework"),
                    "Aggregate Extent report must use the same report name as the standard Extent shell."
            );
            Assert.assertTrue(
                    html.contains(ExtentMarkers.NAVIGATION_GUARD)
                            && html.contains("window.atfShowExtentView=show;"),
                    "Aggregate Extent report must include the standard navigation guard."
            );
            Assert.assertTrue(
                    html.contains("class=\"fa fa-bug\""),
                    "Aggregate Extent report must use the standard ATF tag-navigation icon."
            );
            Assert.assertFalse(
                    html.contains("class=\"fa fa-tag\""),
                    "Aggregate Extent report must not keep Spark's old tag icon after standardization."
            );
            Assert.assertTrue(
                    html.contains("atf-runtime-dashboard-summary")
                            && html.contains("atf-runtime-dashboard-tests-row")
                            && html.contains("atf-runtime-timeline-row")
                            && html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                    "Aggregate Extent report must expose the standard compact runtime dashboard, timeline, and tag audit."
            );
            assertRuntimeAuditedAggregateShell(html);
            Assert.assertTrue(html.contains(ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES)),
                    "Aggregate Extent report must use Test cases score-card terminology.");
            assertRuntimeOverviewTotals(html, 4);
            Assert.assertTrue(html.contains("id=\"" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "\""),
                    "Aggregate Extent report must explicitly publish the aggregate final-audit section.");
            Assert.assertTrue(html.contains("<th>Wall-clock duration</th><td>35 ms</td>")
                            && html.contains("<th>Cumulative test duration</th><td>20 ms</td>")
                            && html.contains("<th>Slowest test</th>"),
                    "Aggregate final audit must publish timing rows from leaf start/end/duration values.");
            Assert.assertFalse(html.contains(ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CLASSES)),
                    "Aggregate Extent report must not publish class-count wording as the score-card unit in Overview.");
            Assert.assertTrue(
                    containsAcrossMarkup(html, "fails_as_expected", "ExpectedFailingTestA")
                            && containsAcrossMarkup(html, "fails_as_expected", "ExpectedFailingTestB"),
                    "Aggregate leaf titles that repeat across classes must include a visible class discriminator."
            );
            Assert.assertFalse(
                    html.contains("<span class=\"test-name\">fails_as_expected</span>"),
                    "Repeated aggregate leaf titles must not remain visually ambiguous."
            );
            Assert.assertEquals(dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED), "4");
            Assert.assertEquals(dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED), "0");
            Assert.assertFalse(
                    html.contains("ATF Core Extent Final Audit"),
                    "Aggregate Extent report must follow the current compact Overview contract, not the old large final-audit block."
            );
        } finally {
            Files.deleteIfExists(report);
            Files.deleteIfExists(reportsDir);
        }
    }

    private static String dashboardTopCount(String html, String label) {
        return ExtentSparkReportInspector.inspect(html).dashboardTopCount(label);
    }

    private static Path timestampedExtentReport(Path directory, String timestamp) {
        return directory.resolve(ExtentReportFiles.TIMESTAMP_FILE_PREFIX
                + timestamp
                + ExtentReportFiles.REPORT_FILE_EXTENSION);
    }

}
