package com.ai.skills.mcp.rules.source;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationBuilder;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.fs.DiffFileExtractor;

import java.nio.file.Files;

@McpContract(id = "source.root-diff.files-exist",
        category = McpRuleCategory.SOURCE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class RootDiffSourceConsistencyRule implements McpValidationRule {
    private final DiffFileExtractor diffFileExtractor = new DiffFileExtractor();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        var rootDiff = context.paths().rootDiff();
        if (!Files.isRegularFile(rootDiff)) {
            return;
        }

        String diff = context.files().read(rootDiff, result);
        for (String relative : diffFileExtractor.referencedFiles(diff)) {
            String normalized = relative.replace('\\', '/');
            if (!normalized.startsWith("src/")) {
                continue;
            }
            var sourceFile = context.sourceAuditScope().sourceRoot().resolve(normalized).normalize();
            if (!Files.isRegularFile(sourceFile)) {
                if (context.sourceAuditScope().canProveSourcePresence()) {
                    result.add(McpPackageViolation.blocker("root-diff.source-file-missing",
                            "root-source-thin-no-index.diff references a source file that does not exist under audited source root "
                                    + context.sourceAuditScope().sourceRootDescription() + ": " + normalized,
                            rootDiff));
                } else {
                    result.add(McpViolationBuilder.warning("root-diff.source-file.requires-source-root")
                            .message("Cannot prove root-diff source presence because package root is external and -Dmcp.project.root was not provided: "
                                    + normalized)
                            .path(rootDiff)
                            .meaning("MCP would otherwise compare the package diff against the validator workspace instead of the audited project")
                            .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                            .artifact("review/root-source-thin-no-index.diff")
                            .build());
                }
            }
        }
    }
}
