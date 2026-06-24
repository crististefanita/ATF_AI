package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "candidate.html-example.promotion-bundle",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "reporting.html",
        artifact = "render/link/mcp/report bundle")
public final class CandidateHtmlExamplePromotionRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        requireFile(context, result, McpArtifactPaths.RENDER_PROOF, "candidate.render-proof.missing");
        requireFile(context, result, "review/local-link-check.txt", "candidate.link-audit.missing");
        requireFile(context, result, McpArtifactPaths.MCP_GATE_RESULT, "candidate.mcp-gate.missing");

        String report = context.files().readIfRegular(context.paths().report(), result);
        if (!report.contains("Score Overview") || !report.contains("Proof Mapping") || !report.contains("Business Flow Graph")) {
            result.add(McpPackageViolation.blocker("candidate.report.promotion-sections-missing",
                    "html-ex candidate report must publish Business Flow Graph, Score Overview and Proof Mapping",
                    context.paths().report()));
        }
    }

    private void requireFile(McpPackageContext context,
                             McpPackageValidationResult result,
                             String relativePath,
                             String code) {
        Path path = context.packageRoot().resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            result.add(McpPackageViolation.blocker(code,
                    "html-ex candidate promotion bundle missing required artifact: " + relativePath,
                    path));
        }
    }
}
