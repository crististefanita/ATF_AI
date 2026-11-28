package com.ai.atf.core.reporting.adapters.support;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import com.ai.core.reporting.adapters.extent.synthetic.SyntheticExtentReportResult;
import com.ai.core.reporting.adapters.extent.synthetic.SyntheticRuntimeInvocation;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public final class ExtentSparkReportFixtures {
    private static final String API_POSITIVE_USER_TESTS = "com.ai.api.tests.PositiveUserTests";
    private static final String UI_REGISTRATION_TESTS = "com.ai.atf.ui.RegistrationTests";
    private static final String UI_REGISTRATION_AUDIT_TESTS = "com.ai.atf.ui.RegistrationAuditTests";
    private static final String UI_DEFAULTS_CONTRACT_TEST =
            "com.ai.atf.ui.component.config.FrameworkRuntimeUiDefaultsContractTest";
    private static final String COMPONENT_ADAPTER_FAIL_FIXTURE =
            "com.ai.atf.core.reporting.ComponentAdapterFailFixture";

    private ExtentSparkReportFixtures() {
    }

    public static String nestedExtentReportHtml(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        String marker = "extent-collapse-contract-" + UUID.randomUUID();
        SyntheticExtentReportResult result = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(marker, "Extent must open as a readable collapsed tree")
                .step("SETUP")
                .detail("setup evidence")
                .pass("setup done")
                .step("TEST BODY")
                .step("Nested UI action")
                .detail("nested evidence")
                .pass("nested done")
                .pass("body done")
                .end(TestStatus.PASS)
                .flushAndFind(marker);

        String html = result.html();
        Assert.assertTrue(html.contains(marker), "Fixture must contain the nested collapse marker.");
        Assert.assertTrue(html.contains("<span>SETUP</span>"), "Fixture must expose a SETUP node.");
        Assert.assertTrue(html.contains("<span>TEST BODY</span>"), "Fixture must expose a TEST BODY node.");
        Assert.assertTrue(html.contains("<span>Nested UI action</span>"), "Fixture must expose a nested step node.");
        return html;
    }

    public static Path apiLikeNestedExtentReport(Path reportsDir, String methodName) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(API_POSITIVE_USER_TESTS, methodName, "API report expand contract")
                .step("TEST BODY")
                .step("POST /public/v2/users")
                .detail("method=POST")
                .detail("url=/public/v2/users")
                .detail("request.body")
                .detail("{\"name\":\"John Doe\",\"status\":\"active\"}")
                .detail("response.status=201")
                .pass("Call completed")
                .step("Validate: status=201")
                .detail("actual=201")
                .pass("Validated")
                .pass("Test body completed")
                .end(TestStatus.PASS)
                .flushAndFind(methodName)
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains("PositiveUserTests"), "Fixture must mimic the API class report.");
        Assert.assertTrue(html.contains("<span>" + methodName + "</span>"), "Fixture must expose the API method node.");
        Assert.assertTrue(html.contains("<span>POST /public/v2/users</span>"), "Fixture must expose the API request node.");
        Assert.assertTrue(html.contains("request.body"), "Fixture must expose API request details.");
        return report;
    }

    public static Path uiLikeNestedExtentReport(Path reportsDir, String methodName) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(UI_REGISTRATION_TESTS, methodName, "UI report expand contract")
                .step("SETUP")
                .step("@BeforeMethod")
                .step("Registration preconditions")
                .pass("Ready")
                .pass("Before method complete")
                .pass("Setup complete")
                .step("TEST BODY")
                .step("Navigate to URL: https://practicesoftwaretesting.com/auth/register")
                .detail("relativePath=/auth/register")
                .pass("Navigated")
                .pass("Test body completed")
                .end(TestStatus.PASS)
                .flushAndFind(methodName)
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains("RegistrationTests"), "Fixture must mimic the UI class report.");
        Assert.assertTrue(html.contains("<span>" + methodName + "</span>"), "Fixture must expose the UI method node.");
        Assert.assertTrue(html.contains("<span>@BeforeMethod</span>"), "Fixture must expose the @BeforeMethod node.");
        Assert.assertTrue(html.contains("Navigate to URL"), "Fixture must expose UI action nodes.");
        return report;
    }

    public static Path finalScreenshotExtentReport(Path reportsDir, String screenshotTitle, String onePixelPng)
            throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(
                        UI_REGISTRATION_TESTS,
                        "final_screenshot_label_contract",
                        "Final screenshot label readability contract"
                )
                .step("TEST BODY")
                .pass("UI proof is ready")
                .attachScreenshotBase64(onePixelPng, screenshotTitle)
                .end(TestStatus.PASS)
                .flushAndFind(screenshotTitle)
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains(screenshotTitle), "Fixture must expose the final screenshot label.");
        Assert.assertTrue(html.contains("base64 img"), "Fixture must expose Extent's embedded screenshot badge.");
        return report;
    }

    public static Path finalScreenshotRuntimeAuditedDashboardReport(Path reportsDir, String screenshotTitle, String onePixelPng)
            throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(
                        UI_REGISTRATION_TESTS,
                        "final_screenshot_overview_contract",
                        "Final screenshot overview isolation contract"
                )
                .step("TEST BODY")
                .pass("UI proof is ready")
                .attachScreenshotBase64(onePixelPng, screenshotTitle)
                .end(TestStatus.PASS)
                .flushFindAndPublishRuntimeAudit(
                        screenshotTitle,
                        List.of(SyntheticRuntimeInvocation.passed(
                                "com.ai.atf.ui.RegistrationTests",
                                "final_screenshot_overview_contract",
                                System.currentTimeMillis()
                        ))
                )
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL), "Runtime tag audit must be injected.");
        Assert.assertTrue(html.contains(screenshotTitle), "Fixture must retain screenshot evidence in the test view.");
        return report;
    }

    public static Path multiCollectionTaggedExtentReport(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(API_POSITIVE_USER_TESTS, "create_user_success",
                        "API collection tag filter fixture")
                .step("TEST BODY")
                .step("POST /public/v2/users")
                .detail("response.status=201")
                .pass("Call completed")
                .pass("API body complete")
                .end(TestStatus.PASS)
                .test(UI_REGISTRATION_TESTS, "negative_invalid_email_should_fail",
                        "UI collection tag filter fixture")
                .step("TEST BODY")
                .step("Navigate to registration")
                .detail("relativePath=/auth/register")
                .pass("Navigated")
                .pass("UI body complete")
                .end(TestStatus.PASS)
                .test(UI_REGISTRATION_AUDIT_TESTS, "registration_audit_contract",
                        "Second exact UI package tag filter fixture")
                .step("TEST BODY")
                .step("Inspect registration audit trail")
                .detail("relativePath=/auth/register")
                .pass("Audit trail visible")
                .pass("UI audit body complete")
                .end(TestStatus.PASS)
                .test(UI_DEFAULTS_CONTRACT_TEST,
                        "ui_defaults_are_read_from_framework_properties",
                        "Framework collection tag filter fixture")
                .step("TEST BODY")
                .detail("ui.headless=true")
                .pass("Defaults verified")
                .end(TestStatus.PASS)
                .flushAndFind("ui_defaults_are_read_from_framework_properties")
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains("PositiveUserTests"), "Fixture must contain API class tag.");
        Assert.assertTrue(html.contains("RegistrationTests"), "Fixture must contain UI class tag.");
        Assert.assertTrue(html.contains("RegistrationAuditTests"), "Fixture must contain second exact UI package class.");
        Assert.assertTrue(html.contains("FrameworkRuntimeUiDefaultsContractTest"),
                "Fixture must contain framework class tag.");
        Assert.assertTrue(html.contains("fa-bug"), "Fixture must expose ATF failure triage navigation.");
        Assert.assertTrue(html.contains("tag-toggle"), "Fixture must expose Spark tag menu.");
        return report;
    }

    public static Path componentAdapterFailureReport(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        String marker = "component_adapter_fail_tab_contract";
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(COMPONENT_ADAPTER_FAIL_FIXTURE, marker,
                        "Component adapter status filter fixture")
                .step("TEST BODY")
                .fail("Component adapter failure", "Expected failure details for status filter")
                .end(TestStatus.FAIL)
                .flushAndFind(marker)
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains(marker), "Component adapter failure fixture must contain marker.");
        Assert.assertTrue(html.contains(ExtentDataAttributes.REPORT_MODE + "=\""
                        + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\""),
                "Fixture must stay component-adapter, not runtime-audited.");
        Assert.assertTrue(html.contains("status=\"fail\""), "Fixture must expose a failed top-level test item.");
        return report;
    }

    public static String runtimeAuditedSingleClassDashboardHtml(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        long start = System.currentTimeMillis();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(
                        ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class.getName(),
                        "runtime_single_class_passed_one",
                        "Runtime audit single-class dashboard fixture"
                )
                .step("TEST BODY")
                .pass("passed one")
                .end(TestStatus.PASS)
                .test(
                        ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class.getName(),
                        "runtime_single_class_passed_two",
                        "Runtime audit single-class dashboard fixture"
                )
                .step("TEST BODY")
                .pass("passed two")
                .end(TestStatus.PASS)
                .flushFindAndPublishRuntimeAudit(
                        "runtime_single_class_passed_two",
                        List.of(
                                SyntheticRuntimeInvocation.passed(
                                        "com.ai.atf.core.support.base.BaseTestAPIContractTest",
                                        "runtime_single_class_passed_one",
                                        start
                                ),
                                SyntheticRuntimeInvocation.passed(
                                        "com.ai.atf.core.support.base.BaseTestAPIContractTest",
                                        "runtime_single_class_passed_two",
                                        start + 1
                                )
                        )
                )
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains("<p>Timeline</p>"), "Runtime timeline must be injected.");
        return html;
    }

    public static Path runtimeAuditedDashboardReport(Path reportsDir) throws IOException {
        ExtentAdapterContractSupport.resetExtentRuntime();
        ExtentSyntheticReportBuilder.clearRuntimeAudit();
        long start = System.currentTimeMillis();
        Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                .test(ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class.getName(),
                        "runtime_audit_passed_one", "Runtime audit dashboard fixture")
                .step("TEST BODY")
                .pass("passed one")
                .end(TestStatus.PASS)
                .test(ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class.getName(),
                        "runtime_audit_passed_two", "Runtime audit dashboard fixture")
                .step("TEST BODY")
                .pass("passed two")
                .end(TestStatus.PASS)
                .test(ExtentSparkPresentationContractSupport.SecondaryExtentHierarchyFixture.class.getName(),
                        "runtime_audit_failed_one", "Runtime audit dashboard fixture")
                .step("TEST BODY")
                .fail("controlled failure", "synthetic failure for dashboard contract")
                .end(TestStatus.FAIL)
                .test("com.ai.atf.ui.RegistrationTests", "runtime_audit_skipped_one",
                        "Runtime audit dashboard fixture")
                .step("TEST BODY")
                .detail("controlled skipped test case")
                .end(TestStatus.SKIP)
                .flushFindAndPublishRuntimeAudit(
                        "runtime_audit_failed_one",
                        List.of(
                                SyntheticRuntimeInvocation.passed(
                                        "com.ai.atf.core.support.base.BaseTestAPIContractTest",
                                        "runtime_audit_passed_one",
                                        start
                                ),
                                SyntheticRuntimeInvocation.passed(
                                        "com.ai.atf.core.support.base.BaseTestAPIContractTest",
                                        "runtime_audit_passed_two",
                                        start + 1
                                ),
                                SyntheticRuntimeInvocation.failed(
                                        "com.ai.atf.ui.RegistrationTests",
                                        "runtime_audit_failed_one",
                                        start + 2,
                                        "synthetic failure for dashboard contract"
                                ),
                                SyntheticRuntimeInvocation.skipped(
                                        "com.ai.atf.ui.RegistrationTests",
                                        "runtime_audit_skipped_one",
                                        start + 3
                                )
                        )
                )
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                "Category failure groups block must be injected.");
        return report;
    }

    public static Path runtimeAuditedFailureFixtureReport(Path reportsDir) throws IOException {
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setSystemProperty("atf.failure.fixtures.enabled", "true")) {
            ExtentAdapterContractSupport.resetExtentRuntime();
            ExtentSyntheticReportBuilder.clearRuntimeAudit();
            long start = System.currentTimeMillis();
            Path report = ExtentSyntheticReportBuilder.in(reportsDir)
                    .test("com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture", "fail_before_test_body",
                            "Controlled before-method failure fixture")
                    .step("TEST BODY")
                    .fail("controlled before-method failure", "fixture-before-method-failure")
                    .end(TestStatus.FAIL)
                    .test("com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture", "test_body_is_blocked_by_before_failure",
                            "Controlled skipped test-body fixture")
                    .step("TEST BODY")
                    .detail("Blocked after before-method failure")
                    .end(TestStatus.SKIP)
                    .test("com.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture", "fail_inside_test_body",
                            "Controlled test-body failure fixture")
                    .step("TEST BODY")
                    .fail("controlled test-body failure", "fixture-test-body-failure")
                    .end(TestStatus.FAIL)
                    .test("com.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture", "fail_after_test_body",
                            "Controlled after-method failure fixture")
                    .step("TEST BODY")
                    .fail("controlled after-method failure", "fixture-after-method-failure")
                    .end(TestStatus.FAIL)
                    .flushFindAndPublishRuntimeAudit(
                            "test_body_is_blocked_by_before_failure",
                            List.of(
                                    SyntheticRuntimeInvocation.failed(
                                            "com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture",
                                            "fail_before_test_body",
                                            start,
                                            "fixture-before-method-failure"
                                    ),
                                    SyntheticRuntimeInvocation.skipped(
                                            "com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture",
                                            "test_body_is_blocked_by_before_failure",
                                            start + 1
                                    ),
                                    SyntheticRuntimeInvocation.failed(
                                            "com.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture",
                                            "fail_inside_test_body",
                                            start + 2,
                                            "fixture-test-body-failure"
                                    ),
                                    SyntheticRuntimeInvocation.failed(
                                            "com.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture",
                                            "fail_after_test_body",
                                            start + 3,
                                            "fixture-after-method-failure"
                                    )
                            )
                    )
                    .report();
            String html = Files.readString(report);
            Assert.assertTrue(
                    html.contains("3 root-cause signatures -> 3 failed test cases -> 3 affected Java classes"),
                    "Failure fixture report must expose three failed test cases."
            );
            Assert.assertTrue(html.contains("test_body_is_blocked_by_before_failure"),
                    "Failure fixture report must retain skipped test body.");
            return report;
        }
    }
}
