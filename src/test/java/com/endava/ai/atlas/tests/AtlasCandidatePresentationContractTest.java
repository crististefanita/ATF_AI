package com.endava.ai.atlas.tests;

import com.endava.ai.atlas.validation.AtlasAuditValidation;
import com.endava.ai.core.listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class AtlasCandidatePresentationContractTest {
    @Test
    public void candidateRuleMatrixIsPublished() throws IOException {
        AtlasAuditValidation.generatedAtlas().publishCandidateRuleMatrix();
    }

    @Test
    public void candidateAtlasHasCanonicalScoreCards() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-cards.shape");
    }

    @Test
    public void candidateAtlasExecutiveContextIsCollapsedAndStructured() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.executive-context.collapsed");
    }

    @Test
    public void candidateAtlasBusinessGraphHasCollapsedLegend() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.business-graph.legend");
    }

    @Test
    public void candidateAtlasSupportingGraphsHaveLegends() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.supporting-graphs.legends");
    }

    @Test
    public void candidateAtlasGraphUsesHtmlExamplePrimitives() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.graph.reference-primitives");
    }

    @Test
    public void candidateAtlasGraphCssIsReusable() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.graph.css-reusable");
    }

    @Test
    public void candidateAtlasEachGraphUsesMinimalHtmlExampleStyle() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.graphs.minimal-style-per-section");
    }

    @Test
    public void candidateAtlasGraphsKeepConsistentVisualScale() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.graphs.visual-scale");
    }

    @Test
    public void candidateAtlasBusinessSubgraphsUseFullWidthRhythm() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.business-subgraphs.full-width");
    }

    @Test
    public void candidateAtlasLegendsAreCollapsedByDefault() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.legends.collapsed");
    }

    @Test
    public void candidateAtlasLegendsExplainBusinessValueWithBullets() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.legends.bulleted-quality");
    }

    @Test
    public void candidateAtlasBusinessFlowsExcludeMetaContractGraphs() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.business-flows.application-only");
    }

    @Test
    public void candidateAtlasBusinessSubgraphsAreCollapsedByDefault() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.business-subgraphs.collapsed");
    }

    @Test
    public void candidateAtlasScoreAxisNamesAreConsistent() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-axes.consistent");
    }

    @Test
    public void candidateAtlasScoreCardsRemainReadable() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-cards.readable");
    }

    @Test
    public void candidateAtlasScoreOpeningPointsToAuditSections() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-opening.audit-pointer");
    }

    @Test
    public void candidateAtlasScoreRationaleIsAuditable() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-rationale.audit");
    }

    @Test
    public void candidateAtlasScoreCardsHaveSupportTrace() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-support.trace");
    }

    @Test
    public void candidateAtlasScoreFormulaIsTraceable() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-formula.trace");
    }

    @Test
    public void candidateAtlasOverallScoreIsCalculatedFromAxes() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-overall.calculation");
    }

    @Test
    public void candidateAtlasScoreCardsAreDocumentedInRequirements() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-cards.documented");
    }

    @Test
    public void candidateAtlasScoreEvidenceIsTraceable() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.score-evidence.trace");
    }

    @Test
    public void candidateAtlasTestToBusinessMappingIsDeepEnough() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.mapping.depth");
    }

    @Test
    public void candidateAtlasSectionsAreNumberedForNavigation() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.sections.numbered");
    }

    @Test
    public void candidateAtlasSectionNumbersStayInTitlesNotHints() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.sections.number-placement");
    }

    @Test
    public void candidateAtlasTopLevelSectionsRemainContinuous() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.sections.top-level-continuity");
    }

    @Test
    public void candidateAtlasFlowsTellCompleteProductStories() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.flow-story.complete");
    }

    @Test
    public void candidateAtlasContainsLayerTraceMap() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.layer-trace-map.complete");
    }

    @Test
    public void candidateAtlasLayerTraceNamesConcreteSourceEvidence() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.layer-trace.concrete-evidence");
    }

    @Test
    public void candidateAtlasSourceTraceContainsMethodLevelEvidence() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.source-trace.method-level");
    }

    @Test
    public void candidateAtlasSourceTraceIsBackedByAnalyzedSource() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.source-trace.backed-by-source");
    }

    @Test
    public void candidateAtlasSourceTraceContainsOnlyApplicationBusinessRows() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.source-trace.application-business-only");
    }

    @Test
    public void candidateAtlasSubgraphsAreBusinessComplexEnough() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.subgraphs.business-complexity");
    }

    @Test
    public void candidateAtlasGraphsStayBusinessObjectCentered() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.business-object-centered");
    }

    @Test
    public void candidateAtlasKeepsComprehensionLimitsSecondary() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.comprehension-limits.secondary");
    }

    @Test
    public void candidateAtlasUsesBusinessComprehensionVocabulary() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.vocabulary.business-comprehension");
    }

    @Test
    public void candidateAtlasPublishesAtlasTestAuditExtent() throws IOException {
        AtlasAuditValidation.generatedAtlas()
                .assertCandidateRulePasses("atlas.candidate.test-audit.extent-linked");
    }
}
