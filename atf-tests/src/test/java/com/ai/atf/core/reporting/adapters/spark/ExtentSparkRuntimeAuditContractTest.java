package com.ai.atf.core.reporting.adapters.spark;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.TestStatus;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.support.base.BaseTestAPIContractTest;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeAuditDashboardAssertions;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeAuditLayoutAssertions;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeStatusLedger;
import com.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture;
import com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture;
import com.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture;
import com.ai.atf.ui.RegistrationTests;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_RUNTIME_AUDIT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_RUNTIME_AUDIT_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkRuntimeAuditContractTest extends ExtentSparkPresentationContractSupport {
    private static final String TIMESTAMPED_EXTENT_REPORT_PATTERN = ExtentReportFiles.TIMESTAMP_FILE_PREFIX
            + "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"
            + "\\.html";


    @Test
    public void extent_runtime_audit_rewrites_dashboard_pass_fail_counts_from_testng_inventory() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditDashboardAssertions.assertPassFailCountsFromTestNgInventory(html);
    }

    @Test
    public void extent_runtime_audit_normalizes_explicit_report_path_for_aggregate_reports() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                "com.ai.atf.aggregate.AggregateRuntimeFixture",
                "aggregate_runtime_passed_one",
                "Explicit aggregate report normalization fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("Aggregate pass node");
        adapter.endTest(TestStatus.PASS);
        adapter.startTest(
                "com.ai.atf.aggregate.AggregateRuntimeFixture",
                "aggregate_runtime_failed_one",
                "Explicit aggregate report normalization fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("Aggregate failed node", "AssertionError: aggregate fixture failure");
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        Path report = extentReportContaining("aggregate_runtime_failed_one");
        ExtentRuntimeAuditRegistry.publishOrUpdateReport(report, List.of(
                ExtentRuntimeAuditRegistry.RuntimeAuditInvocation.of(
                        "PASSED",
                        "com.ai.atf.aggregate.AggregateRuntimeFixture",
                        "aggregate_runtime_passed_one",
                        1L,
                        "",
                        List.of("PROFILE:EXTENT_CONTRACT")
                ),
                ExtentRuntimeAuditRegistry.RuntimeAuditInvocation.of(
                        "FAILED",
                        "com.ai.atf.aggregate.AggregateRuntimeFixture",
                        "aggregate_runtime_failed_one",
                        2L,
                        "- AssertionError: aggregate fixture failure",
                        List.of("PROFILE:EXTENT_CONTRACT")
                )
        ));

        String html = Files.readString(report);
        ExtentRuntimeAuditDashboardAssertions.assertExplicitAggregateReportRuntimeDashboard(html);
    }

    @Test
    public void extent_runtime_audit_ignores_internal_fixture_failures_in_dashboard_counts() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "runtime_visible_contract_pass",
                "Runtime audit fixture filtering contract"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("visible contract passed");
        adapter.endTest(TestStatus.PASS);
        adapter.startTest(
                ExtentHierarchyFixture.class.getName(),
                "fakeUiTest",
                "Internal Spark node that must not affect runtime audit counts"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("controlled fixture failure", "synthetic internal node");
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        ExtentRuntimeAuditRegistry.record(
                result("runtime_visible_contract_pass", BaseTestAPIContractTest.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("fakeUiTest", BaseTestAPIContractTest.class, ITestResult.FAILURE),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("fails_as_expected", ExtentHierarchyFixture.class, ITestResult.FAILURE),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("fail_inside_test_body", TestBodyFailureFixture.class, ITestResult.FAILURE),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("test_body_is_blocked_by_before_failure", BeforeMethodFailureFixture.class, ITestResult.FAILURE),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("test_body_passes_before_after_failure", AfterMethodFailureFixture.class, ITestResult.FAILURE),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String html = Files.readString(extentReportContaining("runtime_visible_contract_pass"));
        ExtentRuntimeAuditDashboardAssertions.assertInternalFixtureFailuresIgnored(html);
    }

    @Test
    public void no_framework_reporting_tests_do_not_pollute_runtime_audit_ledger() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "visible_runtime_contract",
                "Visible runtime audit contract"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("visible contract passed");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        new TestListener().onTestSuccess(result(
                "self_auditing_extent_contract_should_not_be_reported",
                ExtentSparkRuntimeAuditContractTest.class,
                ITestResult.SUCCESS
        ));
        ExtentRuntimeAuditRegistry.record(
                result("visible_runtime_contract", BaseTestAPIContractTest.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String html = Files.readString(extentReportContaining("visible_runtime_contract"));
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "1",
                "@NoFrameworkReporting tests must not be counted in the final runtime audit ledger."
        );
        Assert.assertFalse(
                html.contains("self_auditing_extent_contract_should_not_be_reported"),
                "@NoFrameworkReporting contract methods must not leak into the generated Extent report."
        );
    }

    @Test
    public void extent_runtime_audit_keeps_only_test_cases_secondary_card_with_runtime_values() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditDashboardAssertions.assertTestCasesSecondaryCard(html);
    }

    @Test
    public void extent_runtime_audit_overview_total_matches_tag_distribution_totals() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditDashboardAssertions.assertOverviewTotalsMatchTagDistributionTotals(html);
    }

    @Test
    public void extent_status_group_script_is_reconciled_with_runtime_invocation_ledger() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditDashboardAssertions.assertStatusGroupScriptReconciledWithLedger(html);
    }

    @Test
    public void extent_runtime_audit_updates_current_report_not_stale_matching_report() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "current_report_path_contract",
                "Current report selection contract"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("current report passed");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        Path currentReport = adapter.currentReportPath();
        Assert.assertTrue(Files.isRegularFile(currentReport), "The Extent adapter must expose the current report path.");
        Path staleReport = contractReportsDir.resolve(ExtentReportFiles.TIMESTAMP_FILE_PREFIX
                + "2999-01-01_00-00-00"
                + ExtentReportFiles.REPORT_FILE_EXTENSION);
        Files.writeString(staleReport, Files.readString(currentReport));
        String staleBefore = Files.readString(staleReport);

        ExtentRuntimeAuditRegistry.record(
                result("current_report_path_contract", BaseTestAPIContractTest.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String currentHtml = Files.readString(currentReport);
        Assert.assertTrue(
                currentHtml.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Runtime audit must enrich the adapter's current report."
        );
        ExtentRuntimeStatusLedger.expected(1, 1, 0, 0).assertMatches(currentHtml);
        Assert.assertEquals(
                Files.readString(staleReport),
                staleBefore,
                "Runtime audit must not rewrite a stale report just because it contains the same test marker."
        );
    }

    @Test
    public void extent_contract_reports_are_timestamped_to_preserve_auditable_references() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        Assert.assertTrue(
                report.getFileName().toString().matches(TIMESTAMPED_EXTENT_REPORT_PATTERN),
                "Extent contract runs must publish timestamped HTML reports instead of overwriting ExtentReport.html: "
                        + report
        );
        Assert.assertFalse(
                Files.exists(contractReportsDir.resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME)),
                "Timestamped Extent contract runs must not also overwrite the stable ExtentReport.html artifact."
        );
    }

    @Test
    public void extent_overview_does_not_embed_raw_audit_or_inventory_sections() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditDashboardAssertions.assertOverviewDoesNotEmbedRawAuditOrInventory(html);
    }

    @Test
    public void extent_runtime_audit_report_does_not_retain_timestamp_mojibake() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        Assert.assertFalse(
                html.contains("\u00E2\u20AC\u00AF")
                        || html.contains("\u202F")
                        || html.contains("\u00C2\u00A0"),
                "Generated Extent reports must not contain timestamp mojibake or narrow no-break timestamp spacing."
        );
    }

    @Test
    public void extent_overview_replaces_native_tags_with_three_runtime_audit_tables() throws Exception {
        String html = runtimeAuditedDashboardHtml();
        String dashboard = dashboardFragment(html);

        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTables(dashboard);
    }

    @Test
    public void extent_overview_tag_tables_survive_republishing_runtime_audit() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String dashboard = dashboardFragment(Files.readString(report));
        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTables(dashboard);
        Assert.assertFalse(
                dashboard.contains("<div class=\"row atf-runtime-overview-tags\"><div class=\"col-md-12\">\n\n</div></div>"),
                "Republishing runtime audit must not leave an empty Overview tag audit container."
        );
    }

    @Test
    public void extent_overview_tag_tables_survive_spark_status_script_line_ending_drift() throws Exception {
        Path report = runtimeAuditedDashboardReport();
        String before = Files.readString(report);
        Files.writeString(
                report,
                before.replace("<script>\nvar statusGroup", "<script>\r\nvar statusGroup")
        );

        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String dashboard = dashboardFragment(Files.readString(report));
        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTables(dashboard);
        Assert.assertFalse(
                dashboard.contains("category-container"),
                "Runtime audit must replace Spark native tag/category cards even when the statusGroup script uses CRLF."
        );
    }

    @Test
    public void extent_green_multi_class_runtime_publishes_one_audited_report() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "green_class_one_pass",
                "First green class fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("first class passed");
        adapter.endTest(TestStatus.PASS);
        adapter.startTest(
                RegistrationTests.class.getName(),
                "green_class_two_pass",
                "Second green class fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.pass("second class passed");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        ExtentRuntimeAuditRegistry.record(
                result("green_class_one_pass", BaseTestAPIContractTest.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("green_class_two_pass", RegistrationTests.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        Path report = extentReportContaining("green_class_two_pass");
        String html = Files.readString(report);
        ExtentRuntimeAuditDashboardAssertions.assertGreenMultiClassRuntimeReport(html, extentReportCount());
    }

    @Test
    public void extent_category_tag_tables_use_full_width_readable_layout() throws Exception {
        String html = runtimeAuditedDashboardHtml();

        ExtentRuntimeAuditLayoutAssertions.assertFullWidthReadableLayout(html);
    }

}
