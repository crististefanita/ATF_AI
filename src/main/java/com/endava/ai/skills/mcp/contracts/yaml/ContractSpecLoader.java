package com.endava.ai.skills.mcp.contracts.yaml;

import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.mcp.contracts.checks.McpCheckSpec;
import com.endava.ai.skills.mcp.contracts.model.KpiSpec;
import com.endava.ai.skills.mcp.contracts.yaml.ContractYamlEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Small YAML subset loader for MCP-owned contract metadata.
 * It intentionally supports only the simple list-of-maps shape used by src/main/resources/skills/mcp/contracts.
 */
public final class ContractSpecLoader {
    private ContractSpecLoader() {
    }

    public static List<KpiSpec> loadKpis(String resourcePath) {
        return loadSpecs(resourcePath, ContractSpecLoader::toKpi);
    }

    public static List<McpCheckSpec> loadMcpChecks(String resourcePath) {
        return loadSpecs(resourcePath, McpCheckSpec::from);
    }

    public static List<McpCheckSpec> loadMcpChecks(String... resourcePaths) {
        return Arrays.stream(resourcePaths)
                .flatMap(resourcePath -> loadMcpChecks(resourcePath).stream())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Map<String, String>> loadEntries(String resourcePath) {
        return SimpleYamlContractLoader.load(resourcePath);
    }

    private static <T> List<T> loadSpecs(String resourcePath,
                                         BiFunction<Map<String, String>, String, T> mapper) {
        return loadEntries(resourcePath).stream()
                .map(entry -> mapper.apply(entry, resourcePath))
                .collect(Collectors.toUnmodifiableList());
    }

    private static KpiSpec toKpi(Map<String, String> entry, String resourcePath) {
        ContractYamlEntry yaml = ContractYamlEntry.of(entry, resourcePath);
        return KpiSpec.builder()
                .id(yaml.required("id"))
                .profile(yaml.required("profile"))
                .category(yaml.required("category"))
                .severity(yaml.requiredUpper("severity"))
                .sourceArtifact(yaml.required("sourceArtifact"))
                .formula(yaml.required("formula"))
                .repair(yaml.repairSpec())
                .why(yaml.required("why"))
                .qualityMeaning(yaml.required("qualityMeaning"))
                .agentAction(yaml.required("agentAction"))
                .repairHint(yaml.required("repairHint"))
                .repairCommand(yaml.required("repairCommand"))
                .capImpact(yaml.required("capImpact"))
                .publishabilityImpact(yaml.required("publishabilityImpact"))
                .metadataOnly(yaml.optionalBoolean("metadataOnly", false))
                .validator(yaml.optional("validator"))
                .build();
    }
}
