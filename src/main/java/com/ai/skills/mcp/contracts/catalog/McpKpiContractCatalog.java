package com.ai.skills.mcp.contracts.catalog;

import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.skills.mcp.contracts.model.KpiSpec;
import com.ai.skills.mcp.contracts.yaml.ContractSpecLoader;

import java.util.List;
import java.util.stream.Collectors;

final class McpKpiContractCatalog {
    private static final ContractLazy<List<KpiSpec>> SOURCE_EVOLUTION_KPIS = ContractLazy.of(
            McpContractResources.SOURCE_EVOLUTION_KPI,
            () -> ContractSpecLoader.loadKpis(McpContractResources.SOURCE_EVOLUTION_KPI));
    private static final ContractLazy<ContractIndex<KpiSpec>> SOURCE_EVOLUTION_KPIS_BY_ID = ContractLazy.of(
            McpContractResources.SOURCE_EVOLUTION_KPI + "#by-id",
            () -> ContractIndex.unique(McpContractResources.SOURCE_EVOLUTION_KPI, SOURCE_EVOLUTION_KPIS.get(), KpiSpec::id));

    private McpKpiContractCatalog() {
    }

    static List<KpiSpec> sourceEvolutionKpis() {
        return SOURCE_EVOLUTION_KPIS.get();
    }

    static List<KpiSpec> sourceEvolutionKpisBySeverity(String severity) {
        return SOURCE_EVOLUTION_KPIS.get().stream()
                .filter(spec -> spec.severity().equalsIgnoreCase(severity))
                .collect(Collectors.toUnmodifiableList());
    }

    static KpiSpec requireSourceEvolutionKpi(String id) {
        return SOURCE_EVOLUTION_KPIS_BY_ID.get().require(id);
    }
}
