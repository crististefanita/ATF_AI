package com.endava.ai.skills.mcp.validation.report;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.report.evidence.McpReportFrozenMcpEvidenceAssertions;
import com.endava.ai.skills.mcp.validation.report.evidence.McpReportGovernanceEvidenceAssertions;
import com.endava.ai.skills.mcp.validation.report.evidence.McpReportProofEvidenceAssertions;
import com.endava.ai.skills.mcp.validation.report.evidence.McpReportRunEvidenceAssertions;

import java.io.IOException;

public final class McpReportEvidenceAssertions {
    private final McpReportProofEvidenceAssertions proofEvidence;
    private final McpReportFrozenMcpEvidenceAssertions frozenMcpEvidence;
    private final McpReportRunEvidenceAssertions runEvidence;
    private final McpReportGovernanceEvidenceAssertions governanceEvidence;

    public McpReportEvidenceAssertions(McpPackageAuditValidation audit) {
        this.proofEvidence = new McpReportProofEvidenceAssertions(audit);
        this.frozenMcpEvidence = new McpReportFrozenMcpEvidenceAssertions(audit);
        this.runEvidence = new McpReportRunEvidenceAssertions(audit);
        this.governanceEvidence = new McpReportGovernanceEvidenceAssertions(audit);
    }

    public void assertFinalReportProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        proofEvidence.assertProofMappingLinksAllMandatoryArtifactFamilies();
    }

    public void assertFinalReportProofMappingUsesBalancedTableColumns() throws IOException {
        proofEvidence.assertProofMappingUsesBalancedTableColumns();
    }

    public void assertFinalReportRunAuditUsesStructuredEvidenceRows() throws IOException {
        proofEvidence.assertRunAuditUsesStructuredEvidenceRows();
    }

    public void assertFinalReportArtifactIndexUsesGroupedEvidenceFamilies() throws IOException {
        proofEvidence.assertArtifactIndexUsesGroupedEvidenceFamilies();
    }

    public void assertFinalReportLinksFrozenMcpContractEvidence() throws IOException {
        frozenMcpEvidence.assertLinksFrozenMcpContractEvidence();
    }

    public void assertFinalReportLinksAuditedFrameworkPropertiesSnapshot() throws IOException {
        proofEvidence.assertLinksAuditedFrameworkPropertiesSnapshot();
    }

    public void assertFinalReportLinksEveryRunEvidenceFamily() throws IOException {
        runEvidence.assertLinksEveryRunEvidenceFamily();
    }

    public void assertFinalReportLinksPackageGovernanceArtifacts() throws IOException {
        governanceEvidence.assertLinksPackageGovernanceArtifacts();
    }

    public void assertFinalReportPublishesAiTaskAdvisorSection() throws IOException {
        governanceEvidence.assertPublishesAiTaskAdvisorSection();
    }
}
