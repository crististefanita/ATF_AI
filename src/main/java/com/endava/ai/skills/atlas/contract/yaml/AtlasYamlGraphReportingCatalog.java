package com.endava.ai.skills.atlas.contract.yaml;

import com.endava.ai.core.support.contract.yaml.ContractIndex;
import com.endava.ai.core.support.contract.yaml.ContractResource;

import java.util.List;

public final class AtlasYamlGraphReportingCatalog {
    public static final String RESOURCE = AtlasYamlContractResources.GRAPH_REPORTING;

    private static final List<AtlasYamlGraphReportingContract> CONTRACTS = ContractResource.of(
            RESOURCE,
            entry -> AtlasYamlGraphReportingContract.from(entry, RESOURCE)).load();
    private static final ContractIndex<AtlasYamlGraphReportingContract> BY_ID =
            ContractIndex.unique(RESOURCE, CONTRACTS, AtlasYamlGraphReportingContract::id);

    private AtlasYamlGraphReportingCatalog() {
    }

    public static List<AtlasYamlGraphReportingContract> contracts() {
        return CONTRACTS;
    }

    public static AtlasYamlGraphReportingContract require(String id) {
        return BY_ID.require(id);
    }

    public static List<String> duplicateIds() {
        return BY_ID.duplicateIds();
    }
}
