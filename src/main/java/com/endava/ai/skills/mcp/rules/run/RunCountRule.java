package com.endava.ai.skills.mcp.rules.run;

import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@McpContract(id = "run.window.count",
        category = McpRuleCategory.RUN,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "runs/")
public final class RunCountRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path runsRoot = context.paths().runsRoot();
        context.files().requireDirectory(runsRoot, "runs directory", result);
        if (!Files.isDirectory(runsRoot)) {
            return;
        }

        List<Path> runDirectories = RunRuleSupport.runDirectories(context, result);
        if (runDirectories.size() < context.runtime().metaIterationCount()) {
            result.add(McpPackageViolation.blocker("runs.under-target",
                    "Expected at least " + context.runtime().metaIterationCount()
                            + " run directories, found " + runDirectories.size(),
                    runsRoot));
        }
    }
}
