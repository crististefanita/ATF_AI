package com.ai.skills.atlas.contract.yaml;

import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.core.support.contract.yaml.ContractResource;

import java.util.List;

public final class AtlasYamlQualityLaneCatalog {
    public static final String RESOURCE = AtlasYamlContractResources.QUALITY_LANES;

    private static final List<AtlasYamlQualityLaneSpec> LANES = ContractResource.of(
            RESOURCE,
            entry -> AtlasYamlQualityLaneSpec.from(entry, RESOURCE)).load();
    private static final ContractIndex<AtlasYamlQualityLaneSpec> BY_ID =
            ContractIndex.unique(RESOURCE, LANES, AtlasYamlQualityLaneSpec::id);

    private AtlasYamlQualityLaneCatalog() {
    }

    public static List<AtlasYamlQualityLaneSpec> lanes() {
        return LANES;
    }

    public static AtlasYamlQualityLaneSpec require(String id) {
        return BY_ID.require(id);
    }

    public static List<String> duplicateIds() {
        return BY_ID.duplicateIds();
    }
}
