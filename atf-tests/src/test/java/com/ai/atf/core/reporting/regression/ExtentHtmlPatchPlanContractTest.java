package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.atf.core.reporting.adapters.audit.ExtentRuntimeOverviewTables;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditPublisher;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;
import com.ai.core.reporting.listener.internal.runtime.audit.ExtentHtmlPatchPlan;
import com.ai.core.reporting.listener.internal.runtime.audit.MarkedHtmlSectionRewriter;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_HTML_PATCH_PLAN_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_HTML_PATCH_PLAN_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ExtentHtmlPatchPlanContractTest {

    @Test
    public void markedSectionRewriterRemovesDuplicateBlocksIdempotently() {
        String html = "before"
                + "<!-- A_START -->old<!-- A_END -->"
                + "middle"
                + "<!-- A_START -->older<!-- A_END -->"
                + "after";

        String patched = MarkedHtmlSectionRewriter.removeMarkedBlock(html, "<!-- A_START -->", "<!-- A_END -->");

        Assert.assertEquals(patched, "beforemiddleafter");
        Assert.assertEquals(
                MarkedHtmlSectionRewriter.removeMarkedBlock(patched, "<!-- A_START -->", "<!-- A_END -->"),
                patched,
                "Removing already-removed marker sections must be idempotent."
        );
    }

    @Test
    public void markedSectionRewriterLeavesMissingOrMalformedMarkersUntouched() {
        String missingEnd = "before<!-- A_START -->dangling";
        String missingStart = "dangling<!-- A_END -->after";

        Assert.assertEquals(
                MarkedHtmlSectionRewriter.removeMarkedBlock(missingEnd, "<!-- A_START -->", "<!-- A_END -->"),
                missingEnd
        );
        Assert.assertEquals(
                MarkedHtmlSectionRewriter.removeMarkedBlock(missingStart, "<!-- A_START -->", "<!-- A_END -->"),
                missingStart
        );
    }

    @Test
    public void patchPlanReplacesOnlyValidCategoryViewContent() {
        String category = "<html><div class=\"test-wrapper row view category-view attributes-view\">"
                + "<div>old categories</div></div>"
                + "<div class=\"test-wrapper row view test-view\">tests</div></html>";
        String malformed = "<html><div class=\"test-wrapper row view category-view attributes-view\">"
                + "<div>old categories</div></html>";

        String patched = ExtentHtmlPatchPlan.replaceCategoryRuntimeAudit(category, "<section>audit</section>");

        Assert.assertTrue(patched.contains("<section>audit</section>"));
        Assert.assertFalse(patched.contains("old categories"));
        Assert.assertEquals(
                ExtentHtmlPatchPlan.replaceCategoryRuntimeAudit(malformed, "<section>audit</section>"),
                malformed,
                "Malformed category view must be left untouched instead of receiving a partial patch."
        );
    }

    @Test
    public void markedSectionRewriterReplacesStableRegionsWithoutPartialMutation() {
        String html = "<html><body>"
                + "<div class=\"row\">summary</div>"
                + scoreCardBody("text-fail", ExtentDashboardLabels.TESTS_FAILED, "")
                + "<div class=\"row\">old dashboard cards</div>"
                + "<div class=\"row\"><div class=\"col-md-12\">timeline</div></div>"
                + "<script>var statusGroup = {};</script>"
                + "</body></html>";

        String patched = MarkedHtmlSectionRewriter.replaceBetweenMarkersAfter(
                html,
                html.indexOf(ExtentDashboardLabels.TESTS_FAILED),
                List.of("<div class=\"row\">"),
                List.of("<div class=\"row\"><div class=\"col-md-12\">"),
                "<section>dashboard</section>"
        );

        Assert.assertTrue(patched.contains("<section>dashboard</section>"));
        Assert.assertFalse(patched.contains("old dashboard cards"));
        Assert.assertTrue(patched.contains("timeline"));
        Assert.assertEquals(
                MarkedHtmlSectionRewriter.replaceBetweenMarkersAfter(
                        html,
                        html.indexOf(ExtentDashboardLabels.TESTS_FAILED),
                        List.of("<missing>"),
                        List.of("<div class=\"row\"><div class=\"col-md-12\">"),
                        "<section>bad</section>"
                ),
                html,
                "Missing region markers must leave the report untouched."
        );
    }

    @Test
    public void markedSectionRewriterReplacesScriptByOwnedToken() {
        String html = "<html><body><script>var statusGroup = {parentCount: 1};</script><main>body</main></body></html>";

        String patched = MarkedHtmlSectionRewriter.replaceScriptContaining(
                html,
                "var statusGroup",
                "<script>var statusGroup = {parentCount: 2};</script>"
        );

        Assert.assertTrue(patched.contains("parentCount: 2"));
        Assert.assertFalse(patched.contains("parentCount: 1"));
        Assert.assertTrue(patched.contains("<main>body</main>"));
    }

    @Test
    public void publisherLeavesMalformedReportNonBreaking() throws Exception {
        Path report = Files.createTempFile("atf-runtime-audit-malformed", ".html");
        Files.writeString(report, "<html><body>malformed without category view</body></html>", StandardCharsets.UTF_8);

        ExtentRuntimeAuditPublisher.writeAuditToReport(report, List.of(new ExtentRuntimeInvocation(
                "PASSED",
                "com.example.PositiveUserTests",
                "getUsers",
                1L,
                "",
                List.of("@Smoke")
        )));

        String patched = Files.readString(report, StandardCharsets.UTF_8);
        Assert.assertTrue(patched.contains("malformed without category view"));
        Assert.assertFalse(
                patched.contains("ATF_RUNTIME_FAILURE_GROUPS_START"),
                "Malformed reports without a category view must not receive a partial runtime audit block."
        );
    }

    @Test
    public void publisherReturnsHashRecordForRuntimeAuditMutation() throws Exception {
        Path report = Files.createTempFile("atf-runtime-audit-hash", ".html");
        Files.writeString(report, validCategoryReport(), StandardCharsets.UTF_8);

        RuntimeAuditWriteRecord record = ExtentRuntimeAuditPublisher.writeAuditToReport(
                report,
                List.of(new ExtentRuntimeInvocation(
                        "FAILED",
                        "com.example.RegistrationTests",
                        "negative_invalid_email_should_fail",
                        10L,
                        "expected validation message",
                        List.of("@UI")
                ))
        );

        Assert.assertEquals(record.report(), report.toAbsolutePath().normalize());
        Assert.assertNotNull(record.timestamp());
        Assert.assertEquals(record.beforeSha256().length(), 64);
        Assert.assertEquals(record.afterSha256().length(), 64);
        Assert.assertTrue(record.changed(), "Runtime audit update must expose before/after hash drift.");
    }

    @Test
    public void publisherInjectsRuntimeTagAuditEvenWhenNativeSparkTagsRowIsMissing() throws Exception {
        Path report = Files.createTempFile("atf-runtime-audit-no-native-tags", ".html");
        Files.writeString(report, validDashboardReportWithoutNativeTags(), StandardCharsets.UTF_8);

        ExtentRuntimeAuditPublisher.writeAuditToReport(
                report,
                List.of(
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.RegistrationTests",
                                "creates_valid_registration",
                                10L,
                                "",
                                List.of("@UI")
                        ),
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.PositiveUserTests",
                                "creates_valid_user",
                                20L,
                                "",
                                List.of("@API")
                        )
                )
        );

        String patched = Files.readString(report, StandardCharsets.UTF_8);

        ExtentRuntimeOverviewTables.assertCanonicalRuntimeTagTableShell(patched);
    }

    @Test
    public void publisherAlignsRuntimeOverviewTopCardsWithDuration() throws Exception {
        Path report = Files.createTempFile("atf-runtime-audit-standard-overview", ".html");
        Files.writeString(report, validDashboardReportWithNativeTiming(), StandardCharsets.UTF_8);
        ExtentRuntimeAuditPublisher.writeAuditToReport(
                report,
                List.of(
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.RegistrationTests",
                                "creates_valid_registration",
                                10L,
                                "",
                                List.of("@UI")
                        ),
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.PositiveUserTests",
                                "creates_valid_user",
                                20L,
                                "",
                                List.of("@API")
                        )
                )
        );

        String patched = Files.readString(report, StandardCharsets.UTF_8);

        Assert.assertTrue(
                patched.contains("<p class=\"m-b-0\">Duration</p><h3>5.000 s</h3>"),
                "Runtime-audited reports must expose the same Overview Duration score-card as aggregate reports."
        );
        Assert.assertEquals(
                count(patched, "atf-aggregate-top-card"),
                5,
                "The first Overview row must contain the five standard top score-cards."
        );
        Assert.assertTrue(
                patched.indexOf("Duration") < patched.indexOf("Test cases"),
                "Duration belongs in the top score-card row, before the Test cases dashboard card."
        );
        Assert.assertTrue(
                ExtentPublishedOverviewInspector.inspect(patched)
                        .violations(ExtentReportPublishPolicy.PUBLISHED_VALIDATION)
                        .isEmpty(),
                "Runtime audit publication must leave a complete Spark report fully publishable, including body mode attributes and status filter shell."
        );
    }

    @Test
    public void postProcessorInjectsCollapseScriptWhenSparkOmitsConfiguredJavascript() {
        String html = "<html><body class=\"spa -report dark\">"
                + "<div class=\"test-wrapper row view test-view\">"
                + "<div class=\"test-list\"><ul class=\"test-list-item\"><li class=\"test-item\" status=\"pass\">"
                + "<div class=\"test-detail\"><p class=\"name\">CollapseFixture</p></div>"
                + "<div class=\"test-contents d-none\"><div class=\"mt-4\"><div class=\"accordion\" role=\"tablist\">"
                + "<div class=\"card\"><div class=\"card-header\"><div class=\"card-title\">"
                + "<a class=\"node\"><span>methodA</span></a></div></div>"
                + "<div class=\"accordion\" role=\"tablist\"><div class=\"card\"><div class=\"card-header\">"
                + "<div class=\"card-title\"><a class=\"node\"><span>TEST BODY</span></a></div></div>"
                + "</div></div></div></div></div></div></li></ul></div>"
                + "<div class=\"test-content\"><div class=\"test-content-detail\"><div class=\"detail-body\"></div></div></div>"
                + "</div></body></html>";

        String patched = ExtentSparkHtmlPostProcessor.normalize(html);

        Assert.assertTrue(
                patched.contains(ExtentMarkers.COLLAPSE_SCRIPT)
                        && patched.contains("collapseExtentTreeByDefault")
                        && patched.contains("document.querySelectorAll('.test-contents .card').forEach(prepareCard)"),
                "Final Extent post-processing must inject the ATF collapse runtime when Spark drops configured custom JS."
        );
    }

    @Test
    public void publisherUpgradesComponentOverviewToSingleRuntimeTagAudit() throws Exception {
        Path report = Files.createTempFile("atf-runtime-audit-stale-component-tags", ".html");
        String componentHtml = ExtentSparkHtmlPostProcessor.normalize(validDashboardReportWithoutNativeTags());
        Assert.assertTrue(
                componentHtml.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                        && componentHtml.contains(ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()),
                "Precondition: component reports own a temporary component tag audit before runtime publication."
        );
        Files.writeString(report, componentHtml, StandardCharsets.UTF_8);

        ExtentRuntimeAuditPublisher.writeAuditToReport(
                report,
                List.of(
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.RegistrationTests",
                                "creates_valid_registration",
                                10L,
                                "",
                                List.of("@UI")
                        ),
                        new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.PositiveUserTests",
                                "creates_valid_user",
                                20L,
                                "",
                                List.of("@API")
                        )
                )
        );

        String patched = Files.readString(report, StandardCharsets.UTF_8);

        Assert.assertTrue(
                patched.contains(ExtentDataAttributes.REPORT_MODE + "=\"" + ExtentReportModeType.RUNTIME_AUDITED.value() + "\"")
                        && patched.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"true\""),
                "Runtime publication must upgrade the report mode from component-adapter to runtime-audited."
        );
        Assert.assertEquals(
                count(patched, ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                1,
                "Final runtime reports must publish exactly one Overview tag-audit zone."
        );
        Assert.assertEquals(
                count(patched, ExtentRuntimeOverviewTables.runtimeTagTableAttributePrefix()),
                3,
                "Final runtime reports must publish exactly three runtime tag tables."
        );
        Assert.assertFalse(
                patched.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                        || patched.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_START)
                        || patched.contains(ExtentRuntimeOverviewTables.componentTagTableAttributePrefix()),
                "Final runtime reports must not retain the temporary component Overview tag audit."
        );
    }

    @Test
    public void publisherRefusesFrozenPublishedArtifacts() throws Exception {
        Path report = Path.of("target", "runtime-audit-guard", "published-frozen-artifacts", "frozen")
                .resolve(ExtentReportFiles.STABLE_REPORT_FILE_NAME);
        Files.createDirectories(report.getParent());
        Files.writeString(report, validCategoryReport(), StandardCharsets.UTF_8);

        IOException failure = Assert.expectThrows(
                IOException.class,
                () -> ExtentRuntimeAuditPublisher.writeAuditToReport(
                        report,
                        List.of(new ExtentRuntimeInvocation(
                                "PASSED",
                                "com.example.PositiveUserTests",
                                "create_user_success",
                                1L,
                                "",
                                List.of("@API")
                        ))
                )
        );

        Assert.assertTrue(failure.getMessage().contains("Refusing to rewrite frozen Extent artifact"));
    }

    private static String validCategoryReport() {
        return "<html><body>"
                + scoreCardBody("text-pass", ExtentDashboardLabels.TESTS_PASSED, "0")
                + scoreCardBody("text-fail", ExtentDashboardLabels.TESTS_FAILED, "0")
                + "<div class=\"test-wrapper row view category-view attributes-view\">"
                + "<div>old categories</div></div>"
                + "<div class=\"test-wrapper row view test-view\">tests</div>"
                + "</body></html>";
    }

    private static String validDashboardReportWithoutNativeTags() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a><a id=\"nav-test\"></a><a id=\"nav-category\"></a>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + scoreCardBody("text-pass", ExtentDashboardLabels.TESTS_PASSED, "0")
                + "</div></div></div><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + scoreCardBody("text-fail", ExtentDashboardLabels.TESTS_FAILED, "0")
                + "</div></div></div></div>"
                + "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-header\">"
                + "<h6 class=\"card-title\">Tests</h6></div>"
                + "<div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                + "<div class=\"card-footer\"><small><b>0</b> tests passed</small></div></div></div>"
                + "</div>"
                + "<div class=\"row\"><div class=\"col-md-12\"><div class=\"card\"><div class=\"card-header\">"
                + "<p>Timeline</p></div><div class=\"card-body\"><canvas id=\"timeline\"></canvas></div></div></div></div>"
                + "<script>var timeline = {};</script>"
                + "<script>var statusGroup = {parentCount: 0};</script>"
                + "</div>"
                + "<div class=\"test-wrapper row view category-view attributes-view\"><div>old categories</div></div>"
                + "<div class=\"test-wrapper row view test-view\">tests</div>"
                + "</body></html>";
    }

    private static String validDashboardReportWithNativeTiming() {
        return "<html><body class=\"spa -report\">"
                + "<a id=\"nav-dashboard\"></a><a id=\"nav-test\"></a><a id=\"nav-category\"></a>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0\">Started</p><h3>Jul 1, 2026 08:43:22 AM</h3>"
                + "</div></div></div><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0\">Ended</p><h3>Jul 1, 2026 08:43:27 AM</h3>"
                + "</div></div></div><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + scoreCardBody("text-pass", ExtentDashboardLabels.TESTS_PASSED, "0")
                + "</div></div></div><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + scoreCardBody("text-fail", ExtentDashboardLabels.TESTS_FAILED, "0")
                + "</div></div></div></div>"
                + "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-header\">"
                + "<h6 class=\"card-title\">Tests</h6></div>"
                + "<div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                + "<div class=\"card-footer\"><small><b>0</b> tests passed</small></div></div></div>"
                + "</div>"
                + "<div class=\"row\"><div class=\"col-md-12\"><div class=\"card\"><div class=\"card-header\">"
                + "<p>Timeline</p></div><div class=\"card-body\"><canvas id=\"timeline\"></canvas></div></div></div></div>"
                + "<script>var timeline = {};</script>"
                + "<script>var statusGroup = {parentCount: 0};</script>"
                + "</div>"
                + "<div class=\"test-wrapper row view category-view attributes-view\"><div>old categories</div></div>"
                + "<div class=\"test-wrapper row view test-view\">tests</div>"
                + "</body></html>";
    }

    private static String scoreCardBody(String statusClass, String label, String count) {
        String countHtml = count.isEmpty() ? "" : "<h3>" + count + "</h3>";
        return "<p class=\"m-b-0 " + statusClass + "\">" + label + "</p>" + countHtml;
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
}
