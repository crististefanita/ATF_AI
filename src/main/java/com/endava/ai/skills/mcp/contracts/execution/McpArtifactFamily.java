package com.endava.ai.skills.mcp.contracts.execution;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

@Getter
@Accessors(fluent = true)
public enum McpArtifactFamily {
    EXTENT(List.of(
            List.of(McpArtifactPaths.MCP_EXTENT_REPORT),
            List.of(McpArtifactPaths.MCP_EXTENT_SHA256),
            List.of(McpArtifactPaths.MCP_EXTENT_ORIGIN))),
    TESTNG_XML(List.of(
            List.of(McpArtifactPaths.MCP_TESTNG_XML, McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML))),
    GATE(List.of(
            List.of(McpArtifactPaths.MCP_GATE_RESULT))),
    METHOD_LEDGER(List.of(
            List.of(McpArtifactPaths.MCP_METHOD_LEDGER))),
    KPI(List.of(
            List.of(McpArtifactPaths.MCP_KPI_MATRIX),
            List.of(McpArtifactPaths.MCP_KPI_MATRIX_HTML),
            List.of(McpArtifactPaths.MCP_KPI_FORMULA_TRACE),
            List.of(McpArtifactPaths.MCP_KPI_REPAIR_BOARD),
            List.of(McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML),
            List.of(McpArtifactPaths.MCP_SCORE_CAP_RATIONALE))),
    FINAL_REPORT(List.of(
            List.of(McpArtifactPaths.FINAL_REPORT))),
    SOURCE_EVOLUTION(List.of(
            List.of(McpArtifactPaths.PACKAGE_STATE),
            List.of(McpArtifactPaths.ROOT_DIFF),
            List.of(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER)));

    private final List<List<String>> artifactAlternatives;

    McpArtifactFamily(List<List<String>> artifactAlternatives) {
        this.artifactAlternatives = artifactAlternatives;
    }

    public boolean covers(String artifact) {
        return artifactAlternatives.stream()
                .flatMap(List::stream)
                .anyMatch(candidate -> candidate.equals(artifact));
    }

    public static boolean coversAny(String artifact) {
        return Arrays.stream(values()).anyMatch(family -> family.covers(artifact));
    }
}
