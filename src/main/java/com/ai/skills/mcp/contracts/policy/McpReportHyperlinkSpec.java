package com.ai.skills.mcp.contracts.policy;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalarLists.strictCommaSeparatedAlternatives;
import static com.ai.core.support.contract.yaml.YamlScalars.contractId;

/**
 * Declarative hyperlink contract for final MCP report entry points.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpReportHyperlinkSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String profile;
    @NonNull
    private final String severity;
    @NonNull
    private final String report;
    @NonNull
    private final String section;
    @NonNull
    private final List<List<String>> requiredLinks;
    @NonNull
    private final String validator;
    @NonNull
    private final String repairTarget;
    @NonNull
    private final String capImpact;
    @NonNull
    private final String publishabilityImpact;

    public static McpReportHyperlinkSpec from(Map<String, String> entry, String resourcePath) {
        return new McpReportHyperlinkSpec(
                required(entry, "id", resourcePath),
                required(entry, "profile", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "report", resourcePath),
                required(entry, "section", resourcePath),
                List.copyOf(strictCommaSeparatedAlternatives(required(entry, "requiredLinks", resourcePath),
                        resourcePath + "#" + contractId(entry) + ".requiredLinks")),
                required(entry, "validator", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "capImpact", resourcePath),
                required(entry, "publishabilityImpact", resourcePath));
    }
}
