package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageExtentEvidenceContractTest {
    @Test
    public void everyRunHasExtentReport() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEveryRunHasExtentReport();
    }

    @Test
    public void extentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentReportsDoNotUseExternalScreenshotFolderReferences();
    }

    @Test
    public void extentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots();
    }

    @Test
    public void perRunExtentReportsAreGeneratedAndSubstantive() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsAreGeneratedAndSubstantive();
    }

    @Test
    public void perRunExtentReportsAreSingleFileProofsWithoutSparkAssets() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets();
    }

    @Test
    public void frozenMcpExtentReportContainsReadableContractDetails() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertFrozenMcpExtentReportContainsReadableContractDetails();
    }

    @Test
    public void frozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertFrozenMcpGateResultMatchesCurrentValidatorState();
    }

    @Test
    public void extentProofClassIsPublishedAndReflectedInScoring() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertExtentProofClassIsPublishedAndReflectedInScoring();
    }
}
