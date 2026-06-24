package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "candidate.positive-ownership",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "review/package-state.json")
public final class CandidatePositiveOwnershipRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String businessObject = McpRuleSupport.extractStringValue(state, "business_object");
        String lower = CandidateTextSupport.normalize(businessObject);
        if (CandidateTextSupport.containsAny(lower, "absence", "absent", "missing", "not visible", "not owned", "no ")) {
            result.add(McpPackageViolation.blocker("candidate.positive-ownership.absence-owned",
                    "html-ex candidate business_object must be positive ownership; absence/blocker can be next frontier or cap reason, not the owned object: "
                            + businessObject,
                    packageState));
        }
    }
}
