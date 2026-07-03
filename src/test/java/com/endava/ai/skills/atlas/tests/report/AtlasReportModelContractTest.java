package com.endava.ai.skills.atlas.tests.report;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.report.AtlasReportModel;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreContract;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Listeners(TestListener.class)
public class AtlasReportModelContractTest {
    @Test
    public void atlasReportModelExtractsMainBusinessAuditSurfaces() {
        AtlasReportModel report = AtlasReportModel.parse(sampleAtlasHtml());

        Assert.assertEquals(report.scoreCards().size(), 2);
        Assert.assertEquals(report.scoreCardScoresByLabel().get("intelegere business"), Integer.valueOf(93));
        Assert.assertTrue(report.scoreCardsOpenBeforeExecutiveContext());
        Assert.assertTrue(report.overallScoreCardIsLast());
        Assert.assertTrue(report.sectionAttributes().contains("1. executive context"));
        Assert.assertTrue(report.sectionAttributes().contains("5. source-to-business trace"));
        Assert.assertEquals(report.businessGraphs().size(), 2);
        Assert.assertEquals(report.proofStrips().size(), 1);
        Assert.assertTrue(report.proofStrips().get(0).methods().contains("profilePublishesRegisteredCustomerIdentity"));
        Assert.assertTrue(report.proofStrips().get(0).classes().contains("AccountProfileFlow"));
        Assert.assertEquals(report.sourceTraceRows().size(), 1);
        Assert.assertTrue(report.sourceTraceRows().get(0).joinedText().contains("source/layer evidence"));
        Assert.assertEquals(report.auditLinks().get(0).href(),
                "validation/" + AtlasContext.ATLAS_EXTENT_REPORT_FILE);
    }

    @Test
    public void atlasReportModelKeepsArtifactNamingGeneric() {
        Assert.assertTrue(AtlasReportModel.isDeclaredAtlasArtifactName(AtlasContext.BUSINESS_ATLAS_FILE));
        Assert.assertTrue(AtlasReportModel.isDeclaredAtlasArtifactName("framework-under-test"
                + AtlasContext.BUSINESS_ATLAS_SUFFIX));
        Assert.assertTrue(AtlasReportModel.isDeclaredAtlasArtifactName("customer-suite"
                + AtlasContext.BUSINESS_ATLAS_SUFFIX));
        Assert.assertTrue(AtlasReportModel.isDeclaredAtlasArtifactName("framework-under-test"
                + AtlasContext.FRAMEWORK_ATLAS_SUFFIX));
        Assert.assertFalse(AtlasReportModel.isDeclaredAtlasArtifactName("framework-under-test_atlas.html"));
    }

    @Test
    public void atlasReportModelCountsNestedBusinessSubgraphsWithoutLosingThem() {
        AtlasReportModel report = AtlasReportModel.parse(String.join("\n", List.of(
                "<details data-atlas-section=\"2. business flow graph\" data-business-flow=\"primary\">",
                "<summary>2. Business Flow Graph</summary>",
                "<details data-atlas-role=\"graph-node-business-proof-strip\"><summary>Main proof</summary></details>",
                "</details>",
                "<details data-atlas-section=\"3. business flow understanding\">",
                "<summary>3. Business Flow Understanding</summary>",
                "<details class=\"subdetails\" data-business-flow=\"refund\"><summary>3.1 Refund</summary>",
                "<details data-atlas-role=\"local-proof-strip\"><summary>Refund proof</summary></details>",
                "</details>",
                "<details class=\"subdetails\" data-business-flow=\"invoice\"><summary>3.2 Invoice</summary>",
                "<details data-atlas-role=\"local-proof-strip\"><summary>Invoice proof</summary></details>",
                "</details>",
                "</details>"
        )));

        Assert.assertEquals(report.businessGraphs().size(), 3,
                "Atlas model must count the primary graph plus nested 3.x business subgraphs.");
    }

    @Test
    public void atlasTextUtilitiesTreatMissingFragmentsAsEmptyText() {
        Assert.assertEquals(AtlasText.plainText(null), "");
        Assert.assertEquals(AtlasText.normalizeLabel(null), "");
        Assert.assertEquals(AtlasText.normalizeLabel(" Customer-Refund / UI.State  "), "customer refund ui state");
        Assert.assertEquals(AtlasText.normalizeWhitespace(null), "");
        Assert.assertEquals(AtlasText.stripTags(null), "");
        Assert.assertEquals(AtlasText.sectionAfter(null, "business", 100), "");
        Assert.assertEquals(AtlasText.sectionBetween(null, "a", "b"), "");
        Assert.assertEquals(AtlasText.firstExistingIndex(null, "a"), -1);
        Assert.assertEquals(AtlasText.count(Pattern.compile("x"), null), 0);
        Assert.assertFalse(AtlasText.containsAll(null, "a"));
        Assert.assertEquals(AtlasText.abbreviate(null, 10), "");
    }

