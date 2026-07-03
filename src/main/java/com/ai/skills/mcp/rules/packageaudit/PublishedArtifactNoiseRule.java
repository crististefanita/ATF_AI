package com.ai.skills.mcp.rules.packageaudit;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;

@McpContract(id = "package.artifacts.no-template-noise",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "published package artifacts")
public final class PublishedArtifactNoiseRule implements McpValidationRule {
    private static final Pattern UNRESOLVED_DOLLAR_VARIABLE =
            Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path artifact : publishedTextArtifacts(context, result)) {
            String text = context.files().read(artifact, result);
            String lower = text.toLowerCase(Locale.ROOT);
            for (String token : ContractCatalog.packagePolicyTokens("published-noise-token")) {
                boolean found = token.equals(token.toLowerCase(Locale.ROOT))
                        ? lower.contains(token)
                        : text.contains(token);
                if (found) {
                    result.add(McpPackageViolation.blocker("artifact.template-noise",
                            "Published artifact contains template/noise token: " + token,
                            artifact));
                }
            }
            if (UNRESOLVED_DOLLAR_VARIABLE.matcher(text).find()) {
                result.add(McpPackageViolation.blocker("artifact.template-noise",
                        "Published artifact contains unresolved $variable template token",
                        artifact));
            }
        }
    }

    private List<Path> publishedTextArtifacts(McpPackageContext context, McpPackageValidationResult result) {
        List<Path> artifacts = new ArrayList<>();
        artifacts.addAll(findPackageLevelTextArtifacts(context, result));
        artifacts.addAll(findRunTextArtifacts(context, result));
        return artifacts.stream()
                .filter(Files::isRegularFile)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Path> findPackageLevelTextArtifacts(McpPackageContext context, McpPackageValidationResult result) {
        List<Path> artifacts = new ArrayList<>(List.of(
                context.paths().readme(),
                context.packageRoot().resolve(McpArtifactPaths.CALIBRATION),
                context.paths().report(),
                context.paths().packageState(),
                context.paths().executionGateCard(),
                context.paths().packageCloseOut(),
                context.paths().rootDiff(),
                context.packageRoot().resolve(McpArtifactPaths.QUALITY_ACCOUNTING_VERDICT),
                context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT)
        ));
        artifacts.addAll(findTextArtifacts(context.packageRoot().resolve(McpArtifactPaths.REVIEW_ROOT), result));
        artifacts.addAll(findTextArtifacts(context.packageRoot().resolve(McpArtifactPaths.ANALYSIS_ROOT), result));
        return artifacts;
    }

    private List<Path> findRunTextArtifacts(McpPackageContext context, McpPackageValidationResult result) {
        Path runsRoot = context.paths().runsRoot();
        if (!Files.isDirectory(runsRoot)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(runsRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".md") || name.endsWith(".json") || name.endsWith(".diff");
                    })
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("artifact.noise.walk-failed",
                    exception.getMessage(), runsRoot));
            return List.of();
        }
    }

    private List<Path> findTextArtifacts(Path root, McpPackageValidationResult result) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isPublishedTextArtifact)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("artifact.noise.walk-failed",
                    exception.getMessage(), root));
            return List.of();
        }
    }

    private boolean isPublishedTextArtifact(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".md")
                || name.endsWith(".json")
                || name.endsWith(".diff")
                || name.endsWith(".txt");
    }
}
