package com.endava.ai.mcp.rules.source;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.fs.DiffFileExtractor;

import java.nio.file.Files;

@com.endava.ai.mcp.contract.McpContract(id = "source.root-diff.files-exist",
        category = com.endava.ai.mcp.contract.McpRuleCategory.SOURCE,
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
            var sourceFile = context.projectRoot().resolve(normalized).normalize();
            if (!Files.isRegularFile(sourceFile)) {
                result.add(McpPackageViolation.blocker("root-diff.source-file-missing",
                        "root-source-thin-no-index.diff references a source file that does not exist in the project: "
                                + normalized,
                        rootDiff));
            }
        }
    }
}
