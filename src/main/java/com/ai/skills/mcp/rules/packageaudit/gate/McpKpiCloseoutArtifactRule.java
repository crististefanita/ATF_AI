package com.ai.skills.mcp.rules.packageaudit.gate;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "mcp.gate.kpi-closeout-artifacts",
        category = McpRuleCategory.MCP_GATE,
        htmlReq = "execute-and-understand-run.html + reporting.html",
        artifact = "validation/mcp/mcp-kpi-matrix.md")
public final class McpKpiCloseoutArtifactRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path gate = context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT);
        if (!Files.isRegularFile(gate)
                && !Files.isDirectory(context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT))) {
            return;
        }
        String gateText = context.files().readIfRegular(gate, result);
        for (String artifact : ContractCatalog.packagePolicyTokens("kpi-closeout-artifact")) {
            Path path = context.packageRoot().resolve(artifact);
            if (!Files.isRegularFile(path)) {
                result.add(McpPackageViolation.blocker("mcp.kpi-closeout-artifact.missing",
                        "MCP package close-out must publish KPI artifact: " + artifact,
                        path));
            }
            if (!gateText.isBlank() && !gateText.contains(fileName(artifact))) {
                result.add(McpPackageViolation.blocker("mcp.kpi-closeout-artifact.not-in-gate",
                        "MCP gate result must name KPI artifact: " + artifact,
                        gate));
            }
        }
    }

    private String fileName(String artifact) {
        return artifact.substring(artifact.lastIndexOf('/') + 1);
    }
}
