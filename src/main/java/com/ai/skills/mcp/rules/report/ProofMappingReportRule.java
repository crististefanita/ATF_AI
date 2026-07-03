package com.ai.skills.mcp.rules.report;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "report.proof-mapping.claim-evidence",
        category = McpRuleCategory.REPORT,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class ProofMappingReportRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }
        String html = context.files().read(report, result);
        requireProofSpine(html, report, result);
        ProofMappingArtifactLinkInspector.requireFrameworkPropertiesHyperlink(html, report, result);
        ProofMappingArtifactLinkInspector.requireCoreArtifactIndexHyperlinks(context, html, report, result);
        ProofMappingArtifactLinkInspector.requireFinalMcpEvidenceHyperlinks(context, html, report, result);
        ProofMappingClaimEvidenceInspector.requireClaimToEvidenceChains(html, report, result);
    }

    private void requireProofSpine(String html, Path report, McpPackageValidationResult result) {
        McpRuleSupport.requireText(html, report, result, "report.proof-mapping", "Missing proof mapping / claim chain",
                "Proof Mapping", "claim -> run -> diff", "claim -&gt; run -&gt; diff");
        McpRuleSupport.requireText(html, report, result, "report.artifact-index", "Missing artifact index",
                "Artifact Index", "Artifact");
        McpRuleSupport.requireText(html, report, result, "report.extent", "Missing Extent proof link", "Extent");
        McpRuleSupport.requireText(html, report, result, "report.diff", "Missing diff proof link", "diff");
        McpRuleSupport.requireText(html, report, result, "report.state", "Missing state file proof link",
                "run-state.json", "package-state.json");
        McpRuleSupport.requireText(html, report, result, "report.css", "Missing shared report CSS",
                "shared-report-reference.css");
    }

}
