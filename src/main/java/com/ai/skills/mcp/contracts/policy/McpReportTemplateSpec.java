package com.ai.skills.mcp.contracts.policy;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalarLists.pipeSeparated;
import static com.ai.core.support.contract.yaml.YamlScalars.contractId;
import static com.ai.core.support.contract.yaml.YamlScalars.optional;

/**
 * Declarative report-template expectation.
 * The validator still decides pass/fail; this model owns stable tokens and messages.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpReportTemplateSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String kind;
    @NonNull
    private final String token;
    @NonNull
    private final List<String> tokens;
    @NonNull
    private final String message;

    public static McpReportTemplateSpec from(Map<String, String> entry, String resourcePath) {
        String kind = required(entry, "kind", resourcePath);
        String token = optional(entry, "token");
        String tokens = optional(entry, "tokens");
        if (token.isEmpty() && tokens.isEmpty()) {
            throw new IllegalArgumentException("Missing token/tokens in " + resourcePath
                    + " for report template contract " + contractId(entry));
        }
        return new McpReportTemplateSpec(
                required(entry, "id", resourcePath),
                kind,
                token,
                List.copyOf(pipeSeparated(tokens.isEmpty() ? token : tokens)),
                required(entry, "message", resourcePath));
    }

    public boolean hasTokenField() {
        return !token.isBlank();
    }

}
