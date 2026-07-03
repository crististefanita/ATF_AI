package com.endava.ai.skills.mcp.rules.source;


import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.fs.DiffFileExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

@McpContract(id = "source.protected-framework.not-mutated",
        category = McpRuleCategory.SOURCE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class ProtectedFrameworkMutationRule implements McpValidationRule {
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
                .forEach(file -> ProtectedSourceSurface.match(file)
                        .ifPresent(surface -> result.add(McpPackageViolation.blocker(
                                "framework-mutation.protected-path",
                                "Generated package must not mutate protected source surface: "
                                        + file + ". Reason: " + surface.reason(),
                                context.packageRoot()))));
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
}
