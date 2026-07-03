package com.endava.ai.skills.mcp.contracts.policy;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.commaSeparated;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optional;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optionalBoolean;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optionalInt;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredString;

@Getter
@Accessors(fluent = true)
public final class McpTestNgProfileSpec {
    private final String id;
    private final String suiteXml;
    private final String role;
    private final boolean finalArtifact;
    private final int allowedSkipped;
    private final String mirrorOf;
    private final List<String> expectedClasses;
    private final List<String> expectedMethods;
    private final List<String> expectedPackages;
    private final List<String> excludedGroups;
    private final List<String> requiredClasses;
    private final List<String> requiredClassFragments;
    private final List<String> forbiddenClasses;
    private final List<String> forbiddenClassFragments;
    private final boolean resourcesOnly;

    private McpTestNgProfileSpec(Map<String, String> entry, String resourcePath) {
        this.id = required(entry, "id", resourcePath);
        this.suiteXml = required(entry, "suiteXml", resourcePath);
        this.role = required(entry, "role", resourcePath);
        this.finalArtifact = optionalBoolean(entry, "finalArtifact", false, resourcePath);
        this.allowedSkipped = optionalInt(entry, "allowedSkipped", -1, resourcePath);
        this.mirrorOf = optional(entry, "mirrorOf");
        this.expectedClasses = commaSeparated(entry.get("expectedClasses"));
        this.expectedMethods = commaSeparated(entry.get("expectedMethods"));
        this.expectedPackages = commaSeparated(entry.get("expectedPackages"));
        this.excludedGroups = commaSeparated(entry.get("excludedGroups"));
        this.requiredClasses = commaSeparated(entry.get("requiredClasses"));
        this.requiredClassFragments = commaSeparated(entry.get("requiredClassFragments"));
        this.forbiddenClasses = commaSeparated(entry.get("forbiddenClasses"));
        this.forbiddenClassFragments = commaSeparated(entry.get("forbiddenClassFragments"));
        this.resourcesOnly = optionalBoolean(entry, "resourcesOnly", false, resourcePath);
    }

    public static McpTestNgProfileSpec from(Map<String, String> entry, String resourcePath) {
        return new McpTestNgProfileSpec(entry, resourcePath);
    }

    private static String required(Map<String, String> entry, String key, String resourcePath) {
        return requiredString(entry, key, resourcePath, "profile");
    }

}
