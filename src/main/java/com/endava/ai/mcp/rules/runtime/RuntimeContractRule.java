package com.endava.ai.mcp.rules.runtime;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "runtime.active-values.published",
        category = com.endava.ai.mcp.contract.McpRuleCategory.RUNTIME,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "docs/prompts/iterativ/agent-runtime.properties")
public final class RuntimeContractRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        context.files().requireNonEmptyFile(context.runtime().source(), "agent runtime", result);
        context.runtime().promptReferencePaths(context.projectRoot())
                .forEach(path -> requireRuntimeReferenceFile(path, result));
    }

    private void requireRuntimeReferenceFile(Path path, McpPackageValidationResult result) {
        if (!Files.isRegularFile(path)) {
            result.add(McpPackageViolation.blocker("runtime.reference.missing",
                    "Missing runtime referenced document", path));
            return;
        }
        try {
            if (Files.size(path) == 0L) {
                result.add(McpPackageViolation.blocker("runtime.reference.empty",
                        "Empty runtime referenced document", path));
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("runtime.reference.unreadable", exception.getMessage(), path));
        }
    }
}
