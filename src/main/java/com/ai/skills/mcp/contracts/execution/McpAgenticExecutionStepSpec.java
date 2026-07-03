package com.ai.skills.mcp.contracts.execution;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Objects;

import static com.ai.core.support.contract.yaml.YamlScalars.requiredBoolean;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredInt;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredString;

@Getter
@Accessors(fluent = true)
public final class McpAgenticExecutionStepSpec {
    private final String id;
    private final int order;
    private final McpExecutionProfile profile;
    private final String role;
    private final String stage;
    private final String primaryArtifact;
    private final String reportLink;
    private final String stopCondition;
    private final String notFor;
    private final boolean mustPass;
    private final int allowedSkipped;

    private McpAgenticExecutionStepSpec(Map<String, String> entry, String resourcePath) {
        this.id = required(entry, "id", resourcePath);
        this.order = requiredInt(entry, "order", resourcePath);
        this.profile = McpExecutionProfile.fromId(required(entry, "profile", resourcePath));
        this.role = required(entry, "role", resourcePath);
        this.stage = required(entry, "stage", resourcePath);
        this.primaryArtifact = required(entry, "primaryArtifact", resourcePath);
        this.reportLink = required(entry, "reportLink", resourcePath);
        this.stopCondition = required(entry, "stopCondition", resourcePath);
        this.notFor = required(entry, "notFor", resourcePath);
        this.mustPass = requiredBoolean(entry, "mustPass", resourcePath);
        this.allowedSkipped = requiredInt(entry, "allowedSkipped", resourcePath);
    }

    public static McpAgenticExecutionStepSpec from(Map<String, String> entry, String resourcePath) {
        return new McpAgenticExecutionStepSpec(entry, resourcePath);
    }

    public String profileId() {
        return profile.id();
    }

    public String suiteXml() {
        return profile.suiteXml();
    }

    private static String required(Map<String, String> entry, String key, String resourcePath) {
        return Objects.requireNonNull(requiredString(entry, key, resourcePath, "agentic execution step"));
    }
}
