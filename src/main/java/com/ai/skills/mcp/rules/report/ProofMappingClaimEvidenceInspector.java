package com.ai.skills.mcp.rules.report;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

final class ProofMappingClaimEvidenceInspector {
    private ProofMappingClaimEvidenceInspector() {
    }

    static void requireClaimToEvidenceChains(String html, Path report, McpPackageValidationResult result) {
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

    private static void requireClaimRowToken(String normalizedRow,
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
