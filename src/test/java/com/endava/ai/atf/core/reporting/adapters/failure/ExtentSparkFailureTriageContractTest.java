package com.endava.ai.atf.core.reporting.adapters.failure;

import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.support.base.BaseTestAPIContractTest;
import com.endava.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture;
import com.endava.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture;
import com.endava.ai.atf.core.reporting.failurefixtures.LongReferenceFailureSignatureFixture;
import com.endava.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture;
import com.endava.ai.atf.core.reporting.regression.StepLoggerContractTest;
import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.endava.ai.atf.ui.RegistrationTests;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_FAILURE_TRIAGE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_FAILURE_TRIAGE_CONTRACT)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkFailureTriageContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_category_view_groups_failed_runtime_test_cases_for_quick_triage() throws Exception {
        String html = runtimeAuditedDashboardHtml();
        String category = categoryFragment(html);

        ExtentSparkFailureTriageAssertions.assertCategoryGroupsFailedRuntimeInvocations(category);
    }

    @Test
    public void extent_failure_group_count_matches_dashboard_failed_card() throws Exception {
        String html = runtimeAuditedDashboardHtml();
        String category = categoryFragment(html);

        ExtentSparkFailureTriageAssertions.assertFailureGroupCountMatchesDashboard(html, category);
    }

    @Test
    public void extent_reportable_controlled_step_logger_failures_do_not_pollute_failed_counts() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        TestNG testng = new TestNG();
        testng.setUseDefaultListeners(false);
        testng.setVerbose(0);
        testng.setTestClasses(new Class<?>[]{StepLoggerContractTest.class});
        testng.run();

        Path report = extentReportContaining("unhandled_exception_outside_step_is_idempotent");
        String html = Files.readString(report);

        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "0",
                "A controlled StepLogger negative contract must not create a native Spark failed-test count."
        );
        Assert.assertFalse(
                html.contains("<span class=\"badge fail-bg log \">Fail</span>"),
                "A controlled StepLogger negative contract must not leave real FAIL log badges in Extent."
        );
        Assert.assertTrue(
                html.contains("unhandled_exception_outside_step_is_idempotent"),
                "The StepLogger contract test must remain reportable; only the synthetic FAIL pollution is forbidden."
        );
    }

    @Test
    public void extent_failure_tab_groups_two_failed_runtime_test_cases_with_repair_details() throws Exception {
        String html = runtimeAuditedTwoFailureDashboardHtml();
        String category = categoryFragment(html);

        ExtentSparkFailureTriageAssertions.assertFailureTabGroupsTwoFailedRuntimeInvocations(html, category);
    }

    @Test
    public void extent_failure_tab_exposes_test_reference_and_root_cause_details() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                RegistrationTests.class.getName(),
                "runtime_audit_failed_ui_navigation",
                "Runtime audit nested UI failure fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("navigation failed", "short visual failure");
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        RuntimeException browserCause = new RuntimeException(
                "net::ERR_CONNECTION_REFUSED at http://127.0.0.1:65535/auth/register\n"
                        + "Call log:\n"
                        + "- navigating to \"http://127.0.0.1:65535/auth/register\""
        );
        AssertionError wrapper = new AssertionError(
                "UI Action failed: Navigate to URL: http://127.0.0.1:65535/auth/register",
                browserCause
        );

        ExtentRuntimeAuditRegistry.record(
                result(
                        "runtime_audit_failed_ui_navigation",
                        RegistrationTests.class,
                        ITestResult.FAILURE,
                        wrapper
                ),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String category = categoryFragment(Files.readString(extentReportContaining("runtime_audit_failed_ui_navigation")));
        ExtentSparkFailureTriageAssertions.assertFailureTabExposesTestReferenceAndRootCauseDetails(category);
    }

    @Test
    public void extent_failure_tab_groups_repeated_error_signatures_before_class_triage() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "create_user_contract_failed",
                "Repeated failure signature fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("create user failed", "Unexpected HTTP status expected [201] but found [530]");
        adapter.endTest(TestStatus.FAIL);
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "delete_user_contract_failed",
                "Repeated failure signature fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("delete user failed", "Unexpected HTTP status expected [201] but found [530]");
        adapter.endTest(TestStatus.FAIL);
        adapter.startTest(
                RegistrationTests.class.getName(),
                "ui_setup_failed_from_same_backend_signature",
                "Repeated failure signature fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("ui setup failed", "Unexpected HTTP status expected [201] but found [530]");
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        AssertionError repeatedFailure = new AssertionError("Unexpected HTTP status expected [201] but found [530]");
        ExtentRuntimeAuditRegistry.record(
                result("create_user_contract_failed", BaseTestAPIContractTest.class, ITestResult.FAILURE, repeatedFailure),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("delete_user_contract_failed", BaseTestAPIContractTest.class, ITestResult.FAILURE, repeatedFailure),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result("ui_setup_failed_from_same_backend_signature", RegistrationTests.class, ITestResult.FAILURE, repeatedFailure),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String html = Files.readString(extentReportContaining("create_user_contract_failed"));
        String category = categoryFragment(html);
        ExtentSparkFailureTriageAssertions.assertRepeatedErrorSignaturesBeforeClassTriage(html, category);
    }

    @Test
    public void extent_failure_tab_compacts_long_repeated_reference_titles_without_losing_details() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        String verboseFailure = LongReferenceFailureSignatureFixture.errorMessage();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "contract_annotation_reference_inventory_failed",
                "Long repeated failure signature fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("contract reference inventory failed", verboseFailure);
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        ExtentRuntimeAuditRegistry.record(
                result(
                        "contract_annotation_reference_inventory_failed",
                        BaseTestAPIContractTest.class,
                        ITestResult.FAILURE,
                        new AssertionError(verboseFailure)
                ),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String category = categoryFragment(Files.readString(extentReportContaining("contract_annotation_reference_inventory_failed")));
        ExtentSparkFailureTriageAssertions.assertLongRepeatedReferenceFailureTitlesAreCompacted(category);
    }

    @Test
    public void extent_failure_tab_can_include_controlled_configuration_failures_when_fixture_mode_is_enabled() throws Exception {
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setSystemProperty("atf.failure.fixtures.enabled", "true")) {
            resetExtentRuntime();
            ExtentRuntimeAuditRegistry.clear();

            ExtentAdapter adapter = ExtentAdapter.getInstance();
            adapter.startTest(
                    TestBodyFailureFixture.class.getName(),
                    "fail_inside_test_body",
                    "Controlled test-body failure fixture"
            );
            adapter.startStep("TEST BODY");
            adapter.fail("controlled failure", "fixture-test-body-failure");
            adapter.endTest(TestStatus.FAIL);
            adapter.startTest(
                    BeforeMethodFailureFixture.class.getName(),
                    "test_body_is_blocked_by_before_failure",
                    "Controlled skipped test-body fixture"
            );
            adapter.startStep("TEST BODY");
            adapter.logDetail("Blocked after before-method failure");
            adapter.endTest(TestStatus.SKIP);
            adapter.flush();

            ExtentRuntimeAuditRegistry.record(
                    result(
                            "fail_before_test_body",
                            BeforeMethodFailureFixture.class,
                            ITestResult.FAILURE,
                            new AssertionError("fixture-before-method-failure")
                    ),
                    "FAILED"
            );
            ExtentRuntimeAuditRegistry.record(
                    result(
                            "test_body_is_blocked_by_before_failure",
                            BeforeMethodFailureFixture.class,
                            ITestResult.SKIP
                    ),
                    "SKIPPED"
            );
            ExtentRuntimeAuditRegistry.record(
                    result(
                            "fail_inside_test_body",
                            TestBodyFailureFixture.class,
                            ITestResult.FAILURE,
                            new AssertionError("fixture-test-body-failure")
                    ),
                    "FAILED"
            );
            ExtentRuntimeAuditRegistry.record(
                    result(
                            "fail_after_test_body",
                            AfterMethodFailureFixture.class,
                            ITestResult.FAILURE,
                            new AssertionError("fixture-after-method-failure")
                    ),
                    "FAILED"
            );
            ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

            String html = Files.readString(extentReportContaining("fail_inside_test_body"));
            String category = categoryFragment(html);
            ExtentSparkFailureTriageAssertions.assertControlledConfigurationFailures(html, category);
        }
    }

    @Test
    public void extent_category_view_shows_empty_failure_state_when_runtime_is_green() throws Exception {
        String html = runtimeAuditedSingleClassDashboardHtml();
        String category = categoryFragment(html);

        Assert.assertTrue(
                category.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                "Category view must keep the failure triage section even when the run is green."
        );
        Assert.assertTrue(
                category.contains("No runtime failures recorded."),
                "Green runs must say explicitly that no runtime failures were recorded."
        );
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "0",
                "A green runtime ledger must keep the top failure card aligned with the category failure view."
        );
    }

    @Test
    public void extent_green_runtime_does_not_keep_native_spark_failure_artifacts() throws Exception {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "green_runtime_with_controlled_negative_contract",
                "Green runtime audit fixture with controlled negative evidence"
        );
        adapter.startStep("TEST BODY");
        adapter.logDetail("Controlled negative path was observed by a test observer, not by the live Spark node.");
        adapter.pass("Controlled negative contract remained green in the runtime report");
        adapter.endTest(TestStatus.PASS);
        adapter.flush();

        ExtentRuntimeAuditRegistry.record(
                result("green_runtime_with_controlled_negative_contract", BaseTestAPIContractTest.class, ITestResult.SUCCESS),
                "PASSED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        String html = Files.readString(extentReportContaining("green_runtime_with_controlled_negative_contract"));
        Assert.assertEquals(
                dashboardTopCount(html, ExtentDashboardLabels.TESTS_FAILED),
                "0",
                "Green runtime reports must not advertise native Spark failures for controlled negative contracts."
        );
        Assert.assertTrue(
                categoryFragment(html).contains("No runtime failures recorded."),
                "Failure triage tab must explicitly show an empty runtime failure state for green runs."
        );
        Assert.assertFalse(
                html.contains("test-status text-fail") || html.contains("badge fail-bg log"),
                "Green runtime reports must not retain native Spark fail nodes or fail log badges."
        );
        Assert.assertFalse(
                html.contains("<div class=\"title\">Failure Screenshot</div>"),
                "Observer-only negative evidence must not publish synthetic Failure Screenshot attachments into green reports."
        );
    }

    private String runtimeAuditedTwoFailureDashboardHtml() throws IOException {
        resetExtentRuntime();
        ExtentRuntimeAuditRegistry.clear();

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        adapter.startTest(
                RegistrationTests.class.getName(),
                "runtime_audit_failed_registration",
                "Runtime audit two-failure fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("registration fixture failure", "registration fixture failure");
        adapter.endTest(TestStatus.FAIL);
        adapter.startTest(
                BaseTestAPIContractTest.class.getName(),
                "runtime_audit_failed_api",
                "Runtime audit two-failure fixture"
        );
        adapter.startStep("TEST BODY");
        adapter.fail("api fixture failure", "api fixture failure");
        adapter.endTest(TestStatus.FAIL);
        adapter.flush();

        ExtentRuntimeAuditRegistry.record(
                result(
                        "runtime_audit_failed_registration",
                        RegistrationTests.class,
                        ITestResult.FAILURE,
                        new AssertionError("registration fixture failure")
                ),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.record(
                result(
                        "runtime_audit_failed_api",
                        BaseTestAPIContractTest.class,
                        ITestResult.FAILURE,
                        new IllegalStateException("api fixture failure")
                ),
                "FAILED"
        );
        ExtentRuntimeAuditRegistry.publishOrUpdateIfExtent();

        Path report = extentReportContaining("runtime_audit_failed_registration");
        return Files.readString(report);
    }
}
