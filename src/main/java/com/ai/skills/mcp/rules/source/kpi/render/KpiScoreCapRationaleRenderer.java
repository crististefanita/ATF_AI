package com.ai.skills.mcp.rules.source.kpi.render;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;

import java.util.List;
import java.util.stream.Collectors;

public final class KpiScoreCapRationaleRenderer {
    private KpiScoreCapRationaleRenderer() {
    }

    public static String render(List<SourceEvolutionKpiRow> rows) {
        List<SourceEvolutionKpiRow> blockers = statusRows(rows, "FAIL");
        List<SourceEvolutionKpiRow> risks = statusRows(rows, "RISK");
        int cap = recommendedCap(blockers, risks);
        StringBuilder rationale = new StringBuilder();
        rationale.append("# MCP Score Cap Rationale").append(System.lineSeparator()).append(System.lineSeparator());
        rationale.append("Recommended maximum score: ").append(cap).append("/100")
                .append(System.lineSeparator()).append(System.lineSeparator());
        rationale.append("## Why").append(System.lineSeparator()).append(System.lineSeparator());
        if (blockers.isEmpty() && risks.isEmpty()) {
            rationale.append("- No KPI blockers or diagnostic risks were generated. Score can be based on business/report quality.")
                    .append(System.lineSeparator());
        } else {
            appendBlockers(rationale, blockers);
            appendRisks(rationale, risks);
        }
        rationale.append(System.lineSeparator()).append("## Repair board").append(System.lineSeparator()).append(System.lineSeparator())
                .append("- `").append(McpArtifactPaths.MCP_KPI_REPAIR_BOARD).append("`").append(System.lineSeparator())
                .append("- `").append(McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML).append("`").append(System.lineSeparator());
        return rationale.toString();
    }

    private static List<SourceEvolutionKpiRow> statusRows(List<SourceEvolutionKpiRow> rows, String status) {
        return rows.stream()
                .filter(row -> status.equals(row.status()))
                .collect(Collectors.toList());
    }

    private static void appendBlockers(StringBuilder rationale, List<SourceEvolutionKpiRow> blockers) {
        for (SourceEvolutionKpiRow row : blockers) {
            rationale.append("- BLOCKER `").append(row.spec().id()).append("`: ")
                    .append(row.firstBlocker()).append(" | cap impact: ")
                    .append(row.spec().capImpact()).append(System.lineSeparator());
        }
    }

    private static void appendRisks(StringBuilder rationale, List<SourceEvolutionKpiRow> risks) {
        for (SourceEvolutionKpiRow row : risks) {
            rationale.append("- RISK `").append(row.spec().id()).append("`: ")
                    .append(row.firstBlocker()).append(" | agent action: ")
                    .append(row.spec().agentAction()).append(System.lineSeparator());
        }
    }

    private static int recommendedCap(List<SourceEvolutionKpiRow> blockers, List<SourceEvolutionKpiRow> risks) {
        if (!blockers.isEmpty()) {
            return 88;
        }
        for (SourceEvolutionKpiRow risk : risks) {
            if ("direct_ui_engine_leakage_count".equals(risk.spec().id())
                    || "layering_violation_count".equals(risk.spec().id())) {
                return 90;
            }
        }
        return risks.isEmpty() ? 100 : 92;
    }
}
