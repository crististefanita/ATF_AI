package com.ai.skills.mcp.contracts.candidate;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredInt;

/**
 * Declarative html-ex corpus contract.
 * Java owns candidate validation; YAML owns the corpus role and repair metadata.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpHtmlExCorpusSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String profile;
    @NonNull
    private final String severity;
    @NonNull
    private final String corpusRoot;
    @NonNull
    private final String rootProperty;
    @NonNull
    private final String primaryStructuralReference;
    private final int minimumExamples;
    @NonNull
    private final String matrixArtifact;
    @NonNull
    private final String validator;
    @NonNull
    private final String repairTarget;
    @NonNull
    private final String capImpact;
    @NonNull
    private final String publishabilityImpact;

    public static McpHtmlExCorpusSpec from(Map<String, String> entry, String resourcePath) {
        return new McpHtmlExCorpusSpec(
                required(entry, "id", resourcePath),
                required(entry, "profile", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "corpusRoot", resourcePath),
                required(entry, "rootProperty", resourcePath),
                required(entry, "primaryStructuralReference", resourcePath),
                requiredInt(entry, "minimumExamples", resourcePath),
                required(entry, "matrixArtifact", resourcePath),
                required(entry, "validator", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "capImpact", resourcePath),
                required(entry, "publishabilityImpact", resourcePath));
    }
}
