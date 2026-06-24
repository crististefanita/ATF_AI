package com.endava.ai.mcp.rules.candidate;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "candidate.business-closure.not-weakest",
        category = com.endava.ai.mcp.contract.McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "review/package-state.json")
public final class HtmlExampleCandidateBusinessClosureRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String weakestAxis = McpRuleSupport.extractStringValue(state, "weakest_axis");
        String capReason = McpRuleSupport.extractStringValue(state, "cap_reason");

        if (containsBusinessClosureWeakness(weakestAxis) || containsBusinessClosureWeakness(capReason)) {
            result.add(McpPackageViolation.blocker("candidate.business-closure.weakest-axis",
                    "html-ex candidate cannot have business closure as its weakest/capping reason",
                    packageState));
        }
    }

    private boolean containsBusinessClosureWeakness(String value) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("business closure")
                || lower.contains("closure")
                || lower.contains("not owned")
                || lower.contains("not closed")
                || lower.contains("missing durable")
                || lower.contains("not materialized");
    }
}
