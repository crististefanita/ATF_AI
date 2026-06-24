package com.endava.ai.mcp.rules.score;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "score.cap.evidence",
        category = com.endava.ai.mcp.contract.McpRuleCategory.SCORE,
        htmlReq = "score-overview-reference.html",
        artifact = "review/package-state.json")
public final class ScoreCapEvidenceRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.extractIntValue(state, "overall");
        Integer cap = McpRuleSupport.extractIntValue(state, "cap");
        String capReason = McpRuleSupport.extractStringValue(state, "cap_reason");
        String weakestAxis = McpRuleSupport.extractStringValue(state, "weakest_axis");
        String proofClass = McpRuleSupport.extractStringValue(state, "proof_class");
        if (overall != null && cap != null && overall > cap) {
            result.add(McpPackageViolation.blocker("score.cap-exceeded",
                    "package-state overall must not exceed cap: overall=" + overall + ", cap=" + cap,
                    packageState));
        }
        if (isBlank(capReason)) {
            result.add(McpPackageViolation.blocker("score.cap-evidence.reason-missing",
                    "package-state.json must explain why Overall is capped",
                    packageState));
        }
        if (isBlank(weakestAxis)) {
            result.add(McpPackageViolation.blocker("score.cap-evidence.weakest-axis-missing",
                    "package-state.json must publish the weakest scoring axis",
                    packageState));
        }
        if (isBlank(proofClass)) {
            result.add(McpPackageViolation.blocker("score.cap-evidence.proof-class-missing",
                    "package-state.json must publish the proof class behind the score",
                    packageState));
        }
        String reportAndReadme = (context.files().readIfRegular(context.paths().report(), result)
                + "\n" + context.files().readIfRegular(context.paths().readme(), result)).toLowerCase(Locale.ROOT);
        if (!isBlank(weakestAxis) && !reportAndReadme.contains(weakestAxis.toLowerCase(Locale.ROOT))) {
            result.add(McpPackageViolation.blocker("score.cap-evidence.not-published",
                    "final report or README must publish weakest_axis from package-state.json",
                    context.paths().report()));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() < 6;
    }
}
