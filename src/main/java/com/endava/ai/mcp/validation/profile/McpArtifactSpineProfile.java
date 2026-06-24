package com.endava.ai.mcp.validation.profile;

import com.endava.ai.mcp.context.McpArtifactPaths;

import java.util.List;

public final class McpArtifactSpineProfile {
    private McpArtifactSpineProfile() {
    }

    public static List<String> packageArtifacts() {
        return List.of(
                McpArtifactPaths.README,
                McpArtifactPaths.CALIBRATION,
                McpArtifactPaths.PACKAGE_MANIFEST,
                McpArtifactPaths.INPUT_SNAPSHOT,
                McpArtifactPaths.FINAL_REPORT,
                McpArtifactPaths.REPORT_CSS,
                McpArtifactPaths.RENDER_PROOF,
                McpArtifactPaths.PACKAGE_STATE,
                McpArtifactPaths.EXECUTION_GATE_CARD,
                McpArtifactPaths.QUALITY_ACCOUNTING_VERDICT,
                McpArtifactPaths.PACKAGE_CLOSE_OUT,
                McpArtifactPaths.ROOT_DIFF);
    }

    public static List<String> runArtifacts() {
        return List.of(
                "SUMMARY.md",
                "manifests/run-manifest.md",
                "prompt/input-snapshot.md",
                "review/run-state.json",
                "review/artifact-checklist.md",
                "review/score-support.md",
                "review/business-understanding.md",
                "review/thin-no-index.diff");
    }

    public static List<String> proofFamilies() {
        return List.of(
                "diff",
                "run-state.json",
                "ExtentReport.html",
                "testng-results.xml",
                "command-result.md",
                "mcp-gate-result.md",
                "canonical-render-check.png",
                "local-link-check.txt");
    }

    public static List<String> proofMappingTokens() {
        return List.of(
                "claim",
                "diff",
                "Extent",
                "XML",
                "run-state.json");
    }

    public static List<String> artifactIndexTokens() {
        return List.of(
                "package-state.json",
                "root-source-thin-no-index.diff",
                "local-link-check.txt",
                "canonical-render-check.png");
    }
}
