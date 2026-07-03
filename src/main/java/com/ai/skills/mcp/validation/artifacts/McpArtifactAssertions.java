package com.ai.skills.mcp.validation.artifacts;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;

public final class McpArtifactAssertions {
    private final McpPackageSpineAssertions packageSpine;
    private final McpBusinessEvidenceAssertions businessEvidence;
    private final McpCloseoutArtifactAssertions closeoutArtifacts;

    public McpArtifactAssertions(McpPackageAuditValidation audit) {
        this.packageSpine = new McpPackageSpineAssertions(audit);
        this.businessEvidence = new McpBusinessEvidenceAssertions(audit);
        this.closeoutArtifacts = new McpCloseoutArtifactAssertions(audit);
    }

    public void assertCompletePackageHasNoBlockers() {
        closeoutArtifacts.assertCompletePackageHasNoBlockers();
    }

    public void assertCompletePackageHasNoNonRegenerableBlockers() {
        closeoutArtifacts.assertCompletePackageHasNoNonRegenerableBlockers();
    }

    public void assertRuntimeValuesPublished() {
        packageSpine.assertRuntimeValuesPublished();
    }

    public void assertRuntimeReferencedPromptDocumentsExist() {
        packageSpine.assertRuntimeReferencedPromptDocumentsExist();
    }

    public void assertPackageAccountingMatchesRuntimeWindow() {
        packageSpine.assertPackageAccountingMatchesRuntimeWindow();
    }

    public void assertPackageLevelArtifactsExistAndAreNotEmpty() {
        packageSpine.assertPackageLevelArtifactsExistAndAreNotEmpty();
    }

    public void assertReadmeMatchesPackageStateTruth() {
        packageSpine.assertReadmeMatchesPackageStateTruth();
    }

    public void assertRootDiffReferencesExistingSourceFiles() {
        businessEvidence.assertRootDiffReferencesExistingSourceFiles();
    }

    public void assertSourceEvolutionLedgerIsAuditable() {
        businessEvidence.assertSourceEvolutionLedgerIsAuditable();
    }

    public void assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers() {
        businessEvidence.assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers();
    }

    public void assertClaimedCommercialAbsenceHasUiAssertion() {
        businessEvidence.assertClaimedCommercialAbsenceHasUiAssertion();
    }

    public void assertPretrainingIntakeQuality() {
        businessEvidence.assertPretrainingIntakeQuality();
    }

    public void assertUiWrapperHandoffQuality() {
        businessEvidence.assertUiWrapperHandoffQuality();
    }

    public void assertClaimToEvidenceChainIsComplete() {
        businessEvidence.assertClaimToEvidenceChainIsComplete();
    }

    public void assertReportStateArtifactsTellSameTruth() {
        closeoutArtifacts.assertReportStateArtifactsTellSameTruth();
    }

    public void assertDecisionTraceIsAuditable() {
        closeoutArtifacts.assertDecisionTraceIsAuditable();
    }

    public void assertArtifactFreshnessIsCoherent() {
        closeoutArtifacts.assertArtifactFreshnessIsCoherent();
    }

    public void assertFullMcpGateUsesCompleteSuite() {
        closeoutArtifacts.assertFullMcpGateUsesCompleteSuite();
    }

    public void assertPublishedArtifactsHaveNoTemplateNoise() {
        closeoutArtifacts.assertPublishedArtifactsHaveNoTemplateNoise();
    }

    public void assertLearningTraceQualityIsAuditable() {
        businessEvidence.assertLearningTraceQualityIsAuditable();
    }

    public void assertFinalReportAvoidsUnverifiableLanguage() {
        businessEvidence.assertFinalReportAvoidsUnverifiableLanguage();
    }

    public void assertRunWindowAndRunArtifactsExist() {
        packageSpine.assertRunWindowAndRunArtifactsExist();
    }

    public void assertRunStatesExposeTestsAndBusinessVerdict() {
        packageSpine.assertRunStatesExposeTestsAndBusinessVerdict();
    }
}
