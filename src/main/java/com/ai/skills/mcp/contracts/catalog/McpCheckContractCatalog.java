package com.ai.skills.mcp.contracts.catalog;

import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.skills.mcp.contracts.checks.McpCheckSpec;
import com.ai.skills.mcp.contracts.yaml.ContractSpecLoader;

import java.util.List;

final class McpCheckContractCatalog {
    private static final ContractLazy<List<McpCheckSpec>> MCP_CHECKS = ContractLazy.of(
            "skills/mcp/contracts/checks/*",
            () -> ContractSpecLoader.loadMcpChecks(McpContractResources.MCP_CHECKS));
    private static final ContractLazy<ContractIndex<McpCheckSpec>> MCP_CHECKS_BY_ID = ContractLazy.of(
            "skills/mcp/contracts/checks/*#by-id",
            () -> ContractIndex.unique("skills/mcp/contracts/checks/*", MCP_CHECKS.get(), McpCheckSpec::id));

    private McpCheckContractCatalog() {
    }

    static List<McpCheckSpec> mcpChecks() {
        return MCP_CHECKS.get();
    }

    static McpCheckSpec requireMcpCheck(String id) {
        return MCP_CHECKS_BY_ID.get().require(id);
    }
}
