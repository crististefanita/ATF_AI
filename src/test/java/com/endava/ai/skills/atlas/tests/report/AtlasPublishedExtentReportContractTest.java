package com.endava.ai.skills.atlas.tests.report;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentStatusGroupSnapshot;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.VALIDATION;
import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.declaredValidationReportsEndingWith;
import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.publishedAggregateAtlasExtentReports;
import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.publishedAtlasValidatorExtentReports;
import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.publishedUiAgenticRuntimeExtentReports;
import static com.endava.ai.skills.atlas.tests.report.AtlasPublishedExtentReportInventory.seleniumAllSuiteEvidence;

@Listeners(TestListener.class)
public class AtlasPublishedExtentReportContractTest {
    private static final String SELENIUM_AGGREGATE_COPY_SUFFIX = "_ui-agentic-selenium-extent-report.html";
    private static final String SELENIUM_SMOKE_COPY_SUFFIX = "_ui-agentic-selenium-runtime-extent-report.html";
    private static final String SELENIUM_ALL_SUITE_SUFFIX = "_ui-agentic-selenium-all-extent-report.html";
    private static final String UI_AGENTIC_CURRENT_SUFFIX = "_ui-agentic-current-extent-report.html";
    private static final String SELENIUM_ALL_SUITE_BLOCKER_SUFFIX =
            "_ui-agentic-selenium-all-suite-blocker" + AtlasContext.NONSTANDARD_HTML_SUFFIX;
    private static final String BOLD_COUNT = "<b>(\\d+)</b>\\s*";
    private static final Pattern TEST_CASES_PASSED = scoreLinePattern(ExtentDashboardLabels.TEST_CASE_NOUN, "passed");
    private static final Pattern TEST_CASES_FAILED = scoreLinePattern(ExtentDashboardLabels.TEST_CASE_NOUN, "failed");
    private static final Pattern TEST_CASE_SKIPS = Pattern.compile(
            "<b>\\d+</b>\\s*" + Pattern.quote(ExtentDashboardLabels.TEST_CASE_NOUN)
                    + "\\s*failed,\\s*" + BOLD_COUNT + "skipped"
    );
    private static final String STATUS_TOGGLE_DOUBLE_QUOTED = ExtentHtmlStructure.idAttribute(ExtentDomIds.STATUS_TOGGLE);
    private static final String STATUS_TOGGLE_SINGLE_QUOTED = "id='" + ExtentDomIds.STATUS_TOGGLE + "'";

    @Test
    public void publishedAtlasExtentReportsUseStandardOverviewAndStatusModel() throws IOException {
        List<Path> reports = publishedAtlasValidatorExtentReports();
        if (reports.isEmpty()) {
            throw new SkipException("No published Atlas Extent validation reports found under " + VALIDATION);
        }

        for (Path report : reports) {
            String html = Files.readString(report);
            boolean runtimeMode = html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL);
            boolean componentMode = html.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL);
            List<String> overviewViolations = ExtentPublishedOverviewInspector.inspect(html)
                    .violations(ExtentReportKind.ATLAS_VALIDATION);

            Assert.assertTrue(overviewViolations.isEmpty(),
                    "Atlas Extent report must satisfy the strict published Overview contract: "
                            + report + " -> " + overviewViolations);

