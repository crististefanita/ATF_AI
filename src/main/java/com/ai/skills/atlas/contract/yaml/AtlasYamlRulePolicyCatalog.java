package com.ai.skills.atlas.contract.yaml;

import com.ai.skills.atlas.contract.AtlasRuleCategory;
import com.ai.skills.atlas.contract.AtlasContractProfile;
import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.core.support.contract.yaml.ContractResource;

import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AtlasYamlRulePolicyCatalog {
    public static final String RESOURCE = AtlasYamlContractResources.RULE_POLICIES;

    private static final List<AtlasYamlRulePolicy> POLICIES = ContractResource.of(
            RESOURCE,
            entry -> AtlasYamlRulePolicy.from(entry, RESOURCE)).load();
    private static final ContractIndex<AtlasYamlRulePolicy> BY_ID =
            ContractIndex.unique(RESOURCE, POLICIES, AtlasYamlRulePolicy::id);
    private static final Map<AtlasRuleCategory, List<AtlasYamlRulePolicy>> BY_CATEGORY = indexByCategory();

    private AtlasYamlRulePolicyCatalog() {
    }

    public static List<AtlasYamlRulePolicy> policies() {
        return POLICIES;
    }

    public static AtlasYamlRulePolicy require(String id) {
        return BY_ID.require(id);
    }

    public static AtlasYamlRulePolicy requireFor(AtlasRuleCategory category) {
        return policiesFor(category).stream()
                .min(Comparator.comparingInt((AtlasYamlRulePolicy policy) -> profileRank(policy.profile()))
                        .thenComparing(policy -> policy.id()))
                .orElseThrow(() -> new IllegalArgumentException("Missing Atlas YAML rule policy for category: " + category));
    }

    public static AtlasYamlRulePolicy requireFor(AtlasRuleCategory category, AtlasContractProfile profile) {
        List<AtlasYamlRulePolicy> policies = policiesFor(category);
        Optional<AtlasYamlRulePolicy> exact = policies.stream()
                .filter(policy -> policy.profile() == profile)
                .findFirst();
        if (exact.isPresent()) {
            return exact.get();
        }
        return policies.stream()
                .filter(policy -> policy.profile() == AtlasContractProfile.ANY)
                .findFirst()
                .orElseGet(() -> requireFor(category));
    }

    public static Map<AtlasRuleCategory, List<AtlasYamlRulePolicy>> policiesByCategory() {
        return Map.copyOf(BY_CATEGORY);
    }

    public static List<AtlasYamlRulePolicy> policiesFor(AtlasRuleCategory category) {
        List<AtlasYamlRulePolicy> policies = BY_CATEGORY.get(category);
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("Missing Atlas YAML rule policy for category: " + category);
        }
        return policies;
    }

    public static List<String> duplicateIds() {
        return BY_ID.duplicateIds();
    }

    private static Map<AtlasRuleCategory, List<AtlasYamlRulePolicy>> indexByCategory() {
        Map<AtlasRuleCategory, List<AtlasYamlRulePolicy>> indexed = new EnumMap<>(AtlasRuleCategory.class);
        for (AtlasYamlRulePolicy policy : POLICIES) {
            indexed.computeIfAbsent(policy.category(), ignored -> new ArrayList<>()).add(policy);
        }
        return indexed.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));
    }

    private static int profileRank(AtlasContractProfile profile) {
        if (profile == AtlasContractProfile.ANY) {
            return 0;
        }
        return 1;
    }
}
