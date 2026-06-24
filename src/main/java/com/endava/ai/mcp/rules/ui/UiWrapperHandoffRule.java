package com.endava.ai.mcp.rules.ui;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;

import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "ui.wrapper.handoff.quality",
        category = com.endava.ai.mcp.contract.McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "runs/run_##/review")
public final class UiWrapperHandoffRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (var runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            String text = context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.STATE), result)
                    + "\n" + context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.BUSINESS_UNDERSTANDING), result)
                    + "\n" + context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.SUMMARY), result);
            String normalized = text.toLowerCase(Locale.ROOT);
            if (normalized.isBlank()) {
                continue;
            }
            if (!McpRuleSupport.containsAny(normalized, "actor", "user", "shopper", "customer", "admin")) {
                result.add(McpPackageViolation.blocker("ui-handoff.actor-missing",
                        "UI wrapper handoff should expose actor/user perspective", runDirectory));
            }
            if (!McpRuleSupport.containsAny(normalized, "intent", "intentie", "goal", "wants", "tries")) {
                result.add(McpPackageViolation.blocker("ui-handoff.intent-missing",
                        "UI wrapper handoff should expose user intent", runDirectory));
            }
            if (!McpRuleSupport.containsAny(normalized, "object", "visible", "screen", "field", "message", "status", "business_object")) {
                result.add(McpPackageViolation.blocker("ui-handoff.visible-object-missing",
                        "UI wrapper handoff should expose visible business object/anchor", runDirectory));
            }
            if (!McpRuleSupport.containsAny(normalized, "result", "verdict", "outcome", "blocked", "partial", "owned", "countable")) {
                result.add(McpPackageViolation.blocker("ui-handoff.verdict-missing",
                        "UI wrapper handoff should expose local result/verdict", runDirectory));
            }
        }
    }
}
