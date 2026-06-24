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
        String businessObject = McpRuleSupport.extractStringValue(state, "business_object");
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
                "row", "record", "ledger", "receipt", "invoice", "order", "history",
                "profile", "account", "thread", "case", "ticket", "transaction", "document",
                "identity", "detail", "line item", "line-item", "persistence", "persisted", "durable");
    }
}
