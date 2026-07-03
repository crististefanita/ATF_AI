package com.ai.skills.mcp.rules;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContractSpec;
import com.ai.skills.mcp.contract.McpRuleCatalog;

public interface McpValidationRule {
    void validate(McpPackageContext context, McpPackageValidationResult result);

    default McpContractSpec contractSpec() {
        return McpRuleCatalog.require(getClass());
    }
}
