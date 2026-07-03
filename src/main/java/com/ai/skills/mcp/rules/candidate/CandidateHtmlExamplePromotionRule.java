package com.ai.skills.mcp.rules.candidate;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationBuilder;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;

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
        requireRenderProofQuality(context, result);
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

    private void requireRenderProofQuality(McpPackageContext context, McpPackageValidationResult result) {
        Path markdown = context.packageRoot().resolve("analysis/canonical-render-check.md");
        String text = context.files().readIfRegular(markdown, result).toLowerCase(java.util.Locale.ROOT);
        if (text.contains("status: limited") || text.contains("static checks")
                || text.contains("refused direct") || text.contains("not bypass")) {
            result.add(McpViolationBuilder.blocker("candidate.render-proof.limited")
                    .message("html-ex candidate render proof is limited/static instead of a real reusable browser render proof")
                    .path(markdown)
                    .meaning("teacher-grade report publication must prove the final HTML rendered, not only that static checks passed")
                    .repair("capture a real final-report render proof or cap the package below html-ex candidate grade")
                    .artifact("analysis/canonical-render-check.md and canonical-render-check.png")
                    .build());
        }
    }
}
