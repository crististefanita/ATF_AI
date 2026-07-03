package com.endava.ai.skills.mcp.contracts.policy;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

/**
 * Protected source root that generated package work must not mutate.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpProtectedRootSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String path;
    @NonNull
    private final String severity;
    @NonNull
    private final String owner;
    @NonNull
    private final String reason;
    @NonNull
    private final String allowedAgentAction;

    public static McpProtectedRootSpec from(Map<String, String> entry, String resourcePath) {
        return new McpProtectedRootSpec(
                required(entry, "id", resourcePath),
                normalize(required(entry, "path", resourcePath)),
                required(entry, "severity", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "reason", resourcePath),
                required(entry, "allowedAgentAction", resourcePath));
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }
}
