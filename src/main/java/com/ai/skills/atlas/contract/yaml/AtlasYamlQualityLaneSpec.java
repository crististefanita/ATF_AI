package com.ai.skills.atlas.contract.yaml;

import com.ai.skills.atlas.contract.AtlasRuleCategory;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Objects;

import static com.ai.core.support.contract.yaml.YamlEnums.enumValue;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredString;

@Getter
@Accessors(fluent = true)
public final class AtlasYamlQualityLaneSpec {
    private final String id;
    private final String lane;
    private final String owner;
    private final AtlasRuleCategory category;
    private final String artifact;
    private final String reportSurface;
    private final String mcpBoundary;
    private final String scoreImpact;
    private final String repairTarget;

    private AtlasYamlQualityLaneSpec(Map<String, String> entry, String resourcePath) {
        this.id = required(entry, "id", resourcePath);
        this.lane = required(entry, "lane", resourcePath);
        this.owner = required(entry, "owner", resourcePath);
        this.category = enumValue(
                AtlasRuleCategory.class,
                required(entry, "category", resourcePath),
                "category",
                resourcePath,
                entry);
        this.artifact = required(entry, "artifact", resourcePath);
        this.reportSurface = required(entry, "reportSurface", resourcePath);
        this.mcpBoundary = required(entry, "mcpBoundary", resourcePath);
        this.scoreImpact = required(entry, "scoreImpact", resourcePath);
        this.repairTarget = required(entry, "repairTarget", resourcePath);
    }

    static AtlasYamlQualityLaneSpec from(Map<String, String> entry, String resourcePath) {
        return new AtlasYamlQualityLaneSpec(entry, resourcePath);
    }

    private static String required(Map<String, String> entry, String key, String resourcePath) {
        return Objects.requireNonNull(requiredString(entry, key, resourcePath, "atlas quality lane"));
    }
}
