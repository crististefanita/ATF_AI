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
@McpSuiteRef("mcp.suite.mcp-runtime-and-package-contract-test")
public class McpRuntimeAndPackageContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("runtime.values.published")
    public void runtimeValuesArePublishedFromAgentRuntime() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRuntimeValuesPublished();
    }

    @Test
    @McpCheckRef("runtime.prompt-documents.exist")
    public void runtimeReferencedPromptDocumentsExist() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRuntimeReferencedPromptDocumentsExist();
    }

    @Test
    @McpCheckRef("package.accounting.runtime-window")
    public void packageAccountingMatchesRuntimeWindow() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPackageAccountingMatchesRuntimeWindow();
    }

    @Test
    @McpCheckRef("package.artifact-spine.exists")
    public void packageLevelArtifactsExistAndAreNotEmpty() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPackageLevelArtifactsExistAndAreNotEmpty();
    }

    @Test
    @McpCheckRef("readme.package-state.sync")
    public void readmeMatchesPackageStateTruth() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertReadmeMatchesPackageStateTruth();
    }

    @Test
    @McpCheckRef("report-state.same-truth")
    public void reportStateArtifactsTellSameTruth() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertReportStateArtifactsTellSameTruth();
    }

    @Test
    @McpCheckRef("decision.trace.auditable")
    public void decisionTraceIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertDecisionTraceIsAuditable();
    }

    @Test
    @McpCheckRef("artifact.freshness.coherent")
    public void artifactFreshnessIsCoherent() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertArtifactFreshnessIsCoherent();
    }

    @Test
    @McpCheckRef("mcp.gate.package-closeout")
    public void fullMcpGateUsesCompleteSuite() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertFullMcpGateUsesCompleteSuite();
    }

    @Test
    @McpCheckRef("published-artifacts.no-template-noise")
    public void publishedArtifactsHaveNoTemplateNoise() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertPublishedArtifactsHaveNoTemplateNoise();
    }
}
