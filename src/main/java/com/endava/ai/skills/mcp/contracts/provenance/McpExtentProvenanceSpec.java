package com.endava.ai.skills.mcp.contracts.provenance;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.contractId;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optional;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optionalNonNegativeInteger;

/**
 * Declarative Extent provenance expectation.
 * Java owns validation semantics; YAML owns stable evidence tokens and thresholds.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpExtentProvenanceSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String kind;
    @NonNull
    private final String token;
    private final Integer threshold;
    @NonNull
    private final String message;

    public static McpExtentProvenanceSpec from(Map<String, String> entry, String resourcePath) {
        String token = optional(entry, "token");
        Integer threshold = optionalNonNegativeInteger(entry, "threshold", resourcePath);
        if (token.isEmpty() && threshold == null) {
            throw new IllegalArgumentException("Missing token/threshold in " + resourcePath
                    + " for Extent provenance contract " + contractId(entry));
        }
        return new McpExtentProvenanceSpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                token,
                threshold,
                required(entry, "message", resourcePath));
    }

    public boolean hasTokenField() {
        return !token.isBlank();
    }
}
