package com.endava.ai.mcp.rules.packageaudit;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpPackageContext;

@com.endava.ai.mcp.contract.McpContract(id = "package.artifact-spine.required",
        category = com.endava.ai.mcp.contract.McpRuleCategory.PACKAGE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "docs/out/<timestamp>/**")
public final class PackageArtifactSpineRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        var files = context.files();
        var root = context.packageRoot();
        files.requireDirectory(root, "package root", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.README), "package README", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.CALIBRATION), "calibration note", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.PACKAGE_MANIFEST), "package manifest", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.INPUT_SNAPSHOT), "package input snapshot", result);
        files.requireNonEmptyFile(context.paths().report(), "business understanding report", result);
        files.requireNonEmptyFile(context.paths().reportCss(), "shared report CSS", result);
        files.requireNonEmptyFile(context.paths().renderProof(), "render proof screenshot", result);
        files.requireNonEmptyFile(context.paths().packageState(), "package state", result);
        files.requireNonEmptyFile(context.paths().executionGateCard(), "execution gate card", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.QUALITY_ACCOUNTING_VERDICT), "quality accounting verdict", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.FRONTIER_RANKING_LEDGER), "frontier ranking ledger", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.NEXT_RUN_ELIGIBILITY_CARD), "next-run eligibility card", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.ROUND_PRETRAINING_BRIEF), "round pretraining brief", result);
        files.requireNonEmptyFile(context.paths().packageCloseOut(), "package close-out", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.FROZEN_PACKAGE_INVENTORY), "frozen package inventory", result);
        files.requireNonEmptyFile(context.paths().rootDiff(), "root git diff", result);
    }
}
