package com.endava.ai.skills.mcp.rules.candidate;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

@McpContract(id = "candidate.reopenability.proof",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "report + run-state + proof")
public final class CandidateReopenabilityProofRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String evidence = CandidateTextSupport.normalize(
                context.files().readIfRegular(context.paths().report(), result)
                        + CandidateTextSupport.allRunLearningText(context, result));

        if (!CandidateTextSupport.containsAny(evidence,
                "relogin", "re-login", "reload", "reopen", "reopened", "revisit",
                "history", "account", "detail", "drawer", "route", "persistence", "persisted", "durable")) {
            result.add(McpPackageViolation.blocker("candidate.reopenability-proof.missing",
                    "html-ex candidate must prove the business object through a reopenable path: relogin, reload, account/history/detail route, drawer, or persistence signal",
                    context.packageRoot()));
        }
    }
}
