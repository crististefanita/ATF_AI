package com.endava.ai.skills.mcp.rules.source.protectedroot;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpViolationBuilder;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.source.ProtectedSourceSurface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@McpContract(id = "source.protected-framework.git-clean",
        category = McpRuleCategory.SOURCE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "git status --porcelain")
public final class ProtectedGitWorkspaceRule implements McpValidationRule {
    private static final Duration GIT_STATUS_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        if (context.sourceAuditScope().requiresExplicitSourceRoot()) {
            result.add(McpViolationBuilder.warning("source.git-status.requires-source-root")
                    .message("Protected workspace git status cannot be audited without explicit -Dmcp.project.root")
                    .path(context.packageRoot())
                    .meaning("MCP must not inspect the validator workspace and pretend it is the audited project")
                    .repair("rerun package close-out with -Dmcp.project.root=<audited-project-root>")
                    .artifact("git status --porcelain")
                    .build());
            return;
        }

        Path sourceRoot = context.sourceAuditScope().sourceRoot();
        if (!Files.isDirectory(sourceRoot.resolve(".git"))) {
            result.add(McpPackageViolation.warning("source.git-status.unavailable",
                    "Audited source root is not a git worktree; protected live workspace drift cannot be checked",
                    sourceRoot));
            return;
        }

        ProtectedGitStatusResult status = gitStatus(sourceRoot);
        if (!status.available()) {
            result.add(McpPackageViolation.warning("source.git-status.unavailable",
                    "Cannot run git status for protected source surfaces: " + status.diagnostic(),
                    sourceRoot));
            return;
        }

        for (String changedPath : changedPaths(status.output())) {
            ProtectedSourceSurface.match(changedPath)
                    .ifPresent(surface -> result.add(McpPackageViolation.blocker(
                            "framework-mutation.protected-git-status",
                            "Protected source surface has live git changes outside the frozen package diff: "
                                    + changedPath + ". Reason: " + surface.reason(),
                            sourceRoot.resolve(changedPath))));
        }
    }

    private ProtectedGitStatusResult gitStatus(Path sourceRoot) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("-C");
        command.add(sourceRoot.toString());
        command.add("status");
        command.add("--porcelain");
        command.add("--");
        ProtectedSourceSurface.entries().stream()
                .map(ProtectedSourceSurface.Entry::prefix)
                .forEach(command::add);

        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(GIT_STATUS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return ProtectedGitStatusResult.unavailable("git status timed out");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                return ProtectedGitStatusResult.unavailable(error.isBlank() ? "exit=" + process.exitValue() : error.trim());
            }
            return ProtectedGitStatusResult.available(output);
        } catch (IOException exception) {
            return ProtectedGitStatusResult.unavailable(exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ProtectedGitStatusResult.unavailable("interrupted");
        }
    }

    private Set<String> changedPaths(String statusOutput) {
        Set<String> paths = new LinkedHashSet<>();
        for (String line : statusOutput.split("\\R")) {
            if (line.length() < 4) {
                continue;
            }
            String pathPart = normalize(line.substring(3).trim());
            if (pathPart.contains(" -> ")) {
                for (String piece : pathPart.split("\\s+->\\s+")) {
                    addPath(paths, piece);
                }
            } else {
                addPath(paths, pathPart);
            }
        }
        return paths;
    }

    private void addPath(Set<String> paths, String path) {
        String normalized = normalize(path);
        if (!normalized.isBlank()) {
            paths.add(normalized);
        }
    }

    private String normalize(String path) {
        String normalized = path.replace('\\', '/').trim();
        if (normalized.length() >= 2 && normalized.charAt(0) == '"' && normalized.charAt(normalized.length() - 1) == '"') {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

}
