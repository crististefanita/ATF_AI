package com.ai.atf.core.reporting.graph;

import com.ai.core.reporting.graph.BusinessGraphLayout;
import com.ai.core.reporting.graph.BusinessGraphLayoutAdapter;
import com.ai.core.reporting.htmlcontract.model.HtmlReportShellProfile;
import com.ai.core.reporting.htmlcontract.validation.HtmlReportShellValidator;
import com.ai.skills.business.model.BusinessGraphConstruction;
import com.ai.skills.business.model.BusinessObject;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public final class BusinessGraphLayoutAdapterContractTest {
    private final BusinessGraphLayoutAdapter adapter = new BusinessGraphLayoutAdapter();
    private final HtmlReportShellValidator shellValidator = new HtmlReportShellValidator();

    @Test
    public void rendersBusinessGraphConstructionAsNeutralReportingLayout() {
        BusinessGraphLayout layout = adapter.render(graph());

        BusinessGraphLayoutAssertions.assertPrimitiveShape(layout);
        BusinessGraphLayoutAssertions.assertLayoutClasses(layout);
    }

    @Test
    public void keepsTechnicalProofBoundaryOutsideSvgBusinessNodes() {
        BusinessGraphLayout layout = adapter.render(graph());

        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "Extent");
        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "XML");
        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "MCP");
        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "diff");
        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "25 / 25");
        BusinessGraphLayoutAssertions.assertSvgNotContains(layout, "run_");
        BusinessGraphLayoutAssertions.assertHtmlContains(layout, "Extent, XML, MCP and diff");
    }

    @Test
    public void exposesAuditableRolesAndAccessibleSvgMetadata() {
        BusinessGraphLayout layout = adapter.render(graph());

        BusinessGraphLayoutAssertions.assertAccessibleMetadata(layout);
        BusinessGraphLayoutAssertions.assertGraphRoles(layout);
        BusinessGraphLayoutAssertions.assertSinglePrimaryOwnedNode(layout);
        BusinessGraphLayoutAssertions.assertOutcomeAndFrontierAreNotOwned(layout);
    }

    @Test
    public void staysAlignedWithFinalReportGraphDensityGate() {
        BusinessGraphLayout layout = adapter.render(graph());

        BusinessGraphLayoutAssertions.assertMcpAlignedNodeDensity(layout);
    }

    @Test
    public void generatedLayoutPassesSharedHtmlGraphPrimitiveContract() {
        String html = report(adapter.render(graph()).html());

        var violations = shellValidator.validate(html, HtmlReportShellProfile.SHARED_FINAL_REPORT);
        Assert.assertTrue(violations.isEmpty(),
                "Generated business graph layout must pass shared shell: " + violations.stream()
                        .map(violation -> violation.code() + "=" + violation.message())
                        .collect(Collectors.joining("; ")));
    }

    @Test
    public void escapesBusinessLabelsBeforeRendering() {
        BusinessGraphConstruction graph = new BusinessGraphConstruction(
                new BusinessObject("Refund <anchor>", "visible & reopenable"),
                "staff confirms \"refund intent\"",
                new BusinessObject("Support case", "owned result"),
                List.of("anchors <intent>", "captures & submits", "awaits \"decision\""),
                new BusinessObject("Settlement frontier", "capped downstream"),
                "Proof stays in Extent, XML, MCP and diff artifacts");

        BusinessGraphLayout layout = adapter.render(graph);

        BusinessGraphLayoutAssertions.assertHtmlContains(layout, "Refund &lt;anchor&gt;");
        BusinessGraphLayoutAssertions.assertHtmlContains(layout, "visible &amp; reopenable");
        BusinessGraphLayoutAssertions.assertHtmlContains(layout, "staff confirms &quot;refund intent&quot;");
        BusinessGraphLayoutAssertions.assertHtmlNotContains(layout, "Refund <anchor>");
    }

    @Test
    public void keepsLongLabelsReadableWhilePreservingFullTextForAudit() {
        String longOwnedObject = "Staff-approved customer refund resolution record";
        String longEdge = "captures visible refund intent without leaking execution proof";
        BusinessGraphConstruction graph = new BusinessGraphConstruction(
                new BusinessObject("Authenticated customer purchase invoice", "refund request visible in UI"),
                "support specialist validates a customer refund request",
                new BusinessObject(longOwnedObject, "owned durable support outcome"),
                List.of("anchors purchase context", longEdge, "awaits settlement posting"),
                new BusinessObject("Settlement posting downstream frontier", "capped outside this test"),
                "Proof stays in Extent, XML, MCP and diff artifacts");

        BusinessGraphLayout layout = adapter.render(graph);

        BusinessGraphLayoutAssertions.assertFullText(layout, longOwnedObject);
        BusinessGraphLayoutAssertions.assertFullText(layout, longEdge);
        BusinessGraphLayoutAssertions.assertVisibleText(layout, "Staff-approved customer refund re...");
        BusinessGraphLayoutAssertions.assertVisibleText(layout, "captures visible refund i...");
        BusinessGraphLayoutAssertions.assertNotVisibleText(layout, longOwnedObject);
        BusinessGraphLayoutAssertions.assertNotVisibleText(layout, longEdge);
    }

    @Test
    public void fillsMissingRelationshipLabelsWithoutChangingPrimitiveShape() {
        BusinessGraphConstruction graph = new BusinessGraphConstruction(
                new BusinessObject("Customer refund", "refund intent is visible"),
                "support staff resolves customer refund",
                new BusinessObject("Support case", "owned resolution record"),
                List.of("anchors claim"),
                new BusinessObject("Settlement posting", "downstream frontier is capped"),
                "Proof stays in Extent, XML, MCP and diff artifacts");

        BusinessGraphLayout layout = adapter.render(graph);

        BusinessGraphLayoutAssertions.assertPrimitiveShape(layout);
        BusinessGraphLayoutAssertions.assertVisibleText(layout, "anchors claim");
        BusinessGraphLayoutAssertions.assertVisibleText(layout, "owns");
        BusinessGraphLayoutAssertions.assertVisibleText(layout, "limits");
    }

    @Test
    public void rendersSupplementalRelationshipLabelsAsCuesWithoutAddingNodes() {
        BusinessGraphConstruction graph = new BusinessGraphConstruction(
                new BusinessObject("Customer refund", "refund intent is visible"),
                "support staff resolves customer refund",
                new BusinessObject("Support case", "owned resolution record"),
                List.of("anchors", "captures", "proves result", "awaits decision", "notifies customer"),
                new BusinessObject("Settlement posting", "downstream frontier is capped"),
                "Proof stays in Extent, XML, MCP and diff artifacts");

        BusinessGraphLayout layout = adapter.render(graph);

        BusinessGraphLayoutAssertions.assertPrimitiveShape(layout);
        BusinessGraphLayoutAssertions.assertSupplementalRelationships(layout,
                "notifies customer");
        BusinessGraphLayoutAssertions.assertSupplementalRelationshipCues(layout,
                "notifies customer");
    }

    private static BusinessGraphConstruction graph() {
        return new BusinessGraphConstruction(
                new BusinessObject("Customer refund", "refund intent is visible"),
                "support staff resolves customer refund",
                new BusinessObject("Support case", "owned resolution record"),
                List.of("anchors", "captures", "awaits decision"),
                new BusinessObject("Settlement posting", "downstream frontier is capped"),
                "Proof stays in Extent, XML, MCP and diff artifacts");
    }

    private static String report(String body) {
        return "<!doctype html>"
                + "<html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Report</title><link rel=\"stylesheet\" href=\"shared-report-reference.css\"></head>"
                + "<body><main class=\"wrap\"><header class=\"hero\"><h1>Report</h1></header>"
                + "<section class=\"score-wall\">"
                + "<article class=\"score-card\"><div class=\"label\">Business</div><div class=\"score\">96</div></article>"
                + "<article class=\"score-card\"><div class=\"label\">Graph</div><div class=\"score\">96</div></article>"
                + "<article class=\"score-card\"><div class=\"label\">Overall</div><div class=\"score\">96</div></article>"
                + "</section>"
                + body
                + "</main></body></html>";
    }

}
