package com.ai.core.support.contract.core.yaml;

import com.ai.core.support.contract.yaml.ContractResource;
import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.core.support.contract.core.yaml.model.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AtfCoreYamlContractCatalog {
    public static final String PROFILES_RESOURCE = AtfCoreYamlContractResources.PROFILES;
    public static final String BOUNDARIES_RESOURCE = AtfCoreYamlContractResources.BOUNDARIES;
    public static final String SIZE_BUDGETS_RESOURCE = AtfCoreYamlContractResources.SIZE_BUDGETS;
    public static final String REPORTING_OWNERSHIP_RESOURCE = AtfCoreYamlContractResources.REPORTING_OWNERSHIP;
    public static final String REPORTING_ARCHITECTURE_RESOURCE = AtfCoreYamlContractResources.REPORTING_ARCHITECTURE;
    public static final String CORE_ADVISOR_RESOURCE = AtfCoreYamlContractResources.CORE_ADVISOR;
    public static final String UI_ENGINE_CONTRACTS_RESOURCE = AtfCoreYamlContractResources.UI_ENGINE_CONTRACTS;
    public static final String REPORTING_SHELL_RESOURCE = AtfCoreYamlContractResources.REPORTING_SHELL;
    public static final String CONSOLE_SIGNATURE_RESOURCE = AtfCoreYamlContractResources.CONSOLE_SIGNATURE;
    public static final String RUNTIME_OPTIONS_RESOURCE = AtfCoreYamlContractResources.RUNTIME_OPTIONS;
    public static final String CONTRACT_ANNOTATIONS_RESOURCE = AtfCoreYamlContractResources.CONTRACT_ANNOTATIONS;
    public static final String CONTRACT_NARRATIVES_RESOURCE = AtfCoreYamlContractResources.CONTRACT_NARRATIVES;

    private static final List<AtfCoreYamlProfileSpec> PROFILES = load(
            PROFILES_RESOURCE, entry -> AtfCoreYamlProfileSpec.from(entry, PROFILES_RESOURCE));
    private static final List<AtfCoreYamlBoundarySpec> BOUNDARIES = load(
            BOUNDARIES_RESOURCE, entry -> AtfCoreYamlBoundarySpec.from(entry, BOUNDARIES_RESOURCE));
    private static final List<AtfCoreYamlSizeBudgetSpec> SIZE_BUDGETS = load(
            SIZE_BUDGETS_RESOURCE, entry -> AtfCoreYamlSizeBudgetSpec.from(entry, SIZE_BUDGETS_RESOURCE));
    private static final List<AtfCoreYamlReportingOwnershipSpec> REPORTING_OWNERSHIP = load(
            REPORTING_OWNERSHIP_RESOURCE,
            entry -> AtfCoreYamlReportingOwnershipSpec.from(entry, REPORTING_OWNERSHIP_RESOURCE));
    private static final List<AtfCoreYamlReportingArchitectureSpec> REPORTING_ARCHITECTURE = load(
            REPORTING_ARCHITECTURE_RESOURCE,
            entry -> AtfCoreYamlReportingArchitectureSpec.from(entry, REPORTING_ARCHITECTURE_RESOURCE));
    private static final List<AtfCoreYamlAdvisorSpec> CORE_ADVISOR = load(
            CORE_ADVISOR_RESOURCE, entry -> AtfCoreYamlAdvisorSpec.from(entry, CORE_ADVISOR_RESOURCE));
    private static final List<AtfCoreYamlUiEngineSpec> UI_ENGINE_CONTRACTS = load(
            UI_ENGINE_CONTRACTS_RESOURCE, entry -> AtfCoreYamlUiEngineSpec.from(entry, UI_ENGINE_CONTRACTS_RESOURCE));
    private static final List<AtfCoreYamlPolicySpec> REPORTING_SHELL = load(
            REPORTING_SHELL_RESOURCE, entry -> AtfCoreYamlPolicySpec.from(entry, REPORTING_SHELL_RESOURCE));
    private static final List<AtfCoreYamlPolicySpec> CONSOLE_SIGNATURE = load(
            CONSOLE_SIGNATURE_RESOURCE, entry -> AtfCoreYamlPolicySpec.from(entry, CONSOLE_SIGNATURE_RESOURCE));
    private static final List<AtfCoreYamlPolicySpec> RUNTIME_OPTIONS = load(
            RUNTIME_OPTIONS_RESOURCE, entry -> AtfCoreYamlPolicySpec.from(entry, RUNTIME_OPTIONS_RESOURCE));
    private static final List<AtfCoreYamlContractAnnotationSpec> CONTRACT_ANNOTATIONS = load(
            CONTRACT_ANNOTATIONS_RESOURCE,
            entry -> AtfCoreYamlContractAnnotationSpec.from(entry, CONTRACT_ANNOTATIONS_RESOURCE));
    private static final List<AtfCoreYamlContractNarrativeSpec> CONTRACT_NARRATIVES = load(
            CONTRACT_NARRATIVES_RESOURCE,
            entry -> AtfCoreYamlContractNarrativeSpec.from(entry, CONTRACT_NARRATIVES_RESOURCE));
    private static final ContractIndex<AtfCoreYamlProfileSpec> PROFILES_BY_ID =
            ContractIndex.unique(PROFILES_RESOURCE, PROFILES, AtfCoreYamlProfileSpec::id);
    private static final ContractIndex<AtfCoreYamlBoundarySpec> BOUNDARIES_BY_ID =
            ContractIndex.unique(BOUNDARIES_RESOURCE, BOUNDARIES, AtfCoreYamlBoundarySpec::id);
    private static final ContractIndex<AtfCoreYamlSizeBudgetSpec> SIZE_BUDGETS_BY_ID =
            ContractIndex.unique(SIZE_BUDGETS_RESOURCE, SIZE_BUDGETS, AtfCoreYamlSizeBudgetSpec::id);
    private static final ContractIndex<AtfCoreYamlReportingOwnershipSpec> REPORTING_OWNERSHIP_BY_ID =
            ContractIndex.unique(REPORTING_OWNERSHIP_RESOURCE, REPORTING_OWNERSHIP, AtfCoreYamlReportingOwnershipSpec::id);
    private static final ContractIndex<AtfCoreYamlReportingArchitectureSpec> REPORTING_ARCHITECTURE_BY_ID =
            ContractIndex.unique(
                    REPORTING_ARCHITECTURE_RESOURCE,
                    REPORTING_ARCHITECTURE,
                    AtfCoreYamlReportingArchitectureSpec::id);
    private static final ContractIndex<AtfCoreYamlAdvisorSpec> CORE_ADVISOR_BY_ID =
            ContractIndex.unique(CORE_ADVISOR_RESOURCE, CORE_ADVISOR, AtfCoreYamlAdvisorSpec::id);
    private static final ContractIndex<AtfCoreYamlUiEngineSpec> UI_ENGINE_CONTRACTS_BY_ID =
            ContractIndex.unique(UI_ENGINE_CONTRACTS_RESOURCE, UI_ENGINE_CONTRACTS, AtfCoreYamlUiEngineSpec::id);
    private static final ContractIndex<AtfCoreYamlPolicySpec> REPORTING_SHELL_BY_ID =
            ContractIndex.unique(REPORTING_SHELL_RESOURCE, REPORTING_SHELL, AtfCoreYamlPolicySpec::id);
    private static final ContractIndex<AtfCoreYamlPolicySpec> CONSOLE_SIGNATURE_BY_ID =
            ContractIndex.unique(CONSOLE_SIGNATURE_RESOURCE, CONSOLE_SIGNATURE, AtfCoreYamlPolicySpec::id);
    private static final ContractIndex<AtfCoreYamlPolicySpec> RUNTIME_OPTIONS_BY_ID =
            ContractIndex.unique(RUNTIME_OPTIONS_RESOURCE, RUNTIME_OPTIONS, AtfCoreYamlPolicySpec::id);
    private static final ContractIndex<AtfCoreYamlContractAnnotationSpec> CONTRACT_ANNOTATIONS_BY_ID =
            ContractIndex.unique(CONTRACT_ANNOTATIONS_RESOURCE, CONTRACT_ANNOTATIONS, AtfCoreYamlContractAnnotationSpec::id);
    private static final ContractIndex<AtfCoreYamlContractNarrativeSpec> CONTRACT_NARRATIVES_BY_ID =
            ContractIndex.unique(CONTRACT_NARRATIVES_RESOURCE, CONTRACT_NARRATIVES, AtfCoreYamlContractNarrativeSpec::id);

    private AtfCoreYamlContractCatalog() {
    }

    public static List<AtfCoreYamlProfileSpec> profiles() {
        return PROFILES;
    }

    public static List<AtfCoreYamlBoundarySpec> boundaries() {
        return BOUNDARIES;
    }

    public static List<AtfCoreYamlSizeBudgetSpec> sizeBudgets() {
        return SIZE_BUDGETS;
    }

    public static List<AtfCoreYamlReportingOwnershipSpec> reportingOwnership() {
        return REPORTING_OWNERSHIP;
    }

    public static List<AtfCoreYamlReportingArchitectureSpec> reportingArchitecture() {
        return REPORTING_ARCHITECTURE;
    }

    public static List<AtfCoreYamlAdvisorSpec> coreAdvisorContracts() {
        return CORE_ADVISOR;
    }

    public static List<AtfCoreYamlUiEngineSpec> uiEngineContracts() {
        return UI_ENGINE_CONTRACTS;
    }

    public static List<AtfCoreYamlPolicySpec> reportingShellContracts() {
        return REPORTING_SHELL;
    }

    public static List<AtfCoreYamlPolicySpec> consoleSignatureContracts() {
        return CONSOLE_SIGNATURE;
    }

    public static List<AtfCoreYamlPolicySpec> runtimeOptionsContracts() {
        return RUNTIME_OPTIONS;
    }

    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationSpecs() {
        return CONTRACT_ANNOTATIONS;
    }

    public static List<AtfCoreYamlContractNarrativeSpec> contractNarrativeSpecs() {
        return CONTRACT_NARRATIVES;
    }

    public static List<AtfCoreYamlContractNarrativeSpec> contractNarrativeContracts() {
        return CONTRACT_NARRATIVES.stream()
                .filter(AtfCoreYamlContractNarrativeSpec::isContract)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<AtfCoreYamlContractNarrativeSpec> contractNarrativeSuites() {
        return CONTRACT_NARRATIVES.stream()
                .filter(AtfCoreYamlContractNarrativeSpec::isSuite)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<AtfCoreYamlContractNarrativeSpec> contractNarrativeExpectedFailures() {
        return CONTRACT_NARRATIVES.stream()
                .filter(AtfCoreYamlContractNarrativeSpec::isExpectedFailure)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationProfiles() {
        return CONTRACT_ANNOTATIONS.stream()
                .filter(AtfCoreYamlContractAnnotationSpec::isProfile)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<AtfCoreYamlContractAnnotationSpec> contractAnnotationAreas() {
        return CONTRACT_ANNOTATIONS.stream()
                .filter(AtfCoreYamlContractAnnotationSpec::isArea)
                .collect(Collectors.toUnmodifiableList());
    }

    public static AtfCoreYamlContractAnnotationSpec contractAnnotationProfile(String name) {
        return contractAnnotationProfiles().stream()
                .filter(spec -> spec.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing ATF contract profile YAML spec: " + name));
    }

    public static AtfCoreYamlProfileSpec requireProfile(String id) {
        return PROFILES_BY_ID.require(id);
    }

    public static AtfCoreYamlBoundarySpec requireBoundary(String id) {
        return BOUNDARIES_BY_ID.require(id);
    }

    public static AtfCoreYamlSizeBudgetSpec requireSizeBudget(String id) {
        return SIZE_BUDGETS_BY_ID.require(id);
    }

    public static AtfCoreYamlReportingOwnershipSpec requireReportingOwnership(String id) {
        return REPORTING_OWNERSHIP_BY_ID.require(id);
    }

    public static AtfCoreYamlReportingArchitectureSpec requireReportingArchitecture(String id) {
        return REPORTING_ARCHITECTURE_BY_ID.require(id);
    }

    public static AtfCoreYamlAdvisorSpec requireCoreAdvisor(String id) {
        return CORE_ADVISOR_BY_ID.require(id);
    }

    public static AtfCoreYamlUiEngineSpec requireUiEngineContract(String id) {
        return UI_ENGINE_CONTRACTS_BY_ID.require(id);
    }

    public static AtfCoreYamlPolicySpec requireReportingShellContract(String id) {
        return REPORTING_SHELL_BY_ID.require(id);
    }

    public static AtfCoreYamlPolicySpec requireConsoleSignatureContract(String id) {
        return CONSOLE_SIGNATURE_BY_ID.require(id);
    }

    public static AtfCoreYamlPolicySpec requireRuntimeOptionsContract(String id) {
        return RUNTIME_OPTIONS_BY_ID.require(id);
    }

    public static AtfCoreYamlContractAnnotationSpec requireContractAnnotation(String id) {
        return CONTRACT_ANNOTATIONS_BY_ID.require(id);
    }

    public static AtfCoreYamlContractNarrativeSpec requireContractNarrative(String id) {
        return CONTRACT_NARRATIVES_BY_ID.require(id);
    }

    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeContract(String id) {
        AtfCoreYamlContractNarrativeSpec spec = requireContractNarrative(id);
        if (!spec.isContract()) {
            throw new IllegalArgumentException("ATF contract narrative is not kind=contract: " + id);
        }
        return spec;
    }

    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeSuite(String id) {
        AtfCoreYamlContractNarrativeSpec spec = requireContractNarrative(id);
        if (!spec.isSuite()) {
            throw new IllegalArgumentException("ATF contract narrative is not kind=suite: " + id);
        }
        return spec;
    }

    public static AtfCoreYamlContractNarrativeSpec requireContractNarrativeExpectedFailure(String id) {
        AtfCoreYamlContractNarrativeSpec spec = requireContractNarrative(id);
        if (!spec.isExpectedFailure()) {
            throw new IllegalArgumentException("ATF contract narrative is not kind=expectedFailure: " + id);
        }
        return spec;
    }

    public static List<String> duplicateProfileIds() {
        return PROFILES_BY_ID.duplicateIds();
    }

    public static List<String> duplicateBoundaryIds() {
        return BOUNDARIES_BY_ID.duplicateIds();
    }

    public static List<String> duplicateSizeBudgetIds() {
        return SIZE_BUDGETS_BY_ID.duplicateIds();
    }

    public static List<String> duplicateReportingOwnershipIds() {
        return REPORTING_OWNERSHIP_BY_ID.duplicateIds();
    }

    public static List<String> duplicateReportingArchitectureIds() {
        return REPORTING_ARCHITECTURE_BY_ID.duplicateIds();
    }

    public static List<String> duplicateCoreAdvisorIds() {
        return CORE_ADVISOR_BY_ID.duplicateIds();
    }

    public static List<String> duplicateUiEngineContractIds() {
        return UI_ENGINE_CONTRACTS_BY_ID.duplicateIds();
    }

    public static List<String> duplicateReportingShellIds() {
        return REPORTING_SHELL_BY_ID.duplicateIds();
    }

    public static List<String> duplicateConsoleSignatureIds() {
        return CONSOLE_SIGNATURE_BY_ID.duplicateIds();
    }

    public static List<String> duplicateRuntimeOptionsIds() {
        return RUNTIME_OPTIONS_BY_ID.duplicateIds();
    }

    public static List<String> duplicateContractAnnotationIds() {
        return CONTRACT_ANNOTATIONS_BY_ID.duplicateIds();
    }

    public static List<String> duplicateContractNarrativeIds() {
        return CONTRACT_NARRATIVES_BY_ID.duplicateIds();
    }

    private static <T> List<T> load(String resource, Function<Map<String, String>, T> mapper) {
        return ContractResource.of(resource, mapper).load();
    }
}
