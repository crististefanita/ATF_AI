package com.ai.atf.core.reporting.adapters.assertions;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import org.testng.Assert;

import java.util.List;

public final class ExtentSparkCanonicalShellAssertions {
    private static final String TEST_CASES_CARD = ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES);
    private static final String TEST_CLASSES_CARD = ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CLASSES);
    private static final String SPARK_TESTS_CARD = ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TESTS);
    private static final String SPARK_BLANK_CARD = ExtentHtmlStructure.BLANK_CARD_TITLE;
    private static final String SPARK_NATIVE_TAGS_CARD = ExtentHtmlStructure.NATIVE_TAGS_CARD_HEADER;
    private static final String RUNTIME_TAG_TABLE_ATTRIBUTE =
            ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE);
    private static final String COMPONENT_TAG_TABLE_ATTRIBUTE =
            ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE);

    private ExtentSparkCanonicalShellAssertions() {
    }

    public static void assertComponentAdapterShell(String label, String html) {
        assertPublishedOverview(label, html, ExtentReportKind.INTERNAL_COMPONENT_FIXTURE);
        assertCommonShell(label, html);
        assertReportMode(label, html, ExtentReportModeType.COMPONENT_ADAPTER, false);
        Assert.assertTrue(html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                label + " must publish the component Overview tag audit.");
        Assert.assertEquals(count(html, COMPONENT_TAG_TABLE_ATTRIBUTE), 3,
                label + " must publish exactly three component Overview tag tables.");
        Assert.assertEquals(count(html, RUNTIME_TAG_TABLE_ATTRIBUTE), 0,
                label + " must not masquerade as a runtime-audited report.");
        Assert.assertFalse(html.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK),
                label + " must not render the legacy component fallback card.");
        Assert.assertFalse(html.contains("Runtime audit ledger is not available"),
                label + " must not explain missing runtime audit as visible Overview copy.");
        assertTestCaseScoreCard(label, html);
        Assert.assertFalse(html.contains(TEST_CLASSES_CARD),
                label + " must not use Test classes in score cards; class counts belong in audit tables.");
    }

    public static void assertRuntimeAuditedShell(String html) {
        assertPublishedOverview("runtime-audited report", html, ExtentReportKind.RUNTIME_VALIDATION);
        assertCommonShell("runtime-audited report", html);
        assertReportMode("runtime-audited report", html, ExtentReportModeType.RUNTIME_AUDITED, true);
        Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Runtime-audited reports must publish the runtime Overview tag audit.");
        Assert.assertEquals(count(html, RUNTIME_TAG_TABLE_ATTRIBUTE), 3,
                "Runtime-audited reports must publish exactly three runtime Overview tag tables.");
        Assert.assertEquals(count(html, COMPONENT_TAG_TABLE_ATTRIBUTE), 0,
                "Runtime-audited reports must not keep component-adapter tag tables.");
        assertTestCaseScoreCard("runtime-audited report", html);
        Assert.assertFalse(html.contains(SPARK_TESTS_CARD),
                "Runtime-audited reports must not expose Spark's ambiguous Tests score-card label.");
    }

    private static void assertPublishedOverview(String label, String html, ExtentReportKind kind) {
        List<String> violations = ExtentPublishedOverviewInspector.inspect(html).violations(kind);
        Assert.assertTrue(violations.isEmpty(),
                label + " must satisfy the strict published Extent Overview contract: " + violations);
    }

    private static void assertCommonShell(String label, String html) {
        Assert.assertTrue(html.contains(ExtentDomIds.DASHBOARD_VIEW),
                label + " must keep the Overview/Dashboard view.");
        Assert.assertTrue(html.contains(ExtentDashboardLabels.TESTS_PASSED)
                        && html.contains(ExtentDashboardLabels.TESTS_FAILED),
                label + " must keep the standard top-level pass/fail score cards.");
        Assert.assertTrue(html.contains("id=\"" + ExtentMarkers.REPORT_MODE + "\""),
                label + " must publish the machine-readable ATF report-mode marker.");
        Assert.assertTrue(html.contains(ExtentMarkers.NAVIGATION_GUARD)
                        && html.contains("window.atfShowExtentView=show;")
                        && html.contains("activate(visibleView());"),
                label + " must include the shared navigation guard and selected-tab behavior.");
        Assert.assertTrue(html.contains(ExtentHtmlStructure.COLLAPSE_FUNCTION)
                        && html.contains("extent-atf-collapsed")
                        && html.contains("extent-atf-collapse-ready"),
                label + " must include the shared collapse runtime.");
        Assert.assertTrue(html.contains(ExtentHtmlStructure.idAttribute(ExtentDomIds.STATUS_TOGGLE))
                        && html.contains("ensureStatusItem('pass','Pass','success'")
                        && html.contains("ensureStatusItem('fail','Fail','danger'")
                        && html.contains("ensureStatusItem('skip','Skip','warning'")
                        && html.contains("normalizeClearItem();"),
                label + " must include the standard Pass/Fail/Skip/Clear status-menu normalizer.");
        Assert.assertFalse(html.contains(SPARK_NATIVE_TAGS_CARD),
                label + " must replace Spark's native mixed Tags card with ATF tag tables.");
        Assert.assertFalse(html.contains(SPARK_BLANK_CARD),
                label + " must not expose Spark's blank grandchild score card.");
    }

    private static void assertReportMode(
            String label,
            String html,
            ExtentReportModeType mode,
            boolean runtimeAudit
    ) {
        Assert.assertTrue(
                html.contains(ExtentDataAttributes.REPORT_MODE + "=\"" + mode.value() + "\"")
                        && html.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"" + runtimeAudit + "\"")
                        && html.contains("\"mode\":\"" + mode.value() + "\""),
                label + " must be explicitly classified as " + mode.value() + "."
        );
    }

    private static void assertTestCaseScoreCard(String label, String html) {
        Assert.assertTrue(html.contains(TEST_CASES_CARD)
                        && html.contains(ExtentDashboardLabels.TESTS_PASSED)
                        && html.contains(ExtentDashboardLabels.TESTS_FAILED),
                label + " must keep Test cases as the canonical score-card unit.");
    }

    private static int count(String text, String needle) {
        int matches = 0;
        int index = text.indexOf(needle);
        while (index >= 0) {
            matches++;
            index = text.indexOf(needle, index + needle.length());
        }
        return matches;
    }
}
