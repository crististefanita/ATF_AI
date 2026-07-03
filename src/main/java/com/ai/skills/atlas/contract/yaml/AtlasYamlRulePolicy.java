package com.ai.skills.atlas.contract.yaml;

import com.ai.skills.atlas.contract.AtlasRuleCategory;
import com.ai.skills.atlas.contract.AtlasRuleSeverity;
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
public final class AtlasYamlRulePolicy {
    @NonNull
    private final String id;
    @NonNull
    private final AtlasContractProfile profile;
    @NonNull
    private final AtlasRuleCategory category;
    @NonNull
    private final AtlasRuleSeverity severity;
    @NonNull
    private final String owner;
    @NonNull
    private final String artifact;
    @NonNull
    private final String repairTarget;
    @NonNull
    private final String matrix;
    @NonNull
    private final String capImpact;

    static AtlasYamlRulePolicy from(Map<String, String> entry, String resourcePath) {
        return new AtlasYamlRulePolicy(
                required(entry, "id", resourcePath),
                enumValue(AtlasContractProfile.class, required(entry, "profile", resourcePath), "profile", resourcePath, entry),
                enumValue(AtlasRuleCategory.class, required(entry, "category", resourcePath), "category", resourcePath, entry),
                enumValue(AtlasRuleSeverity.class, required(entry, "severity", resourcePath), "severity", resourcePath, entry),
                required(entry, "owner", resourcePath),
                required(entry, "artifact", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "matrix", resourcePath),
                required(entry, "capImpact", resourcePath));
    }

}
