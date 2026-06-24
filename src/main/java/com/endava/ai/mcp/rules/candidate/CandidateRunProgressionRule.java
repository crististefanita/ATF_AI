package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpValidationRule;

@McpContract(id = "candidate.run-progression",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "runs/run_##")
public final class CandidateRunProgressionRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String text = CandidateTextSupport.normalize(CandidateTextSupport.allRunLearningText(context, result));
        int markers = 0;
        markers += CandidateTextSupport.containsAny(text, "create", "created", "submit", "submitted", "add", "added") ? 1 : 0;
        markers += CandidateTextSupport.containsAny(text, "reopen", "reopened", "reload", "revisit", "re-enter") ? 1 : 0;
        markers += CandidateTextSupport.containsAny(text, "relogin", "re-login", "account", "authenticated") ? 1 : 0;
        markers += CandidateTextSupport.containsAny(text, "detail", "drawer", "route", "history", "line item", "line-item") ? 1 : 0;
        markers += CandidateTextSupport.containsAny(text, "audit", "cap", "capped", "close-out", "verdict", "blocker") ? 1 : 0;

        if (markers < 3) {
            result.add(McpPackageViolation.blocker("candidate.run-progression.too-flat",
                    "html-ex candidate runs must show progression across at least three learning stages: create/submit, reopen/reload, relogin/account, detail/history, audit/cap. Actual stages="
                            + markers,
                    context.paths().runsRoot()));
        }
    }
}
