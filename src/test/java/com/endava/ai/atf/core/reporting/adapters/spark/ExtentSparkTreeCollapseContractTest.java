package com.endava.ai.atf.core.reporting.adapters.spark;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.support.base.BaseTestAPIContractTest;
import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.endava.ai.atf.ui.RegistrationTests;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.TestStatus;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterFactory;
import com.endava.ai.core.reporting.adapters.extent.synthetic.ExtentSyntheticReportBuilder;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_TREE_COLLAPSE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_TREE_COLLAPSE_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkTreeCollapseContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_spark_collapses_tests_and_nested_steps_by_default() throws Exception {
        String html = nestedExtentReportHtml();
        Assert.assertTrue(
                html.contains("collapseExtentTreeByDefault"),
                "Extent Spark must inject an explicit default-collapse function for tests and nested nodes."
        );
        Assert.assertTrue(
                html.contains("extent-atf-collapsed"),
                "Extent Spark must mark each card with child content as collapsed by default."
        );
        Assert.assertTrue(
                html.contains(".test-contents .card.extent-atf-collapsed > .accordion"),
                "Default-collapse CSS must hide direct child accordions, because Extent parent nodes are not always .collapse.show."
        );
        Assert.assertTrue(
                html.contains(".test-contents .card.extent-atf-collapsed > .collapse"),
                "Default-collapse CSS must also hide classic Spark collapse bodies."
        );
        Assert.assertTrue(
                html.contains(".test-contents a.node:before")
                        && html.contains(".test-contents a.node:after"),
                "ATF must suppress native Spark node chevrons before adding its own single controlled arrow."
        );
        Assert.assertTrue(
                html.contains("extent-atf-root-node")
                        && html.contains("extent-atf-sub-node")
                        && html.contains(".test-contents .card.extent-atf-sub-node > .card-header .node-attr")
                        && html.contains(".test-contents .card .card > .card-header .node-attr"),
                "Extent steps must classify root method cards separately from nested setup/body/action nodes, so only the root keeps class/package badges."
        );
    }

    @Test
    public void extent_spark_collapses_direct_children_instead_of_only_global_toolbar() throws Exception {
        String html = nestedExtentReportHtml();

        Assert.assertTrue(
                html.contains("function directChildBodies(card)"),
                "Extent collapse must inspect each card's direct child bodies, not only call the global collapse toolbar."
        );
        Assert.assertTrue(
                html.contains("function injectArrowOverrideStyle()"),
                "Extent must inject a final runtime style override so Spark's own chevron CSS cannot reappear on nested nodes."
        );
        Assert.assertTrue(
                html.contains("child.classList.contains('accordion')||child.classList.contains('collapse')"),
                "Extent collapse must handle direct child accordions used by SETUP, @BeforeMethod, TEST BODY and nested steps."
        );
        Assert.assertTrue(
                html.contains("document.querySelectorAll('.test-contents .card').forEach(prepareCard)"),
                "Every visible test/step card must be prepared for default collapsed rendering."
        );
    }

    @Test
    public void extent_spark_preserves_manual_node_toggle_after_default_collapse() throws Exception {
        String html = nestedExtentReportHtml();

        Assert.assertTrue(
                html.contains("function togglePreparedCard(card,event)"),
                "Each collapsed card must remain manually expandable through delegated header clicks."
        );
        Assert.assertTrue(
                html.contains("extent-atf-user-expanded"),
                "Manual node expansion must be tracked so observer-based default collapse does not immediately close it again."
        );
        Assert.assertTrue(
                html.contains("function collapseDescendantCards(card)"),
                "Manual node expansion must reveal only the next level; deeper child cards stay collapsed until clicked."
        );
        Assert.assertTrue(
                html.contains("function cardForHeaderClick(event)"),
                "Manual expansion must resolve the clicked header to the prepared card so browser clicks reveal nested content."
        );
        Assert.assertTrue(
                html.contains("var targetCard=cardForHeaderClick(event);"),
                "Manual expansion must resolve the clicked header to its prepared card before toggling."
        );
        Assert.assertTrue(
                html.contains("setCardCollapsed(card,!expanding);"),
                "Manual expansion must remove the collapsed state so child accordions such as TEST BODY and API call details become visible."
        );
        Assert.assertTrue(
                html.contains("body.classList.toggle('show',!collapsed)"),
                "Manual expansion must also toggle Bootstrap's .show class; otherwise leaf detail bodies such as API request rows stay hidden."
        );
        Assert.assertFalse(
                html.contains("document.querySelectorAll('.test-contents .collapse.show').forEach(function(node){node.classList.remove('show');});"),
                "Default-collapse cleanup must not globally remove .show from user-expanded detail bodies."
        );
        Assert.assertTrue(
                html.contains("if(targetCard){togglePreparedCard(targetCard,event);return;}"),
                "Click handling must be delegated globally because Spark can rehydrate visible card nodes after initial preparation."
        );
        Assert.assertTrue(
                html.contains("!event.target.closest('.test-contents')"),
                "Selecting another test may re-apply the default collapse, but clicking inside test details must not instantly re-collapse the node."
        );
    }

    @Test
    public void extent_spark_manual_expand_reveals_nested_api_details_in_browser() throws Exception {
        String methodName = "create_user_success_" + UUID.randomUUID().toString().replace("-", "_");
        Path report = apiLikeNestedExtentReport(methodName);

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            Assert.assertEquals(
                    arrowCount(page, methodName),
                    1,
                    "Each expandable Extent node must have exactly one visible arrow marker."
            );
            Assert.assertEquals(
                    nativePseudoArrow(page, methodName),
                    "none",
                    "Native Extent pseudo-arrow must be disabled so the ATF arrow is not duplicated."
            );
            Assert.assertFalse(
                    isVisibleNode(page, "TEST BODY"),
                    "TEST BODY must start collapsed so the report opens cleanly."
            );

            clickNode(page, methodName);
            assertNodeHeaderContains(page, methodName, "PositiveUserTests");
            assertNodeHeaderContains(page, methodName, "com.endava.ai.api.tests");
            Assert.assertTrue(
                    isVisibleNode(page, "TEST BODY"),
                    "Clicking a collapsed API test card must reveal its TEST BODY child. "
                            + extentNodeDebug(page, methodName, "TEST BODY")
            );
            Assert.assertFalse(
                    isVisibleNode(page, "POST /public/v2/users"),
                    "Expanding the method must reveal only TEST BODY; API call details stay collapsed until TEST BODY is clicked."
            );

            clickNode(page, "TEST BODY");
            assertNodeHeaderDoesNotContain(page, "TEST BODY", "PositiveUserTests");
            assertNodeHeaderDoesNotContain(page, "TEST BODY", "com.endava.ai.api.tests");
            Assert.assertTrue(
                    isVisibleNode(page, "POST /public/v2/users"),
                    "Clicking TEST BODY must reveal API call details, not leave an empty expanded container."
            );
            Assert.assertTrue(
                    isVisibleNode(page, "Validate: status=201"),
                    "Clicking TEST BODY must also reveal API validation nodes as collapsed siblings."
            );
            Assert.assertEquals(
                    arrowCount(page, "Validate: status=201"),
                    1,
                    "Nested validation nodes must have exactly one visible arrow marker."
            );
            Assert.assertEquals(
                    nativePseudoArrow(page, "Validate: status=201"),
                    "none",
                    "Nested validation nodes must not keep Spark's native pseudo-arrow beside the ATF arrow."
            );
            clickNode(page, "POST /public/v2/users");
            assertNodeHeaderDoesNotContain(page, "POST /public/v2/users", "PositiveUserTests");
            assertNodeHeaderDoesNotContain(page, "POST /public/v2/users", "com.endava.ai.api.tests");
            Assert.assertTrue(
                    isVisibleText(page, "request.body"),
                    "Expanded API call node must expose the request details. "
                            + extentNodeDebug(page, "POST /public/v2/users", "request.body")
            );
        });
    }

    @Test
    public void extent_spark_manual_expand_reveals_ui_setup_and_test_body_in_browser() throws Exception {
        String methodName = "ui_invalid_email_contract_" + UUID.randomUUID().toString().replace("-", "_");
        Path report = uiLikeNestedExtentReport(methodName);

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            Assert.assertFalse(
                    isVisibleNode(page, "SETUP"),
                    "UI setup must start collapsed under the test method."
            );
            Assert.assertFalse(
                    isVisibleNode(page, "TEST BODY"),
                    "UI test body must start collapsed under the test method."
            );

            clickNode(page, methodName);
            assertNodeHeaderContains(page, methodName, "RegistrationTests");
            assertNodeHeaderContains(page, methodName, "com.endava.ai.atf.ui");
            Assert.assertTrue(
                    isVisibleNode(page, "SETUP"),
                    "Clicking the UI test method must reveal SETUP. "
                            + extentNodeDebug(page, methodName, "SETUP")
            );
            Assert.assertTrue(
                    isVisibleNode(page, "TEST BODY"),
                    "Clicking the UI test method must reveal TEST BODY. "
                            + extentNodeDebug(page, methodName, "TEST BODY")
            );
            Assert.assertFalse(
                    isVisibleNode(page, "@BeforeMethod"),
                    "Expanding the UI method reveals only top-level SETUP/TEST BODY; nested setup details stay collapsed."
            );

            clickNode(page, "SETUP");
            assertNodeHeaderDoesNotContain(page, "SETUP", "RegistrationTests");
            assertNodeHeaderDoesNotContain(page, "SETUP", "com.endava.ai.atf.ui");
            Assert.assertTrue(
                    isVisibleNode(page, "@BeforeMethod"),
                    "Clicking SETUP must reveal @BeforeMethod."
            );

            clickNode(page, "TEST BODY");
            assertNodeHeaderDoesNotContain(page, "TEST BODY", "RegistrationTests");
            assertNodeHeaderDoesNotContain(page, "TEST BODY", "com.endava.ai.atf.ui");
            Assert.assertTrue(
                    isVisibleNode(page, "Navigate to URL"),
                    "Clicking TEST BODY must reveal UI action nodes."
            );
        });
    }

    @Test
    public void extent_spark_keeps_every_root_test_method_collapsed_after_class_selection() throws Exception {
        String className = "com.endava.ai.atf.core.reporting.MultiMethodCollapseFixture";
        String firstMethod = "collapse_contract_method_one_" + UUID.randomUUID().toString().replace("-", "_");
        String secondMethod = "collapse_contract_method_two_" + UUID.randomUUID().toString().replace("-", "_");
        String thirdMethod = "collapse_contract_method_three_" + UUID.randomUUID().toString().replace("-", "_");
        Path report = ExtentSyntheticReportBuilder.in(contractReportsDir)
                .test(className, firstMethod, "Multi-method collapse fixture")
                .step("TEST BODY")
                .step("first method action")
                .detail("first evidence")
                .pass("first action done")
                .pass("first body done")
                .end(TestStatus.PASS)
                .test(className, secondMethod, "Multi-method collapse fixture")
                .step("TEST BODY")
                .step("second method action")
                .detail("second evidence")
                .pass("second action done")
                .pass("second body done")
                .end(TestStatus.PASS)
                .test(className, thirdMethod, "Multi-method collapse fixture")
                .step("TEST BODY")
                .step("third method action")
                .detail("third evidence")
                .pass("third action done")
                .pass("third body done")
                .end(TestStatus.PASS)
                .flushAndFind(thirdMethod)
                .report();
        String html = Files.readString(report);
        Assert.assertTrue(
                html.contains("collapseExtentTreeByDefault"),
                "Generated Extent reports must contain executable ATF collapse JavaScript, not only collapse CSS."
        );

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            selectClassFromTestList(page, "MultiMethodCollapseFixture");
            assertRootMethodCollapsed(page, firstMethod);
            assertRootMethodCollapsed(page, secondMethod);
            assertRootMethodCollapsed(page, thirdMethod);

            clickNode(page, firstMethod);
            Assert.assertTrue(
                    isVisibleNode(page, "TEST BODY"),
                    "Manual expand of one root method must still reveal its TEST BODY."
            );
            assertRootMethodCollapsed(page, secondMethod);
            assertRootMethodCollapsed(page, thirdMethod);
        });
    }

    @Test
    public void extent_spark_does_not_show_expand_arrows_for_leaf_nodes_without_details() throws Exception {
        String html = nestedExtentReportHtml();

        Assert.assertTrue(
                html.contains("extent-atf-leaf"),
                "Extent Spark must mark nodes without direct child content as leaf nodes."
        );
        Assert.assertTrue(
                html.contains(".test-contents .card.extent-atf-leaf > .card-header .extent-atf-arrow"),
                "Leaf nodes must not show a fake expand arrow when there is no visible body to open."
        );
        Assert.assertTrue(
                html.contains("if(!hasDirectChildBodies(card)){card.classList.add(leafClass);return;}"),
                "Leaf classification must happen before collapse/toggle wiring so empty API nodes do not look expandable."
        );
    }

    private static void selectClassFromTestList(Page page, String className) {
        boolean selected = Boolean.TRUE.equals(page.evaluate("className => {"
                + "const item = Array.from(document.querySelectorAll('.test-list li.test-item, .test-list .test-item'))"
                + ".find(node => (node.innerText || node.textContent || '').includes(className));"
                + "if (!item) { return false; }"
                + "item.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));"
                + "if (window.collapseExtentTreeByDefault) { window.collapseExtentTreeByDefault(); }"
                + "return true;"
                + "}", className));
        Assert.assertTrue(selected, "Expected Extent side-list class item to be selectable: " + className);
        page.waitForTimeout(300);
        waitForPreparedExtentTree(page);
    }

    private static void assertRootMethodCollapsed(Page page, String methodName) {
        String state = String.valueOf(page.evaluate("methodName => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const cards = Array.from(document.querySelectorAll('.test-content .detail-body .test-contents .card, "
                + ".test-content .detail-body .card')).filter(visible);"
                + "const card = cards.find(candidate => "
                + "(candidate.querySelector(':scope > .card-header a.node')?.textContent || '').includes(methodName));"
                + "if (!card) { return 'missing-root-method:' + methodName; }"
                + "const bodies = Array.from(card.children).filter(child => child.classList "
                + "&& (child.classList.contains('accordion') || child.classList.contains('collapse')));"
                + "const bodyState = bodies.map(body => ["
                + "body.className,"
                + "getComputedStyle(body).display,"
                + "body.classList.contains('show')"
                + "].join(':')).join('|');"
                + "return 'method=' + methodName"
                + " + ';collapsed=' + card.classList.contains('extent-atf-collapsed')"
                + " + ';ready=' + card.classList.contains('extent-atf-collapse-ready')"
                + " + ';bodies=' + bodies.length"
                + " + ';bodyState=' + bodyState;"
                + "}", methodName));
        Assert.assertTrue(
                state.contains("collapsed=true")
                        && state.contains("ready=true")
                        && state.contains("bodies=1")
                        && state.contains(":none:false"),
                "Root test method must be collapsed by default with its direct child body hidden. State: " + state
        );
    }

}
