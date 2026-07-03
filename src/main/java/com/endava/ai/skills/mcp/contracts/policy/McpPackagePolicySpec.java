package com.endava.ai.skills.mcp.contracts.policy;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

/**
 * Declarative package policy token.
 * Java owns the validation algorithm; YAML owns the stable token catalog.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpPackagePolicySpec {
    private final String id;
    private final String kind;
    private final String token;
    private final String message;

    public static McpPackagePolicySpec from(Map<String, String> entry, String resourcePath) {
        return new McpPackagePolicySpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                required(entry, "token", resourcePath),
                required(entry, "message", resourcePath));
    }
}
