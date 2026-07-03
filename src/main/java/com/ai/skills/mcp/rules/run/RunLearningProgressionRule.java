package com.ai.skills.mcp.rules.run;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@McpContract(id = "run.learning.progression",
        category = McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/SUMMARY.md")
public final class RunLearningProgressionRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path runsRoot = context.paths().runsRoot();
        if (!Files.isDirectory(runsRoot)) {
            return;
        }
        int index = 0;
        for (Path runDirectory : context.files().runDirectories(runsRoot, result)) {
            index++;
            String trace = String.join("\n",
                    context.files().readIfRegular(runDirectory.resolve("SUMMARY.md"), result),
                    context.files().readIfRegular(runDirectory.resolve("review/business-understanding.md"), result),
                    context.files().readIfRegular(runDirectory.resolve("review/frontier-decision.md"), result)
            ).toLowerCase(Locale.ROOT);
            boolean hasProgression = index == 1
                    ? McpRuleSupport.containsAny(trace, "baseline", "new frontier", "first", "beyond")
                    : McpRuleSupport.containsAny(trace, "from run_", "from prior", "progression from",
                    "after run_", "extends run_", "builds on run_");
            if (!hasProgression) {
                result.add(McpPackageViolation.blocker("run-learning.progression-missing",
                        "Run learning trace must state how this run progresses from baseline or a prior run",
                        runDirectory.resolve("SUMMARY.md")));
            }
        }
    }
}
