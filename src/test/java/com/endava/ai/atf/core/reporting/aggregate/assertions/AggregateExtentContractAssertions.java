package com.endava.ai.atf.core.reporting.aggregate.assertions;

import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentSparkReportInspector;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;

import java.util.List;
import java.util.regex.Pattern;

public final class AggregateExtentContractAssertions {
    private AggregateExtentContractAssertions() {
    }

    public static void assertRuntimeAuditedAggregateShell(String html) {
        assertRuntimeAuditedAggregateMode(html);
        List<String> violations = ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportKind.AGGREGATE_VALIDATION);
        Assert.assertTrue(
                violations.isEmpty(),
                "Aggregate Extent reports must satisfy the shared publishable Overview contract."
                        + " Violations: " + violations
        );
    }

    public static void assertRuntimeAuditedAggregateMode(String html) {
        Assert.assertTrue(
                html.contains(ExtentDataAttributes.REPORT_MODE + "=\"runtime-audited\"")
                        && html.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"true\""),
                "Aggregate Extent reports must be published as runtime-audited, not component-adapter."
        );
        Assert.assertTrue(
                html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Aggregate Extent reports must expose runtime tag audit tables in Overview."
        );
        Assert.assertEquals(count(html, ExtentDataAttributes.RUNTIME_TAG_TABLE), 3,
                "Aggregate Extent reports must expose the three runtime tag audit tables.");
        Assert.assertEquals(count(html, ExtentDataAttributes.COMPONENT_TAG_TABLE), 0,
                "Aggregate Extent reports must not fall back to component tag audit tables.");
        Assert.assertFalse(
                html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                "Aggregate Extent reports must not publish the internal component-adapter Overview block."
        );
    }

    public static void assertRuntimeOverviewTotals(String html, int expectedTotal) {
        ExtentSparkReportInspector inspector = ExtentSparkReportInspector.inspect(html);
        Assert.assertTrue(
                inspector.dashboardTestsCard().contains("data-atf-test-cases-total=\"true\">"
                        + expectedTotal
                        + "</h3>"),
                "Aggregate Overview must publish an explicit Test cases total."
        );
        Assert.assertEquals(
                inspector.dashboardSummaryCount(ExtentDashboardLabels.TEST_CASES),
                String.valueOf(expectedTotal),
                "Aggregate Overview Test cases card must use the runtime test-case total."
        );
        Assert.assertEquals(
                inspector.runtimeTagTableCountSum(ExtentDataAttributes.TAG_TABLE_CLASSES),
                expectedTotal,
                "Aggregate class tag table must sum to the Test cases total."
        );
        Assert.assertEquals(
                inspector.runtimeTagTableCountSum(ExtentDataAttributes.TAG_TABLE_PACKAGES),
                expectedTotal,
                "Aggregate package tag table must sum to the Test cases total."
        );
    }

    public static boolean containsAcrossMarkup(String html, String left, String right) {
        return Pattern.compile(Pattern.quote(left) + ".{0,240}" + Pattern.quote(right), Pattern.DOTALL)
                .matcher(html)
                .find();
    }

    private static int count(String html, String token) {
        int matches = 0;
        int index = html.indexOf(token);
        while (index >= 0) {
            matches++;
            index = html.indexOf(token, index + token.length());
        }
        return matches;
    }
}
