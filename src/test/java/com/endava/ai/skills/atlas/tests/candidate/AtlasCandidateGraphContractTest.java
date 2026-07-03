package com.endava.ai.skills.atlas.tests.candidate;

import com.endava.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.endava.ai.skills.atlas.contract.AtlasSuiteIds;
import com.endava.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.endava.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_GRAPH)
public class AtlasCandidateGraphContractTest extends AtlasContractTestSupport {
    @Test
    public void candidateAtlasBusinessGraphHasCollapsedLegend() throws IOException {
        assertCandidateRule(BUSINESS_GRAPH_LEGEND);
    }

    @Test
    public void candidateAtlasSupportingGraphsHaveLegends() throws IOException {
        assertCandidateRule(SUPPORTING_GRAPHS_LEGENDS);
    }

    @Test
    public void candidateAtlasGraphUsesHtmlExamplePrimitives() throws IOException {
        assertCandidateRule(GRAPH_REFERENCE_PRIMITIVES);
    }

    @Test
    public void candidateAtlasGraphCssIsReusable() throws IOException {
        assertCandidateRule(GRAPH_CSS_REUSABLE);
    }

    @Test
    public void candidateAtlasEachGraphUsesMinimalHtmlExampleStyle() throws IOException {
        assertCandidateRule(GRAPHS_MINIMAL_STYLE_PER_SECTION);
    }

    @Test
    public void candidateAtlasGraphsKeepConsistentVisualScale() throws IOException {
        assertCandidateRule(GRAPHS_VISUAL_SCALE);
    }

    @Test
    public void candidateAtlasBusinessSubgraphsUseFullWidthRhythm() throws IOException {
        assertCandidateRule(BUSINESS_SUBGRAPHS_FULL_WIDTH);
    }

    @Test
    public void candidateAtlasLegendsAreCollapsedByDefault() throws IOException {
        assertCandidateRule(LEGENDS_COLLAPSED);
    }

    @Test
    public void candidateAtlasLegendsExplainBusinessValueWithBullets() throws IOException {
        assertCandidateRule(LEGENDS_BULLETED_QUALITY);
    }

    @Test
    public void candidateAtlasGraphNodesHaveBusinessProofStrips() throws IOException {
        assertCandidateRule(GRAPH_NODE_BUSINESS_PROOF_STRIP);
    }

    @Test
    public void candidateAtlasMainGraphNodesHaveMaturityProofRows() throws IOException {
        assertCandidateRule(MAIN_GRAPH_NODE_MATURITY_PROOF);
    }

    @Test
    public void candidateAtlasGraphProofStripsStayBelowCollapsedLegends() throws IOException {
        assertCandidateRule(GRAPH_PROOF_STRIP_PLACEMENT);
    }

    @Test
    public void candidateAtlasEachSubgraphHasLocalBusinessUnderstandingStrip() throws IOException {
        assertCandidateRule(SUBGRAPH_UNDERSTANDING_STRIPS);
    }

    @Test
    public void candidateAtlasSubgraphsHaveDomainSpecificNodeDensity() throws IOException {
        assertCandidateRule(SUBGRAPH_SEMANTIC_DENSITY);
    }

    @Test
    public void candidateAtlasSubgraphsReflectDominantSourceFamilies() throws IOException {
        assertCandidateRule(SUBGRAPHS_SOURCE_SPECIFIC_DOMINANCE);
    }

    @Test
    public void candidateAtlasLocalProofStripsAreBackedBySource() throws IOException {
        assertCandidateRule(LOCAL_PROOF_STRIP_SOURCE_BACKED);
    }

    @Test
    public void candidateAtlasBusinessFlowsExcludeMetaContractGraphs() throws IOException {
        assertCandidateRule(BUSINESS_FLOWS_APPLICATION_ONLY);
    }

}
