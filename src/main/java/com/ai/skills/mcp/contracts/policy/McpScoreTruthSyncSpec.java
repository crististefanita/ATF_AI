package com.ai.skills.mcp.contracts.policy;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalarLists.commaSeparated;

/**
 * Declarative score-truth synchronization contract.
 * Java validators compare artifacts; YAML explains the evidence, repair target and score impact.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpScoreTruthSyncSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String profile;
    @NonNull
    private final String severity;
    @NonNull
    private final String truthSource;
    @NonNull
    private final List<String> synchronizedArtifacts;
    @NonNull
    private final List<String> requiredEvidence;
    @NonNull
    private final String validator;
    @NonNull
    private final String repairTarget;
    @NonNull
    private final String capImpact;
    @NonNull
    private final String publishabilityImpact;

    public static McpScoreTruthSyncSpec from(Map<String, String> entry, String resourcePath) {
        return new McpScoreTruthSyncSpec(
                required(entry, "id", resourcePath),
                required(entry, "profile", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "truthSource", resourcePath),
                List.copyOf(commaSeparated(required(entry, "synchronizedArtifacts", resourcePath))),
                List.copyOf(commaSeparated(required(entry, "requiredEvidence", resourcePath))),
                required(entry, "validator", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "capImpact", resourcePath),
                required(entry, "publishabilityImpact", resourcePath));
    }
}
