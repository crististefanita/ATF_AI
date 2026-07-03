package com.endava.ai.skills.mcp.rules.source.kpi;

import lombok.RequiredArgsConstructor;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent-facing KPI catalog inspired by Atlas rule specs.
 * Static KPI metadata is loaded from YAML; Java remains responsible for executable validation.
 */
public final class SourceEvolutionKpiCatalog {
    private static final String CLOSEOUT_PROFILE = "package-closeout";

    private SourceEvolutionKpiCatalog() {
    }

    public static List<KpiSpec> rootKpis() {
        return ContractCatalog.sourceEvolutionKpisBySeverity("BLOCKER").stream()
                .map(SourceEvolutionKpiCatalog::fromYaml)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<KpiSpec> diagnosticKpis() {
        return ContractCatalog.sourceEvolutionKpisBySeverity("INFO").stream()
                .map(SourceEvolutionKpiCatalog::fromYaml)
                .collect(Collectors.toUnmodifiableList());
    }

    public static KpiSpec runFilesCountSpec(String id, String run) {
        return new KpiSpec(id,
                CLOSEOUT_PROFILE,
                "source",
                "BLOCKER",
                "runs/" + run + "/" + McpRunArtifacts.DIFF,
                "count distinct src/** paths in runs/" + run + "/review/thin-no-index.diff",
                McpKpiRepairTarget.of("runs/" + run + "/" + McpRunArtifacts.DIFF,
                        McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
                        McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
                        "source-evolution-run-delta"),
                "Proves every kept run has a measurable source delta row.",
                "A missing or mismatched run counter means the iteration history is not auditable.",
                "update run_" + run.substring(4) + "_files_count from the run diff",
                "Recalculate run diff evidence and update source-evolution-ledger.md.",
                "regenerate runs/" + run + "/review/thin-no-index.diff and update run_" + run.substring(4) + "_files_count",
                "95+ blocked until this run KPI matches source evidence",
                "package close-out cannot be trusted when kept run deltas are not accounted");
    }

    private static KpiSpec fromYaml(com.endava.ai.skills.mcp.contracts.model.KpiSpec spec) {
        return new KpiSpec(spec.id(),
                spec.profile(),
                spec.category(),
                spec.severity(),
                spec.sourceArtifact(),
                spec.formula(),
                McpKpiRepairTarget.of(spec.repair().observedArtifact(),
                        spec.repair().primaryArtifact(),
                        spec.repair().supportingArtifact(),
                        spec.repair().route()),
                spec.why(),
                spec.qualityMeaning(),
                spec.agentAction(),
                spec.repairHint(),
                spec.repairCommand(),
                spec.capImpact(),
                spec.publishabilityImpact());
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class KpiSpec {
        private final String id;
        private final String profile;
        private final String category;
        private final String severity;
        private final String sourceArtifact;
        private final String formula;
        @Getter(AccessLevel.NONE)
        private final McpKpiRepairTarget repairTarget;
        private final String why;
        private final String qualityMeaning;
        private final String agentAction;
        private final String repairHint;
        private final String repairCommand;
        private final String capImpact;
        private final String publishabilityImpact;

        public String repairTarget() {
            return repairTarget.primaryRepairArtifact();
        }

        public McpKpiRepairTarget repairTargetModel() {
            return repairTarget;
        }

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Accessors(fluent = true)
    public static final class McpKpiRepairTarget {
        private final String observedArtifact;
        private final String primaryRepairArtifact;
        private final String supportingArtifact;
        private final String route;

        static McpKpiRepairTarget of(String observedArtifact,
                                     String primaryRepairArtifact,
                                     String supportingArtifact,
                                     String route) {
            return new McpKpiRepairTarget(observedArtifact, primaryRepairArtifact, supportingArtifact, route);
        }
    }
}
