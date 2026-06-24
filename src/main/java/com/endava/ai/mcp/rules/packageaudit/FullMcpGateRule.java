package com.endava.ai.mcp.rules.packageaudit;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "mcp.gate.full-suite",
        category = com.endava.ai.mcp.contract.McpRuleCategory.MCP_GATE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "validation/mcp/mcp-gate-result.md")
public final class FullMcpGateRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path gate = context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT);
        if (!Files.isRegularFile(gate)) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.missing",
                    "Missing frozen full MCP gate result", gate));
            return;
        }

        String text = context.files().read(gate, result);
        String lower = text.toLowerCase(Locale.ROOT);
        boolean claimsPass = lower.matches("(?s).*\\b(result|status)\\s*:\\s*pass\\b.*")
                || lower.contains("no mcp blocker remains")
                || lower.matches("(?s).*\\bpassed\\s*=\\s*\\d+.*");
        boolean fullSuiteMarker = text.contains("-Dtest=Mcp*Test")
                || text.contains("\"-Dtest=Mcp*Test\"")
                || lower.contains("full mcp")
                || lower.contains("complete mcp")
                || lower.contains("mcp*test");
        boolean subsetMarker = lower.contains("mcplatestpackage")
                && !lower.contains("mcpgeneratedpackagevalidatortest")
                && !fullSuiteMarker;

        if (claimsPass && !fullSuiteMarker) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.not-full-suite",
                    "Frozen MCP gate claims PASS but does not prove the full Mcp*Test suite was executed", gate));
        }
        if (subsetMarker) {
            result.add(McpPackageViolation.blocker("mcp.gate-result.subset-only",
                    "Frozen MCP gate looks like a subset run, not the full MCP contract", gate));
        }
    }
}
