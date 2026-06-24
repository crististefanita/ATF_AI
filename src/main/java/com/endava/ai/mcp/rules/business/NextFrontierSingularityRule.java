package com.endava.ai.mcp.rules.business;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "business.next-frontier.single-object",
        category = com.endava.ai.mcp.contract.McpRuleCategory.PACKAGE,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "review/package-close-out.md")
public final class NextFrontierSingularityRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String primary = McpRuleSupport.extractStringValue(state, "primary_next_business_object");
        if (primary == null) {
            return;
        }
        String lower = primary.toLowerCase(Locale.ROOT);
        if (McpRuleSupport.containsAny(lower, ",", " and ", " or ", "/", "multiple", "various", "several")) {
            result.add(McpPackageViolation.blocker("next-frontier.primary-not-singular",
                    "primary_next_business_object must name one dominant object, not a bundle of candidates",
                    packageState));
        }
        String closeOut = context.files().readIfRegular(context.paths().packageCloseOut(), result).toLowerCase(Locale.ROOT);
        if (!closeOut.isBlank() && !closeOut.contains(lower)) {
            result.add(McpPackageViolation.blocker("next-frontier.close-out-drift",
                    "package-close-out.md must carry the same primary next business object as package-state.json",
                    context.paths().packageCloseOut()));
        }
    }
}
