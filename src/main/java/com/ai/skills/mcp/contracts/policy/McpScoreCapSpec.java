package com.ai.skills.mcp.contracts.policy;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredInt;

/**
 * Declarative scoring cap contract for publishability blockers.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpScoreCapSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String profile;
    @NonNull
    private final String severity;
    @NonNull
    private final String trigger;
    private final int maxScore;
    @NonNull
    private final String validator;
    @NonNull
    private final String repairTarget;
    @NonNull
    private final String capImpact;
    @NonNull
    private final String publishabilityImpact;

    public static McpScoreCapSpec from(Map<String, String> entry, String resourcePath) {
        return new McpScoreCapSpec(
                required(entry, "id", resourcePath),
                required(entry, "profile", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "trigger", resourcePath),
                requiredInt(entry, "maxScore", resourcePath),
                required(entry, "validator", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "capImpact", resourcePath),
                required(entry, "publishabilityImpact", resourcePath));
    }
}
