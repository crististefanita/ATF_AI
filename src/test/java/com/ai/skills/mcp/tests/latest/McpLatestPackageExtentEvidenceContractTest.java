package com.ai.skills.mcp.tests.latest;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-latest-package-extent-evidence-contract-test")
public class McpLatestPackageExtentEvidenceContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("run.extent.exists")
    public void everyRunHasExtentReport() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEveryRunHasExtentReport();
    }

    @Test
    @McpCheckRef("run.extent.no-external-screenshot-folder")
    public void extentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentReportsDoNotUseExternalScreenshotFolderReferences();
    }

    @Test
    @McpCheckRef("run.extent.final-state-screenshot")
    public void extentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots();
    }

    @Test
    @McpCheckRef("run.extent.generated.substantive")
    public void perRunExtentReportsAreGeneratedAndSubstantive() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsAreGeneratedAndSubstantive();
    }

    @Test
    @McpCheckRef("run.extent.single-file-proof")
    public void perRunExtentReportsAreSingleFileProofsWithoutSparkAssets() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets();
    }

    @Test
    @McpCheckRef("run.extent.maven-provenance")
    public void perRunExtentReportsHaveMavenProvenanceAndHashChain() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsHaveMavenProvenanceAndHashChain();
    }

    @Test
    @McpCheckRef("run.extent.no-manual-proof-injection")
    public void extentReportsHaveNoManualProofInjection() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentReportsHaveNoManualProofInjection();
    }

    @Test
    @McpCheckRef("run.extent.native-testlistener-provenance")
    public void perRunExtentReportsHaveNativeTestListenerProvenance() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsHaveNativeTestListenerProvenance();
    }

    @Test
    @McpCheckRef("mcp.extent.frozen-report.details")
    public void frozenMcpExtentReportContainsReadableContractDetails() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertFrozenMcpExtentReportContainsReadableContractDetails();
    }

    @Test
    @McpCheckRef("mcp.gate-result.freshness")
    public void frozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertFrozenMcpGateResultMatchesCurrentValidatorState();
    }

    @Test
    @McpCheckRef("run.extent.proof-class.scoring")
    public void extentProofClassIsPublishedAndReflectedInScoring() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentProofClassIsPublishedAndReflectedInScoring();
    }
}
