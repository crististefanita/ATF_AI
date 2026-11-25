package com.ai.core.support.contract.core.yaml;

import com.ai.core.support.contract.yaml.ContractResource;
import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractAnnotationSpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractNarrativeSpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlPolicySpec;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import java.util.List;
import java.util.stream.Collectors;

/** Metadata used by runtime diagnostics and framework regressions. */
public final class AtfCoreYamlContractCatalog {
    public static final String CONTRACT_NARRATIVES_RESOURCE = "atf-core/contracts/narratives/contract-narratives.yml";
    private static final List<AtfCoreYamlContractNarrativeSpec> CONTRACT_NARRATIVES = ContractResource.of(CONTRACT_NARRATIVES_RESOURCE,
            entry -> AtfCoreYamlContractNarrativeSpec.from(entry, CONTRACT_NARRATIVES_RESOURCE)).load();
    public static final String CONTRACT_ANNOTATIONS_RESOURCE = "atf-core/contracts/narratives/contract-annotations.yml";
    private static final List<AtfCoreYamlContractAnnotationSpec> CONTRACT_ANNOTATIONS = ContractResource.of(CONTRACT_ANNOTATIONS_RESOURCE,
            entry -> AtfCoreYamlContractAnnotationSpec.from(entry, CONTRACT_ANNOTATIONS_RESOURCE)).load();
    public static final String CONSOLE_SIGNATURE_RESOURCE = "atf-core/contracts/reporting/console-signature.yml";
    private static final List<AtfCoreYamlPolicySpec> CONSOLE_SIGNATURE = ContractResource.of(CONSOLE_SIGNATURE_RESOURCE,
            entry -> AtfCoreYamlPolicySpec.from(entry, CONSOLE_SIGNATURE_RESOURCE)).load();
    public static final String UI_ENGINE_CONTRACTS_RESOURCE = "atf-core/contracts/ui/ui-engine-contracts.yml";
    private static final List<AtfCoreYamlUiEngineSpec> UI_ENGINE_CONTRACTS = ContractResource.of(UI_ENGINE_CONTRACTS_RESOURCE,
            entry -> AtfCoreYamlUiEngineSpec.from(entry, UI_ENGINE_CONTRACTS_RESOURCE)).load();
    private static final ContractIndex<AtfCoreYamlContractNarrativeSpec> NARRATIVES =
            ContractIndex.unique(CONTRACT_NARRATIVES_RESOURCE, CONTRACT_NARRATIVES, AtfCoreYamlContractNarrativeSpec::id);
    private AtfCoreYamlContractCatalog() {}
    public static List<AtfCoreYamlContractNarrativeSpec> contractNarrativeSpecs() { return CONTRACT_NARRATIVES; }
    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationSpecs() { return CONTRACT_ANNOTATIONS; }
    public static List<AtfCoreYamlPolicySpec> consoleSignatureContracts() { return CONSOLE_SIGNATURE; }
    public static List<AtfCoreYamlUiEngineSpec> uiEngineContracts() { return UI_ENGINE_CONTRACTS; }
    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationProfiles() {
        return CONTRACT_ANNOTATIONS.stream().filter(AtfCoreYamlContractAnnotationSpec::isProfile).collect(Collectors.toUnmodifiableList());
    }
    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationAreas() {
        return CONTRACT_ANNOTATIONS.stream().filter(AtfCoreYamlContractAnnotationSpec::isArea).collect(Collectors.toUnmodifiableList());
    }
    public static AtfCoreYamlContractAnnotationSpec contractAnnotationProfile(String name) {
        return contractAnnotationProfiles().stream().filter(spec -> spec.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing framework profile: " + name));
    }
    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeContract(String id) {
        AtfCoreYamlContractNarrativeSpec spec = NARRATIVES.require(id);
        if (!spec.isContract()) throw new IllegalArgumentException("Wrong narrative kind: " + id);
        return spec;
    }
    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeSuite(String id) {
        AtfCoreYamlContractNarrativeSpec spec = NARRATIVES.require(id);
        if (!spec.isSuite()) throw new IllegalArgumentException("Wrong narrative kind: " + id);
        return spec;
    }
    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeExpectedFailure(String id) {
        AtfCoreYamlContractNarrativeSpec spec = NARRATIVES.require(id);
        if (!spec.isExpectedFailure()) throw new IllegalArgumentException("Wrong narrative kind: " + id);
        return spec;
    }
}
