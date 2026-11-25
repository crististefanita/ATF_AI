package com.ai.atf.core.reporting.adapters.audit;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;

public final class ExtentRuntimeAuditDashboardAssertions extends ExtentSparkPresentationContractSupport {
    private ExtentRuntimeAuditDashboardAssertions() {
    }

    public static void assertPassFailCountsFromTestNgInventory(String html) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED),
                "2",
                "Dashboard first row must show passed test cases, not Spark top-level node count."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "1",
                "Dashboard first row must show failed test cases, not Spark top-level node count."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "4",
                "Dashboard summary cards must expose the real test-case count."
        );
    }

    public static void assertExplicitAggregateReportRuntimeDashboard(String html) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED),
                "1",
                "Explicit aggregate reports must use the same test-case dashboard pass count as normal Extent reports."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "1",
                "Explicit aggregate reports must use the same test-case dashboard fail count as normal Extent reports."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "2",
                "Explicit aggregate reports must expose test-case totals, not Spark node totals."
        );
        Assert.assertEquals(
                runtimeTagTableCountSum(html, ExtentDataAttributes.TAG_TABLE_CLASSES),
                2,
                "Explicit aggregate reports must reuse runtime class distribution tables."
        );
        Assert.assertTrue(
                html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Explicit aggregate reports must reuse the canonical runtime tag audit section."
        );
        ExtentRuntimeStatusLedger.expected(2, 1, 1, 0).assertMatches(html);
    }

    public static void assertInternalFixtureFailuresIgnored(String html) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED),
                "1",
                "Dashboard pass count must include only reportable test cases, not internal fixtures."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "0",
                "Dashboard fail count must ignore controlled internal fixture failures."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "1",
                "Runtime test-case summary must exclude fake methods, nested fixtures, and controlled failure fixture classes."
        );
        Assert.assertFalse(
                dashboardFragment(html).contains("reporting.failurefixtures"),
                "Failure fixture packages must not leak into runtime audit tags, timeline, or dashboard reconciliation."
        );
    }

    public static void assertTestCasesSecondaryCard(String html) {
        String dashboard = dashboardFragment(html);
        String testsCard = dashboardTestsCard(html);

        Assert.assertTrue(
                html.contains(".dashboard-view > .row:first-of-type .card,")
                        && html.contains(".dashboard-view .atf-runtime-dashboard-tests-card,")
                        && html.contains("row-gap: 0.75rem;")
                        && html.contains("min-height: 118px;")
                        && html.contains("font-size: 1.15rem;")
                        && html.contains("font-size: 1.78rem;")
                        && html.contains("font-size: 1.01rem;"),
                "Dashboard score cards must use a readable visual contract with aligned labels, values, and descriptions."
        );
        Assert.assertTrue(
                testsCard.contains("<h6 class=\"card-title\">" + ExtentDashboardLabels.TEST_CASES + "</h6>")
                        && testsCard.contains(ExtentRuntimeOverviewTables.attributeEquals(
                                ExtentDataAttributes.TEST_CASES_TOTAL,
                                "true"
                        ) + ">4</h3>")
                        && testsCard.contains("total test cases")
                        && testsCard.contains("test cases passed")
                        && testsCard.contains("test cases failed"),
                "Dashboard second row must expose the explicit test-case total plus pass/fail breakdown."
        );
        Assert.assertTrue(
                dashboard.contains("<div class=\"row atf-runtime-dashboard-tests-row\"><div class=\"col-md-4 col-lg-4\">"),
                "Dashboard Test cases card must be compact and left-aligned, not stretched across the full overview width."
        );
        Assert.assertFalse(
                dashboard.contains("<div class=\"row atf-runtime-dashboard-tests-row\"><div class=\"col-md-12\">"),
                "Dashboard Test cases card must not be rendered as a full-width row."
        );
        Assert.assertEquals(
                dashboardTestsCardCount(html),
                "4",
                "Dashboard Test cases card footer must reconcile to the TestListener test-case total."
        );
        Assert.assertFalse(
                dashboard.contains("<h6 class=\"card-title\">Tests</h6>"),
                "Dashboard second row must not expose the ambiguous Spark Tests label for runtime test cases."
        );
        Assert.assertFalse(
                dashboard.contains("<h6 class=\"card-title\"></h6>"),
                "Dashboard second row must not expose Spark's blank grandchild score card."
        );
        Assert.assertFalse(
                dashboard.contains("<h6 class=\"card-title\">Log events</h6>"),
                "Dashboard second row must not expose Spark's Log events score card."
        );
        ExtentRuntimeStatusLedger.expected(4, 2, 1, 1).assertMatches(html);
    }

    public static void assertOverviewTotalsMatchTagDistributionTotals(String html) {
        int overviewTotal = Integer.parseInt(dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES));

        Assert.assertEquals(
                runtimeTagTableCountSum(html, ExtentDataAttributes.TAG_TABLE_CLASSES),
                overviewTotal,
                "Runtime Test methods by class table must reconcile exactly to the test-case total shown in overview."
        );
        Assert.assertEquals(
                runtimeTagTableCountSum(html, ExtentDataAttributes.TAG_TABLE_PACKAGES),
                overviewTotal,
                "Runtime Test methods by package table must reconcile exactly to the test-case total shown in overview."
        );
    }

    public static void assertStatusGroupScriptReconciledWithLedger(String html) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED),
                "2",
                "Top passed card must use the runtime TestListener ledger."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "1",
                "Top failed card must use the runtime TestListener ledger."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "4",
                "Runtime summary total must use the TestListener test-case ledger."
        );
        ExtentRuntimeStatusLedger.expected(4, 2, 1, 1).assertMatches(html);
    }

    public static void assertOverviewDoesNotEmbedRawAuditOrInventory(String html) {
        String dashboard = dashboardFragment(html);

        Assert.assertFalse(
                dashboard.contains("ATF Core Extent Final Audit"),
                "Overview must not contain a large final audit block; details belong in compact summary cards and tag tables."
        );
        Assert.assertFalse(
                dashboard.contains("ATF Core Runtime Invocation Inventory"),
                "Overview must not expose raw runtime inventory; it makes the dashboard visually noisy."
        );
        Assert.assertFalse(
                html.contains(ExtentMarkers.CORE_FINAL_AUDIT_START),
                "The old final audit marker must not be emitted into the report anymore."
        );
        Assert.assertFalse(
                dashboard.contains("<div class=\"card-header\"><p>Tags</p></div>"),
                "Overview must not expose Spark's native mixed Tags table; ATF replaces it with audited tag tables."
        );
    }

    public static void assertGreenMultiClassRuntimeReport(String html, long reportCount) {
        Assert.assertEquals(
                reportCount,
                1,
                "A single Extent runtime must publish one HTML artifact for multiple green test classes."
        );
        Assert.assertTrue(
                dashboardFragment(html).contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "The single green report must still include the Overview runtime tag audit."
        );
        Assert.assertTrue(
                categoryFragment(html).contains("No runtime failures recorded."),
                "The single green report must still expose an explicit empty failure triage state."
        );
    }
}
