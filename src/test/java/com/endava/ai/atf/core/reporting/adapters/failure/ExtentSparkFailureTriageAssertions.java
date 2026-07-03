package com.endava.ai.atf.core.reporting.adapters.failure;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeStatusLedger;
import com.endava.ai.atf.core.reporting.failurefixtures.LongReferenceFailureSignatureFixture;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentFailureTriageSnapshot;
import org.testng.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ExtentSparkFailureTriageAssertions extends ExtentSparkPresentationContractSupport {
    private static final Pattern ROOT_CAUSE_TITLE = Pattern.compile(
            "<div class=\"atf-runtime-failure-signature-text\"><strong>(.*?)</strong>",
            Pattern.DOTALL
    );
    private static final Pattern DRILLDOWN_TITLE = Pattern.compile(
            "<details class=\"atf-runtime-failure-details\"><summary>(.*?)</summary>",
            Pattern.DOTALL
    );

    private ExtentSparkFailureTriageAssertions() {
    }

    static void assertCategoryGroupsFailedRuntimeInvocations(String category) {
        Assert.assertTrue(
                category.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                "Category view must expose failed tests grouped for fast triage, not duplicate the Overview tag audit."
        );
        Assert.assertFalse(
                category.contains("<div class=\"test-list\">")
                        || category.contains("<div class=\"test-content\">"),
                "Failure category view must be a clean ATF-owned failure grouping surface, not Spark native tags."
        );
        Assert.assertTrue(
                category.contains(ExtentRuntimeOverviewTables.attributeEquals(
                        ExtentDataAttributes.RUNTIME_FAILURE_TABLE,
                        ExtentDataAttributes.FAILURE_TABLE_FAILURES
                ))
                        && category.contains("class=\"atf-runtime-failure-lines\"")
                        && category.contains("class=\"atf-runtime-failure-class\"")
                        && category.contains("class=\"atf-runtime-failure-line\"")
                        && category.contains("<details class=\"atf-runtime-failure-details\"><summary>AssertionError: synthetic failure for dashboard contract</summary>")
                        && category.contains("<pre class=\"atf-runtime-failure-code\"><code>")
                        && category.contains("Test reference: com.endava.ai.atf.ui.RegistrationTests#runtime_audit_failed_one")
                        && category.contains("Failure chain:")
                        && category.contains("Root cause detail:")
                        && category.contains("AssertionError: synthetic failure for dashboard contract")
                        && category.contains(failureGroup("RegistrationTests")),
                "Failure grouping must publish horizontal method lines with code-block error details."
        );
        Assert.assertFalse(
                category.contains("class=\"atf-runtime-failure-preview\""),
                "Failure grouping must not duplicate the same AssertionError in a separate preview block before the collapsed details."
        );
        Assert.assertTrue(
                category.contains("1 root-cause signature -> 1 failed test case -> 1 affected Java class")
                        && category.contains("RegistrationTests")
                        && category.contains("runtime_audit_failed_one")
                        && category.contains("AssertionError: synthetic failure for dashboard contract"),
                "Failure grouping must include failed class, method, and failure reason from the reconciled runtime ledger."
        );
        Assert.assertTrue(
                category.contains("<span class=\"atf-runtime-failure-status\">FAILED</span>"),
                "Failure grouping must show the exact failed test case total."
        );
        Assert.assertFalse(
                category.contains("card atf-runtime-failure-group"),
                "Failure grouping must not render heavy card boxes for each failed test class."
        );
        Assert.assertFalse(
                category.contains("class=\"atf-runtime-failure-detail-table\"")
                        || category.contains("class=\"atf-runtime-failure-nav\""),
                "Failure grouping must not regress to the old navigator/table layout."
        );
        Assert.assertFalse(
                category.contains(ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()),
                "Category view must not duplicate runtime tag tables because they already live in the Overview."
        );
    }

    static void assertFailureGroupCountMatchesDashboard(String html, String category) {
        ExtentFailureTriageSnapshot triage = ExtentFailureTriageSnapshot.fromCategoryHtml(category);
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "1",
                "Fixture must expose one failed test case for reconciliation."
        );
        Assert.assertEquals(
                triage.groupCount(),
                1,
                "Failure navigator must expose exactly the failed runtime categories counted by the dashboard fixture."
        );
        Assert.assertTrue(
                triage.hasRootCauseSummary("1 root-cause signature -> 1 failed test case -> 1 affected Java class"),
                "Failure groups total must match the dashboard failed count."
        );
    }

    static void assertFailureTabGroupsTwoFailedRuntimeInvocations(String html, String category) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "2",
                "Dashboard failed count must be sourced from the TestListener runtime ledger."
        );
        Assert.assertTrue(
                category.contains("2 root-cause signatures -> 2 failed test cases -> 2 affected Java classes"),
                "Failure tab must publish the exact failed test case total."
        );
        Assert.assertTrue(
                category.contains("class=\"atf-runtime-failure-lines\"")
                        && category.contains("class=\"atf-runtime-failure-class-header\"")
                        && category.contains("class=\"atf-runtime-failure-line-main\"")
                        && category.contains("<pre class=\"atf-runtime-failure-code\"><code>")
                        && category.contains(runtimeMethod("runtime_audit_failed_registration")),
                "Failure tab must provide class separators, method links, and code-block failure details."
        );
        Assert.assertTrue(
                category.contains(failureGroup("RegistrationTests"))
                        && category.contains("runtime_audit_failed_registration")
                        && category.contains("AssertionError: registration fixture failure"),
                "Failure tab must expose the failed UI class, method, and throwable message."
        );
        Assert.assertTrue(
                category.contains(failureGroup("BaseTestAPIContractTest"))
                        && category.contains("runtime_audit_failed_api")
                        && category.contains("IllegalStateException: api fixture failure"),
                "Failure tab must expose the failed API class, method, and throwable message."
        );
    }

    static void assertFailureTabExposesTestReferenceAndRootCauseDetails(String category) {
        Assert.assertTrue(
                category.contains("Test reference: com.endava.ai.atf.ui.RegistrationTests#runtime_audit_failed_ui_navigation"),
                "Failure triage must link the failure to the exact Java test reference."
        );
        Assert.assertTrue(
                category.contains("<details class=\"atf-runtime-failure-details\"><summary>AssertionError: UI Action failed: Navigate to URL: http://127.0.0.1:65535/auth/register</summary>"),
                "Failure triage must use the short cause as the only visible collapsed details label."
        );
        Assert.assertFalse(
                category.contains("class=\"atf-runtime-failure-preview\""),
                "Failure triage must not render a separate preview because method and owner are already visible in the failure row."
        );
        Assert.assertTrue(
                category.contains("Failure chain:")
                        && category.contains("AssertionError: UI Action failed: Navigate to URL: http://127.0.0.1:65535/auth/register")
                        && category.contains("caused by: RuntimeException: net::ERR_CONNECTION_REFUSED"),
                "Failure triage must preserve the wrapper error and the deeper runtime/browser cause."
        );
        Assert.assertTrue(
                category.contains("Root cause detail:")
                        && category.contains("net::ERR_CONNECTION_REFUSED at http://127.0.0.1:65535/auth/register")
                        && category.contains("Call log:")
                        && category.contains("navigating to"),
                "Failure triage must keep multi-line root-cause details, not only the short AssertionError wrapper."
        );
    }

    static void assertRepeatedErrorSignaturesBeforeClassTriage(String html, String category) {
        ExtentFailureTriageSnapshot triage = ExtentFailureTriageSnapshot.fromCategoryHtml(category);
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_PASSED),
                "0",
                "Overview pass card must not stay green when every test case failed."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "3",
                "Overview fail card must reconcile with the repeated root-cause failure ledger."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "3",
                "Overview Test cases total must count failed test methods exactly once."
        );
        assertStatusGroupMatchesRuntimeLedger(html, 3, 0, 3, 0);
        Assert.assertTrue(
                triage.hasRepeatedRootCauseSection()
                        && triage.hasRootCauseSummary("1 root-cause signature -> 3 failed test cases -> 2 affected Java classes")
                        && category.contains("3 test cases")
                        && category.contains("Affects 2 Java test classes")
                        && category.contains("3 affected methods")
                        && category.contains("Grouped by failure message first; methods are navigation targets")
                        && category.contains("<details class=\"atf-runtime-failure-drilldown\">")
                        && category.contains("<summary>Class-level failure drill-down</summary>")
                        && category.contains("atf-runtime-failure-signature-target")
                        && category.contains("atf-runtime-failure-signature-methods"),
                "Failure triage must summarize repeated error signatures before collapsed class-level failure rows."
        );
        Assert.assertTrue(
                category.contains("AssertionError: Unexpected HTTP status expected [201] but found [530]")
                        && category.contains("BaseTestAPIContractTest")
                        && category.contains("create_user_contract_failed")
                        && category.contains("delete_user_contract_failed")
                        && category.contains("RegistrationTests")
                        && category.contains("ui_setup_failed_from_same_backend_signature"),
                "Repeated failure signature summary must list the affected Java classes and methods."
        );
    }

    static void assertLongRepeatedReferenceFailureTitlesAreCompacted(String category) {
        String rootCauseTitle = firstMatch(ROOT_CAUSE_TITLE, category, "root-cause title");
        String drilldownTitle = firstMatch(DRILLDOWN_TITLE, category, "drill-down title");

        assertCompactReferenceTitle(rootCauseTitle, "root-cause title");
        assertCompactReferenceTitle(drilldownTitle, "drill-down title");
        Assert.assertTrue(
                category.contains("Root cause detail:")
                        && category.contains(LongReferenceFailureSignatureFixture.DIAGNOSTIC_SHELL_REFERENCE),
                "Failure triage must keep the full verbose evidence in the code block, only the visible titles are compacted."
        );
    }

    static void assertControlledConfigurationFailures(String html, String category) {
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "3",
                "Failure fixture diagnostics must reconcile before, body, and after failures in the dashboard."
        );
        Assert.assertEquals(
                dashboardSummaryCount(html, ExtentDashboardLabels.TEST_CASES),
                "4",
                "Failure fixture diagnostics must keep the skipped method in the Overview Test cases total."
        );
        Assert.assertTrue(
                dashboardTestsCard(html).contains("<b>1</b> skipped")
                        && html.contains("test_body_is_blocked_by_before_failure"),
                "Failure fixture diagnostics must expose blocked methods through the Test cases footer and report ledger."
        );
        assertStatusGroupMatchesRuntimeLedger(html, 4, 0, 3, 1);
        Assert.assertTrue(
                category.contains("3 root-cause signatures -> 3 failed test cases -> 3 affected Java classes")
                        && category.contains("fixture-before-method-failure")
                        && category.contains("fixture-test-body-failure")
                        && category.contains("fixture-after-method-failure"),
                "Failure triage tab must include configuration and test-body failures when fixture diagnostics are explicitly enabled."
        );
    }

    private static void assertStatusGroupMatchesRuntimeLedger(
            String html,
            int total,
            int passed,
            int failed,
            int skipped
    ) {
        ExtentRuntimeStatusLedger.expected(total, passed, failed, skipped).assertMatches(html);
    }

    private static String firstMatch(Pattern pattern, String html, String label) {
        Matcher matcher = pattern.matcher(html);
        Assert.assertTrue(matcher.find(), "Failure triage must expose a " + label + ".");
        return matcher.group(1)
                .replaceAll("<[^>]+>", "")
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void assertCompactReferenceTitle(String title, String label) {
        Assert.assertTrue(
                title.startsWith("AssertionError: " + LongReferenceFailureSignatureFixture.ACTIONABLE_REASON),
                "Compacted " + label + " must keep the actionable failure reason: " + title
        );
        Assert.assertTrue(
                title.contains(LongReferenceFailureSignatureFixture.FIRST_REFERENCE_TITLE),
                "Compacted " + label + " must keep the first concrete Java reference: " + title
        );
        Assert.assertTrue(
                title.contains("... +"),
                "Compacted " + label + " must disclose omitted repeated references: " + title
        );
        Assert.assertFalse(
                title.contains(LongReferenceFailureSignatureFixture.REPEATED_SOURCE_PREFIX),
                "Compacted " + label + " must not expose repeated source path prefixes: " + title
        );
        Assert.assertTrue(
                title.length() <= 220,
                "Compacted " + label + " must fit the visible title budget. Actual length: " + title.length()
        );
    }

    private static String failureGroup(String value) {
        return ExtentRuntimeOverviewTables.attributeEquals(ExtentDataAttributes.RUNTIME_FAILURE_GROUP, value);
    }

    private static String runtimeMethod(String value) {
        return ExtentRuntimeOverviewTables.attributeEquals(ExtentDataAttributes.RUNTIME_METHOD, value);
    }
}
