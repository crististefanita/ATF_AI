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

@McpContract(id = "candidate.single-family.cap",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "score-overview-reference.html",
        artifact = "review/package-state.json")
public final class CandidateSingleFamilyCapRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.extractIntValue(state, "overall");
        HtmlExampleCandidateProfile profile = HtmlExampleCandidateProfile.from(context);
        if (overall == null || overall <= profile.teacherThreshold()) {
            return;
        }

        String combined = CandidateTextSupport.normalize(state + "\n"
                + context.files().readIfRegular(context.paths().report(), result));
        boolean partialOrSingle = CandidateTextSupport.containsAny(combined,
                "\"breadth_verdict\":  \"partial", "\"breadth_verdict\":\"partial",
                "partial", "single family", "single-family", "one macro-lane", "one macro lane");
        boolean hasDepthEscape = CandidateTextSupport.containsAny(combined,
                "detail route", "details drawer", "detail drawer", "line item", "line-item",
                "second durable", "secondary business object", "secondary_business_object",
                "another durable", "two durable", "multi-family");

        if (partialOrSingle && !hasDepthEscape) {
            result.add(McpPackageViolation.blocker("candidate.single-family.cap-too-high",
                    "single-family/partial html-ex candidate cannot exceed runtime teacher threshold without details, line-items, or a second durable business object. Threshold="
                            + profile.teacherThreshold() + "; overall=" + overall,
                    packageState));
        }
    }
}
