package com.endava.ai.atf.core.reporting.adapters.spark;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_REPORT_MODE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_REPORT_MODE_CONTRACT)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkReportModeContractTest {

    @Test
    public void componentAdapterReportGetsModeMarkerAndComponentOverviewAudit() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());

        Assert.assertTrue(
                normalized.contains(ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE))
                        && normalized.contains(modeJson(ExtentReportModeType.COMPONENT_ADAPTER))
                        && normalized.contains(modeAttribute(ExtentReportModeType.COMPONENT_ADAPTER))
                        && normalized.contains(ExtentRuntimeOverviewTables.attributeEquals(
                                ExtentDataAttributes.RUNTIME_AUDIT,
                                "false"
                        )),
                "Component/adapter Extent reports must expose a machine-readable non-runtime mode marker."
        );
        Assert.assertTrue(normalized.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                        && normalized.contains(ExtentRuntimeOverviewTables.componentTagTableAttribute(
                                ExtentDataAttributes.TAG_TABLE_CLASSES))
                        && normalized.contains(ExtentRuntimeOverviewTables.componentTagTableAttribute(
                                ExtentDataAttributes.TAG_TABLE_PACKAGES))
                        && normalized.contains(ExtentRuntimeOverviewTables.componentTagTableAttribute(
                                ExtentDataAttributes.TAG_TABLE_ANNOTATIONS)),
                "Component/adapter Extent reports must not leave Overview as raw Spark; publish the ATF component tag audit.");
        Assert.assertFalse(normalized.contains(ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()),
                "Component/adapter tag tables must not masquerade as runtime-audit tables.");
        Assert.assertFalse(normalized.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK),
                "Component/adapter mode must use the canonical component audit, not the old fallback marker.");
    }

    @Test
    public void postProcessorNormalizesSparkTimestampSpacingAndNavigationGuard() {
        String html = "<span>6:27:24\u00E2\u20AC\u00AFPM</span>"
                + "<td>6:27:25\u202FPM</td>"
                + "<td>6:27:26\u00C2\u00A0PM</td>"
                + "<i class=\"fa fa-tag\"></i>";

        String normalized = ExtentSparkHtmlPostProcessor.normalize(html);

        Assert.assertTrue(
                normalized.contains("6:27:24 PM")
                        && normalized.contains("6:27:25 PM")
                        && normalized.contains("6:27:26 PM"),
                "Extent post-processing must convert Spark timestamp spacing artifacts into plain readable spaces."
        );
        Assert.assertFalse(
                normalized.contains("\u00E2\u20AC\u00AF")
                        || normalized.contains("\u202F")
                        || normalized.contains("\u00C2\u00A0"),
                "Extent report HTML must not retain timestamp characters that render as mojibake in local file browsers."
        );
        Assert.assertTrue(normalized.contains("class=\"fa fa-bug\""),
                "The category navigation icon must read as failure triage, not as a generic tag browser.");
        Assert.assertTrue(
                normalized.contains(ExtentMarkers.NAVIGATION_GUARD)
                        && normalized.contains("window.atfShowExtentView=show;")
                        && normalized.contains("activate(visibleView());")
                        && normalized.contains("document.addEventListener('click',navClick,true);")
                        && normalized.contains("event.stopPropagation();"),
                "Extent navigation guard must intercept Spark nav clicks and expose the ATF view switcher."
        );
        Assert.assertEquals(count(ExtentSparkHtmlPostProcessor.normalize(normalized), ExtentMarkers.NAVIGATION_GUARD), 1,
                "Extent navigation guard must be injected once and remain idempotent.");
    }

    @Test
    public void runtimeAuditedReportGetsRuntimeModeWithoutComponentFallback() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml()
                .replace("</div></body>", runtimeAuditShell("<section>"
                        + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL
                        + "</section>") + "</div></body>"));

        Assert.assertTrue(
                normalized.contains(ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE))
                        && normalized.contains(modeJson(ExtentReportModeType.RUNTIME_AUDITED))
                        && normalized.contains(ExtentRuntimeOverviewTables.attributeEquals(
                                ExtentDataAttributes.RUNTIME_AUDIT,
                                "true"
                        )),
                "Runtime-audited Extent reports must publish runtime-audited mode."
        );
        Assert.assertFalse(
                normalized.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK),
                "Runtime-audited reports must not receive the component/adapter fallback card."
        );
    }

    @Test
    public void reportModeMarkerAndFallbackAreIdempotent() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());
        String normalizedTwice = ExtentSparkHtmlPostProcessor.normalize(normalized);

        Assert.assertEquals(count(normalizedTwice, ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE)), 1,
                "Report mode marker must be injected exactly once.");
        Assert.assertEquals(count(normalizedTwice, ExtentMarkers.LEGACY_COMPONENT_FALLBACK), 0,
                "Component fallback card must not be injected.");
        Assert.assertEquals(count(normalizedTwice, ExtentMarkers.COMPONENT_TAG_AUDIT_START), 1,
                "Component Overview audit must be injected once and remain idempotent.");
        Assert.assertEquals(count(normalizedTwice, ExtentMarkers.NAVIGATION_GUARD), 1,
                "Navigation guard must remain idempotent after report-mode normalization.");
    }

    @Test
    public void postProcessorUpgradesStaleComponentModeWhenRuntimeAuditIsAddedLater() {
        String component = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());
        String auditedLater = component.replace("</body>", runtimeAuditShell("<section>"
                + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL
                + "</section>") + "</body>");

        String normalized = ExtentSparkHtmlPostProcessor.normalize(auditedLater);

        Assert.assertEquals(count(normalized, ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE)), 1,
                "Upgrading a stale report mode must replace the old marker instead of appending a second one.");
        Assert.assertTrue(
                normalized.contains(modeAttribute(ExtentReportModeType.RUNTIME_AUDITED))
                        && normalized.contains(ExtentRuntimeOverviewTables.attributeEquals(
                                ExtentDataAttributes.RUNTIME_AUDIT,
                                "true"
                        ))
                        && normalized.contains(modeJson(ExtentReportModeType.RUNTIME_AUDITED))
                        && normalized.contains(runtimeAuditJson(true)),
                "Reports that gain runtime audit content after an earlier normalization must be reclassified."
        );
        Assert.assertFalse(
                normalized.contains(modeAttribute(ExtentReportModeType.COMPONENT_ADAPTER))
                        || normalized.contains(ExtentRuntimeOverviewTables.attributeEquals(
                                ExtentDataAttributes.RUNTIME_AUDIT,
                                "false"
                        ))
                        || normalized.contains(modeJson(ExtentReportModeType.COMPONENT_ADAPTER))
                        || normalized.contains(runtimeAuditJson(false)),
                "The stale component-adapter mode must be removed when runtime audit content is present."
        );
        Assert.assertFalse(
                normalized.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                        || normalized.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_START)
                        || normalized.contains(ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()),
                "The stale component Overview tag audit must be removed when the final report becomes runtime-audited."
        );
        Assert.assertEquals(count(normalized, ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL), 1,
                "Runtime-audited reports must expose exactly one tag-audit zone.");
    }

    @Test
    public void overviewTagAuditZoneIsExclusiveAcrossReportModes() {
        String componentOnly = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());
        assertOverviewAuditShape(componentOnly, "component-only report", "component-adapter", 1, 0, 3, 0);

        String runtimeOnly = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml()
                .replace("</div></body>", runtimeAuditBlock() + "</div></body>"));
        assertOverviewAuditShape(runtimeOnly, "runtime-first report", "runtime-audited", 0, 1, 0, 3);

        String staleComponentThenRuntime = ExtentSparkHtmlPostProcessor.normalize(componentOnly
                .replace("</body>", runtimeAuditBlock() + "</body>"));
        assertOverviewAuditShape(staleComponentThenRuntime, "component upgraded to runtime", "runtime-audited", 0, 1, 0, 3);

        String normalizedTwice = ExtentSparkHtmlPostProcessor.normalize(staleComponentThenRuntime);
        assertOverviewAuditShape(normalizedTwice, "runtime report normalized twice", "runtime-audited", 0, 1, 0, 3);
    }

    @Test
    public void postProcessorUpgradesExistingNavigationGuardToCurrentActiveTabBehavior() {
        String oldGuard = "<script " + ExtentMarkers.scriptId(ExtentMarkers.NAVIGATION_GUARD) + ">"
                + "(function(){window.atfShowExtentView=function(view){return view;};})();"
                + "</script>";

        String normalized = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml()
                .replace("</body>", oldGuard + "</body>"));

        Assert.assertEquals(count(normalized, ExtentMarkers.NAVIGATION_GUARD), 1,
                "Normalizing an older report must replace the existing navigation guard, not append a second one.");
        Assert.assertTrue(
                normalized.contains("activate(visibleView());")
                        && normalized.contains("setTimeout(function(){activate(visibleView());},0);"),
                "Older Extent reports with an existing guard must be upgraded to highlight the selected tab on load."
        );
        Assert.assertFalse(normalized.contains("return view;"),
                "The stale navigation guard body must be removed.");
    }

    @Test
    public void postProcessorRenamesNativeSparkScoreCardsToAtfMeaning() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(nativeScoreCardsSparkHtml());

        Assert.assertTrue(
                normalized.contains("<h6 class=\"card-title\">Test cases</h6>")
                        && normalized.contains("test cases passed"),
                "Native Spark score cards must be relabeled to the ATF Test cases meaning."
        );
        Assert.assertFalse(normalized.contains("<h6 class=\"card-title\">Test classes</h6>"),
                "Overview score cards must not use class-distribution wording.");
    }

    @Test
    public void postProcessorAddsDurationScoreCardToEveryTimedOverview() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(timedOverviewSparkHtml());

        Assert.assertTrue(
                normalized.contains("<p class=\"m-b-0\">Duration</p><h3>1.000 s</h3>"),
                "Every timed Extent Overview must expose Duration as a top score-card."
        );
        Assert.assertEquals(count(normalized, "atf-aggregate-top-card"), 5,
                "Timed Overview reports must use the five-card standard top row.");
        Assert.assertTrue(
                normalized.indexOf("Duration") < normalized.indexOf("Test cases"),
                "Duration belongs in the top score-card row before the Test cases chart."
        );
    }

    @Test
    public void postProcessorKeepsRuntimeTestCasesCardAuditableWhenCardAlreadyLooksStandard() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize(timedOverviewSparkHtml());

        Assert.assertTrue(
                normalized.contains("class=\"card atf-runtime-dashboard-tests-card\"")
                        && normalized.contains(ExtentHtmlStructure.dataAttributeEquals(
                                ExtentDataAttributes.TEST_CASES_TOTAL,
                                "true"
                        ) + ">1</h3>")
                        && normalized.contains("<p>total test cases</p>"),
                "Runtime/aggregate Overview can already use the standard ATF card class; "
                        + "post-processing must still publish an explicit auditable Test cases total."
        );
    }

    @Test
    public void htmlWithoutDashboardStillGetsMachineReadableModeOnly() {
        String normalized = ExtentSparkHtmlPostProcessor.normalize("<html><body class=\"spa -report\">"
                + "<div class=\"test-view\">test body</div></body></html>");

        Assert.assertTrue(normalized.contains(ExtentMarkers.REPORT_MODE),
                "All Extent-like HTML should receive a report mode marker.");
        Assert.assertFalse(normalized.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK),
                "Fallback card requires a Spark dashboard container.");
    }

    private static String componentSparkHtml() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\"><div class=\"card\"><div class=\"card-body\">Native Spark summary</div></div></div>"
                + "<script>var statusGroup = {parentCount: 1};</script>"
                + "</div></body></html>";
    }

    private static String timedOverviewSparkHtml() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\">"
                + "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0\">Started</p><h3>Jun 30, 2026 5:00:00 PM</h3>"
                + "</div></div></div>"
                + "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0\">Ended</p><h3>Jun 30, 2026 5:00:01 PM</h3>"
                + "</div></div></div>"
                + "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0 text-pass\">" + ExtentDashboardLabels.TESTS_PASSED + "</p><h3>1</h3>"
                + "</div></div></div>"
                + "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0 text-fail\">" + ExtentDashboardLabels.TESTS_FAILED + "</p><h3>0</h3>"
                + "</div></div></div>"
                + "</div>"
                + "<div class=\"row atf-runtime-dashboard-tests-row\"><div class=\"col-md-4 col-lg-4\">"
                + "<div class=\"card atf-runtime-dashboard-tests-card\">"
                + "<div class=\"card-header\"><h6 class=\"card-title\">Test cases</h6></div>"
                + "<div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                + "<div class=\"card-footer\"><small><b>1</b> test cases passed</small></div>"
                + "</div></div></div>"
                + "</div></body></html>";
    }

    private static String nativeScoreCardsSparkHtml() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\">"
                + "<div class=\"card\"><div class=\"card-header\"><h6 class=\"card-title\">Tests</h6></div>"
                + "<div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                + "<div class=\"card-footer\"><small><b>2</b> tests passed</small>"
                + "<small><b>0</b> tests failed</small></div></div>"
                + "</div></div></body></html>";
    }

    private static String runtimeAuditBlock() {
        return runtimeAuditShell("<section class=\"atf-runtime-tags\"><h3>"
                + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL
                + "</h3>"
                + "<table " + ExtentRuntimeOverviewTables.runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_CLASSES)
                + "></table>"
                + "<table " + ExtentRuntimeOverviewTables.runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_PACKAGES)
                + "></table>"
                + "<table " + ExtentRuntimeOverviewTables.runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_ANNOTATIONS)
                + "></table>"
                + "</section>");
    }

    private static void assertOverviewAuditShape(
            String html,
            String label,
            String expectedMode,
            int expectedComponentLabels,
            int expectedRuntimeLabels,
            int expectedComponentTables,
            int expectedRuntimeTables
    ) {
        Assert.assertTrue(html.contains(ExtentRuntimeOverviewTables.attributeEquals(
                        ExtentDataAttributes.REPORT_MODE,
                        expectedMode
                )),
                label + " must publish mode " + expectedMode + ".");
        Assert.assertEquals(count(html, ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL), expectedComponentLabels,
                label + " must expose the expected component audit label count.");
        Assert.assertEquals(count(html, ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL), expectedRuntimeLabels,
                label + " must expose the expected runtime audit label count.");
        Assert.assertEquals(count(html, ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()), expectedComponentTables,
                label + " must expose exactly the expected component audit table count.");
        Assert.assertEquals(count(html, ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()), expectedRuntimeTables,
                label + " must expose exactly the expected runtime audit table count.");
        Assert.assertFalse(html.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK),
                label + " must never fall back to the legacy blank component card.");
    }

    private static int count(String text, String needle) {
        int matches = 0;
        int next = text.indexOf(needle);
        while (next >= 0) {
            matches++;
            next = text.indexOf(needle, next + needle.length());
        }
        return matches;
    }

    private static String modeAttribute(ExtentReportModeType mode) {
        return ExtentRuntimeOverviewTables.attributeEquals(ExtentDataAttributes.REPORT_MODE, mode.value());
    }

    private static String modeJson(ExtentReportModeType mode) {
        return "\"mode\":\"" + mode.value() + "\"";
    }

    private static String runtimeAuditJson(boolean runtimeAudit) {
        return "\"runtimeAudit\":" + runtimeAudit;
    }

    private static String runtimeAuditShell(String body) {
        return ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_START)
                + body
                + ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_END);
    }
}
