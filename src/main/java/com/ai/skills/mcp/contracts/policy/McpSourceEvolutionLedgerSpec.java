package com.ai.skills.mcp.contracts.policy;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

/**
 * Declarative source-evolution ledger token contract.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpSourceEvolutionLedgerSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String kind;
    @NonNull
    private final String token;
    @NonNull
    private final String reason;
    @NonNull
    private final String repairTarget;

    public static McpSourceEvolutionLedgerSpec from(Map<String, String> entry, String resourcePath) {
        return new McpSourceEvolutionLedgerSpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                required(entry, "token", resourcePath),
                required(entry, "reason", resourcePath),
                required(entry, "repairTarget", resourcePath));
    }

}
