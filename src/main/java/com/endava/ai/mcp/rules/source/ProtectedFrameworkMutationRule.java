package com.endava.ai.mcp.rules.source;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpProjectLayout;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.fs.DiffFileExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@com.endava.ai.mcp.contract.McpContract(id = "source.protected-framework.not-mutated",
        category = com.endava.ai.mcp.contract.McpRuleCategory.SOURCE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class ProtectedFrameworkMutationRule implements McpValidationRule {
    private static final List<String> PROTECTED_PREFIXES = List.of(
            McpProjectLayout.MCP_MAIN_PATH,
            McpProjectLayout.MCP_TEST_PATH,
            McpProjectLayout.CORE_MAIN_PATH,
            McpProjectLayout.UI_MAIN_PATH + "core/",
            McpProjectLayout.UI_MAIN_PATH + "engine/",
            McpProjectLayout.UI_MAIN_PATH + "factory/",
            McpProjectLayout.UI_MAIN_PATH + "reporting/",
            McpProjectLayout.UI_MAIN_PATH + "service/",
            McpProjectLayout.UI_MAIN_PATH + "utils/",
            McpProjectLayout.API_MAIN_PATH + "client/",
            McpProjectLayout.API_MAIN_PATH + "core/",
            McpProjectLayout.API_MAIN_PATH + "factory/",
            McpProjectLayout.API_MAIN_PATH + "service/",
            McpProjectLayout.API_MAIN_PATH + "steps/",
            McpProjectLayout.API_MAIN_PATH + "utils/",
            "docs/prompts/iterativ/",
            "docs/shared-report-reference.css"
    );
    private final DiffFileExtractor diffFileExtractor = new DiffFileExtractor();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Set<String> referencedFiles = new LinkedHashSet<>();
        addReferencedFiles(context, context.paths().rootDiff(), result, referencedFiles);
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            addReferencedFiles(context, runDirectory.resolve(McpRunArtifacts.DIFF), result, referencedFiles);
        }

        referencedFiles.stream()
                .map(this::normalize)
                .filter(this::isProtected)
                .forEach(file -> result.add(McpPackageViolation.blocker("framework-mutation.protected-path",
                        "Generated package must not mutate framework/MCP/core/service infrastructure: " + file,
                        context.packageRoot())));
    }

    private void addReferencedFiles(McpPackageContext context,
                                    Path diff,
                                    McpPackageValidationResult result,
                                    Set<String> referencedFiles) {
        if (Files.isRegularFile(diff)) {
            referencedFiles.addAll(diffFileExtractor.referencedFiles(context.files().read(diff, result)));
        }
    }

    private String normalize(String path) {
        return path.replace('\\', '/');
    }

    private boolean isProtected(String path) {
        return PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
