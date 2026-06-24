package com.endava.ai.mcp.rules;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContractSpec;

public interface McpValidationRule {
    void validate(McpPackageContext context, McpPackageValidationResult result);

    default McpContractSpec contractSpec() {
        return McpContractSpec.fromRule(this);
    }
}
