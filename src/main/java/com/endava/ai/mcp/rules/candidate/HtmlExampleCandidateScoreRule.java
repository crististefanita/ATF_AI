package com.endava.ai.mcp.rules.candidate;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "candidate.score.teacher-threshold",
        category = com.endava.ai.mcp.contract.McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "score-overview-reference.html",
        artifact = "review/package-state.json")
public final class HtmlExampleCandidateScoreRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.extractIntValue(state, "overall");
        Integer cap = McpRuleSupport.extractIntValue(state, "cap");
        HtmlExampleCandidateProfile profile = HtmlExampleCandidateProfile.from(context);

        if (overall == null || overall < profile.teacherThreshold()) {
            result.add(McpPackageViolation.blocker("candidate.overall.below-teacher-threshold",
                    "html-ex candidate Overall must meet RUN_EXPECTED_DELTA_QUALITY threshold " + profile.teacherThreshold()
                            + "; actual=" + (overall == null ? "missing" : overall),
                    packageState));
        }
        if (cap == null || cap < profile.teacherThreshold()) {
            result.add(McpPackageViolation.blocker("candidate.cap.below-teacher-threshold",
                    "html-ex candidate cap must meet RUN_EXPECTED_DELTA_QUALITY threshold " + profile.teacherThreshold()
                            + "; actual=" + (cap == null ? "missing" : cap),
                    packageState));
        }
    }
}
