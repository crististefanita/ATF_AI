package com.ai.skills.atlas.tests.candidate;

import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_SCORE)
public class AtlasCandidateScoreContractTest extends AtlasContractTestSupport {
    @Test
    public void candidateAtlasHasCanonicalScoreCards() throws IOException {
        assertCandidateRule(SCORE_CARDS_SHAPE);
    }

    @Test
    public void candidateAtlasScoreAxisNamesAreConsistent() throws IOException {
        assertCandidateRule(SCORE_AXES_CONSISTENT);
    }

    @Test
    public void candidateAtlasScoreCardsRemainReadable() throws IOException {
        assertCandidateRule(SCORE_CARDS_READABLE);
    }

    @Test
    public void candidateAtlasScoreOpeningPointsToAuditSections() throws IOException {
        assertCandidateRule(SCORE_OPENING_AUDIT_POINTER);
    }

    @Test
    public void candidateAtlasScoreRationaleIsAuditable() throws IOException {
        assertCandidateRule(SCORE_RATIONALE_AUDIT);
    }

    @Test
    public void candidateAtlasScoreCardsHaveSupportTrace() throws IOException {
        assertCandidateRule(SCORE_SUPPORT_TRACE);
    }

    @Test
    public void candidateAtlasScoreFormulaIsTraceable() throws IOException {
        assertCandidateRule(SCORE_FORMULA_TRACE);
    }

    @Test
    public void candidateAtlasScoreUsesGoalQuestionEvidenceChain() throws IOException {
        assertCandidateRule(SCORE_GQM_TRACEABILITY);
    }

    @Test
    public void candidateAtlasUniformScoresExposeSharedCap() throws IOException {
        assertCandidateRule(SCORE_UNIFORMITY_CAP);
    }

    @Test
    public void candidateAtlasEndToEndLimitsCapOverallScore() throws IOException {
        assertCandidateRule(END_TO_END_LIMIT_SCORE_CAP);
    }

    @Test
    public void candidateAtlasExcellentOverallRequiresLimitDiscipline() throws IOException {
        assertCandidateRule(LIMIT_DISCIPLINE_95_PLUS);
    }

    @Test
    public void candidateAtlasSeparatesSuiteCompletenessFromAtlasQuality() throws IOException {
        assertCandidateRule(SUITE_COMPLETENESS_SEPARATION);
    }

    @Test
    public void candidateAtlasOverallScoreIsCalculatedFromAxes() throws IOException {
        assertCandidateRule(SCORE_OVERALL_CALCULATION);
    }

    @Test
    public void candidateAtlasScoreCardsAreDocumentedInRequirements() throws IOException {
        assertCandidateRule(SCORE_CARDS_DOCUMENTED);
    }

    @Test
    public void candidateAtlasScoreBusinessUnderstandingIsEvidenceBacked() throws IOException {
        assertCandidateRule(SCORE_BUSINESS_EVIDENCE);
    }
}
