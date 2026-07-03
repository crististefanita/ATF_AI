package com.endava.ai.atf.core.reporting.adapters.spark;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.css.ExtentSparkShellPaletteAssertions;
import com.endava.ai.atf.core.reporting.adapters.css.ExtentSparkTypographyAssertions;
import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_SHELL_STYLE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_SHELL_STYLE_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkShellStyleContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_shell_sections_render_with_shared_dark_palette_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000,
                ExtentSparkShellPaletteAssertions::assertShellSectionsRenderWithSharedDarkPalette);
    }

    @Test
    public void extent_left_sidebar_selected_tab_is_visually_distinct_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000,
                ExtentSparkShellPaletteAssertions::assertSideNavSelectedStateIsVisible);
    }

    @Test
    public void extent_component_adapter_report_keeps_overview_free_of_fallback_card_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 900, page -> {
            page.click("#nav-dashboard");
            page.waitForFunction("() => !!document.querySelector('#" + ExtentMarkers.REPORT_MODE + "')");

            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => {"
                    + "if (!el || !el.getClientRects().length) { return false; }"
                    + "for (let node = el; node; node = node.parentElement) {"
                    + "const style = getComputedStyle(node);"
                    + "if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') { return false; }"
                    + "}"
                    + "return true;"
                    + "};"
                    + "const marker = document.querySelector('#" + ExtentMarkers.REPORT_MODE + "');"
                    + "const fallback = document.querySelector('#" + ExtentMarkers.LEGACY_COMPONENT_FALLBACK + "');"
                    + "let parsed = 'missing';"
                    + "try { parsed = marker ? JSON.parse(marker.textContent).mode : 'missing'; } catch (error) { parsed = 'invalid'; }"
                    + "return ["
                    + "'bodyMode=' + document.body.getAttribute('" + ExtentDataAttributes.REPORT_MODE + "'),"
                    + "'bodyRuntime=' + document.body.getAttribute('" + ExtentDataAttributes.RUNTIME_AUDIT + "'),"
                    + "'markerMode=' + parsed,"
                    + "'fallbackPresent=' + !!fallback,"
                    + "'fallbackVisible=' + visible(fallback)"
                    + "].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("bodyMode=component-adapter")
                            && state.contains("bodyRuntime=false")
                            && state.contains("markerMode=component-adapter"),
                    "Component/adapter report mode must be visible to browser code and MCP classifiers. State: " + state
            );
            Assert.assertTrue(state.contains("fallbackPresent=false") && state.contains("fallbackVisible=false"),
                    "Component/adapter reports must not render a fallback card in Overview. State: " + state);
        });
    }

    @Test
    public void extent_final_screenshot_label_stays_on_one_readable_line_in_browser() throws Exception {
        String label = "Final Screenshot - final-state UI proof";
        Path report = finalScreenshotExtentReport(label);

        ExtentSparkBrowserSession.openReport(report, 1480, 760, page -> {
            clickNode(page, "final_screenshot_label_contract");
            page.evaluate("() => document.querySelectorAll('.test-contents .card.extent-atf-collapsed > .card-header')"
                    + ".forEach(header => header.dispatchEvent(new MouseEvent('click', "
                    + "{bubbles: true, cancelable: true, view: window})))");
            page.waitForFunction("label => Array.from(document.querySelectorAll('.title'))"
                    + ".some(el => (el.textContent || '').trim() === label)", label);

            String state = String.valueOf(page.evaluate("label => {"
                    + "const visible = el => {"
                    + "if (!el || !el.getClientRects().length) { return false; }"
                    + "for (let node = el; node; node = node.parentElement) {"
                    + "const style = getComputedStyle(node);"
                    + "if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') { return false; }"
                    + "}"
                    + "return true;"
                    + "};"
                    + "const title = Array.from(document.querySelectorAll('.title'))"
                    + ".find(el => (el.textContent || '').trim() === label);"
                    + "if (!title) { return 'found=false'; }"
                    + "const style = getComputedStyle(title);"
                    + "const rect = title.getBoundingClientRect();"
                    + "return ["
                    + "'found=true',"
                    + "'visible=' + visible(title),"
                    + "'text=' + (title.textContent || '').trim().replace(/\\s+/g, ' '),"
                    + "'whiteSpace=' + style.whiteSpace,"
                    + "'height=' + Math.round(rect.height),"
                    + "'scrollWidth=' + title.scrollWidth,"
                    + "'clientWidth=' + title.clientWidth"
                    + "].join(';');"
                    + "}", label));

            Assert.assertTrue(state.contains("found=true"), "Screenshot media title must be rendered. State: " + state);
            Assert.assertTrue(
                    state.contains("text=" + label),
                    "Final screenshot proof label must remain intact. State: " + state
            );
            Assert.assertTrue(
                    state.contains("whiteSpace=nowrap"),
                    "Final screenshot proof label must be styled as a single readable line. State: " + state
            );
            int height = Integer.parseInt(state.replaceAll("(?s).*height=(\\d+).*", "$1"));
            if (height > 0) {
                Assert.assertTrue(
                        height <= 24,
                        "Visible final screenshot proof label must not wrap into multiple lines. State: " + state
                );
            }
        });
    }

    @Test
    public void extent_overview_does_not_show_screenshot_or_test_content_below_tag_audit_in_browser() throws Exception {
        String label = "Final Screenshot - final-state UI proof";
        Path report = finalScreenshotRuntimeAuditedDashboardReport(label);

        ExtentSparkBrowserSession.openReport(report, 1480, 900, page -> {
            page.click("#nav-dashboard");
            page.waitForTimeout(500);

            String state = String.valueOf(page.evaluate("() => {"
                    + "const visible = el => {"
                    + "if (!el || !el.getClientRects().length) { return false; }"
                    + "for (let node = el; node; node = node.parentElement) {"
                    + "const style = getComputedStyle(node);"
                    + "if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') { return false; }"
                    + "}"
                    + "return true;"
                    + "};"
                    + "const dashboard = document.querySelector('.dashboard-view');"
                    + "const testView = document.querySelector('.test-view');"
                    + "const categoryView = document.querySelector('.category-view');"
                    + "const describe = el => el ? [el.className, el.getAttribute('style') || '', getComputedStyle(el).display, getComputedStyle(el).visibility].join('|') : 'missing';"
                    + "const tags = dashboard && dashboard.querySelector('.atf-runtime-overview-tags');"
                    + "const media = dashboard ? Array.from(dashboard.querySelectorAll('img, a[href^=\"data:image\"], .media, .detail-body, .test-content'))"
                    + ".filter(visible).length : -1;"
                    + "const foreignMedia = Array.from(document.querySelectorAll('.test-view img, .test-view a[href^=\"data:image\"],"
                    + ".test-view .media, .test-view .detail-body, .test-view .test-content'))"
                    + ".filter(visible).length;"
                    + "let following = 0;"
                    + "if (tags) {"
                    + "for (let node = tags.nextElementSibling; node; node = node.nextElementSibling) {"
                    + "if (visible(node) && (node.innerText || node.textContent || '').trim()) { following++; }"
                    + "}"
                    + "}"
                    + "return ['dashboard=' + visible(dashboard), 'tagAudit=' + visible(tags),"
                    + "'testView=' + visible(testView), 'categoryView=' + visible(categoryView),"
                    + "'visibleMedia=' + media, 'visibleForeignMedia=' + foreignMedia,"
                    + "'visibleFollowing=' + following,"
                    + "'guard=' + (typeof window.atfShowExtentView),"
                    + "'navDashboard=' + !!document.querySelector('#nav-dashboard'),"
                    + "'dashboardState=' + describe(dashboard),"
                    + "'testState=' + describe(testView),"
                    + "'categoryState=' + describe(categoryView)].join(';');"
                    + "}"));

            Assert.assertTrue(
                    state.contains("dashboard=true") && state.contains("tagAudit=true"),
                    "Overview dashboard and runtime tag audit must be visible. State: " + state
            );
            Assert.assertTrue(
                    state.contains("visibleMedia=0"),
                    "Overview must not expose screenshot/media/test-content below runtime tag audit. State: " + state
            );
            Assert.assertTrue(
                    state.contains("testView=false")
                            && state.contains("categoryView=false")
                            && state.contains("visibleForeignMedia=0"),
                    "Overview navigation must hide Tests/Failures views so their screenshots cannot bleed under tag audit. State: "
                            + state
            );
            Assert.assertTrue(
                    state.contains("visibleFollowing=0"),
                    "Overview must end after the audited tag tables; no stray content should remain below them. State: " + state
            );
        });
    }

    @Test
    public void extent_spark_uses_hierarchical_typography_for_failure_metadata_and_code_blocks() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960,
                ExtentSparkTypographyAssertions::assertHierarchicalTypographyForFailureMetadataAndCodeBlocks);
    }

    @Test
    public void extent_spark_test_view_uses_single_runtime_background_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960,
                ExtentSparkShellPaletteAssertions::assertTestViewUsesSingleRuntimeBackground);
    }

    @Test
    public void extent_spark_category_view_reuses_test_view_visual_contract_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960,
                ExtentSparkShellPaletteAssertions::assertCategoryViewReusesTestViewVisualContract);
    }
}
