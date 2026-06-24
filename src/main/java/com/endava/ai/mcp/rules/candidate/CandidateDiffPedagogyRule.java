package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "candidate.diff.pedagogy",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "root diff + run diffs")
public final class CandidateDiffPedagogyRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path rootDiff = context.paths().rootDiff();
        long rootSize = fileSize(rootDiff);
        if (rootSize < 800) {
            result.add(McpPackageViolation.blocker("candidate.root-diff.too-thin",
                    "html-ex candidate root diff must be pedagogic enough to reopen the package evolution",
                    rootDiff));
        }

        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            Path diff = runDirectory.resolve(McpRunArtifacts.DIFF);
            String content = context.files().readIfRegular(diff, result);
            if (content.length() < 180 || !content.contains("diff --git")) {
                result.add(McpPackageViolation.blocker("candidate.run-diff.not-pedagogic",
                        "html-ex candidate run diff must be focused, non-empty, and git-readable: "
                                + context.paths().runsRoot().relativize(diff),
                        diff));
            }
        }
    }

    private long fileSize(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.size(path) : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
