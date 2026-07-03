package com.endava.ai.skills.mcp.rules.source.kpi.render;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;

import java.util.List;

public final class KpiMatrixMarkdownRenderer {
    private KpiMatrixMarkdownRenderer() {
    }

    public static String render(List<SourceEvolutionKpiRow> rows) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# MCP Source Evolution KPI Matrix").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("Generated from executable MCP KPI rules. This is the first repair map for diff/KPI close-out.")
                .append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("| Profile | Category | Severity | KPI | Expected | Declared | Status | First blocker | Why | Quality meaning | Observed artifact | Formula | Primary repair artifact | Supporting artifact | Route | Agent action | Cap impact | Publishability impact | Repair command |")
                .append(System.lineSeparator());
        markdown.append("|---|---|---|---|---:|---:|---|---|---|---|---|---|---|---|---|---|---|---|---|")
                .append(System.lineSeparator());
        for (SourceEvolutionKpiRow row : rows) {
            appendMatrixRow(markdown, row);
        }
        markdown.append(System.lineSeparator())
                .append("Final report must link ")
                .append(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER)
                .append(", ")
                .append(McpArtifactPaths.MCP_KPI_MATRIX)
                .append(", ")
                .append(McpArtifactPaths.MCP_KPI_MATRIX_HTML)
                .append(", ")
                .append(McpArtifactPaths.MCP_KPI_FORMULA_TRACE)
                .append(" and ")
                .append(McpArtifactPaths.MCP_KPI_REPAIR_BOARD)
                .append("; it must summarize root_diff_files_count plus protected_root_dirty_count.")
                .append(System.lineSeparator());
        return markdown.toString();
    }

    public static String renderFormulaTrace(List<SourceEvolutionKpiRow> rows) {
        StringBuilder trace = new StringBuilder();
        trace.append("# MCP KPI Formula Trace").append(System.lineSeparator()).append(System.lineSeparator());
        trace.append("Formula trace for source-evolution close-out. Values are generated from the executable MCP inventory, not from prose.")
                .append(System.lineSeparator()).append(System.lineSeparator());
        trace.append("| Profile | Category | KPI | Formula | Source artifact | Expected | Declared | Status | Why | Quality meaning | Agent action | Repair route |")
                .append(System.lineSeparator());
        trace.append("|---|---|---|---|---|---:|---:|---|---|---|---|---|").append(System.lineSeparator());
        for (SourceEvolutionKpiRow row : rows) {
            trace.append("| ")
                    .append(KpiTextEscapes.markdown(row.spec().profile())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().category())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().id())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().formula())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().observedArtifact())).append(" | ")
                    .append(row.expected()).append(" | ")
                    .append(row.declaredText()).append(" | ")
                    .append(row.status()).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().why())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().qualityMeaning())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().agentAction())).append(" | ")
                    .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().route())).append(" |")
                    .append(System.lineSeparator());
        }
        return trace.toString();
    }

    private static void appendMatrixRow(StringBuilder markdown, SourceEvolutionKpiRow row) {
        markdown.append("| ")
                .append(KpiTextEscapes.markdown(row.spec().profile())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().category())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().severity())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().id())).append(" | ")
                .append(row.expected()).append(" | ")
                .append(row.declaredText()).append(" | ")
                .append(row.status()).append(" | ")
                .append(KpiTextEscapes.markdown(row.firstBlocker())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().why())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().qualityMeaning())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().observedArtifact())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().formula())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().primaryRepairArtifact())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().supportingArtifact())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().repairTargetModel().route())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().agentAction())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().capImpact())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().publishabilityImpact())).append(" | ")
                .append(KpiTextEscapes.markdown(row.spec().repairCommand())).append(" |")
                .append(System.lineSeparator());
    }
}
