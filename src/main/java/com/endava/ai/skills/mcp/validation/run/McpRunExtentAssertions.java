package com.endava.ai.skills.mcp.validation.run;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentPortabilityAssertions;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentPresenceAssertions;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentProvenanceAssertions;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentScreenshotAssertions;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentXmlAlignmentAssertions;
import com.endava.ai.skills.mcp.validation.run.extent.McpGateEvidenceAssertions;

import java.io.IOException;

public final class McpRunExtentAssertions {
    private final McpExtentPresenceAssertions presence;
    private final McpExtentScreenshotAssertions screenshots;
    private final McpExtentPortabilityAssertions portability;
    private final McpGateEvidenceAssertions gateEvidence;
    private final McpExtentProvenanceAssertions provenance;

    public McpRunExtentAssertions(McpPackageAuditValidation audit) {
        McpExtentXmlAlignmentAssertions xmlAlignment = new McpExtentXmlAlignmentAssertions(audit);
        this.portability = new McpExtentPortabilityAssertions(audit);
        this.presence = new McpExtentPresenceAssertions(audit, xmlAlignment, portability);
        this.screenshots = new McpExtentScreenshotAssertions(audit);
        this.gateEvidence = new McpGateEvidenceAssertions(audit);
        this.provenance = new McpExtentProvenanceAssertions(audit);
    }

    public void assertEveryRunHasExtentReport() throws IOException {
        presence.assertEveryRunHasExtentReport();
    }

    public void assertExtentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
        screenshots.assertExtentReportsDoNotUseExternalScreenshotFolderReferences();
    }

    public void assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots() throws IOException {
        screenshots.assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots();
    }

    public void assertPerRunExtentReportsAreGeneratedAndSubstantive() throws IOException {
        presence.assertPerRunExtentReportsAreGeneratedAndSubstantive();
    }

    public void assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets() throws IOException {
        portability.assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets();
    }

    public void assertPerRunExtentReportsHaveMavenProvenanceAndHashChain() throws IOException {
        provenance.assertPerRunExtentReportsHaveMavenProvenanceAndHashChain();
    }

    public void assertExtentReportsHaveNoManualProofInjection() throws IOException {
        provenance.assertExtentReportsHaveNoManualProofInjection();
    }

    public void assertPerRunExtentReportsHaveNativeTestListenerProvenance() throws IOException {
        provenance.assertPerRunExtentReportsHaveNativeTestListenerProvenance();
    }

    public void assertFrozenMcpExtentReportContainsReadableContractDetails() throws IOException {
        gateEvidence.assertFrozenMcpExtentReportContainsReadableContractDetails();
    }

    public void assertFrozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
        gateEvidence.assertFrozenMcpGateResultMatchesCurrentValidatorState();
    }

    public void assertExtentProofClassIsPublishedAndReflectedInScoring() throws IOException {
        gateEvidence.assertExtentProofClassIsPublishedAndReflectedInScoring();
    }
}
