package com.endava.ai.skills.mcp.contracts.policy;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.strictCommaSeparatedAlternatives;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.contractId;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredString;

@Getter
@Accessors(fluent = true)
public final class McpCloseoutArtifactSpec {
    private final String id;
    private final String profile;
    private final String severity;
    private final List<List<String>> requiredArtifacts;
    private final List<List<String>> requiredGateResultMentions;
    private final List<List<String>> requiredFinalReportLinks;
    private final List<List<String>> requiredArtifactIndexLinks;
    private final String capImpact;
    private final String publishabilityImpact;

    private McpCloseoutArtifactSpec(Map<String, String> entry, String resourcePath) {
        this.id = required(entry, "id", resourcePath);
        this.profile = required(entry, "profile", resourcePath);
        this.severity = required(entry, "severity", resourcePath);
        this.requiredArtifacts = alternatives(entry, "requiredArtifacts", resourcePath);
        this.requiredGateResultMentions = alternatives(entry, "requiredGateResultMentions", resourcePath);
        this.requiredFinalReportLinks = alternatives(entry, "requiredFinalReportLinks", resourcePath);
        this.requiredArtifactIndexLinks = alternatives(entry, "requiredArtifactIndexLinks", resourcePath);
        this.capImpact = required(entry, "capImpact", resourcePath);
        this.publishabilityImpact = required(entry, "publishabilityImpact", resourcePath);
    }

    public static McpCloseoutArtifactSpec from(Map<String, String> entry, String resourcePath) {
        return new McpCloseoutArtifactSpec(entry, resourcePath);
    }

    private static String required(Map<String, String> entry, String key, String resourcePath) {
        return requiredString(entry, key, resourcePath, "close-out artifact contract");
    }

    private static List<List<String>> alternatives(Map<String, String> entry, String key, String resourcePath) {
        return strictCommaSeparatedAlternatives(required(entry, key, resourcePath),
                resourcePath + "#" + contractId(entry) + "." + key);
    }

}
