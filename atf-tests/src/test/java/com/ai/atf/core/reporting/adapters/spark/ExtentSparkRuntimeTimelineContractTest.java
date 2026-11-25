package com.ai.atf.core.reporting.adapters.spark;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_RUNTIME_TIMELINE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_RUNTIME_TIMELINE_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkRuntimeTimelineContractTest extends ExtentSparkPresentationContractSupport {

    @Test
    public void extent_runtime_timeline_groups_invocations_by_test_class() throws Exception {
        String html = runtimeAuditedSingleClassDashboardHtml();
        String dashboard = dashboardFragment(html);

        Assert.assertTrue(
                dashboard.contains("<p>Timeline</p>"),
                "Dashboard must keep the native Spark-style Timeline title."
        );
        Assert.assertTrue(
                dashboard.contains("style=\"width:100%\""),
                "Runtime timeline must collapse invocations from the same class into one class-level segment."
        );
        Assert.assertEquals(
                countOccurrences(dashboard, "atf-runtime-timeline-segment "),
                1,
                "A same-class fixture must render one timeline segment, not one segment per test invocation."
        );
        Assert.assertTrue(
                dashboard.contains("BaseTestAPIContractTest: 2 test(s), 2 passed, 0 failed, 0 skipped"),
                "Class-level timeline tooltip must summarize invocation count and statuses."
        );
        Assert.assertFalse(
                dashboard.contains("ATF Runtime Timeline") || dashboard.contains("atf-runtime-timeline-legend"),
                "Runtime timeline must not expose custom ATF title or legend in the Spark overview."
        );
    }

    @Test
    public void extent_runtime_timeline_segments_publish_hover_explanations() throws Exception {
        String html = runtimeAuditedSingleClassDashboardHtml();
        String dashboard = dashboardFragment(html);

        Assert.assertTrue(
                dashboard.contains(ExtentDataAttributes.RUNTIME_TOOLTIP + "="),
                "Each runtime timeline segment must publish a tooltip payload for mouse-over explanations."
        );
        Assert.assertFalse(
                dashboard.contains("#runtime_single_class_passed_one")
                        || dashboard.contains("#runtime_single_class_passed_two"),
                "Runtime timeline tooltip must be class-level, not method-level."
        );
        Assert.assertTrue(
                dashboard.contains("tabindex=\"0\""),
                "Runtime timeline tooltips must also be keyboard-focusable, not mouse-only."
        );
        Assert.assertTrue(
                html.contains(".dashboard-view .atf-runtime-timeline-segment:hover:after,")
                        && html.contains("content: attr(" + ExtentDataAttributes.RUNTIME_TOOLTIP + ");")
                        && html.contains("bottom: calc(100% + 0.35rem);")
                        && html.contains("background: " + EXTENT_BACKGROUND_HEX + ";")
                        && html.contains("color: " + EXTENT_TEXT_HEX + ";"),
                "Runtime timeline must render a controlled dark hover tooltip, not rely only on the browser title."
        );
    }

    @Test
    public void extent_runtime_timeline_hover_tooltip_is_visible_in_browser() throws Exception {
        Path report = runtimeAuditedDashboardReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            page.click("#nav-dashboard");
            page.waitForTimeout(500);
            page.hover(".dashboard-view .atf-runtime-timeline-segment");
            page.waitForTimeout(100);

            String state = (String) page.evaluate("() => {"
                    + "const segment = document.querySelector('.dashboard-view .atf-runtime-timeline-segment');"
                    + "const style = segment ? getComputedStyle(segment, '::after') : null;"
                    + "return ["
                    + "'segment=' + !!segment,"
                    + "'payload=' + (segment ? segment.getAttribute('" + ExtentDataAttributes.RUNTIME_TOOLTIP + "') : ''),"
                    + "'title=' + (segment ? segment.getAttribute('title') : ''),"
                    + "'content=' + (style ? style.content : ''),"
                    + "'bg=' + (style ? style.backgroundColor : ''),"
                    + "'color=' + (style ? style.color : ''),"
                    + "'position=' + (style ? style.position : '')"
                    + "].join(';');"
                    + "}");

            Assert.assertTrue(
                    state.contains("segment=true"),
                    "Rendered dashboard must contain runtime timeline segments. State: " + state
            );
            Assert.assertTrue(
                    state.contains("payload=")
                            && state.contains(": 2 test(s),")
                            && state.contains(" passed, ")
                            && state.contains(" failed, ")
                            && state.contains(" skipped")
                            && !state.contains("#"),
                    "Rendered segment must expose class-level invocation/status tooltip payload. State: " + state
            );
            Assert.assertTrue(
                    state.contains("title=")
                            && state.contains(": 2 test(s),")
                            && !state.contains("#"),
                    "Rendered segment must keep a native class-level title fallback. State: " + state
            );
            Assert.assertTrue(
                    state.contains("content=")
                            && state.contains(": 2 test(s),")
                            && !state.contains("#"),
                    "Hovering a timeline segment must render the class-level tooltip content. State: " + state
            );
            Assert.assertTrue(
                    state.contains("bg=" + EXTENT_BACKGROUND_RGB)
                            && state.contains("color=" + EXTENT_TEXT_RGB)
                            && state.contains("position=absolute"),
                    "Runtime timeline tooltip must use the controlled dark Spark-compatible style. State: " + state
            );
        });
    }
}
