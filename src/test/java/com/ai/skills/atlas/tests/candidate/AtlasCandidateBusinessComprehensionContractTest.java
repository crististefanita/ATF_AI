package com.ai.skills.atlas.tests.candidate;

import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.business.AtlasSubgraphBusinessComplexityRule;
import com.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;
import static com.ai.skills.atlas.tests.candidate.AtlasCandidateGraphFixtures.validateFixture;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_BUSINESS_COMPREHENSION)
public class AtlasCandidateBusinessComprehensionContractTest extends AtlasContractTestSupport {
    @Test
    public void candidateAtlasTestToBusinessMappingIsDeepEnough() throws IOException {
        assertCandidateRule(MAPPING_DEPTH);
    }

    @Test
    public void candidateAtlasFlowsTellCompleteProductStories() throws IOException {
        assertCandidateRule(FLOW_STORY_COMPLETE);
    }

    @Test
    public void candidateAtlasContainsLayerTraceMap() throws IOException {
        assertCandidateRule(LAYER_TRACE_MAP_COMPLETE);
    }

    @Test
    public void candidateAtlasLayerTraceNamesConcreteSourceEvidence() throws IOException {
        assertCandidateRule(LAYER_TRACE_CONCRETE_EVIDENCE);
    }

    @Test
    public void candidateAtlasSourceTraceContainsMethodLevelEvidence() throws IOException {
        assertCandidateRule(SOURCE_TRACE_METHOD_LEVEL);
    }

    @Test
    public void candidateAtlasSourceTraceIsBackedByAnalyzedSource() throws IOException {
        assertCandidateRule(SOURCE_TRACE_BACKED_BY_SOURCE);
    }

    @Test
    public void candidateAtlasSourceTraceContainsOnlyApplicationBusinessRows() throws IOException {
        assertCandidateRule(SOURCE_TRACE_APPLICATION_BUSINESS);
    }

    @Test
    public void candidateAtlasSubgraphsAreBusinessComplexEnough() throws IOException {
        assertCandidateRule(SUBGRAPHS_BUSINESS_COMPLEXITY);
    }

    @Test
    public void candidateAtlasSubgraphsRejectActorIntentTemplates() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "subgraph-business-complexity");
        Path atlas = fixture.resolve("actor-intent-template-subgraph.html");
        Files.createDirectories(fixture);
        Files.writeString(atlas,
                "<html><body>\n"
                        + "<details class=\"subdetails\"><summary>3.1 ProfileAccount identity lifecycle</summary>\n"
                        + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1120 160\" aria-label=\"ProfileAccount flow\">\n"
                        + "    <path class=\"edge\" d=\"M170 80 H305\"></path>\n"
                        + "    <path class=\"edge\" d=\"M465 80 H600\"></path>\n"
                        + "    <path class=\"edge\" d=\"M760 80 H900\"></path>\n"
                        + "    <g transform=\"translate(40 50)\"><rect class=\"node\"></rect><text class=\"nt\">Actor customer</text><text class=\"ns\">opens profile</text></g>\n"
                        + "    <g transform=\"translate(305 50)\"><rect class=\"node\"></rect><text class=\"nt\">Intent identity</text><text class=\"ns\">verify registered data</text></g>\n"
                        + "    <g transform=\"translate(600 50)\"><rect class=\"node\"></rect><text class=\"nt\">Profile identity</text><text class=\"ns\">phone and address</text></g>\n"
                        + "    <g transform=\"translate(860 50)\"><rect class=\"node\"></rect><text class=\"nt\">Profile memory</text><text class=\"ns\">registered fields kept</text></g>\n"
                        + "  </svg></div>\n"
                        + "</details>\n"
                        + "</body></html>\n");

        AtlasValidationResult result = validateFixture(atlas, new AtlasSubgraphBusinessComplexityRule());
        Assert.assertTrue(result.byCode(SUBGRAPHS_BUSINESS_COMPLEXITY).stream()
                        .anyMatch(violation -> violation.toString().contains("template role nodes")
                                && violation.toString().contains("actor customer")
                                && violation.toString().contains("intent identity")),
                "Subgraphs must not pass with Actor/Intent template nodes instead of concrete business objects/states.");
        Assert.assertTrue(result.byCode(SUBGRAPHS_BUSINESS_COMPLEXITY).stream()
                        .anyMatch(violation -> violation.toString().contains("not object-rich enough")),
                "Template Actor/Intent nodes must not count as concrete business object depth.");
    }

    @Test
    public void candidateAtlasGraphsStayBusinessObjectCentered() throws IOException {
        assertCandidateRule(BUSINESS_OBJECT_CENTERED);
    }

    @Test
    public void candidateAtlasHasObjectMaturityMatrix() throws IOException {
        assertCandidateRule(OBJECT_MATURITY_MATRIX);
    }

    @Test
    public void candidateAtlasShowsEndToEndClosureLadder() throws IOException {
        assertCandidateRule(END_TO_END_CLOSURE_LADDER);
    }

    @Test
    public void candidateAtlasExplainsFlowMaturityDistribution() throws IOException {
        assertCandidateRule(FLOW_MATURITY_DISTRIBUTION);
    }

    @Test
    public void candidateAtlasDoesNotSellPartialFlowsAsOwned() throws IOException {
        assertCandidateRule(PARTIAL_FLOW_NOT_SOLD_AS_OWNED);
    }

    @Test
    public void candidateAtlasKeepsComprehensionLimitsSecondary() throws IOException {
        assertCandidateRule(COMPREHENSION_LIMITS_SECONDARY);
    }

    @Test
    public void candidateAtlasUsesBusinessComprehensionVocabulary() throws IOException {
        assertCandidateRule(BUSINESS_COMPREHENSION_VOCABULARY);
    }
}
