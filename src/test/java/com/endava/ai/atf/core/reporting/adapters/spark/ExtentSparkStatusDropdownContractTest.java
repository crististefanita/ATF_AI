package com.endava.ai.atf.core.reporting.adapters.spark;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_STATUS_DROPDOWN)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_STATUS_DROPDOWN_CONTRACT)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkStatusDropdownContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_spark_status_dropdown_counts_test_cases_without_nested_step_duplicates() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String failState = selectStatusAndReadMain(page, "fail");
            Assert.assertTrue(
                    failState.contains("3 matching test case(s)"),
                    "Selecting Fail must count failed test cases, not nested failed substeps. State: " + failState
            );
            Assert.assertFalse(
                    failState.contains("6 matching test case(s)"),
                    "Selecting Fail must not double-count method cards and nested TEST BODY cards. State: " + failState
            );
            Assert.assertTrue(
                    failState.contains("fail_before_test_body")
                            && failState.contains("fail_inside_test_body")
                            && failState.contains("fail_after_test_body"),
                    "Selecting Fail must keep all three failed fixture methods visible. State: " + failState
            );

            String skippedState = selectStatusAndReadMain(page, "skip");
            Assert.assertTrue(
                    skippedState.contains("1 matching test case(s)")
                            && skippedState.contains("test_body_is_blocked_by_before_failure"),
                    "Selecting Skipped must count and show the single skipped test case. State: " + skippedState
            );
            Assert.assertFalse(
                    skippedState.contains("fail_before_test_body")
                            || skippedState.contains("fail_inside_test_body")
                            || skippedState.contains("fail_after_test_body"),
                    "Selecting Skipped must not leak failed fixture methods into the selected details. State: " + skippedState
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_renders_pass_fail_skip_clear_for_real_skipped_report()
            throws Exception {
        Path report = passFailSkipStatusDropdownReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                    + "if (!statusButton) { return 'missing-status-button'; }"
                    + "statusButton.click();"
                    + "const menu = document.querySelector('#status-toggle.dropdown-menu');"
                    + "if (!menu) { return 'missing-status-menu'; }"
                    + "const items = Array.from(menu.querySelectorAll('.dropdown-item'))"
                    + ".filter(visible)"
                    + ".map(item => {"
                    + "const text = (item.innerText || item.textContent || '').replace(/\\s+/g, ' ').trim();"
                    + "const status = item.getAttribute('status') || '';"
                    + "const marker = item.querySelector('.status.warning') ? 'warning' :"
                    + " item.querySelector('.status.danger') ? 'danger' :"
                    + " item.querySelector('.status.success') ? 'success' :"
                    + " item.querySelector('.fa-times,.fa-close') ? 'clear' : 'missing';"
                    + "return status + ':' + text + ':' + marker;"
                    + "}).join('|');"
                    + "return ['items=' + items, 'html=' + menu.innerHTML.replace(/\\s+/g, ' ')].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("items=pass:Pass:success|fail:Fail:danger|skip:Skip:warning|clear:Clear:clear"),
                    "Status dropdown must visibly render Pass, Fail, Skip and Clear in order for a report with a real skipped test. State: "
                            + state
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_survives_fail_skip_fail_round_trip_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String firstFailState = selectStatusAndReadMain(page, "fail");
            String skippedState = selectStatusAndReadMain(page, "skip");
            String secondFailState = selectStatusAndReadMain(page, "fail");

            Assert.assertTrue(
                    firstFailState.contains("3 matching test case(s)")
                            && firstFailState.contains("fail_before_test_body")
                            && firstFailState.contains("fail_inside_test_body")
                            && firstFailState.contains("fail_after_test_body"),
                    "First Fail selection must expose before/body/after failures. State: " + firstFailState
            );
            Assert.assertTrue(
                    skippedState.contains("1 matching test case(s)")
                            && skippedState.contains("test_body_is_blocked_by_before_failure"),
                    "Skipped selection after Fail must reopen the status menu and expose the blocked test-body test case. State: "
                            + skippedState
            );
            Assert.assertFalse(
                    skippedState.contains("fail_before_test_body")
                            || skippedState.contains("fail_inside_test_body")
                            || skippedState.contains("fail_after_test_body"),
                    "Skipped selection after Fail must not leak failed before/body/after test cases. State: " + skippedState
            );
            Assert.assertTrue(
                    secondFailState.contains("3 matching test case(s)")
                            && secondFailState.contains("fail_before_test_body")
                            && secondFailState.contains("fail_inside_test_body")
                            && secondFailState.contains("fail_after_test_body"),
                    "Second Fail selection after Skipped must still work; dropdown state must not get stuck. State: "
                            + secondFailState
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_pass_filter_isolates_green_test_cases_in_mixed_runtime_report() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String passState = selectStatusAndReadMain(page, "pass");

            Assert.assertTrue(
                    passState.contains("2 matching test case(s)")
                            && passState.contains("runtime_audit_passed_one")
                            && passState.contains("runtime_audit_passed_two"),
                    "Pass selection in a mixed pass/fail/skip report must expose only green test cases. State: "
                            + passState
            );
            Assert.assertFalse(
                    passState.contains("runtime_audit_failed_one")
                            || passState.contains("runtime_audit_skipped_one"),
                    "Pass selection must not leak failed or skipped test cases in the selected details. State: " + passState
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_clear_refreshes_report_after_status_filter_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String failState = selectStatusAndReadMain(page, "fail");
            Assert.assertTrue(
                    failState.contains("3 matching test case(s)"),
                    "Precondition: Fail selection must enter the filtered test case view. State: " + failState
            );

            String clearState = selectStatusClearAndReadPage(page);
            Assert.assertFalse(
                    clearState.contains("matching test case(s)")
                            || clearState.contains("atf-runtime-status-selection"),
                    "Clear must refresh the report instead of leaving the synthetic status-selection view active. State: "
                            + clearState
            );
            Assert.assertTrue(
                    clearState.contains("BeforeMethodFailureFixture")
                            && clearState.contains("TestBodyFailureFixture")
                            && clearState.contains("AfterMethodFailureFixture"),
                    "Clear must restore the full test hierarchy after a status filter. State: " + clearState
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_fail_filter_works_for_small_component_adapter_reports()
            throws Exception {
        Path report = componentAdapterFailureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String failState = selectStatusAndReadMain(page, "fail");

            Assert.assertTrue(
                    failState.contains("component_adapter_fail_tab_contract")
                            && failState.contains("ComponentAdapterFailFixture")
                            && failState.contains("Expected failure details for status filter"),
                    "Selecting Fail must expose the failed component-adapter test even without runtime audit. State: "
                            + failState
            );
            Assert.assertTrue(
                    failState.contains("matching test case(s)"),
                    "Fail selection must still render the canonical status-selection shell. State: " + failState
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_skipped_filter_reaches_skipped_runtime_node_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                    + "if (!statusButton) { return 'missing-status-button'; }"
                    + "statusButton.click();"
                    + "const menu = document.querySelector('#status-toggle');"
                    + "const skipped = menu && Array.from(menu.querySelectorAll('[status=\"skip\"],[status=\"skipped\"]'))"
                    + ".find(visible);"
                    + "if (!skipped) { return 'missing-skipped-item'; }"
                    + "skipped.click();"
                    + "const side = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                    + ".filter(visible).map(el => (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()).join('|');"
                    + "const main = (document.querySelector('.test-content')?.innerText || '').replace(/\\s+/g, ' ');"
                    + "return ['side=' + side, 'main=' + main].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("runtime_audit_skipped_one"),
                    "Selecting Skipped in the status dropdown must reach a real skipped runtime node. State: " + state
            );
            Assert.assertFalse(
                    state.contains("runtime_audit_failed_one"),
                    "Selecting Skipped must not leave failed runtime nodes visible as the active selection. State: " + state
            );
        });
    }

    @Test
    public void extent_spark_status_dropdown_skipped_filter_isolates_skipped_fixture_from_failed_fixtures_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                    + "if (!statusButton) { return 'missing-status-button'; }"
                    + "statusButton.click();"
                    + "const menu = document.querySelector('#status-toggle');"
                    + "const skipped = menu && Array.from(menu.querySelectorAll('[status=\"skip\"],[status=\"skipped\"]'))"
                    + ".find(visible);"
                    + "if (!skipped) { return 'missing-skipped-item'; }"
                    + "skipped.click();"
                    + "const side = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                    + ".filter(visible).map(el => (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()).join('|');"
                    + "const main = (document.querySelector('.test-content')?.innerText || '').replace(/\\s+/g, ' ');"
                    + "return ['side=' + side, 'main=' + main].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("test_body_is_blocked_by_before_failure"),
                    "Selecting Skipped must reveal the test blocked by the before-method failure. State: " + state
            );
            Assert.assertFalse(
                    state.contains("fail_before_test_body")
                            || state.contains("fail_inside_test_body")
                            || state.contains("fail_after_test_body"),
                    "Selecting Skipped must isolate skipped test cases from failed before/body/after fixtures. State: " + state
            );
        });
    }

    private static String selectStatusAndReadMain(Page page, String status) {
        return String.valueOf(page.evaluate("status => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                + "if (!statusButton) { return 'missing-status-button'; }"
                + "statusButton.click();"
                + "const menu = document.querySelector('#status-toggle');"
                + "const item = menu && Array.from(menu.querySelectorAll('[status]'))"
                + ".find(node => visible(node) && (node.getAttribute('status') === status"
                + " || (status === 'skip' && node.getAttribute('status') === 'skipped')));"
                + "if (!item) { return 'missing-status-item:' + status; }"
                + "item.click();"
                + "const main = (document.querySelector('.test-content')?.innerText || '').replace(/\\s+/g, ' ');"
                + "return main;"
                + "}", status));
    }

    private static String selectStatusClearAndReadPage(Page page) {
        page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const statusButton = document.querySelector('.test-list-tools .fa.fa-exclamation-circle')?.closest('a');"
                + "if (!statusButton) { return 'missing-status-button'; }"
                + "statusButton.click();"
                + "const menu = document.querySelector('#status-toggle');"
                + "const item = menu && Array.from(menu.querySelectorAll('[status]'))"
                + ".find(node => visible(node) && node.getAttribute('status') === 'clear');"
                + "if (!item) { return 'missing-clear-item'; }"
                + "item.click();"
                + "return 'clear-clicked';"
                + "}");
        page.waitForLoadState();
        page.waitForTimeout(500);
        waitForPreparedExtentTree(page);
        return String.valueOf(page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const side = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                + ".filter(visible).map(el => (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()).join('|');"
                + "const main = (document.querySelector('.test-content')?.innerText || '').replace(/\\s+/g, ' ');"
                + "return ['side=' + side, 'main=' + main].join(';');"
                + "}"));
    }
}
