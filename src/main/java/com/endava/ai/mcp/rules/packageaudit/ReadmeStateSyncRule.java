package com.endava.ai.mcp.rules.packageaudit;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "package.readme.state-sync",
        category = com.endava.ai.mcp.contract.McpRuleCategory.PACKAGE,
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
    }
}
