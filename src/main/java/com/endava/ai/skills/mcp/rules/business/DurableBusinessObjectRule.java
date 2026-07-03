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

@McpContract(id = "business.object.durable-owned",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "review/package-state.json")
public final class DurableBusinessObjectRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String ownedObject = McpRuleSupport.extractStringValue(state, "business_object");
        String nextObject = McpRuleSupport.extractStringValue(state, "primary_next_business_object");
        if (isWeakObject(ownedObject)) {
            result.add(McpPackageViolation.blocker("business-object.owned-missing",
                    "package-state.json must name the durable business object owned by this package",
                    packageState));
        }
        if (isWeakObject(nextObject)) {
            result.add(McpPackageViolation.blocker("next-frontier.primary-missing",
                    "package-state.json must name one primary_next_business_object for the next fresh package",
                    packageState));
        }
        if (!isWeakObject(ownedObject) && !isWeakObject(nextObject)
                && ownedObject.toLowerCase(Locale.ROOT).equals(nextObject.toLowerCase(Locale.ROOT))) {
            result.add(McpPackageViolation.blocker("next-frontier.primary-repeats-owned",
                    "primary_next_business_object must not repeat the object already owned by this package",
                    packageState));
        }
    }

    private boolean isWeakObject(String value) {
        if (value == null || value.trim().length() < 8) {
            return true;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("next") || lower.contains("tbd") || lower.contains("later")
                || lower.contains("more tests") || lower.contains("continue");
    }
}
