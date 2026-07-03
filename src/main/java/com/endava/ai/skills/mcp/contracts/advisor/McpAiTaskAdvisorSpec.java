package com.endava.ai.skills.mcp.contracts.advisor;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredBoolean;

/**
 * Non-blocking guidance for making agentic MCP work smaller, cheaper and more
 * auditable.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpAiTaskAdvisorSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String severity;
    @NonNull
    private final String signal;
    @NonNull
    private final String recommendation;
    @NonNull
    private final String modelTier;
    @NonNull
    private final String acceptanceCriteria;
    @NonNull
    private final String stopCondition;
    @NonNull
    private final String valueRationale;
    @NonNull
    private final String reportAnchor;
    private final boolean blocking;

    public static McpAiTaskAdvisorSpec from(Map<String, String> entry, String resourcePath) {
        return new McpAiTaskAdvisorSpec(
                required(entry, "id", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "signal", resourcePath),
                required(entry, "recommendation", resourcePath),
                required(entry, "modelTier", resourcePath),
                required(entry, "acceptanceCriteria", resourcePath),
                required(entry, "stopCondition", resourcePath),
                required(entry, "valueRationale", resourcePath),
                required(entry, "reportAnchor", resourcePath),
                requiredBoolean(entry, "blocking", resourcePath));
    }
}
