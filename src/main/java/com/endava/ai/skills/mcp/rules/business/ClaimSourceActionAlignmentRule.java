package com.endava.ai.skills.mcp.rules.business;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpClaimEvidence;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Path;

@McpContract(id = "claim-action.source-proof-alignment",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "run-state + run diff + source + XML/Extent + Proof Mapping")
public final class ClaimSourceActionAlignmentRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String finalReport = ClaimActionEvidenceFactory.normalize(
                context.files().readIfRegular(context.paths().report(), result));
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            McpClaimEvidence evidence = ClaimActionEvidenceFactory.create(
                    context.sourceAuditScope(), context, result, runDirectory, finalReport);
            if (evidence.normalizedVerb() == null || evidence.normalizedVerb().isBlank()) {
                continue;
            }
            ClaimActionContractEvaluator.validate(evidence, result);
        }
    }
}