    @Test
    public void atlasScoreContractUsesSharedTextNormalizationAndMissingTextDefaults() {
        Assert.assertEquals(AtlasScoreContract.normalized("<b>Overall</b>   94"), "overall 94");
        Assert.assertEquals(AtlasScoreContract.normalized(null), "");
        Assert.assertEquals(AtlasScoreContract.sectionAfter(null, "score", "</details>"), "");
        Assert.assertEquals(AtlasScoreContract.sectionBetween(null, "a", "b"), "");
        Assert.assertEquals(AtlasScoreContract.scoreNear(null, "overall"), -1);
        Assert.assertEquals(AtlasScoreContract.scoreNear("overall is 94", null), -1);
        Assert.assertEquals(AtlasScoreContract.scoreNear("overall is 94", "overall"), 94);
        Assert.assertEquals(AtlasScoreContract.rowContext(null, "overall"), "");
        Assert.assertEquals(AtlasScoreContract.lastRowContext(null, "overall"), "");
    }

    @Test
    public void generatedAtlasExposesModelReadableAuditSurfaces() throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasReportModel report = AtlasReportModel.parse(context.atlasText());
        String atlasFileName = context.atlasHtml().getFileName().toString();

        Assert.assertTrue(AtlasReportModel.isDeclaredAtlasArtifactName(atlasFileName),
                "Atlas sanity must validate an explicitly declared business/framework atlas artifact, not an "
                        + "ad-hoc calibration file: " + atlasFileName);
        Assert.assertTrue(report.scoreCards().size() >= 6, "Generated atlas should expose canonical score cards.");
        Assert.assertTrue(report.businessGraphs().size() >= 4, "Generated atlas should expose top graph plus business subgraphs.");
        Assert.assertTrue(report.proofStrips().size() >= 4, "Generated atlas should expose local proof strips.");
        Assert.assertFalse(report.sourceTraceRows().isEmpty(), "Generated atlas should expose source-to-business trace rows.");
    }

    private static String sampleAtlasHtml() {
        return String.join("\n", List.of(
                "<html><body>",
                "<section class=\"score-wall\" data-atlas-section=\"score cards\">",
                "  <article class=\"score-card\" data-atlas-role=\"score-card\"><div class=\"label\">Intelegere Business</div><div class=\"score\">93</div><p>Clar.</p></article>",
                "  <article class=\"score-card\" data-atlas-role=\"score-card\"><div class=\"label\">Overall</div><div class=\"score\">91</div><p>Derivat.</p></article>",
                "</section>",
                "<details data-atlas-section=\"1. executive context\"><summary>1. Executive Context</summary></details>",
                "<details data-atlas-section=\"2. business flow graph\" data-business-flow=\"primary-map\"><summary>2. Business Flow Graph</summary></details>",
                "<details data-atlas-section=\"3. business flow understanding\">",
                "  <summary>3. Business Flow Understanding</summary>",
                "  <details class=\"subdetails\" data-business-flow=\"account-profile\"><summary>3.1 Account profile</summary>",
                "    <details class=\"subdetails\" data-atlas-role=\"local-proof-strip\"><summary>Proof strip local</summary>",
                "      <table><tr><td>Profile</td><td><code>profilePublishesRegisteredCustomerIdentity()</code></td><td><code>AccountProfileFlow</code></td></tr></table>",
                "    </details>",
                "  </details>",
                "</details>",
                "<details data-atlas-section=\"5. source-to-business trace\"><summary>5. Source-to-Business Trace</summary>",
                "  <table><thead><tr><th>Axa</th><th>Scor</th><th>Evidence</th></tr></thead><tbody>",
                "    <tr><td>Intelegere Business</td><td>93</td><td>source/layer evidence is reopenable</td></tr>",
                "  </tbody></table>",
                "</details>",
                "<details data-atlas-section=\"6. atlas test audit\"><summary>6. Atlas Test Audit</summary>",
                "  <a href=\"validation/" + AtlasContext.ATLAS_EXTENT_REPORT_FILE + "\">Atlas Extent</a>",
                "</details>",
                "</body></html>"
        ));
    }
}
