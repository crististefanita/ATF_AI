package com.endava.ai.skills.atlas.contract.yaml;

import com.endava.ai.core.support.contract.yaml.ContractIndex;
import com.endava.ai.core.support.contract.yaml.ContractResource;

import java.util.List;

public final class AtlasYamlSuiteCatalog {
    public static final String RESOURCE = AtlasYamlContractResources.SUITES;

    private static final List<AtlasYamlSuiteSpec> SUITES = ContractResource.of(
            RESOURCE,
            entry -> AtlasYamlSuiteSpec.from(entry, RESOURCE)).load();
    private static final ContractIndex<AtlasYamlSuiteSpec> BY_ID =
            ContractIndex.unique(RESOURCE, SUITES, AtlasYamlSuiteSpec::id);

    private AtlasYamlSuiteCatalog() {
    }

    public static List<AtlasYamlSuiteSpec> suites() {
        return SUITES;
    }

    public static AtlasYamlSuiteSpec require(String id) {
        return BY_ID.require(id);
    }

    public static List<String> duplicateIds() {
        return BY_ID.duplicateIds();
    }
}
