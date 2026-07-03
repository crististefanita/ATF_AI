package com.ai.skills.mcp.rules.source.kpi;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;
import com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixHtmlRenderer;
import com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixMarkdownRenderer;
import com.ai.skills.mcp.rules.source.kpi.render.KpiRepairBoardRenderer;
import com.ai.skills.mcp.rules.source.kpi.render.KpiScoreCapRationaleRenderer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds executable source-evolution KPI rows and delegates artifact rendering.
 */
final class SourceEvolutionKpiMatrix {
    static final String MATRIX_ARTIFACT = McpArtifactPaths.MCP_KPI_MATRIX;

    private SourceEvolutionKpiMatrix() {
    }

    static void write(McpPackageContext context,
                      SourceEvolutionInventory inventory,
                      Map<String, Integer> declaredValues) {
        List<SourceEvolutionKpiRow> rows = rows(inventory, declaredValues);
        Path matrix = context.packageRoot().resolve(MATRIX_ARTIFACT);
        try {
            Files.createDirectories(matrix.getParent());
            Files.writeString(matrix, KpiMatrixMarkdownRenderer.render(rows), StandardCharsets.UTF_8);
            Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_KPI_MATRIX_HTML),
                    KpiMatrixHtmlRenderer.renderMatrix(rows), StandardCharsets.UTF_8);
            Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_KPI_FORMULA_TRACE),
                    KpiMatrixMarkdownRenderer.renderFormulaTrace(rows), StandardCharsets.UTF_8);
            Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_KPI_REPAIR_BOARD),
                    KpiRepairBoardRenderer.renderMarkdown(rows), StandardCharsets.UTF_8);
            Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML),
                    KpiRepairBoardRenderer.renderHtml(rows), StandardCharsets.UTF_8);
            Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_SCORE_CAP_RATIONALE),
                    KpiScoreCapRationaleRenderer.render(rows), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // The validation result remains authoritative; matrix emission is diagnostic help for the repairing agent.
        }
    }

    private static List<SourceEvolutionKpiRow> rows(SourceEvolutionInventory inventory,
                                                    Map<String, Integer> declaredValues) {
        List<SourceEvolutionKpiRow> rows = new ArrayList<>();
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.rootKpis()) {
            rows.add(row(spec, inventory.expectedValue(spec.id()), declaredValues.get(spec.id())));
        }
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.diagnosticKpis()) {
            rows.add(row(spec, inventory.expectedValue(spec.id()), declaredValues.get(spec.id())));
        }
        for (Map.Entry<String, Integer> run : inventory.runFileCounts().entrySet()) {
            String runName = run.getKey().replace("_files_count", "");
            SourceEvolutionKpiCatalog.KpiSpec spec = SourceEvolutionKpiCatalog.runFilesCountSpec(run.getKey(), runName);
            rows.add(row(spec, run.getValue(), declaredValues.get(run.getKey())));
        }
        return rows;
    }

    private static SourceEvolutionKpiRow row(SourceEvolutionKpiCatalog.KpiSpec spec,
                                             int expected,
                                             Integer declared) {
        String status;
        String firstBlocker = "";
        if (declared == null && "INFO".equals(spec.severity())) {
            firstBlocker = diagnosticRisk(spec, expected);
            status = firstBlocker.isBlank() ? "INFO" : "RISK";
        } else if (declared != null && declared == expected) {
            status = "PASS";
        } else {
            status = "FAIL";
            firstBlocker = declared == null
                    ? "missing numeric value"
                    : "declared " + declared + " but expected " + expected;
        }
        return SourceEvolutionKpiRow.of(spec, expected, declared, status, firstBlocker);
    }

    private static String diagnosticRisk(SourceEvolutionKpiCatalog.KpiSpec spec, int expected) {
        switch (spec.id()) {
            case "final_report_mcp_link_completeness":
            case "artifact_reopenability_score":
            case "closeout_profile_purity":
            case "mcp_extent_freshness":
                return expected < 100 ? "expected 100 but observed " + expected : "";
            case "direct_ui_engine_leakage_count":
            case "layering_violation_count":
            case "skipped_closeout_count":
                return expected > 0 ? "expected 0 or explicit cap, observed " + expected : "";
            case "duplicate_action_line_ratio":
            case "duplicate_assertion_line_ratio":
            case "test_duplication_signal":
                return expected > 25 ? "duplication ratio above 25: observed " + expected : "";
            case "assertion_locality_score":
            case "support_cohesion_score":
                return expected > 0 && expected < 70 ? "quality score below 70: observed " + expected : "";
            default:
                return "";
        }
    }
}
