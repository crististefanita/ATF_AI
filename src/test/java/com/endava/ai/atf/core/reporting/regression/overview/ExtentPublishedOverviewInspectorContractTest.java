package com.endava.ai.atf.core.reporting.regression.overview;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.atf.core.reporting.support.AtfHtmlExExtentCorpus;
import com.endava.ai.atf.core.reporting.support.AtfHtmlExExtentCorpus.HtmlExExtentReport;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportSectionIds;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_CLASS)
@Listeners(TestListener.class)
public class ExtentPublishedOverviewInspectorContractTest {
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_REJECTS_NATIVE_DRIFT)
    @Test
    public void inspectorRejectsUnstandardizedSparkOverviewVocabulary() {
        String html = "<html><body>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CLASSES)
                + ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.CONFIGURATION_METHODS)
                + ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TESTNG_INVOCATIONS)
                + ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.STEPS)
                + ExtentHtmlStructure.BLANK_CARD_TITLE
                + ExtentHtmlStructure.NATIVE_TAGS_CARD_HEADER
                + "</div></body></html>";

        List<String> violations = ExtentPublishedOverviewInspector.inspect(html).violations();

        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("Test classes score-card")),
                "Strict Overview inspection must reject class-count score-cards: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("Configuration methods")),
                "Strict Overview inspection must reject TestNG configuration-method vocabulary: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("TestNG invocations")),
                "Strict Overview inspection must reject TestNG invocation score-cards: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("Steps")),
                "Strict Overview inspection must reject step-count score-cards: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("blank Spark score-card")),
                "Strict Overview inspection must reject blank Spark score-cards: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("native Spark mixed Tags")),
                "Strict Overview inspection must reject Spark's native mixed Tags card: " + violations);
        Assert.assertTrue(violations.stream().anyMatch(message -> message.contains("Duration")),
                "Strict Overview inspection must reject reports without a Duration score-card: " + violations);
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_RUNTIME_AUDIT)
    @Test
    public void currentHtmlExExtentCorpusSatisfiesPublishedOverviewPolicy() throws Exception {
        List<HtmlExExtentReport> corpus = AtfHtmlExExtentCorpus.currentStandardReports();
        Assert.assertFalse(corpus.isEmpty(),
                "docs/html-ex must contain at least one dynamically discovered current-standard Extent report.");

        for (HtmlExExtentReport report : corpus) {
            List<String> violations = ExtentPublishedOverviewInspector.inspect(report.html())
                    .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION);

            Assert.assertTrue(violations.isEmpty(),
                    "Current-standard html-ex Extent report must satisfy the ATF published Overview contract. "
                            + report.path() + " -> " + violations);
        }
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_COMPONENT_POLICY)
    @Test
    public void componentAdapterReportsAreInternalOnlyForPublishedPolicy() {
        String html = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());
        List<String> internalViolations = ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE);

        Assert.assertTrue(
                internalViolations.isEmpty(),
                "Component-adapter shell remains valid only for internal adapter/component fixtures: "
                        + internalViolations
        );

        List<String> publishedViolations = ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION);

        Assert.assertTrue(
                publishedViolations.stream().anyMatch(message -> message.contains("must be runtime-audited")),
                "Published validation reports must reject component-adapter mode: " + publishedViolations
        );
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_COMPONENT_POLICY)
    @Test
    public void targetReportsMustNotPublishComponentAdapterOverviewShape() {
        String html = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());

        Assert.assertTrue(
                html.contains(ExtentDataAttributes.REPORT_MODE
                        + "=\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\""),
                "Fixture must model the stale target/reports shape that was finalized as component-adapter."
        );
        Assert.assertEquals(
                count(html, ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE)),
                3,
                "Stale target/reports component-adapter shape exposes component tag tables."
        );
        Assert.assertTrue(
                html.contains(ExtentDashboardLabels.TEST_CLASSES),
                "Stale component adapter reports expose class-tag audit vocabulary."
        );

        List<String> publishedViolations = ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportKind.RUNTIME_VALIDATION);

        Assert.assertTrue(
                publishedViolations.stream().anyMatch(message -> message.contains("must be runtime-audited")),
                "Runtime validation must reject reports that never received runtime audit: " + publishedViolations
        );
        Assert.assertTrue(
                publishedViolations.stream().anyMatch(message -> message.contains("component-adapter tag tables")),
                "Runtime validation must call out component tag tables as internal-only evidence: "
                        + publishedViolations
        );
        Assert.assertTrue(
                publishedViolations.stream().anyMatch(message -> message.contains("TestListener/runtime audit")),
                "Failure must tell agents to regenerate through the runtime reporting path: "
                        + publishedViolations
        );
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_RUNTIME_AUDIT)
    @Test
    public void runtimeAuditedReportsSatisfyPublishedPolicy() {
        String html = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml()
                .replace("</div></body>", runtimeAuditBlock() + "</div></body>"));
        List<String> publishedViolations = ExtentPublishedOverviewInspector.inspect(html)
                .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION);

        Assert.assertTrue(
                publishedViolations.isEmpty(),
                "Runtime-audited reports must satisfy the published validation policy: " + publishedViolations
        );
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_RUNTIME_AUDIT)
    @Test
    public void reportKindSeparatesAggregateAuditFromNormalRuntimeReports() {
        String runtimeHtml = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml()
                .replace("</div></body>", runtimeAuditBlock() + "</div></body>"));
        String aggregateHtml = runtimeHtml.replace("</body>",
                "<section id=\"" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "\"></section></body>");

        List<String> runtimeViolations = ExtentPublishedOverviewInspector.inspect(aggregateHtml)
                .violations(ExtentReportKind.RUNTIME_VALIDATION);
        Assert.assertTrue(
                runtimeViolations.stream().anyMatch(message -> message.contains("aggregate final audit")),
                "Normal runtime reports must reject aggregate-only audit sections: " + runtimeViolations
        );

        List<String> aggregateViolations = ExtentPublishedOverviewInspector.inspect(aggregateHtml)
                .violations(ExtentReportKind.AGGREGATE_VALIDATION);
        Assert.assertTrue(
                aggregateViolations.isEmpty(),
                "Aggregate validation reports must reuse the shared shell and require aggregate audit: "
                        + aggregateViolations
        );

        List<String> missingAggregateViolations = ExtentPublishedOverviewInspector.inspect(runtimeHtml)
                .violations(ExtentReportKind.AGGREGATE_VALIDATION);
        Assert.assertTrue(
                missingAggregateViolations.stream().anyMatch(message -> message.contains("aggregate final audit")),
                "Aggregate validation reports must not silently degrade to a normal runtime report: "
                        + missingAggregateViolations
        );
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_COMPONENT_POLICY)
    @Test
    public void finalizerRejectsComponentAdapterWhenPublishingValidationArtifacts() throws Exception {
        Path report = Files.createTempFile("atf-component-report", ".html");
        Files.writeString(report, componentSparkHtml());

        try {
            ExtentSparkReportFinalizer.standardize(report, ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE);
            IllegalStateException failure = Assert.expectThrows(
                    IllegalStateException.class,
                    () -> ExtentSparkReportFinalizer.standardizePublishedValidation(report)
            );
            Assert.assertTrue(
                    failure.getMessage().contains("must be runtime-audited"),
                    "Published finalizer must explain that component-adapter is internal-only: " + failure.getMessage()
            );
        } finally {
            Files.deleteIfExists(report);
        }
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_RUNTIME_AUDIT)
    @Test
    public void finalizerAcceptsRuntimeAuditedValidationArtifacts() throws Exception {
        Path report = Files.createTempFile("atf-runtime-report", ".html");
        Files.writeString(report, componentSparkHtml()
                .replace("</div></body>", runtimeAuditBlock() + "</div></body>"));

        try {
            ExtentSparkReportFinalizer.standardizePublishedValidation(report);
            List<String> violations = ExtentPublishedOverviewInspector.inspect(Files.readString(report))
                    .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION);
            Assert.assertTrue(violations.isEmpty(),
                    "Runtime-audited validation artifact must remain publishable after finalization: " + violations);
        } finally {
            Files.deleteIfExists(report);
        }
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_PUBLISHED_OVERVIEW_INSPECTOR_RUNTIME_AUDIT)
    @Test
    public void finalizerReportKindBlocksAggregateAuditInRuntimeValidation() throws Exception {
        Path report = Files.createTempFile("atf-runtime-with-aggregate-audit", ".html");
        Files.writeString(report, componentSparkHtml()
                .replace("</div></body>", runtimeAuditBlock() + "</div></body>")
                .replace("</body>", "<section id=\""
                        + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT
                        + "\"></section></body>"));

        try {
            IllegalStateException failure = Assert.expectThrows(
                    IllegalStateException.class,
                    () -> ExtentSparkReportFinalizer.standardize(report, ExtentReportKind.RUNTIME_VALIDATION)
            );
            Assert.assertTrue(
                    failure.getMessage().contains("aggregate final audit"),
                    "Runtime validation must reject aggregate-only sections: " + failure.getMessage()
            );
        } finally {
            Files.deleteIfExists(report);
        }
    }

    private static String componentSparkHtml() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a>"
                + "<div id=\"status-toggle\" class=\"dropdown-menu\"></div>"
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
                + "<div class=\"row\"><div class=\"card\"><div class=\"card-header\"><h6 class=\"card-title\">Tests</h6></div>"
                + "<div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                + "<div class=\"card-footer\"><small><b>1</b> tests passed</small>"
                + "<small><b>0</b> tests failed, <b>0</b> skipped</small></div></div></div>"
                + "<script>var statusGroup = {parentCount: 1, passParent: 1, failParent: 0, skipParent: 0};</script>"
                + "</div></body></html>";
    }

    private static String runtimeAuditBlock() {
        return ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_START)
                + "<section class=\"atf-runtime-tags\"><h3>"
                + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL
                + "</h3>"
                + "<table " + ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE)
                + ExtentDataAttributes.TAG_TABLE_CLASSES + "\"></table>"
                + "<table " + ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE)
                + ExtentDataAttributes.TAG_TABLE_PACKAGES + "\"></table>"
                + "<table " + ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE)
                + ExtentDataAttributes.TAG_TABLE_ANNOTATIONS + "\"></table>"
                + "</section>"
                + ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_END);
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
