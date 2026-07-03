package com.endava.ai.skills.atlas.contract.yaml;

import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasRuleCategory;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlEnums.enumValue;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.optionalInt;

@Getter
@Accessors(fluent = true)
public final class AtlasYamlGraphReportingContract {
    private final String id;
    private final AtlasContractProfile profile;
    private final String owner;
    private final AtlasRuleCategory category;
    private final List<String> requiredClasses;
    private final List<String> visibleNodeShapeTags;
    private final List<String> forbiddenWrapperClassTags;
    private final String requiredTextAnchor;
    private final String proofStripRole;
    private final List<String> proofStripLabels;
    private final List<String> requiredConcepts;
    private final int minSourceBackedMethods;
    private final List<String> genericNodeLabels;
    private final int minDomainNodeLabelsPerSubgraph;
    private final int minDistinctSubgraphSignatures;
    private final String toolbarClass;
    private final String toolbarAriaLabel;
    private final String openLabel;
    private final String closeLabel;
    private final String openAction;
    private final String closeAction;
    private final String functionName;
    private final String expectedLanguage;
    private final List<String> requiredVisibleTokens;
    private final List<String> requiredRomanianSentences;
    private final List<String> forbiddenKeyEnglishPhrases;
    private final String repair;

    private AtlasYamlGraphReportingContract(Map<String, String> entry, String resourcePath) {
        this.id = required(entry, "id", resourcePath);
        this.profile = enumValue(AtlasContractProfile.class, required(entry, "profile", resourcePath),
                "profile", resourcePath, entry);
        this.owner = required(entry, "owner", resourcePath);
        this.category = enumValue(AtlasRuleCategory.class, required(entry, "category", resourcePath),
                "category", resourcePath, entry);
        this.requiredClasses = list(entry.get("requiredClasses"));
        this.visibleNodeShapeTags = list(entry.get("visibleNodeShapeTags"));
        this.forbiddenWrapperClassTags = list(entry.get("forbiddenWrapperClassTags"));
        this.requiredTextAnchor = entry.getOrDefault("requiredTextAnchor", "").trim();
        this.proofStripRole = entry.getOrDefault("proofStripRole", "").trim();
        this.proofStripLabels = list(entry.get("proofStripLabels"));
        this.requiredConcepts = list(entry.get("requiredConcepts"));
        this.minSourceBackedMethods = optionalInt(entry, "minSourceBackedMethods", 0, resourcePath);
        this.genericNodeLabels = list(entry.get("genericNodeLabels"));
        this.minDomainNodeLabelsPerSubgraph = optionalInt(entry, "minDomainNodeLabelsPerSubgraph", 0, resourcePath);
        this.minDistinctSubgraphSignatures = optionalInt(entry, "minDistinctSubgraphSignatures", 0, resourcePath);
        this.toolbarClass = entry.getOrDefault("toolbarClass", "").trim();
        this.toolbarAriaLabel = entry.getOrDefault("toolbarAriaLabel", "").trim();
        this.openLabel = entry.getOrDefault("openLabel", "").trim();
        this.closeLabel = entry.getOrDefault("closeLabel", "").trim();
        this.openAction = entry.getOrDefault("openAction", "").trim();
        this.closeAction = entry.getOrDefault("closeAction", "").trim();
        this.functionName = entry.getOrDefault("functionName", "").trim();
        this.expectedLanguage = entry.getOrDefault("expectedLanguage", "").trim();
        this.requiredVisibleTokens = list(entry.get("requiredVisibleTokens"));
        this.requiredRomanianSentences = list(entry.get("requiredRomanianSentences"));
        this.forbiddenKeyEnglishPhrases = list(entry.get("forbiddenKeyEnglishPhrases"));
        this.repair = required(entry, "repair", resourcePath);
    }

    public static AtlasYamlGraphReportingContract from(Map<String, String> entry, String resourcePath) {
        return new AtlasYamlGraphReportingContract(entry, resourcePath);
    }

    private static List<String> list(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

}
