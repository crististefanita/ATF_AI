package com.endava.ai.skills.mcp.contracts.checks;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.commaSeparated;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optional;

/**
 * Declarative metadata for a TestNG MCP check.
 * Java tests remain executable proof; this model owns the narrative text.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpCheckSpec {
    private final String id;
    private final String expected;
    @Getter(AccessLevel.NONE)
    private final String artifacts;
    private final String failureMeaning;
    private final String repairHint;

    public static McpCheckSpec from(Map<String, String> entry, String resourcePath) {
        return new McpCheckSpec(
                required(entry, "id", resourcePath),
                required(entry, "expected", resourcePath),
                optional(entry, "artifacts"),
                required(entry, "failureMeaning", resourcePath),
                required(entry, "repairHint", resourcePath));
    }

    public List<String> artifacts() {
        return commaSeparated(artifacts);
    }

}
