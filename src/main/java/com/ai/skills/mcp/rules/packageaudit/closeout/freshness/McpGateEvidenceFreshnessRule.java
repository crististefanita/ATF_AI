package com.ai.skills.mcp.rules.packageaudit.closeout.freshness;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

@McpContract(id = "mcp.gate.evidence-freshness",
        category = McpRuleCategory.MCP_GATE,
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
        if (lower.contains("pass")
                && !lower.contains("-dtest=mcp*test")
                && !lower.contains("mcp contract suite")
                && !lower.contains(McpExecutionProfile.FULL.suiteFileName())
                && !lower.contains(McpExecutionProfile.PACKAGE_AUDIT.suiteFileName())
                && !lower.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName())) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.command-missing",
                    "MCP gate PASS must name the MCP package close-out command or an explicitly accepted MCP health-sweep marker",
                    gate));
        }
        if (Pattern.compile("(?i)tests\\s+run:\\s*0\\b").matcher(text).find()
                || !Pattern.compile("(?i)tests\\s+run:\\s*\\d+").matcher(text).find()) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.count-missing",
                    "MCP gate must publish a non-zero Tests run count",
                    gate));
        }
        boolean claimsPass = Pattern.compile("(?im)^\\s*-?\\s*verdict\\s*:\\s*PASS\\b").matcher(text).find()
                || Pattern.compile("(?im)^\\s*-?\\s*exit_code\\s*:\\s*0\\b").matcher(text).find()
                || Pattern.compile("(?i)\\bMCP_EXIT_CODE\\s*=\\s*0\\b").matcher(text).find();
        if (claimsPass && Pattern.compile("(?i)(failures|errors)\\s*:\\s*[1-9]").matcher(text).find()) {
            result.add(McpPackageViolation.blocker("mcp.gate-evidence.failures-present",
                    "MCP gate cannot claim PASS while publishing failures or errors",
                    gate));
        }
    }
}

