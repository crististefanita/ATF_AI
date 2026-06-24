package com.endava.ai.mcp.rules.run;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "run.window.state-completeness",
        category = com.endava.ai.mcp.contract.McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/review/run-state.json")
public final class RunStateCompletenessRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path runDirectory : RunRuleSupport.runDirectories(context, result)) {
            requireRunStateSemantics(context, runDirectory.resolve(McpRunArtifacts.STATE), result);
        }
    }

    private void requireRunStateSemantics(McpPackageContext context,
                                          Path runState,
                                          McpPackageValidationResult result) {
        if (!Files.isRegularFile(runState)) {
            return;
        }
        String content = context.files().read(runState, result);
        McpRuleSupport.requireText(content, runState, result, "run-state.tests-produced",
                "Run state should publish tests produced", "tests_produced", "test_count", "\"tests\"", "tests");
        requireRunTestCount(content, runState, context, result);
        if (!content.contains(String.valueOf(context.runtime().roundTargetTestCount()))) {
            result.add(McpPackageViolation.warning("run-state.target-not-visible",
                    "Run state should make the active test target visible", runState));
        }
        McpRuleSupport.requireText(content, runState, result, "run-state.verdict",
                "Run state should publish business/countability status",
                "countable", "owned", "blocked", "partial", "verdict");
        requireCanonicalBusinessFields(content, runState, result);
    }

    private void requireRunTestCount(String content,
                                     Path runState,
                                     McpPackageContext context,
                                     McpPackageValidationResult result) {
        Integer actual = McpRuleSupport.firstIntValue(content, "tests_produced", "test_count", "tests");
        if (actual == null) {
            return;
        }
        if (actual < context.runtime().roundTargetTestCount()) {
            result.add(McpPackageViolation.blocker("run-state.tests-under-target",
                    "Run produced " + actual + " tests, below active ROUND_TARGET_TEST_COUNT="
                            + context.runtime().roundTargetTestCount(),
                    runState));
        }
    }

    private void requireCanonicalBusinessFields(String content,
                                                Path runState,
                                                McpPackageValidationResult result) {
        boolean hasBusinessClaim = McpRuleSupport.extractStringValue(content, "business_claim") != null;
        boolean hasLegacyClaim = McpRuleSupport.extractStringValue(content, "claim") != null;
        boolean hasProgressionFromPrevious = McpRuleSupport.extractStringValue(content, "progression_from_previous_run") != null;
        boolean hasLegacyProgression = McpRuleSupport.extractStringValue(content, "progression") != null;

        if (!hasBusinessClaim && hasLegacyClaim) {
            result.add(McpPackageViolation.warning("run-state.business-claim.legacy-field",
                    "Prefer canonical business_claim; legacy claim is accepted only as transitional compatibility",
                    runState));
        }
        if (!hasBusinessClaim && !hasLegacyClaim) {
            result.add(McpPackageViolation.warning("run-state.business-claim.missing",
                    "Run state should publish business_claim so MCP can trace claim -> source -> proof",
                    runState));
        }
        if (!hasProgressionFromPrevious && hasLegacyProgression) {
            result.add(McpPackageViolation.warning("run-state.progression.legacy-field",
                    "Prefer canonical progression_from_previous_run; legacy progression is accepted only as transitional compatibility",
                    runState));
        }
        if (!hasProgressionFromPrevious && !hasLegacyProgression) {
            result.add(McpPackageViolation.warning("run-state.progression.missing",
                    "Run state should publish progression_from_previous_run so learning is explicit",
                    runState));
        }
    }
}
