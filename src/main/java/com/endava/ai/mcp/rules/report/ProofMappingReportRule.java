package com.endava.ai.mcp.rules.report;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
        requireClaimToEvidenceChains(html, report, result);
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

    private void requireClaimToEvidenceChains(String html, Path report, McpPackageValidationResult result) {
        String proofMapping = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Proof Mapping");
        List<String> rows = ReportRuleSupport.DETAILS.tableRowsOrListItems(proofMapping);
        if (rows.isEmpty()) {
            result.add(McpPackageViolation.blocker("claim-evidence.missing",
                    "Proof Mapping must expose claim-level evidence rows or list items", report));
            return;
        }

        int checkedClaims = 0;
        for (String row : rows) {
            String normalized = row.toLowerCase(Locale.ROOT);
            if (ReportRuleSupport.SECTIONS.stripTags(row).trim().length() < 20 || normalized.contains("<th")) {
                continue;
            }
            checkedClaims++;
            requireClaimRowToken(normalized, report, result, "claim-evidence.diff-missing",
                    "Claim row must link to run/root diff evidence", "thin-no-index.diff", "root-source-thin-no-index.diff");
            requireClaimRowToken(normalized, report, result, "claim-evidence.result-missing",
                    "Claim row must link to machine result evidence", "testng-results.xml", "test-", "command-result.md");
            requireClaimRowToken(normalized, report, result, "claim-evidence.state-missing",
                    "Claim row must link to state evidence", "run-state.json", "package-state.json");
            requireClaimRowToken(normalized, report, result, "claim-evidence.verdict-missing",
                    "Claim row must publish a cold verdict", "verdict", "owned", "countable", "partial", "blocked", "capped");
            if (!McpRuleSupport.containsAny(normalized, "extentreport", "extentreport_", "validation/reports", "validation/")) {
                result.add(McpPackageViolation.blocker("claim-evidence.execution-missing",
                        "Claim row must link to execution/proof evidence, not only prose", report));
            }
        }
        if (checkedClaims == 0) {
            result.add(McpPackageViolation.blocker("claim-evidence.missing",
                    "Proof Mapping has no claim rows that can be audited", report));
        }
    }

    private void requireClaimRowToken(String normalizedRow,
                                      Path report,
                                      McpPackageValidationResult result,
                                      String code,
                                      String message,
                                      String... acceptedTokens) {
        if (!McpRuleSupport.containsAny(normalizedRow, acceptedTokens)) {
            result.add(McpPackageViolation.blocker(code, message, report));
        }
    }
}
