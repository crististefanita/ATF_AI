package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;

final class SourceEvolutionKpiArtifactChecks {
    private SourceEvolutionKpiArtifactChecks() {
    }

    static void requireGeneratedKpiArtifacts(McpPackageContext context, McpPackageValidationResult result) {
        for (String artifact : new String[]{
                McpArtifactPaths.MCP_KPI_MATRIX,
                McpArtifactPaths.MCP_KPI_MATRIX_HTML,
                McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML,
                McpArtifactPaths.MCP_SCORE_CAP_RATIONALE}) {
            Path path = context.packageRoot().resolve(artifact);
            if (!Files.isRegularFile(path)) {
                result.add(McpPackageViolation.blocker("source-evolution.kpi.artifact-missing",
                        "MCP package close-out must publish generated KPI artifact: " + artifact,
                        path));
            }
        }
    }
}
