package com.endava.ai.skills.mcp.tests.latest;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-business-evidence-contract-test")
public class McpBusinessEvidenceContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("root-diff.source-files.exist")
    public void rootDiffReferencesExistingSourceFiles() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRootDiffReferencesExistingSourceFiles();
    }

    @Test
    @McpCheckRef("source.evolution.ledger.auditable")
    public void sourceEvolutionLedgerIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertSourceEvolutionLedgerIsAuditable();
    }

    @Test
    @McpCheckRef("claimed-absence.requires-ui-assertion")
    public void claimedCommercialAbsenceHasUiAssertion() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertClaimedCommercialAbsenceHasUiAssertion();
    }

    @Test
    @McpCheckRef("pretraining.intake.quality")
    public void pretrainingIntakeQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPretrainingIntakeQuality();
    }

    @Test
    @McpCheckRef("ui-wrapper.handoff.quality")
    public void uiWrapperHandoffQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertUiWrapperHandoffQuality();
    }

    @Test
    @McpCheckRef("claim-to-evidence.chain")
    public void claimToEvidenceChainIsComplete() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertClaimToEvidenceChainIsComplete();
    }

    @Test
    @McpCheckRef("learning.trace.quality")
    public void learningTraceQualityIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertLearningTraceQualityIsAuditable();
    }

    @Test
    @McpCheckRef("final-report.no-unverifiable-language")
    public void finalReportAvoidsUnverifiableLanguage() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertFinalReportAvoidsUnverifiableLanguage();
    }
}
