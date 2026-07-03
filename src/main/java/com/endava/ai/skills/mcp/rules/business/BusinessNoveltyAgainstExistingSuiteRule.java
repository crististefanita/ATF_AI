package com.endava.ai.skills.mcp.rules.business;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@McpContract(id = "business.novelty.against-baseline",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "review/package-state.json")
public final class BusinessNoveltyAgainstExistingSuiteRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String baseline = McpRuleSupport.extractStringValue(state, "existing_coverage_baseline");
        String frontier = McpRuleSupport.extractStringValue(state, "new_frontier");
        if (isBlank(baseline)) {
            result.add(McpPackageViolation.blocker("business-novelty.baseline-missing",
                    "package-state.json must name the existing coverage baseline it is moving beyond",
                    packageState));
        }
        if (isBlank(frontier)) {
            result.add(McpPackageViolation.blocker("business-novelty.frontier-missing",
                    "package-state.json must name the new business frontier of this package",
                    packageState));
        }
        if (!isBlank(baseline) && !isBlank(frontier)
                && baseline.toLowerCase(Locale.ROOT).contains(frontier.toLowerCase(Locale.ROOT))) {
            result.add(McpPackageViolation.blocker("business-novelty.frontier-not-new",
                    "new_frontier must not merely repeat existing_coverage_baseline",
                    packageState));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() < 8;
    }
}
