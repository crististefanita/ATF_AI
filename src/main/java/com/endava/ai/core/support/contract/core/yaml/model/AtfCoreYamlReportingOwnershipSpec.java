package com.endava.ai.core.support.contract.core.yaml.model;

import java.nio.file.Path;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlReportingOwnershipSpec {
    private final String id;
    private final String kind;
    private final String path;
    private final String owner;
    private final String repair;

    public static AtfCoreYamlReportingOwnershipSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlReportingOwnershipSpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                required(entry, "path", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "repair", resourcePath));
    }

    public Path asPath() {
        return Path.of(path);
    }
}
