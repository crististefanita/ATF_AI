package com.endava.ai.skills.mcp.rules;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContractSpec;
import com.endava.ai.skills.mcp.contract.McpRuleCatalog;

public interface McpValidationRule {
    void validate(McpPackageContext context, McpPackageValidationResult result);

    default McpContractSpec contractSpec() {
        return McpRuleCatalog.require(getClass());
    }
}
