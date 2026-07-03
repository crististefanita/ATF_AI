package com.endava.ai.skills.mcp.rules.packageaudit;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@McpContract(id = "package.artifact-spine.required",
        category = McpRuleCategory.PACKAGE,
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
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT), "framework properties snapshot", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.FRONTIER_RANKING_LEDGER), "frontier ranking ledger", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.NEXT_RUN_ELIGIBILITY_CARD), "next-run eligibility card", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.ROUND_PRETRAINING_BRIEF), "round pretraining brief", result);
        files.requireNonEmptyFile(context.paths().packageCloseOut(), "package close-out", result);
        files.requireNonEmptyFile(root.resolve(McpArtifactPaths.FROZEN_PACKAGE_INVENTORY), "frozen package inventory", result);
        files.requireNonEmptyFile(context.paths().rootDiff(), "root git diff", result);
        requireFrameworkSnapshotMatchesAuditedKeys(context, result);
    }

    private void requireFrameworkSnapshotMatchesAuditedKeys(McpPackageContext context,
                                                            McpPackageValidationResult result) {
        Path source = context.projectRoot().resolve("src/main/resources/framework.properties").normalize();
        Path snapshot = context.packageRoot().resolve(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT).normalize();
        if (!Files.isRegularFile(source) || !Files.isRegularFile(snapshot)) {
            return;
        }
        try {
            Properties sourceProperties = load(source);
            Properties snapshotProperties = load(snapshot);
            for (String key : ContractCatalog.packagePolicyTokens("framework-audit-key")) {
                String sourceValue = sourceProperties.getProperty(key);
                String snapshotValue = snapshotProperties.getProperty(key);
                if (sourceValue == null) {
                    continue;
                }
                if (snapshotValue == null) {
                    result.add(McpPackageViolation.blocker("framework-properties.snapshot.key-missing",
                            "framework.properties snapshot is missing audited key: " + key,
                            snapshot));
                } else if (!sourceValue.trim().equals(snapshotValue.trim())) {
                    result.add(McpPackageViolation.blocker("framework-properties.snapshot.key-drift",
                            "framework.properties snapshot key drift for " + key
                                    + ": source=" + sourceValue.trim()
                                    + ", snapshot=" + snapshotValue.trim(),
                            snapshot));
                }
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("framework-properties.snapshot.unreadable",
                    exception.getMessage(), snapshot));
        }
    }

    private Properties load(Path path) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        return properties;
    }
}
