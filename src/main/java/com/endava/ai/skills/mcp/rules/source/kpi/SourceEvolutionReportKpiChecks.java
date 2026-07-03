package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class SourceEvolutionReportKpiChecks {
    private SourceEvolutionReportKpiChecks() {
    }

    static void requireFinalReportKpiSummary(McpPackageContext context,
                                             String ledgerText,
                                             SourceEvolutionInventory inventory,
                                             McpPackageValidationResult result) {
        Path finalReport = context.packageRoot().resolve(McpArtifactPaths.FINAL_REPORT);
        if (!Files.isRegularFile(finalReport)) {
            return;
        }
        String normalizedReport = context.files().read(finalReport, result).toLowerCase(Locale.ROOT);
        String normalizedLedger = ledgerText.toLowerCase(Locale.ROOT);
        if (!normalizedReport.contains(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER)) {
            return;
        }
        if (!linksAllKpiArtifacts(normalizedReport)) {
            result.add(McpPackageViolation.blocker("source-evolution.report-kpi-artifacts.missing",
                    "Final report must link source-evolution ledger, KPI matrix MD/HTML, formula trace, repair board MD/HTML and score cap rationale",
                    finalReport));
        }
        if (!containsReportKpi(normalizedReport, normalizedLedger, "root_diff_files_count",
                inventory.expectedValue("root_diff_files_count"))
                || !containsReportKpi(normalizedReport, normalizedLedger, "protected_root_dirty_count",
                inventory.expectedValue("protected_root_dirty_count"))) {
            result.add(McpPackageViolation.blocker("source-evolution.report-kpi-summary.missing",
                    "Final report links source-evolution-ledger.md but does not summarize matching numeric source KPIs",
                    finalReport));
        }
    }

    private static boolean linksAllKpiArtifacts(String normalizedReport) {
        return normalizedReport.contains(McpArtifactPaths.MCP_KPI_MATRIX)
                && normalizedReport.contains(McpArtifactPaths.MCP_KPI_MATRIX_HTML)
                && normalizedReport.contains(McpArtifactPaths.MCP_KPI_FORMULA_TRACE)
                && normalizedReport.contains(McpArtifactPaths.MCP_KPI_REPAIR_BOARD)
                && normalizedReport.contains(McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML)
                && normalizedReport.contains(McpArtifactPaths.MCP_SCORE_CAP_RATIONALE);
    }

    private static boolean containsReportKpi(String normalizedReport,
                                             String normalizedLedger,
                                             String key,
                                             int expectedValue) {
        return normalizedLedger.contains(key)
                && normalizedReport.contains(key)
                && SourceEvolutionKpiReader.readNumericValue(normalizedReport, key) != null
                && SourceEvolutionKpiReader.readNumericValue(normalizedReport, key) == expectedValue;
    }
}
