package com.endava.ai.skills.mcp.contracts.yaml;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Locale;
import lombok.AccessLevel;

import com.endava.ai.core.support.contract.yaml.YamlScalars;
import com.endava.ai.skills.mcp.contracts.model.ContractSpec;

import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredString;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ContractYamlEntry {
    private final Map<String, String> values;
    private final String resourcePath;

    public static ContractYamlEntry of(Map<String, String> values, String resourcePath) {
        return new ContractYamlEntry(values, resourcePath);
    }

    public String required(String key) {
        return requiredString(values, key, resourcePath, "contract");
    }

    public String requiredUpper(String key) {
        return required(key).toUpperCase(Locale.ROOT);
    }

    public String optional(String key) {
        return YamlScalars.optional(values, key);
    }

    public boolean optionalBoolean(String key, boolean fallback) {
        return YamlScalars.optionalBoolean(values, key, fallback, resourcePath);
    }

    public ContractSpec.RepairSpec repairSpec() {
        return ContractSpec.RepairSpec.of(
                required("repair.observedArtifact"),
                required("repair.primaryArtifact"),
                required("repair.supportingArtifact"),
                required("repair.route"));
    }
}
