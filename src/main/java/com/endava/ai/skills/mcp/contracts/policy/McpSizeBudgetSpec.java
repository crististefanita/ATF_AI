package com.endava.ai.skills.mcp.contracts.policy;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optional;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optionalInt;

/**
 * Declarative MCP package and class size budget.
 */
@Getter
@Accessors(fluent = true)
public final class McpSizeBudgetSpec {
    private final String id;
    private final String type;
    private final String root;
    private final String path;
    private final int maxClasses;
    private final int maxLines;
    private final int maxBytes;
    private final String reason;

    private McpSizeBudgetSpec(String id,
                              String type,
                              String root,
                              String path,
                              int maxClasses,
                              int maxLines,
                              int maxBytes,
                              String reason) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.root = normalize(Objects.requireNonNull(root, "root"));
        this.path = normalize(Objects.requireNonNull(path, "path"));
        this.maxClasses = maxClasses;
        this.maxLines = maxLines;
        this.maxBytes = maxBytes;
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public static McpSizeBudgetSpec from(Map<String, String> entry, String resourcePath) {
        String type = required(entry, "type", resourcePath);
        return new McpSizeBudgetSpec(
                required(entry, "id", resourcePath),
                type,
                optional(entry, "root"),
                optional(entry, "path"),
                optionalInt(entry, "maxClasses", -1, resourcePath),
                optionalInt(entry, "maxLines", -1, resourcePath),
                optionalInt(entry, "maxBytes", -1, resourcePath),
                required(entry, "reason", resourcePath));
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }
}
