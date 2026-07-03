package com.endava.ai.skills.mcp.rules.candidate.business;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "candidate.business-object.depth",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "review/package-state.json")
public final class CandidateBusinessObjectDepthRule implements McpValidationRule {
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

        if (lower.isBlank()) {
            return;
        }
        if (isGenericBoundary(lower) || !hasDurableObjectSignal(lower)) {
            result.add(McpPackageViolation.blocker("candidate.business-object.not-durable",
                    "html-ex candidate business_object must be durable and specific, not screen/boundary/state generic: "
                            + businessObject,
                    packageState));
        }
    }

    private boolean isGenericBoundary(String value) {
        boolean weakSurface = CandidateTextSupport.containsAny(value,
                "boundary", "screen", "surface", "shell", "status", "success state", "page state", "form state");
        return weakSurface && !hasDurableObjectSignal(value);
    }

    private boolean hasDurableObjectSignal(String value) {
        return CandidateTextSupport.containsAny(value,
                "row", "record", "ledger", "history",
                "profile", "account", "thread", "case", "transaction", "document",
                "identity", "detail", "line item", "line-item", "persistence", "persisted", "durable");
    }
}
