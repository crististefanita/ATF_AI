package com.ai.skills.mcp.rules.packageaudit;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;

@McpContract(id = "package.accounting.runtime-window",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "review/package-state.json")
public final class PackageAccountingRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        var packageState = context.paths().packageState();
        McpRuleSupport.requireRuntimeTruth(packageState, context.runtime(), result, context);
        if (!Files.isRegularFile(packageState)) {
            return;
        }

        String content = context.files().read(packageState, result);
        int runCount = context.files().runDirectories(context.paths().runsRoot(), result).size();
        int expectedRuns = context.runtime().metaIterationCount();
        int expectedTests = context.runtime().roundTargetTestCount() * context.runtime().metaIterationCount();
        McpRuleSupport.requireNumericValue(content, packageState, result, "accounting.runs-expected",
                "Package state should publish runs_expected from META_ITERATION_COUNT",
                expectedRuns, "runs_expected", "runs_required", "meta_iteration_count", "META_ITERATION_COUNT");
        McpRuleSupport.requireNumericValue(content, packageState, result, "accounting.runs-completed",
                "Package state should publish completed run count matching actual run folders",
                runCount, "runs_completed", "runs_consumed", "runs_kept");
        McpRuleSupport.requireNumericValue(content, packageState, result, "accounting.tests-expected",
                "Package state should publish total expected tests as ROUND_TARGET_TEST_COUNT * META_ITERATION_COUNT",
                expectedTests, "tests_expected", "tests_required", "tests_target_total");

        if (runCount != expectedRuns) {
            result.add(McpPackageViolation.blocker("accounting.run-window-mismatch",
                    "Actual run folders must equal META_ITERATION_COUNT: expected "
                            + expectedRuns + ", found " + runCount,
                    context.paths().runsRoot()));
        }
    }
}
