package com.endava.ai.skills.mcp.contracts.catalog;

import com.endava.ai.skills.mcp.contracts.model.ContractSpec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ContractCatalogSync {
    private ContractCatalogSync() {
    }

    public static List<String> duplicateIds(List<? extends ContractSpec> specs) {
        Set<String> seen = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (ContractSpec spec : specs) {
            if (!seen.add(spec.id())) {
                duplicates.add(spec.id());
            }
        }
        return List.copyOf(duplicates);
    }

    public static List<String> specsWithoutExecutableValidator(List<? extends ContractSpec> specs) {
        return specs.stream()
                .filter(spec -> !spec.metadataOnly())
                .filter(spec -> spec.validator().isBlank())
                .map(ContractSpec::id)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> invalidSeverities(List<? extends ContractSpec> specs) {
        return specs.stream()
                .filter(spec -> !"BLOCKER".equals(spec.severity()) && !"MAJOR".equals(spec.severity())
                        && !"INFO".equals(spec.severity()))
                .map(spec -> spec.id() + "=" + spec.severity())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> invalidSourceEvolutionProfiles(List<? extends ContractSpec> specs) {
        return specs.stream()
                .filter(spec -> !"package-closeout".equals(spec.profile()) && !"diagnostic".equals(spec.profile()))
                .map(spec -> spec.id() + "=" + spec.profile())
                .collect(Collectors.toUnmodifiableList());
    }
}
