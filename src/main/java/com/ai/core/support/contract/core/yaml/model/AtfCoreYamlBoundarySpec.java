package com.ai.core.support.contract.core.yaml.model;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalarLists.strictCommaSeparated;
import static com.ai.core.support.contract.yaml.YamlScalars.optionalOrDefault;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlBoundarySpec {
    @Getter
    @NonNull
    private final String id;
    @Getter
    @NonNull
    private final String owner;
    @Getter
    @NonNull
    private final String severity;
    @Getter
    @NonNull
    private final String sourceRoot;
    @NonNull
    private final String scanRoots;
    @NonNull
    private final String protectedRoots;
    @NonNull
    private final String forbidden;
    @Getter
    @NonNull
    private final String repair;

    public static AtfCoreYamlBoundarySpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlBoundarySpec(
                required(entry, "id", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "sourceRoot", resourcePath),
                optionalOrDefault(entry, "scanRoots", required(entry, "sourceRoot", resourcePath)),
                required(entry, "protectedRoots", resourcePath),
                required(entry, "forbidden", resourcePath),
                required(entry, "repair", resourcePath));
    }

    public List<String> scanRoots() {
        return split(scanRoots, "scanRoots");
    }

    public List<String> protectedRoots() {
        return split(protectedRoots, "protectedRoots");
    }

    public List<String> forbidden() {
        return split(forbidden, "forbidden");
    }

    private List<String> split(String value, String field) {
        return strictCommaSeparated(value, "atf-core boundary " + id + "." + field);
    }
}
