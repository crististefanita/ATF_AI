package com.ai.skills.mcp.tests.latest.report;

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
@McpSuiteRef("mcp.suite.mcp-latest-package-html-report-contract-test")
public class McpLatestPackageHtmlReportContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("report.final.business-structure")
    public void finalReportHasRequiredBusinessStructure() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportHasRequiredBusinessStructure();
    }

    @Test
    public void finalReportContainsCanonicalBusinessSections() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportContainsCanonicalBusinessSections();
    }

    @Test
    public void finalReportPublishesRuntimeConstantsAndCounters() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportPublishesRuntimeConstantsAndCounters();
    }

    @Test
    public void finalReportLinksEveryRunEvidenceFamily() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksEveryRunEvidenceFamily();
    }

    @Test
    @McpCheckRef("report.governance-evidence.final-links")
    public void finalReportLinksPackageGovernanceArtifacts() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksPackageGovernanceArtifacts();
    }

    @Test
    @McpCheckRef("report.ai-task-advisor.visible-nonblocking")
    public void finalReportPublishesAiTaskAdvisorSection() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportPublishesAiTaskAdvisorSection();
    }

    @Test
    public void finalReportDoesNotLeakExternalCalibrationSources() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportDoesNotLeakExternalCalibrationSources();
    }
}
