package com.endava.ai.skills.mcp.rules.packageaudit;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

@McpContract(id = "package.readme.state-sync",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "README.md")
public final class ReadmeStateSyncRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        var readme = context.paths().readme();
        var packageState = context.paths().packageState();
        if (!Files.isRegularFile(readme) || !Files.isRegularFile(packageState)) {
            return;
        }

        String readmeText = context.files().read(readme, result).toLowerCase(Locale.ROOT);
        String state = context.files().read(packageState, result);
        Integer runsCompleted = McpRuleSupport.extractIntValue(state, "runs_completed");
        Integer testsProduced = McpRuleSupport.extractIntValue(state, "tests_produced");
        String status = McpRuleSupport.extractStringValue(state, "status");
        if (runsCompleted != null && runsCompleted >= context.runtime().metaIterationCount()
                && McpRuleSupport.containsAny(readmeText, "run 01 is green", "not yet executed", "not executed", "not launched")) {
            result.add(McpPackageViolation.blocker("readme.package-state.stale",
                    "README contradicts package-state.json: package-state says runs_completed="
                            + runsCompleted + ", but README still describes a partial one-run package",
                    readme));
        }
        if (status != null && status.toLowerCase(Locale.ROOT).contains("closed") && readmeText.contains("partial")) {
            result.add(McpPackageViolation.blocker("readme.package-state.stale",
                    "README says partial while package-state.json status is " + status,
                    readme));
        }
        if (runsCompleted != null && !readmeText.contains(String.valueOf(runsCompleted))) {
            result.add(McpPackageViolation.warning("readme.package-state.accounting-missing",
                    "README should echo runs_completed from package-state.json: " + runsCompleted,
                    readme));
        }
        if (testsProduced != null && !readmeText.contains(String.valueOf(testsProduced))) {
            result.add(McpPackageViolation.warning("readme.package-state.accounting-missing",
                    "README should echo tests_produced from package-state.json: " + testsProduced,
                    readme));
        }
        requireReadmeMcpEvidenceLinks(context, result, readmeText);
    }

    private void requireReadmeMcpEvidenceLinks(McpPackageContext context,
                                               McpPackageValidationResult result,
                                               String readmeText) {
        boolean mcpEvidenceStarted = Files.isDirectory(context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT))
                || Files.isRegularFile(context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT));
        if (!mcpEvidenceStarted) {
            return;
        }
        for (String artifact : List.of(
                McpArtifactPaths.MCP_EXTENT_REPORT,
                McpArtifactPaths.MCP_GATE_RESULT,
                McpArtifactPaths.MCP_METHOD_LEDGER)) {
            if (!readmeText.contains(artifact.toLowerCase(Locale.ROOT))) {
                result.add(McpPackageViolation.blocker("readme.mcp-evidence-link.missing",
                        "README must explicitly link final MCP close-out artifact: " + artifact,
                        context.paths().readme()));
            }
        }
        boolean hasXml = readmeText.contains(McpArtifactPaths.MCP_TESTNG_XML.toLowerCase(Locale.ROOT))
                || readmeText.contains(McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML.toLowerCase(Locale.ROOT));
        if (!hasXml) {
            result.add(McpPackageViolation.blocker("readme.mcp-evidence-link.missing",
                    "README must explicitly link the final MCP TestNG XML artifact",
                    context.paths().readme()));
        }
    }
}
