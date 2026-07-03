package com.ai.skills.atlas.contract.yaml;

import com.ai.skills.atlas.contract.AtlasContractProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlEnums.enumValue;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtlasYamlSuiteSpec {
    @NonNull
    private final String id;
    @NonNull
    private final AtlasContractProfile profile;
    @NonNull
    private final String htmlOwner;
    @NonNull
    private final String artifact;
    @NonNull
    private final String sourceRoot;
    @NonNull
    private final String purpose;

    static AtlasYamlSuiteSpec from(Map<String, String> entry, String resourcePath) {
        return new AtlasYamlSuiteSpec(
                required(entry, "id", resourcePath),
                enumValue(AtlasContractProfile.class, required(entry, "profile", resourcePath),
                        "profile", resourcePath, entry),
                required(entry, "htmlOwner", resourcePath),
                required(entry, "artifact", resourcePath),
                required(entry, "sourceRoot", resourcePath),
                required(entry, "purpose", resourcePath));
    }
}
