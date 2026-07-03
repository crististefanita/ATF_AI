package com.endava.ai.skills.mcp.tests.negative;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.validation.McpContractMetadataValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
@ValidatesMcpContract("generated-package-validator-metadata-contract")
@McpSuiteRef("mcp.suite.mcp-generated-package-validator-test")
public class McpGeneratedPackageValidatorTest {
    @Test
    @McpCheckRef("mcp.rule-metadata.catalog.negative")
    public void allDefaultRulesExposeMcpContractMetadata() {
        McpContractMetadataValidation.assertDefaultRulesExposeMcpContractMetadata();
    }
}
