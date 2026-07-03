package com.ai.atf.core.reporting.adapters;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchRecord;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchResult;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.ai.core.reporting.adapters.extent.patch.model.PatchOutcome;
import com.ai.core.reporting.adapters.extent.inspection.ExtentStatusGroupSnapshot;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_HTML_PATCH_PIPELINE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_HTML_PATCH_PIPELINE_CONTRACT)
@Listeners(TestListener.class)
public class ExtentHtmlPatchPipelineContractTest {

    @Test
    public void componentReportProducesTypedPatchResultAndComponentOverviewAudit() {
        ExtentHtmlPatchResult result = ExtentSparkHtmlPostProcessor.normalizeWithResult(componentSparkHtmlWithNativeTags());
        Map<String, ExtentHtmlPatchRecord> records = recordsById(result);

        Assert.assertTrue(result.changed(), "First normalization must expose applied patch records.");
        Assert.assertEquals(records.get("extent.navigation.guard").outcome(), PatchOutcome.APPLIED);
        Assert.assertEquals(records.get("extent.component-overview.audit").outcome(), PatchOutcome.APPLIED);
        Assert.assertEquals(records.get("extent.report-mode.marker").outcome(), PatchOutcome.APPLIED);
        Assert.assertEquals(records.get("extent.component-status-group.reconcile").outcome(), PatchOutcome.APPLIED);
        Assert.assertEquals(records.get("extent.collapse-script.fallback").outcome(), PatchOutcome.APPLIED);
        ExtentStatusGroupSnapshot statusGroup = ExtentStatusGroupSnapshot.fromHtml(result.html());
        Assert.assertEquals(statusGroup.parentCount(), 2,
                "Component statusGroup parent count must match the Test cases score-card.");
        Assert.assertEquals(statusGroup.passParent(), 2,
                "Component statusGroup parent pass count must match the Test cases score-card.");
        Assert.assertEquals(statusGroup.childCount(), 4,
                "Component statusGroup child count must match the Tests score-card.");
        Assert.assertEquals(statusGroup.passChild(), 4,
                "Component statusGroup child pass count must match the Tests score-card.");
        Assert.assertTrue(result.html().contains("<h6 class=\"card-title\">Test cases</h6>"),
                "Component score card must use Test cases, not Test classes.");
        Assert.assertFalse(result.html().contains("<h6 class=\"card-title\">Test classes</h6>"),
                "Component score cards must not use the tag-distribution wording Test classes.");
        Assert.assertTrue(result.html().contains("id=\"" + ExtentMarkers.REPORT_MODE + "\""));
        Assert.assertTrue(result.html().contains("id=\"" + ExtentMarkers.COLLAPSE_SCRIPT + "\""));
        Assert.assertTrue(result.html().contains("collapseExtentTreeByDefault"));
        Assert.assertTrue(result.html().contains("\"mode\":\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\""));
        Assert.assertTrue(result.html().contains("\"runtimeAudit\":false"));
        Assert.assertTrue(result.html().contains(ExtentDataAttributes.REPORT_MODE + "=\""
                + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\""));
        Assert.assertTrue(result.html().contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL));
        Assert.assertEquals(
                count(result.html(), ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE)),
                3,
                "Component/adapter Overview must expose the same three tag-table shape as runtime reports.");
        Assert.assertFalse(result.html().contains("<h6 class=\"card-title\"></h6>"),
                "Component/adapter Overview must remove Spark's blank grandchild score card.");
        Assert.assertFalse(result.html().contains("<div class=\"card-header\"><p>Tags</p></div>"),
                "Component/adapter Overview must replace Spark's mixed Tags card with ATF tag tables.");
    }

    @Test
    public void runtimeAuditedReportSkipsComponentFallback() {
        String html = componentSparkHtml()
                .replace("</body>", runtimeAuditMarkerSection() + "</body>");

        ExtentHtmlPatchResult result = ExtentSparkHtmlPostProcessor.normalizeWithResult(html);
        Map<String, ExtentHtmlPatchRecord> records = recordsById(result);

        Assert.assertEquals(records.get("extent.navigation.guard").outcome(), PatchOutcome.APPLIED);
        Assert.assertEquals(records.get("extent.report-mode.marker").outcome(), PatchOutcome.APPLIED);
        Assert.assertFalse(records.containsKey("extent.component-fallback.dashboard"),
                "Runtime-audited reports must not plan component fallback injection.");
        Assert.assertFalse(records.containsKey("extent.component-overview.audit"),
                "Runtime-audited reports own their Overview through the runtime audit renderer, not component overview patching.");
        Assert.assertTrue(result.html().contains("\"mode\":\"" + ExtentReportModeType.RUNTIME_AUDITED.value() + "\""));
        Assert.assertTrue(result.html().contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"true\""));
    }

    @Test
    public void componentOverviewAuditUsesStatusGroupAnchorWhenNativeTagsRowIsMissing() {
        ExtentHtmlPatchResult result = ExtentSparkHtmlPostProcessor.normalizeWithResult(
                componentSparkHtmlWithStatusGroupButNoNativeTags()
        );
        Map<String, ExtentHtmlPatchRecord> records = recordsById(result);

        Assert.assertEquals(
                records.get("extent.component-overview.audit").outcome(),
                PatchOutcome.APPLIED,
                "Component Overview audit must still standardize reports when Spark omits the native Tags row."
        );
        Assert.assertTrue(
                result.html().contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                        && count(result.html(), ExtentHtmlStructure.dataAttributePrefix(
                                ExtentDataAttributes.COMPONENT_TAG_TABLE)) == 3,
                "A component report without the native Tags row must still receive the canonical ATF Overview audit."
        );
        Assert.assertTrue(result.html().contains("\"mode\":\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\""));
    }

    @Test
    public void runtimeAuditedReportRemovesStaleComponentOverviewAudit() {
        String component = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtmlWithNativeTags());
        String html = component.replace("</body>", runtimeAuditMarkerSection() + "</body>");

        ExtentHtmlPatchResult result = ExtentSparkHtmlPostProcessor.normalizeWithResult(html);
        Map<String, ExtentHtmlPatchRecord> records = recordsById(result);

        Assert.assertEquals(records.get("extent.component-overview.remove-stale-runtime").outcome(), PatchOutcome.APPLIED);
        Assert.assertFalse(result.html().contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL),
                "Runtime-audited reports must not keep the earlier component Overview audit.");
        Assert.assertFalse(result.html().contains(ExtentHtmlStructure.dataAttributePrefix(
                        ExtentDataAttributes.COMPONENT_TAG_TABLE)),
                "Runtime-audited reports must expose only runtime tag tables.");
        Assert.assertEquals(count(result.html(), ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL), 1,
                "Runtime-audited Overview must contain one canonical tag-audit zone.");
    }

    @Test
    public void normalizationIsIdempotentAtHtmlLevel() {
        String once = ExtentSparkHtmlPostProcessor.normalize(componentSparkHtml());
        String twice = ExtentSparkHtmlPostProcessor.normalize(once);

        Assert.assertEquals(twice, once);
    }

    private static Map<String, ExtentHtmlPatchRecord> recordsById(ExtentHtmlPatchResult result) {
        return result.records().stream().collect(Collectors.toMap(ExtentHtmlPatchRecord::id, Function.identity()));
    }

    private static String runtimeAuditMarkerSection() {
        return "<section>"
                + ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_START)
                + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL
                + "</section>";
    }

    private static String componentSparkHtml() {
        return "<html><body class=\"spa -report\">"
                + "<div class=\"side-nav-menu\"><li class=\"nav-item\"><a id=\"nav-test\"></a></li>"
                + "<li class=\"nav-item\"><a id=\"nav-category\"></a></li>"
                + "<li class=\"nav-item\"><a id=\"nav-dashboard\"></a></li></div>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER + "<div>dashboard</div></div>"
                + "<div class=\"test-wrapper row view category-view attributes-view\">categories</div>"
                + "<div class=\"test-wrapper row view test-view\">tests</div>"
                + "</body></html>";
    }

    private static String componentSparkHtmlWithNativeTags() {
        return componentSparkHtml().replace("<div>dashboard</div>",
                "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-header\">"
                        + "<h6 class=\"card-title\">Tests</h6></div><div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                        + "<div class=\"card-footer\"><small><b>2</b> tests passed</small></div></div></div>"
                        + "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-header\">"
                        + "<h6 class=\"card-title\"></h6></div><div class=\"card-footer\"><small><b>2</b> passed</small></div></div></div></div>"
                        + "<div class=\"row\"><div class=\"col-lg-6 col-md-12 category-container\"><div class=\"card\">"
                        + "<div class=\"card-header\"><p>Tags</p></div><div class=\"card-body\">Spark native tags</div></div></div></div>"
                        + "<script>var statusGroup = {parentCount: 1,passParent: 2,failParent: 0,skipParent: 0,"
                        + "childCount: 1,passChild: 4,failChild: 0,skipChild: 0,"
                        + "grandChildCount: 1,passGrandChild: 4,failGrandChild: 0,skipGrandChild: 0,"
                        + "eventsCount: 0};</script>")
                .replace("<div class=\"test-wrapper row view test-view\">tests</div>",
                        "<div class=\"test-wrapper row view test-view\"><ul class=\"test-list-item\">"
                                + "<li class=\"test-item\" status=\"pass\"><div class=\"test-detail\"><p class=\"name\">AlphaContractTest</p></div>"
                                + "<div class=\"test-contents\"><span class=\"badge badge-pill badge-default\">com.ai.alpha</span></div></li>"
                                + "<li class=\"test-item\" status=\"pass\"><div class=\"test-detail\"><p class=\"name\">BetaContractTest</p></div>"
                                + "<div class=\"test-contents\"><span class=\"badge badge-pill badge-default\">com.ai.beta</span></div></li>"
                                + "</ul></div>");
    }

    private static String componentSparkHtmlWithStatusGroupButNoNativeTags() {
        return componentSparkHtml().replace("<div>dashboard</div>",
                "<div class=\"row\"><div class=\"col-md-3\"><div class=\"card\"><div class=\"card-header\">"
                        + "<h6 class=\"card-title\">Tests</h6></div><div class=\"card-body\"><canvas id='parent-analysis'></canvas></div>"
                        + "<div class=\"card-footer\"><small><b>2</b> tests passed</small></div></div></div></div>"
                        + "<script>var statusGroup = {parentCount: 2,passParent: 2,failParent: 0,skipParent: 0,"
                        + "childCount: 2,passChild: 2,failChild: 0,skipChild: 0,"
                        + "grandChildCount: 0,passGrandChild: 0,failGrandChild: 0,skipGrandChild: 0,"
                        + "eventsCount: 0};</script>");
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
