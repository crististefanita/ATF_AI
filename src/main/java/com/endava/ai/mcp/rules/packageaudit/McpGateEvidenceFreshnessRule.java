package com.endava.ai.mcp.rules.packageaudit;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

@com.endava.ai.mcp.contract.McpContract(id = "mcp.gate.evidence-freshness",
        category = com.endava.ai.mcp.contract.McpRuleCategory.MCP_GATE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "validation/mcp/mcp-gate-result.md")
public final class McpGateEvidenceFreshnessRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path gate = context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT);
        if (!Files.isRegularFile(gate)) {
            return;
        }
        String text = context.files().read(gate, result);
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("pass") && !lower.contains("-dtest=mcp*test") && !lower.contains("mcp contract suite")) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.command-missing",
                    "MCP gate PASS must name the full MCP suite command or contract-suite marker",
                    gate));
        }
        if (Pattern.compile("(?i)tests\\s+run:\\s*0\\b").matcher(text).find()
                || !Pattern.compile("(?i)tests\\s+run:\\s*\\d+").matcher(text).find()) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.count-missing",
                    "MCP gate must publish a non-zero Tests run count",
                    gate));
        }
        if (Pattern.compile("(?i)(failures|errors)\\s*:\\s*[1-9]").matcher(text).find()) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.failures-present",
                    "MCP gate cannot claim PASS while publishing failures or errors",
                    gate));
        }
    }
}
