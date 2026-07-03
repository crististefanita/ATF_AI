package com.endava.ai.skills.mcp.rules.run;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@McpContract(id = "run.learning.trace-quality",
        category = McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/review")
public final class LearningTraceQualityRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path runsRoot = context.paths().runsRoot();
        if (!Files.isDirectory(runsRoot)) {
            return;
        }
        for (Path runDirectory : context.files().runDirectories(runsRoot, result)) {
            validateRunLearningTrace(context, runDirectory, result);
        }
    }

    private void validateRunLearningTrace(McpPackageContext context, Path runDirectory,
                                          McpPackageValidationResult result) {
        String trace = String.join("\n",
                context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.SUMMARY), result),
                context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.STATE), result),
                context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.BUSINESS_UNDERSTANDING), result),
                context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.LESSON_LEARNED), result),
                context.files().readIfRegular(runDirectory.resolve(McpRunArtifacts.FRONTIER_DECISION), result)
        ).toLowerCase(Locale.ROOT);

        requireGroup(trace, runDirectory, result, "claim/frontier",
                "claim", "frontier", "business_object", "business object", "anchor", "intent");
        requireGroup(trace, runDirectory, result, "decision/verdict",
                "decision", "verdict", "countable", "owned", "partial", "blocked", "status");
        requireGroup(trace, runDirectory, result, "evidence/proof",
                "evidence", "proof", "extent", "xml", "testng", "diff", "command-result");
        requireGroup(trace, runDirectory, result, "lesson/learning",
                "lesson", "learned", "learning", "understanding", "observation");
        requireGroup(trace, runDirectory, result, "next implication",
                "next", "handoff", "continue", "fresh", "close-out", "implication", "remaining");
    }

    private void requireGroup(String trace, Path runDirectory, McpPackageValidationResult result,
                              String label, String... tokens) {
        if (McpRuleSupport.containsAny(trace, tokens)) {
            return;
        }
        result.add(McpPackageViolation.blocker("learning-trace.weak",
                "Run learning trace misses " + label + " tokens: " + String.join(" | ", tokens),
                runDirectory));
    }
}
