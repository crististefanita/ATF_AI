package com.endava.ai.skills.mcp.contracts.policy;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

/**
 * Declarative repair coaching for MCP blocker root-cause buckets.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpRootCauseGroupSpec {
    private final String id;
    private final String label;
    private final String repair;

    public static McpRootCauseGroupSpec from(Map<String, String> entry, String resourcePath) {
        return new McpRootCauseGroupSpec(
                required(entry, "id", resourcePath),
                required(entry, "label", resourcePath),
                required(entry, "repair", resourcePath));
    }
}
