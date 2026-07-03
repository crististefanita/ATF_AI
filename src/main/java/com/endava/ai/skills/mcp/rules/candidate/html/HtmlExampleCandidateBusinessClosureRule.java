package com.endava.ai.skills.mcp.rules.candidate.html;


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

@McpContract(id = "candidate.business-closure.not-weakest",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
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
