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
@McpSuiteRef("mcp.suite.mcp-latest-package-architecture-contract-test")
public class McpLatestPackageArchitectureContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("source.framework-protection")
    public void generatedPackageDoesNotMutateProtectedFrameworkLayers() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers();
    }

    @Test
    public void diffTouchedUiTestsRespectLivingArchitecture() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertDiffTouchedUiTestsRespectLivingArchitecture();
    }
}
