package com.ai.skills.atlas.tests.candidate;

import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.rules.graph.AtlasReusableGraphCssRule;
import com.ai.skills.atlas.rules.business.AtlasBusinessOnlyFlowRule;
import com.ai.skills.atlas.rules.graph.proof.AtlasGraphProofStripPlacementRule;
import com.ai.skills.atlas.rules.graph.proof.AtlasMainGraphNodeMaturityProofRule;
import com.ai.skills.atlas.rules.graph.proof.AtlasSubgraphSemanticDensityRule;
import com.ai.skills.atlas.rules.graph.style.AtlasGraphVisualScaleRule;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;
import static com.ai.skills.atlas.tests.candidate.AtlasCandidateGraphFixtures.*;
import static com.ai.skills.atlas.tests.candidate.AtlasCandidateGraphFixtures.restore;
import static com.ai.skills.atlas.tests.candidate.AtlasCandidateGraphFixtures.validateFixture;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_GRAPH)
public class AtlasCandidateGraphNegativeFixtureContractTest {
    @Test
    public void candidateAtlasGraphCssStylesEdgeLabelsWhenUsed() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "graph-css-edge-labels");
        Path atlas = fixture.resolve("edge-label-atlas.html");
        Path css = fixture.resolve("shared-report-reference.css");
        Files.createDirectories(fixture);
        Files.writeString(atlas,
                "<html><head><link rel=\"stylesheet\" href=\"shared-report-reference.css\"></head><body>\n"
                        + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                        + "  <summary>2. Business Flow Graph</summary>\n"
                        + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1180 430\" aria-label=\"Business Flow Graph\">\n"
                        + "    <line class=\"edge\" x1=\"10\" y1=\"20\" x2=\"150\" y2=\"20\"></line>\n"
                        + "    <text class=\"el\">opens account</text>\n"
                        + "    <rect class=\"node\"></rect><text class=\"nt\">Customer account</text><text class=\"ns\">owned maturity</text>\n"
                        + "  </svg></div>\n"
                        + "</details>\n"
                        + "</body></html>\n");
        Files.writeString(css, ".diag .edge { stroke: #7aa2ff; } .nt { fill: #fff; } .ns { fill: #aaa; }");

        String previousAtlas = System.getProperty("atlas.html");
        String previousCss = System.getProperty("atlas.sharedCss");
        try {
            System.setProperty("atlas.html", atlas.toString());
            System.setProperty("atlas.sharedCss", css.toString());
            AtlasValidationResult result = new AtlasValidationResult();
            new AtlasReusableGraphCssRule().validate(AtlasContext.fromWorkingDirectory(), result);
            Assert.assertTrue(result.byCode(GRAPH_CSS_REUSABLE).stream()
                            .anyMatch(violation -> violation.toString().contains("shared CSS does not style it")
                                    && violation.toString().contains("edge-label class el")),
                    "Reusable graph CSS must style edge labels when reports use class el.");
        } finally {
            restore("atlas.html", previousAtlas);
            restore("atlas.sharedCss", previousCss);
        }
    }

    @Test
    public void candidateAtlasMainGraphRejectsFlatLineOnlyObjectMap() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "flat-line-only-main-graph");
        Path atlas = fixture.resolve("flat-main-graph-atlas.html");
        Files.createDirectories(fixture);
        Files.writeString(atlas,
                "<html><body>\n"
                        + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                        + "  <summary>2. Business Flow Graph</summary>\n"
                        + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1180 430\" aria-label=\"Business Flow Graph\">\n"
                        + "    <g transform=\"translate(60 70)\" data-maturity=\"diagnostic\"><rect class=\"node\"></rect><text class=\"nt\">Customer account</text><text class=\"ns\">diagnostic maturity</text></g>\n"
                        + "    <g transform=\"translate(410 70)\" data-maturity=\"owned\"><rect class=\"node\"></rect><text class=\"nt\">Profile identity</text><text class=\"ns\">owned maturity</text></g>\n"
                        + "    <g transform=\"translate(760 70)\" data-maturity=\"owned\"><rect class=\"node\"></rect><text class=\"nt\">Contact address</text><text class=\"ns\">owned maturity</text></g>\n"
                        + "    <g transform=\"translate(760 245)\" data-maturity=\"surface-only\"><rect class=\"node\"></rect><text class=\"nt\">Messages area</text><text class=\"ns\">surface-only maturity</text></g>\n"
                        + "    <g transform=\"translate(410 245)\" data-maturity=\"partial\"><rect class=\"node\"></rect><text class=\"nt\">Invoice record</text><text class=\"ns\">partial boundary</text></g>\n"
                        + "    <g transform=\"translate(60 245)\" data-maturity=\"diagnostic\"><rect class=\"node\"></rect><text class=\"nt\">Account state</text><text class=\"ns\">diagnostic maturity</text></g>\n"
                        + "    <line class=\"edge\" x1=\"260\" y1=\"120\" x2=\"410\" y2=\"120\"></line><text class=\"el\">opens account</text>\n"
                        + "    <line class=\"edge\" x1=\"610\" y1=\"120\" x2=\"760\" y2=\"120\"></line><text class=\"el\">retains profile</text>\n"
                        + "    <line class=\"edge\" x1=\"910\" y1=\"170\" x2=\"910\" y2=\"245\"></line><text class=\"el\">exposes contact</text>\n"
                        + "    <line class=\"edge\" x1=\"760\" y1=\"295\" x2=\"610\" y2=\"295\"></line><text class=\"el\">bounds invoice</text>\n"
                        + "    <line class=\"edge\" x1=\"410\" y1=\"295\" x2=\"260\" y2=\"295\"></line><text class=\"el\">returns account state</text>\n"
                        + "  </svg></div>\n"
                        + "</details>\n"
                        + "</body></html>\n");

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        String feedback = result.byCode(GRAPHS_VISUAL_SCALE).toString();
        Assert.assertTrue(feedback.contains("too compressed"),
                "Flat two-band graph should be rejected for compressed ATF1-style canvas feedback.");
        Assert.assertTrue(feedback.contains("too linear"),
                "Flat two-band graph should be rejected for missing a third visual band.");
        Assert.assertTrue(feedback.contains("line-only"),
                "Main graph should not pass when all relationships are straight <line> edges.");
        Assert.assertTrue(feedback.contains("primary owned object"),
                "Main graph should tell the agent to mark the primary owned business object.");
        Assert.assertTrue(feedback.contains("capped frontier") && feedback.contains("dashed frontier"),
                "Main graph should require capped/dashed frontier treatment instead of only partial/surface text.");
    }

    @Test
    public void candidateAtlasSupportingGraphsRejectLineOnlyConnectors() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "supporting-graph-readability");
        Path atlas = fixture.resolve("line-only-supporting-graph-atlas.html");
        Files.createDirectories(fixture);
        Files.writeString(atlas, supportingLineOnlyGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("Supporting Business Flow Understanding graph is line-only")
                                && violation.toString().contains("account corridor flow")),
                "Supporting 3.x business graphs must not remain line-only when the primary graph uses calibrated path edges.");
    }

    @Test
    public void candidateAtlasSupportingGraphsRejectSparseVerticalLayout() throws IOException {
        Path atlas = writeFixture("supporting-graph-readability",
                "sparse-supporting-graph-atlas.html",
                supportingStraightPathGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("vertical dead space")
                                && violation.toString().contains("account corridor flow")),
                "Supporting 3.x business graphs must be compact, not just tag-rewritten into a sparse panel.");
    }

    @Test
    public void candidateAtlasSupportingGraphsRequireArrowheadsAndEdgeLabels() throws IOException {
        Path atlas = writeFixture("supporting-graph-readability",
                "unlabeled-unarrowed-supporting-graph-atlas.html",
                supportingGraphWithoutArrowheadsAndEdgeLabels());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("edges are missing arrowheads")
                                && violation.toString().contains("account corridor flow")),
                "Supporting 3.x graph edges must show direction with marker-end like the calibrated ATF1 graph.");
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("too few visible relationship labels")
                                && violation.toString().contains("account corridor flow")),
                "Supporting 3.x graph edges must carry relationship labels, not hide all semantics inside node subtitles.");
    }

    @Test
    public void candidateAtlasMainGraphReadabilityRejectsInvisibleSemantics() throws IOException {
        Path atlas = writeFixture("main-graph-readability",
                "unlabeled-invisible-semantics-atlas.html",
                invisibleSemanticsGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("too few visible relationship labels")),
                "Main graph readability must reject unlabeled business relationships.");
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("no visible maturity cue")
                                && violation.toString().contains("data-maturity=\"diagnostic\"")),
                "Main graph readability must tell the agent to render visible maturity cues, not only hidden data attributes.");
    }

    @Test
    public void candidateAtlasMainGraphReadabilityRejectsTerseEdgeLabelsAndApproximateMaturity() throws IOException {
        Path atlas = writeFixture("main-graph-readability",
                "terse-labels-approximate-maturity-atlas.html",
                terseLabelsApproximateMaturityGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphVisualScaleRule());
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("relationship labels are too terse")),
                "Main graph readability must reject one-word relationship labels.");
        Assert.assertTrue(result.byCode(GRAPHS_VISUAL_SCALE).stream()
                        .anyMatch(violation -> violation.toString().contains("messages area -> surface-only")
                                && violation.toString().contains("data-maturity=\"surface-only\"")),
                "Main graph readability must require the exact surface-only maturity cue.");
    }

    @Test
    public void candidateAtlasMainGraphMaturityProofNamesRepairNode() throws IOException {
        Path atlas = writeFixture("main-graph-maturity-proof", "missing-maturity-atlas.html", missingMaturityGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasMainGraphNodeMaturityProofRule());
        Assert.assertFalse(result.byCode(MAIN_GRAPH_NODE_MATURITY_PROOF).isEmpty(),
                "Missing main-graph maturity must be rejected.");
        Assert.assertTrue(result.byCode(MAIN_GRAPH_NODE_MATURITY_PROOF).stream()
                        .anyMatch(violation -> violation.toString().contains("invoice record")
                                && violation.toString().contains("maturity")),
                "Violation should name the graph node and tell the agent to repair maturity.");
    }

    @Test
    public void candidateAtlasMainGraphRequiresInNodeMaturityMarkers() throws IOException {
        Path atlas = writeFixture("main-graph-in-node-maturity",
                "proof-strip-with-unmarked-svg-nodes.html",
                unmarkedMaturityGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasMainGraphNodeMaturityProofRule());
        Assert.assertTrue(result.byCode(MAIN_GRAPH_NODE_MATURITY_PROOF).stream()
                        .anyMatch(violation -> violation.toString().contains("customer account expected diagnostic")
                                && violation.toString().contains("data-maturity=\"diagnostic\"")),
                "Violation must give enough local feedback to repair diagnostic graph nodes without prompt hints.");
        Assert.assertTrue(result.byCode(MAIN_GRAPH_NODE_MATURITY_PROOF).stream()
                        .anyMatch(violation -> violation.toString().contains("messages area expected surface-only")
                                && violation.toString().contains("data-maturity=\"surface-only\"")),
                "Violation must give enough local feedback to repair surface-only graph nodes without prompt hints.");
        Assert.assertTrue(result.byCode(MAIN_GRAPH_NODE_MATURITY_PROOF).stream()
                        .anyMatch(violation -> violation.toString().contains("invoice record expected partial")
                                && violation.toString().contains("data-maturity=\"partial\"")),
                "Violation must give enough local feedback to repair partial graph nodes without prompt hints.");
    }

    @Test
    public void candidateAtlasGraphProofPlacementRejectsInlineOrMisorderedProof() throws IOException {
        Path atlas = writeFixture("graph-proof-placement", "misordered-proof-atlas.html", misorderedProofGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasGraphProofStripPlacementRule());
        Assert.assertFalse(result.byCode(GRAPH_PROOF_STRIP_PLACEMENT).isEmpty(),
                "Inline or misordered graph proof must be rejected.");
        Assert.assertTrue(result.byCode(GRAPH_PROOF_STRIP_PLACEMENT).stream()
                        .anyMatch(violation -> violation.toString().contains("Legend")),
                "Violation should tell the agent to place Legend before Banda de dovada noduri.");
    }

    @Test
    public void candidateAtlasSubgraphSemanticDensityRejectsTemplateClones() throws IOException {
        Path atlas = writeFixture("subgraph-semantic-density", "template-clone-atlas.html", templateCloneGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasSubgraphSemanticDensityRule());
        Assert.assertFalse(result.byCode(SUBGRAPH_SEMANTIC_DENSITY).isEmpty(),
                "Template-like 3.x subgraphs must be rejected.");
        Assert.assertTrue(result.byCode(SUBGRAPH_SEMANTIC_DENSITY).stream()
                        .anyMatch(violation -> violation.toString().contains("too template-like")),
                "Violation should name the template-like subgraph density problem.");
    }

    @Test
    public void candidateAtlasBusinessFlowGraphRejectsMetaTestVocabulary() throws IOException {
        Path atlas = writeFixture("business-flow-application-only", "meta-vocabulary-graph-atlas.html", metaVocabularyGraph());

        AtlasValidationResult result = validateFixture(atlas, new AtlasBusinessOnlyFlowRule());
        Assert.assertFalse(result.byCode(BUSINESS_FLOWS_APPLICATION_ONLY).isEmpty(),
                "Business Flow Graph legend must reject meta test-suite vocabulary.");
    }

    private static Path writeFixture(String directory, String fileName, String html) throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", directory);
        Path atlas = fixture.resolve(fileName);
        Files.createDirectories(fixture);
        Files.writeString(atlas, html);
        return atlas;
    }

    private static String supportingLineOnlyGraph() {
        return "<html><body>\n"
                + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                + "  <summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1180 560\" aria-label=\"Business Flow Graph\">\n"
                + "    <path class=\"edge\" d=\"M230 100 C295 82 330 82 395 100\"></path><text class=\"el\">opens account</text>\n"
                + "    <path class=\"edge\" d=\"M605 100 C680 78 725 78 790 100\"></path><text class=\"el\">retains profile</text>\n"
                + "    <path class=\"edge dashed\" d=\"M895 145 C960 190 970 230 900 255\"></path><text class=\"el\">exposes contact</text>\n"
                + "    <path class=\"edge\" d=\"M790 300 C710 338 650 338 605 300\"></path><text class=\"el\">bounds invoice</text>\n"
                + "    <path class=\"edge\" d=\"M500 345 C430 410 260 455 230 475\"></path><text class=\"el\">returns account state</text>\n"
                + nodeGroupWithSubtitle("Customer account", "transform=\"translate(40 55)\" data-maturity=\"diagnostic\"", "diagnostic maturity")
                + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(395 55)\" data-maturity=\"owned\" data-primary=\"true\"", "primary owned object")
                + nodeGroupWithSubtitle("Contact address", "transform=\"translate(790 55)\" data-maturity=\"owned\"", "owned maturity")
                + nodeGroupWithSubtitle("Messages area", "class=\"frontier\" transform=\"translate(790 255)\" data-maturity=\"frontier\"", "frontier maturity")
                + nodeGroupWithSubtitle("Invoice record", "transform=\"translate(395 255)\" data-maturity=\"partial\"", "partial boundary")
                + nodeGroupWithSubtitle("Account state", "transform=\"translate(40 430)\" data-maturity=\"diagnostic\"", "diagnostic maturity")
                + "  </svg></div>\n"
                + "</details>\n"
                + "<details><summary>3. Business Flow Understanding</summary>\n"
                + "  <details><summary>3.1 Account corridor</summary>\n"
                + "    <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1120 230\" aria-label=\"Account corridor flow\">\n"
                + "      <line class=\"edge\" x1=\"170\" y1=\"90\" x2=\"305\" y2=\"90\"></line>\n"
                + "      <line class=\"edge\" x1=\"465\" y1=\"90\" x2=\"600\" y2=\"90\"></line>\n"
                + "      <line class=\"edge\" x1=\"760\" y1=\"90\" x2=\"760\" y2=\"155\"></line>\n"
                + "      <text class=\"el\">opens account</text><text class=\"el\">retains profile</text><text class=\"el\">returns state</text>\n"
                + nodeGroupWithSubtitle("Actor customer", "transform=\"translate(40 50)\"", "registered user")
                + nodeGroupWithSubtitle("Intent account", "transform=\"translate(305 50)\"", "account intent")
                + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(600 50)\"", "business object")
                + nodeGroupWithSubtitle("Account workspace", "transform=\"translate(600 155)\"", "observable state")
                + "    </svg></div>\n"
                + "  </details>\n"
                + "</details>\n"
                + "</body></html>\n";
    }
}
