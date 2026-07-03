package com.endava.ai.skills.mcp.tests.contracts;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.skills.mcp.support.contracts.McpYamlCheckRefCatalogAssertions;
import com.endava.ai.skills.mcp.support.contracts.McpYamlCheckRefEvidenceAssertions;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-yaml-check-ref-contract-test")
public class McpYamlCheckRefContractTest {

    @Test
    @McpCheckRef("mcp.yaml-contract.check-ref.schema")
    public void yamlCheckRefsHaveCompleteSchema() {
        McpYamlCheckRefCatalogAssertions.assertCheckRefsHaveCompleteSchema();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.check-ref.resource-registry")
    public void everyMcpCheckYamlResourceIsLoadedByCatalog() throws IOException {
        McpYamlCheckRefCatalogAssertions.assertEveryMcpCheckYamlResourceIsLoadedByCatalog();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.check-ref.source-scan")
    public void everyJavaMcpCheckRefResolvesToYamlMetadata() throws IOException {
        McpYamlCheckRefCatalogAssertions.assertEveryJavaMcpCheckRefResolvesToYamlMetadata();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.check-ref.no-inline-mcp-check")
    public void mcpTestsDoNotUseInlineMcpCheckNarratives() throws IOException {
        McpYamlCheckRefCatalogAssertions.assertMcpTestsDoNotUseInlineMcpCheckNarratives();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.suite-ref.schema")
    public void yamlSuiteRefsHaveCompleteSchema() {
        McpYamlCheckRefCatalogAssertions.assertSuiteRefsHaveCompleteSchema();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.suite-ref.source-scan")
    public void everyJavaMcpSuiteRefResolvesToYamlMetadata() throws IOException {
        McpYamlCheckRefCatalogAssertions.assertEveryJavaMcpSuiteRefResolvesToYamlMetadata();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.suite-ref.no-inline-mcp-suite")
    public void mcpTestsDoNotUseInlineMcpSuiteNarratives() throws IOException {
        McpYamlCheckRefCatalogAssertions.assertMcpTestsDoNotUseInlineMcpSuiteNarratives();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.suite-ref.testng-profile-coverage")
    public void mcpTestNgProfileClassesHaveYamlSuiteMetadata() {
        McpYamlCheckRefCatalogAssertions.assertMcpTestNgProfileClassesHaveYamlSuiteMetadata();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.suite-ref.testng-profile-coverage")
    public void yamlSuiteClassNamesPointBackToMatchingSuiteRefs() {
        McpYamlCheckRefCatalogAssertions.assertYamlSuiteClassNamesPointBackToMatchingSuiteRefs();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.check-ref.evidence-rendering")
    public void everyJavaMcpCheckRefAppearsInMatrixAndFinalReportSection() throws IOException {
        McpYamlCheckRefEvidenceAssertions.assertEveryJavaMcpCheckRefAppearsInMatrixAndFinalReportSection();
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.governance-map.evidence-rendering")
    public void mcpGovernanceMapConnectsTestsYamlArtifactsAndFinalReportEvidence() throws IOException {
        McpYamlCheckRefEvidenceAssertions.assertMcpGovernanceMapConnectsTestsYamlArtifactsAndFinalReportEvidence();
    }
}
