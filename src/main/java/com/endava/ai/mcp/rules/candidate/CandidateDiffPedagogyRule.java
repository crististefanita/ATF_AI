package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.McpViolationBuilder;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@McpContract(id = "candidate.diff.pedagogy",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "root diff + run diffs")
public final class CandidateDiffPedagogyRule implements McpValidationRule {
    private static final List<String> ACTION_TOKENS = List.of(
            "click", "open", "reopen", "login", "logout", "submit", "send", "save",
            "fill", "select", "search", "filter", "verify", "assert", "visible",
            "record", "row", "document", "identity", "status", "history", "detail",
            "transition", "persist", "reload", "confirmation");

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path rootDiff = context.paths().rootDiff();
        String rootContent = context.files().readIfRegular(rootDiff, result);
        long rootSize = fileSize(rootDiff);
        if (rootSize < 800) {
            result.add(McpViolationBuilder.blocker("candidate.root-diff.too-thin")
                    .message("html-ex candidate root diff must be pedagogic enough to reopen the package evolution")
                    .path(rootDiff)
                    .meaning("candidate diff must teach the source mutation, not only prove that a file changed")
                    .repair("regenerate root diff from the actual source delta and keep enough context to reopen the mutation")
                    .artifact("review/root-source-thin-no-index.diff")
                    .build());
        }
        if (!touchesSource(rootContent) || !hasActionToken(rootContent)) {
            result.add(McpViolationBuilder.blocker("candidate.root-diff.no-business-action")
                    .message("html-ex candidate root diff must touch relevant source/test files and contain business/action tokens")
                    .path(rootDiff)
                    .meaning("a candidate diff cannot be only formatting, metadata, or artifact churn")
                    .repair("ensure the root diff shows test/action/validation changes tied to the owned business object")
                    .artifact("review/root-source-thin-no-index.diff")
                    .build());
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
            if (!touchesSource(content) || !hasActionToken(content)) {
                result.add(McpViolationBuilder.blocker("candidate.run-diff.no-business-action")
                        .message("html-ex candidate run diff must expose source/test mutation plus action tokens: "
                                + context.paths().runsRoot().relativize(diff))
                        .path(diff)
                        .meaning("run diff should explain what the run learned or changed, not merely exist")
                        .repair("keep each run diff focused on the class/action/validation delta for that run")
                        .artifact("runs/run_##/review/thin-no-index.diff")
                        .build());
            }
        }
    }

    private boolean touchesSource(String diff) {
        String normalized = normalize(diff);
        return normalized.contains("src/test/java") || normalized.contains("src/main/java")
                || normalized.contains("source/src/test/java") || normalized.contains("source/src/main/java");
    }

    private boolean hasActionToken(String diff) {
        String normalized = normalize(diff);
        return ACTION_TOKENS.stream().anyMatch(normalized::contains);
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private long fileSize(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.size(path) : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
