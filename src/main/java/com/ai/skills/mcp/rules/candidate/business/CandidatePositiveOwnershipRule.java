package com.ai.skills.mcp.rules.candidate.business;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

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
        String businessObject = McpRuleSupport.firstStringValue(state,
                "business_object", "owned_business_object", "business_object_owned");
        String lower = CandidateTextSupport.normalize(businessObject);
        if (CandidateTextSupport.containsAny(lower, "absence", "absent", "missing", "not visible", "not owned", "no ")) {
            result.add(McpPackageViolation.blocker("candidate.positive-ownership.absence-owned",
                    "html-ex candidate business_object must be positive ownership; absence/blocker can be next frontier or cap reason, not the owned object: "
                            + businessObject,
                    packageState));
        }
    }
}
