package com.endava.ai.atf.core.reporting.aggregate;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import com.endava.ai.core.reporting.aggregate.model.AggregateSummary;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.endava.ai.atf.core.reporting.aggregate.assertions.AggregateExtentContractAssertions.assertRuntimeOverviewTotals;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.AGGREGATE_EXTENT_TEST_DISPLAY_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.AGGREGATE_EXTENT_TEST_DISPLAY_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class AggregateExtentTestDisplayContractTest {
    private static final String DISPLAY_CONTRACT_REPORT_FILE = "AggregateExtentReport.html";

    @Test
    public void aggregate_test_cases_use_collapsible_runtime_style_sections() throws Exception {
        Path directory = Files.createTempDirectory("aggregate-display-contract-");
        Path report = directory.resolve(DISPLAY_CONTRACT_REPORT_FILE);
        try {
            AggregateExtentReportWriter writer = new AggregateExtentReportWriter();
            writer.write(report, AtfCoreAggregateEngine.EXTENT.configValue(), new AggregateSummary(2, 2, 0, 0), List.of(
                    caseRecord(aggregateFixture("AggregateDisplayFixtureA"), 1_000L, 1_500L),
                    caseRecord(aggregateFixture("AggregateDisplayFixtureB"), 1_100L, 3_100L)
            ));

            String html = Files.readString(report);

            Assert.assertTrue(html.contains("Execution metadata"),
                    "Aggregate case details must expose a collapsible metadata section.");
            Assert.assertTrue(html.contains("Contract proof"),
                    "Aggregate case details must expose a collapsible contract-proof section.");
            Assert.assertTrue(html.contains("Status evidence"),
                    "Aggregate case details must expose a collapsible status-evidence section.");
            Assert.assertTrue(html.contains(ExtentHtmlStructure.COLLAPSE_FUNCTION)
                            && html.contains("extent-atf-collapsed"),
                    "Aggregate reports must reuse the same collapse behavior as non-aggregate Extent reports.");
            Assert.assertTrue(html.contains("aggregate_display_contract [AggregateDisplayFixtureA]")
                            && html.contains("aggregate_display_contract [AggregateDisplayFixtureB]"),
                    "Duplicate aggregate leaf titles must include a class discriminator for visual drill-down.");
            Assert.assertFalse(html.contains("<span>aggregate_display_contract</span>"),
                    "Duplicate aggregate leaf titles must not remain visually ambiguous.");
            Assert.assertTrue(html.contains(ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES)),
                    "Aggregate Overview must keep Test cases as the canonical unit.");
            assertRuntimeOverviewTotals(html, 2);
            String topScoreCardRow = dashboardTopScoreCardRow(html);
            Assert.assertEquals(count(topScoreCardRow, "<div class=\"col-md atf-aggregate-top-card\">"), 5,
                    "Aggregate Overview must keep Started, Ended, Duration, Tests Passed, and Tests Failed on one score-card row.");
            Assert.assertTrue(html.contains("<canvas id='parent-analysis' width='115' height='90'></canvas>"),
                    "Aggregate Overview must keep the standard Test cases chart placeholder after the top score-cards.");
            Assert.assertNotEquals(
                    dashboardCardValue(html, ExtentDashboardLabels.STARTED),
                    dashboardCardValue(html, ExtentDashboardLabels.ENDED),
                    "Aggregate Overview Started/Ended must be reconciled from case timings, not report-generation time."
            );
            Assert.assertEquals(
                    dashboardCardValue(html, ExtentDashboardLabels.DURATION),
                    "2.100 s",
                    "Aggregate Overview must expose the Started/Ended wall-clock difference as a score-card."
            );
            Assert.assertEquals(count(html, "<p class=\"m-b-0\">" + ExtentDashboardLabels.DURATION + "</p>"), 1,
                    "Aggregate Overview must expose exactly one Duration score-card.");
            Assert.assertTrue(html.contains("<th>Wall-clock duration</th><td>2.100 s</td>"),
                    "Aggregate final audit must expose wall-clock duration even when Spark Started/Ended round to the same second.");
            Assert.assertTrue(html.contains("<th>Cumulative test duration</th><td>2.500 s</td>"),
                    "Aggregate final audit must expose cumulative test duration from leaf cases.");
            Assert.assertTrue(html.contains("<th>Slowest test</th>")
                            && html.contains("AggregateDisplayFixtureB#aggregate_display_contract")
                            && html.contains("2.000 s"),
                    "Aggregate final audit must expose the slowest leaf test for optimization.");
            Assert.assertTrue(html.contains("<th>Slowest class/group</th>")
                            && html.contains("AggregateDisplayFixtureB - 2.000 s (1 cases)"),
                    "Aggregate final audit must expose the slowest class/group for optimization.");
            Assert.assertFalse(html.contains("00:00:000") || html.contains("00:00:00:000"),
                    "Aggregate Extent reports must set explicit timings on overview, class, case and child-section nodes; "
                            + "Spark 0 ms placeholders hide timing regressions.");
            Assert.assertFalse(html.contains("Duration ms: 0"),
                    "Aggregate execution metadata must publish real case durations, not zero placeholders.");
            Assert.assertTrue(ExtentPublishedOverviewInspector.inspect(html)
                            .violations(ExtentReportKind.AGGREGATE_VALIDATION)
                            .isEmpty(),
                    "Aggregate display changes must not break the published Extent Overview contract.");
        } finally {
            Files.deleteIfExists(report);
            Files.deleteIfExists(directory);
        }
    }

    private static String dashboardCardValue(String html, String label) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?s)<p class=\"m-b-0\">" + java.util.regex.Pattern.quote(label) + "</p>\\s*<h3[^>]*>(.*?)</h3>")
                .matcher(html);
        Assert.assertTrue(matcher.find(), "Missing aggregate dashboard card: " + label);
        return matcher.group(1);
    }

    private static String dashboardTopScoreCardRow(String html) {
        int dashboardStart = html.indexOf("<div class=\"container-fluid p-4 view dashboard-view\">");
        Assert.assertTrue(dashboardStart >= 0, "Aggregate report must expose a Spark dashboard view.");
        int topRowStart = html.indexOf("<div class=\"row\">", dashboardStart);
        Assert.assertTrue(topRowStart >= 0, "Aggregate dashboard must expose a top score-card row.");
        int testCasesRowStart = html.indexOf("<div class=\"row atf-runtime-dashboard-tests-row\">", topRowStart);
        Assert.assertTrue(testCasesRowStart > topRowStart,
                "Aggregate dashboard top score-card row must precede the Test cases row.");
        return html.substring(topRowStart, testCasesRowStart);
    }

    private static int count(String text, String token) {
        int matches = 0;
        int index = text.indexOf(token);
        while (index >= 0) {
            matches++;
            index = text.indexOf(token, index + token.length());
        }
        return matches;
    }

    private static AggregateExtentReportWriter.CaseRecord caseRecord(String className, long startMillis, long endMillis) {
        return new AggregateExtentReportWriter.CaseRecord() {
            @Override public String suiteName() { return "ATF Core Package Sweep"; }
            @Override public String contextName() { return AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE; }
            @Override public String className() { return className; }
            @Override public String methodName() { return "aggregate_display_contract"; }
            @Override public String parameters() { return ""; }
            @Override public String suiteProfile() { return "EXTENT_CONTRACT"; }
            @Override public String capability() { return "Aggregate Extent test display"; }
            @Override public String purpose() { return "Validate aggregate drill-down structure."; }
            @Override public String area() { return "REPORTING_ADAPTER"; }
            @Override public String owner() { return "AggregateExtentReportWriter"; }
            @Override public String expected() { return "Aggregate details are grouped under child nodes."; }
            @Override public String observed() { return "Generated report contains standard display sections."; }
            @Override public String repairHint() { return "Keep metadata/proof/status sections as child nodes."; }
            @Override public String statusName() { return "PASSED"; }
            @Override public AggregateStatus status() { return AggregateStatus.PASSED; }
            @Override public long startMillis() { return startMillis; }
            @Override public long endMillis() { return endMillis; }
            @Override public String stackTrace() { return ""; }
            @Override public String displayName() { return "aggregate_display_contract"; }
            @Override public String description() { return "Aggregate display contract"; }
            @Override public long durationMillis() { return Math.max(0L, endMillis - startMillis); }
        };
    }

    private static String aggregateFixture(String simpleName) {
        return AggregateExtentTestDisplayContractTest.class.getPackageName() + "." + simpleName;
    }
}
