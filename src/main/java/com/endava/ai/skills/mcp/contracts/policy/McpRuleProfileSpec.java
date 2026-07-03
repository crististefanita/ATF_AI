package com.endava.ai.skills.mcp.contracts.policy;

import com.endava.ai.skills.mcp.contract.McpProfile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlEnums.enumValue;
import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.commaSeparated;

/**
 * Declarative rule composition for an MCP validation profile.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpRuleProfileSpec {
    @NonNull
    private final McpProfile id;
    @NonNull
    private final String role;
    @NonNull
    private final List<McpProfile> includeProfiles;
    @NonNull
    private final List<String> ruleIds;

    public static McpRuleProfileSpec from(Map<String, String> entry, String resourcePath) {
        return new McpRuleProfileSpec(
                enumValue(McpProfile.class, required(entry, "id", resourcePath), "id", resourcePath, entry),
                required(entry, "role", resourcePath),
                List.copyOf(splitProfiles(entry.get("includeProfiles"), resourcePath, entry)),
                List.copyOf(commaSeparated(entry.get("ruleIds"))));
    }

    private static List<McpProfile> splitProfiles(String value, String resourcePath, Map<String, String> entry) {
        return commaSeparated(value).stream()
                .map(part -> enumValue(McpProfile.class, part, "includeProfiles", resourcePath, entry))
                .collect(Collectors.toUnmodifiableList());
    }
}
