package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageArtifactAuditTest {
    @Test
    public void runtimeValuesArePublishedFromAgentRuntime() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRuntimeValuesPublished();
    }

    @Test
    public void runtimeReferencedPromptDocumentsExist() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRuntimeReferencedPromptDocumentsExist();
    }

    @Test
    public void packageAccountingMatchesRuntimeWindow() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPackageAccountingMatchesRuntimeWindow();
    }

    @Test
    public void packageLevelArtifactsExistAndAreNotEmpty() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPackageLevelArtifactsExistAndAreNotEmpty();
    }

    @Test
    public void readmeMatchesPackageStateTruth() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertReadmeMatchesPackageStateTruth();
    }

    @Test
    public void rootDiffReferencesExistingSourceFiles() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRootDiffReferencesExistingSourceFiles();
    }

    @Test
    public void claimedCommercialAbsenceHasUiAssertion() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertClaimedCommercialAbsenceHasUiAssertion();
    }

    @Test
    public void pretrainingIntakeQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPretrainingIntakeQuality();
    }

    @Test
    public void uiWrapperHandoffQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertUiWrapperHandoffQuality();
    }

    @Test
    public void claimToEvidenceChainIsComplete() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertClaimToEvidenceChainIsComplete();
    }

    @Test
    public void scoreCapIsConsistentWithWeakestAxis() throws IOException {
        McpPackageAuditValidation.latestPackage().score().assertScoreCapIsConsistentWithWeakestAxis();
    }

    @Test
    public void reportStateArtifactsTellSameTruth() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertReportStateArtifactsTellSameTruth();
    }

    @Test
    public void decisionTraceIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertDecisionTraceIsAuditable();
    }

    @Test
    public void artifactFreshnessIsCoherent() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertArtifactFreshnessIsCoherent();
    }

    @Test
    public void fullMcpGateUsesCompleteSuite() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertFullMcpGateUsesCompleteSuite();
    }

    @Test
    public void publishedArtifactsHaveNoTemplateNoise() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPublishedArtifactsHaveNoTemplateNoise();
    }

    @Test
    public void learningTraceQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertLearningTraceQualityIsAuditable();
    }

    @Test
    public void finalReportAvoidsUnverifiableLanguage() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertFinalReportAvoidsUnverifiableLanguage();
    }

    @Test
    public void runWindowAndRunArtifactsExist() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRunWindowAndRunArtifactsExist();
    }

    @Test
    public void runStatesExposeTestsAndBusinessVerdict() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRunStatesExposeTestsAndBusinessVerdict();
    }

    @Test
    public void finalReportHasRequiredBusinessStructure() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportHasRequiredBusinessStructure();
    }

    @Test
    public void finalReportLocalHyperlinksResolve() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLocalHyperlinksResolve();
    }
}