            Assert.assertTrue(
                    html.contains(ExtentMarkers.REPORT_MODE)
                            && html.contains(ExtentMarkers.NAVIGATION_GUARD)
                            && html.contains(ExtentHtmlStructure.COLLAPSE_FUNCTION),
                    "Atlas Extent report must use the standard ATF shell: " + report
            );
            Assert.assertTrue(runtimeMode,
                    "Atlas published Extent reports must use the standard runtime-audited shell: " + report);
            Assert.assertFalse(componentMode,
                    "Atlas published Extent reports must not use component-mode Extent output: " + report);
            Assert.assertEquals(count(html, runtimeMode
                            ? ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE)
                            : ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE)),
                    3,
                    "Atlas Extent Overview must expose exactly three tag tables: " + report);
            Assert.assertTrue(
                    html.contains(ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES)),
                    "Atlas Extent Overview must use Test cases score-card terminology: " + report);
            Assert.assertFalse(html.contains(ExtentHtmlStructure.BLANK_CARD_TITLE),
                    "Atlas Extent Overview must not expose Spark's blank score-card: " + report);
            Assert.assertFalse(html.toLowerCase(Locale.ROOT)
                            .contains(ExtentDashboardLabels.CONFIGURATION_METHODS.toLowerCase(Locale.ROOT)),
                    "Atlas Extent Overview must normalize TestNG configuration-method terminology out of published reports: "
                            + report);
            Assert.assertFalse(html.contains(ExtentHtmlStructure.NATIVE_TAGS_CARD_HEADER),
                    "Atlas Extent Overview must replace Spark's native mixed Tags card with ATF tag tables: " + report);
            Assert.assertTrue(
                    (html.contains(STATUS_TOGGLE_DOUBLE_QUOTED) || html.contains(STATUS_TOGGLE_SINGLE_QUOTED))
                            && html.contains(ExtentDomIds.STATUS_FILTER_LABEL_CLASS),
                    "Atlas Extent reports must keep the standard status navigation surface: " + report
            );

            Assert.assertTrue(html.contains(ExtentDomIds.CATEGORY_VIEW),
                    "Runtime Atlas reports must keep the standard category/failure navigation surface: " + report);
            Assert.assertTrue(html.contains(ExtentDashboardLabels.TESTS_FAILED),
                    "Runtime Atlas reports must keep the Tests Failed score-card: " + report);
            ExtentStatusGroupSnapshot status = ExtentStatusGroupSnapshot.fromHtml(html);
            int testCaseTotal = first(TEST_CASES_PASSED, html)
                    + first(TEST_CASES_FAILED, html)
                    + first(TEST_CASE_SKIPS, html);
            Assert.assertEquals(status.parentCount(), testCaseTotal,
                    "Runtime Atlas statusGroup parentCount must match Test cases score-card: " + report);
            Assert.assertEquals(status.parentStatusTotal(), testCaseTotal,
                    "Runtime Atlas statusGroup parent statuses must add up to Test cases score-card: "
                            + report);
            Assert.assertTrue(status.hasZeroChildAndEventTotals(),
                    "Runtime Atlas statusGroup must keep secondary levels zeroed after runtime reconciliation: " + report);
            Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                    "Runtime Atlas reports must expose the standard failure-groups surface, even when no failures exist: "
                            + report);
        }
    }

    @Test
    public void uiAgenticSeleniumReportsAreDirectRuntimeReportsNotAggregateCopies() throws IOException {
        for (Path report : publishedUiAgenticRuntimeExtentReports()) {
            String fileName = report.getFileName().toString();
            Assert.assertFalse(fileName.endsWith(SELENIUM_AGGREGATE_COPY_SUFFIX),
                    "Do not publish aggregate/inventory copies as Selenium Extent reports. Selenium evidence must "
                            + "come from direct BaseTestUI/TestListener runtime execution: " + report);
            Assert.assertFalse(fileName.endsWith(SELENIUM_SMOKE_COPY_SUFFIX),
                    "Do not publish smoke-only Selenium reports as Atlas section 6 evidence. Atlas needs the "
                            + "all-suite Selenium report or an explicit blocker: " + report);
        }
    }

    @Test
    public void uiAgenticSeleniumAllSuiteReportsArePublishedForAtlasAudit() throws IOException {
        if (publishedUiAgenticRuntimeExtentReports().isEmpty()) {
            throw new SkipException("No UI Agentic runtime Extent reports are declared by Atlas HTML documents.");
        }
        for (AtlasPublishedExtentReportInventory.SeleniumRuntimeEvidence evidence : seleniumAllSuiteEvidence()) {
            Path report = evidence.report;
            String artifactBase = report.getFileName().toString().replace(AtlasContext.HTML_SUFFIX, "");
            Path readme = report.resolveSibling(artifactBase + AtlasContext.README_SUFFIX);
            Path command = report.resolveSibling(artifactBase + AtlasContext.COMMAND_SUFFIX);
            Path suite = report.resolveSibling(artifactBase + AtlasContext.TESTNG_XML_SUFFIX);
            Path log = report.resolveSibling(artifactBase + AtlasContext.MAVEN_LOG_SUFFIX);

            Assert.assertTrue(Files.isRegularFile(report),
                    "Missing all-suite Selenium Extent report: " + report);
            Assert.assertTrue(Files.isRegularFile(readme), "Missing all-suite Selenium README: " + readme);
            Assert.assertTrue(Files.isRegularFile(command), "Missing all-suite Selenium command evidence: " + command);
            Assert.assertTrue(Files.isRegularFile(suite), "Missing all-suite Selenium TestNG suite evidence: " + suite);
            Assert.assertTrue(Files.isRegularFile(log), "Missing all-suite Selenium Maven log evidence: " + log);

            String html = Files.readString(report);
            String readmeText = Files.readString(readme);
            Assert.assertTrue(html.contains(evidence.expectedTestClass),
                    "All-suite Selenium report must expose a real UI test class in the Tests section: " + report);
            Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                    "All-suite Selenium report must keep the runtime tag audit overview: " + report);
            Assert.assertTrue(html.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL),
                    "All-suite Selenium report must keep the runtime failure-groups overview: " + report);
            Assert.assertTrue(readmeText.contains("- Status: " + evidence.expectedStatus),
                    "All-suite Selenium README must publish the observed runtime status: " + readme);
            Assert.assertTrue(readmeText.contains("- Passed: " + evidence.expectedPassed)
                            && readmeText.contains("- Failed: " + evidence.expectedFailed)
                            && readmeText.contains("- Skipped: " + evidence.expectedSkipped),
                    "All-suite Selenium README must publish pass/fail/skip counts: " + readme);
        }
    }

    @Test
    public void seleniumAllSuiteBlockerIsDocumentedWhenPublishedInsteadOfStandardReport() throws IOException {
        List<Path> blockers = declaredValidationReportsEndingWith(SELENIUM_ALL_SUITE_BLOCKER_SUFFIX);
        if (blockers.isEmpty()) {
            return;
        }
        Path nonstandard = blockers.get(0);
        String artifactBase = nonstandard.getFileName().toString().replace(AtlasContext.NONSTANDARD_HTML_SUFFIX, "");
        Path readme = nonstandard.resolveSibling(artifactBase + AtlasContext.README_SUFFIX);
        Path log = nonstandard.resolveSibling(artifactBase + AtlasContext.MAVEN_LOG_SUFFIX);

        Assert.assertTrue(Files.isRegularFile(readme), "Missing Selenium all-suite blocker README: " + readme);
        Assert.assertTrue(Files.isRegularFile(nonstandard),
                "Missing nonstandard partial HTML evidence: " + nonstandard);
        Assert.assertTrue(Files.isRegularFile(log), "Missing blocker Maven log: " + log);

        String readmeText = Files.readString(readme);
        String html = Files.readString(nonstandard);
        Assert.assertTrue(readmeText.contains("NonBlockingDiagnostic")
                        && readmeText.contains("ReportingEngineCleanup"),
                "Blocker README must name the missing reporting/core classes: " + readme);
        Assert.assertFalse(html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Nonstandard blocker HTML must not be treated as a valid runtime-audited report: "
                        + nonstandard);
    }

    @Test
    public void uiAgenticPublishedReportsDoNotExposeSyntheticAggregateOverviewAsATest() throws IOException {
        for (Path report : publishedUiAgenticRuntimeExtentReports()) {
            String html = Files.readString(report);
            Assert.assertFalse(html.contains("UI Agentic Current Inventory"),
                    "UI Agentic reports must not publish a synthetic aggregate overview item in the Tests section: "
                            + report);
            Assert.assertFalse(html.contains("Aggregate suite status is green."),
                    "UI Agentic reports must keep Tests section focused on real test cases, not aggregate summary nodes: "
                            + report);
            Assert.assertFalse(html.contains("Why this exists:"),
                    "UI Agentic reports must not render aggregate descriptor prose as a Tests item: " + report);
            Assert.assertFalse(html.contains("Audit shape:"),
                    "UI Agentic reports must not render aggregate descriptor prose as a Tests item: " + report);
        }
    }

    @Test
    public void uiAgenticCurrentInventoryReportsExposeNonZeroVisibleDurations() throws IOException {
        for (Path report : declaredValidationReportsEndingWith(UI_AGENTIC_CURRENT_SUFFIX)) {
            if (!isCurrentFrameworkInventory(report)) {
                continue;
            }
            String html = Files.readString(report);
            Assert.assertFalse(html.contains("00:00:000") || html.contains("00:00:00:000"),
                    "UI Agentic inventory reports are synthetic aggregate reports, but each visible Extent node "
                            + "must still expose deterministic non-zero timing instead of Spark 0 ms placeholders: "
                            + report);
            Assert.assertFalse(html.contains("Duration ms: 0"),
                    "UI Agentic inventory execution metadata must not publish zero-duration cases: " + report);
            Assert.assertFalse(html.contains("<p class=\"m-b-0\">Duration</p><h3>0 ms</h3>"),
                    "UI Agentic inventory Overview Duration score-card must be reconciled from case timings, "
                            + "not Spark second-rounded Started/Ended values: " + report);
            Assert.assertFalse(html.contains("AdminOrderStatusRun05ReopenTests.fulfillment_admin_edit_page_owns_invoice_identity"),
                    "UI Agentic inventory report must not concatenate Java class and @Test method into the class "
                            + "dimension; the Tests list should stay method-first and class-disambiguated: " + report);
            Assert.assertTrue(html.contains("fulfillment_admin_edit_page_owns_invoice_identity [AdminOrderStatusRun05ReopenTests]"),
                    "UI Agentic inventory report should display duplicate method names with a compact class "
                            + "discriminator, not the full class/method chain in the list: " + report);
            Assert.assertFalse(html.contains("<p class=\"m-b-0\">Duration</p><h3>1.500 s</h3>"),
                    "UI Agentic inventory report duration must not use the old under-calibrated 25 ms/case timing: "
                            + report);
        }
    }

    private static boolean isCurrentFrameworkInventory(Path report) {
        String normalized = report.toString().replace('\\', '/');
        return normalized.contains("/ATF6/") || normalized.contains("/framework-under-test/");
    }

    @Test
    public void aggregateAtlasExtentReportsAreExplicitlySeparatedFromNormalPublishedReports() throws IOException {
        for (Path report : publishedAggregateAtlasExtentReports()) {
            String html = Files.readString(report);
            List<String> overviewViolations = ExtentPublishedOverviewInspector.inspect(html)
                    .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION);

            Assert.assertTrue(overviewViolations.isEmpty(),
                    "Aggregate Atlas reports still need the shared runtime-audited shell: "
                            + report + " -> " + overviewViolations);
            Assert.assertTrue(report.getFileName().toString().contains("aggregate"),
                    "Aggregate Atlas reports must be named as aggregate artifacts: " + report);
        }
    }

    private static int first(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static Pattern scoreLinePattern(String noun, String status) {
        return Pattern.compile(BOLD_COUNT + Pattern.quote(noun) + "\\s*" + Pattern.quote(status));
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
