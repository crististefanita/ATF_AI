package com.ai.skills.mcp.rules.run;

import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@McpContract(id = "run.window.artifact-spine",
        category = McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/**")
public final class RunArtifactSpineRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path runDirectory : RunRuleSupport.runDirectories(context, result)) {
            requireSingleRunSpine(context, runDirectory, result);
        }
    }

    private void requireSingleRunSpine(McpPackageContext context,
                                       Path runDirectory,
                                       McpPackageValidationResult result) {
        var files = context.files();
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.MANIFEST), "run manifest", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.INPUT_SNAPSHOT), "run input snapshot", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.STATE), "run state", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.SCORE_SUPPORT), "run score support", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.CHECKLIST), "run artifact checklist", result);
        requireRunFeedback(context, runDirectory, result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.DIFF), "run git diff", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.COMMAND_RESULT), "run command result", result);
        files.requireNonEmptyFile(runDirectory.resolve(McpRunArtifacts.TESTNG_RESULTS), "run TestNG XML", result);
    }

    private void requireRunFeedback(McpPackageContext context,
                                    Path runDirectory,
                                    McpPackageValidationResult result) {
        List<Path> accepted = List.of(
                runDirectory.resolve(McpRunArtifacts.BUSINESS_UNDERSTANDING),
                runDirectory.resolve(McpRunArtifacts.LESSON_LEARNED),
                runDirectory.resolve(McpRunArtifacts.FRONTIER_DECISION),
                runDirectory.resolve(McpRunArtifacts.SUMMARY)
        );
        boolean exists = accepted.stream().anyMatch(Files::isRegularFile);
        if (!exists) {
            result.add(McpPackageViolation.blocker("run.feedback.missing",
                    "Run needs saved feedback or summary markdown", runDirectory));
        }
        accepted.stream()
                .filter(Files::isRegularFile)
                .forEach(path -> context.files().requireNonEmptyFile(path, "run feedback markdown", result));
    }
}
