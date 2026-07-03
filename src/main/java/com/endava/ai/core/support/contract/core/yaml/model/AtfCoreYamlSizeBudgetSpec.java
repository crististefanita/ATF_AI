package com.endava.ai.core.support.contract.core.yaml.model;

import java.nio.file.Path;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredInt;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlSizeBudgetSpec {
    private final String id;
    private final String kind;
    private final String path;
    private final int max;
    private final String owner;
    private final String repair;

    public static AtfCoreYamlSizeBudgetSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlSizeBudgetSpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                required(entry, "path", resourcePath),
                requiredInt(entry, "max", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "repair", resourcePath));
    }

    public Path asPath() {
        return Path.of(path);
    }
}
