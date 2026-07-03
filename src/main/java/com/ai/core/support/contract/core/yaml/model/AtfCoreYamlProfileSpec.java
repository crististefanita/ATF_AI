package com.ai.core.support.contract.core.yaml.model;

import java.util.Map;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalarLists.commaSeparated;
import static com.ai.core.support.contract.yaml.YamlScalars.optionalBoolean;
import static com.ai.core.support.contract.yaml.YamlScalars.optionalInt;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlProfileSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String suiteXml;
    @NonNull
    private final String role;
    @NonNull
    private final String stability;
    @NonNull
    private final String browser;
    @NonNull
    private final List<String> areas;
    @NonNull
    private final String closeoutRole;
    private final boolean finalArtifact;
    private final int allowedSkipped;
    @NonNull
    private final List<String> expectedClasses;
    @NonNull
    private final List<String> expectedPackages;
    @NonNull
    private final List<String> includedGroups;
    @NonNull
    private final List<String> excludedGroups;
    @NonNull
    private final List<String> requiredClasses;
    @NonNull
    private final List<String> forbiddenClasses;
    @NonNull
    private final List<String> forbiddenClassFragments;

    public static AtfCoreYamlProfileSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlProfileSpec(
                required(entry, "id", resourcePath),
                required(entry, "suiteXml", resourcePath),
                required(entry, "role", resourcePath),
                required(entry, "stability", resourcePath),
                required(entry, "browser", resourcePath),
                commaSeparated(required(entry, "areas", resourcePath)),
                required(entry, "closeoutRole", resourcePath),
                optionalBoolean(entry, "finalArtifact", false, resourcePath),
                optionalInt(entry, "allowedSkipped", -1, resourcePath),
                List.copyOf(commaSeparated(entry.get("expectedClasses"))),
                List.copyOf(commaSeparated(entry.get("expectedPackages"))),
                List.copyOf(commaSeparated(entry.get("includedGroups"))),
                List.copyOf(commaSeparated(entry.get("excludedGroups"))),
                List.copyOf(commaSeparated(entry.get("requiredClasses"))),
                List.copyOf(commaSeparated(entry.get("forbiddenClasses"))),
                List.copyOf(commaSeparated(entry.get("forbiddenClassFragments"))));
    }

}
