package com.ai.skills.mcp.contract.execution;

/**
 * Coarse execution/repair cost used by MCP matrices and agent routing.
 */
public enum McpRuleCost {
    FAST,
    MEDIUM,
    HEAVY,
    FINAL_ONLY
}
