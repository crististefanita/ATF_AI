package com.endava.ai.atf.core.reporting.adapters.tags;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_RUNTIME_TAG_TABLES_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_RUNTIME_TAG_TABLES_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkRuntimeTagTablesContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_category_view_shows_only_failure_groups_after_failure_triage() throws Exception {
        String html = runtimeAuditedDashboardHtml();
        String category = categoryFragment(html);

        Assert.assertTrue(
                html.contains(".category-view > .test-list,")
                        && html.contains("display: none !important;"),
                "Theme keeps a defensive hide rule for Spark native category content."
        );
        Assert.assertTrue(
                category.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                "ATF failure groups must own the Category/Tag tab."
        );
        Assert.assertFalse(
                category.contains("<div class=\"test-list\">")
                        || category.contains("<div class=\"test-content\">"),
                "Category view must contain only the ATF failure grouping surface, not Spark's native tag/category tree."
        );
        Assert.assertEquals(
                countOccurrences(category, ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()),
                0,
                "Category view must not duplicate the three Overview tag tables."
        );
    }

    @Test
    public void extent_pass_fail_skip_dropdown_report_keeps_runtime_audited_overview_tag_tables() throws Exception {
        Path report = passFailSkipStatusDropdownReport();
        String html = Files.readString(report);
        String dashboard = dashboardFragment(html);

        Assert.assertTrue(html.contains("status_dropdown_pass_contract"),
                "Status dropdown fixture must keep the passed test evidence.");
        Assert.assertTrue(html.contains("status_dropdown_fail_contract"),
                "Status dropdown fixture must keep the failed test evidence.");
        Assert.assertTrue(html.contains("status_dropdown_skip_contract"),
                "Status dropdown fixture must keep the skipped test evidence.");
        Assert.assertTrue(html.contains("status=\"skip\"") || html.contains("status=\"skipped\""),
                "Status dropdown fixture must expose a skipped status item.");
        Assert.assertTrue(html.contains("skip-bg") && html.contains(">Skip</span>"),
                "Status dropdown fixture must expose the agreed visible Skip label.");
        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(dashboard);
        Assert.assertFalse(dashboard.contains(ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()),
                "Runtime-audited status dropdown reports must not fall back to component-adapter tag tables.");
    }

    @Test
    public void extent_runtime_tag_tables_render_as_three_equal_width_cards_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            page.click("#nav-dashboard");
            page.waitForTimeout(500);

            String state = (String) page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const cards = Array.from(document.querySelectorAll('.dashboard-view .atf-runtime-tag-card')).filter(visible);"
                    + "const annotation = document.querySelector('.dashboard-view .atf-runtime-tag-card-annotations');"
                    + "const classCard = document.querySelector('.dashboard-view .atf-runtime-tag-card-classes');"
                    + "const packageCard = document.querySelector('.dashboard-view .atf-runtime-tag-card-packages');"
                    + "const cardWidths = [classCard, packageCard, annotation].map(node => node ? Math.round(node.getBoundingClientRect().width) : 0).join('|');"
                    + "const tableKinds = Array.from(document.querySelectorAll('.dashboard-view ["
                    + ExtentDataAttributes.RUNTIME_TAG_TABLE + "]'))"
                    + ".map(node => node.getAttribute('" + ExtentDataAttributes.RUNTIME_TAG_TABLE + "')).join('|');"
                    + "return ["
                    + "'cards=' + cards.length,"
                    + "'tables=' + tableKinds,"
                    + "'annotationGridColumn=' + (annotation ? getComputedStyle(annotation).gridColumn : 'missing'),"
                    + "'cardWidths=' + cardWidths"
                    + "].join(';');"
                    + "}");

            Assert.assertTrue(
                    state.contains("cards=3"),
                    "Rendered Overview must expose exactly three ATF tag cards. State: " + state
            );
            Assert.assertTrue(
                    state.contains("tables=classes|packages|annotations"),
                    "Rendered Overview must expose separate class, package, and annotation tables. State: " + state
            );
            Assert.assertFalse(
                    state.contains("cardWidths=0"),
                    "Rendered Overview must measure all three tag cards. State: " + state
            );
            assertEqualRuntimeTagCardWidths(state);
        });
    }

    @Test
    public void extent_overview_tag_tables_render_as_three_distinct_cards_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1900, 1100, page -> {
            page.click("#nav-dashboard");
            page.waitForTimeout(500);

            String state = (String) page.evaluate("() => {"
                    + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                    + "const dashboard = document.querySelector('.dashboard-view');"
                    + "const nativeTags = dashboard && Array.from(dashboard.querySelectorAll('.card-header p'))"
                    + ".some(node => (node.textContent || '').trim() === 'Tags');"
                    + "const timelineCard = dashboard && dashboard.querySelector('.atf-runtime-timeline-card');"
                    + "const timelineSegments = dashboard ? dashboard.querySelectorAll('.atf-runtime-timeline-segment').length : 0;"
                    + "const timelineTitle = timelineCard ? (timelineCard.querySelector('.card-header p')?.textContent || '').trim() : '';"
                    + "const timelineLegend = dashboard && dashboard.querySelector('.atf-runtime-timeline-legend');"
                    + "const sparkCanvas = dashboard && dashboard.querySelector('#timeline');"
                    + "const cards = dashboard ? Array.from(dashboard.querySelectorAll('.atf-runtime-tag-card')).filter(visible) : [];"
                    + "const tableKinds = dashboard ? Array.from(dashboard.querySelectorAll('["
                    + ExtentDataAttributes.RUNTIME_TAG_TABLE + "]'))"
                    + ".map(node => node.getAttribute('" + ExtentDataAttributes.RUNTIME_TAG_TABLE + "')).join('|') : '';"
                    + "return ['visible=' + visible(dashboard), 'nativeTags=' + nativeTags,"
                    + "'timelineCardVisible=' + visible(timelineCard),"
                    + "'timelineSegments=' + timelineSegments,"
                    + "'timelineTitle=' + timelineTitle,"
                    + "'timelineLegend=' + !!timelineLegend,"
                    + "'sparkCanvas=' + !!sparkCanvas,"
                    + "'cards=' + cards.length, 'tables=' + tableKinds].join(';');"
                    + "}");

            Assert.assertTrue(
                    state.contains("visible=true"),
                    "Dashboard overview must be visible after clicking the overview icon. State: " + state
            );
            Assert.assertTrue(
                    state.contains("nativeTags=false"),
                    "Dashboard overview must not render Spark's native mixed Tags table. State: " + state
            );
            Assert.assertTrue(
                    state.contains("timelineCardVisible=true"),
                    "Dashboard overview must render the runtime timeline card. State: " + state
            );
            Assert.assertTrue(
                    state.contains("timelineSegments=") && !state.contains("timelineSegments=0"),
                    "Dashboard overview must render visible ATF runtime timeline segments. State: " + state
            );
            Assert.assertTrue(
                    state.contains("timelineTitle=Timeline") && state.contains("timelineLegend=false"),
                    "Dashboard timeline must remain Spark-like: plain title and no custom ATF legend. State: " + state
            );
            Assert.assertTrue(
                    state.contains("sparkCanvas=false"),
                    "Dashboard overview must not keep Spark's empty timeline canvas after ATF runtime reconciliation. State: "
                            + state
            );
            Assert.assertTrue(
                    state.contains("cards=3"),
                    "Dashboard overview must render exactly three ATF runtime tag cards. State: " + state
            );
            Assert.assertTrue(
                    state.contains("tables=classes|packages|annotations"),
                    "Dashboard overview must render class, package, and annotation tables in order. State: " + state
            );
        });
    }
}
