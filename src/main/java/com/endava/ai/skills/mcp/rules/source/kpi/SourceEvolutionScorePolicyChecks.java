package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class SourceEvolutionScorePolicyChecks {
    private SourceEvolutionScorePolicyChecks() {
    }

    static void requireScoreCapFromKpiMatrix(McpPackageContext context,
                                             String ledgerText,
                                             Path ledger,
                                             SourceEvolutionInventory inventory,
                                             McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        int publishedScore = publishedScore(context, result, packageState);
        requireCloseoutInfoThresholds(ledger, inventory, publishedScore, result);
        if (!Files.isRegularFile(packageState) || publishedScore <= 0) {
            return;
        }
        if (blockerKpiFailed(ledgerText, inventory)
                && publishedScore > context.runtime().expectedDeltaQualityAsOverallCap()) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.score-cap-required",
                    "KPI matrix has BLOCKER mismatch/missing values, so Overall/cap must not exceed "
                            + context.runtime().expectedDeltaQualityAsOverallCap()
                            + "; published score/cap max=" + publishedScore,
                    ledger));
        }
        if (inventory.hasProtectedRootFiles() && publishedScore > 88) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.protected-root-cap-required",
                    "protected_root_dirty_count > 0 requires cap <= 88 or undoing protected source drift; published score/cap max="
                            + publishedScore,
                    ledger));
        }
    }

    private static int publishedScore(McpPackageContext context,
                                      McpPackageValidationResult result,
                                      Path packageState) {
        if (!Files.isRegularFile(packageState)) {
            return 0;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.extractIntValue(state, "overall");
        Integer cap = McpRuleSupport.extractIntValue(state, "cap");
        return Math.max(overall == null ? 0 : overall, cap == null ? 0 : cap);
    }

    private static boolean blockerKpiFailed(String ledgerText, SourceEvolutionInventory inventory) {
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.rootKpis()) {
            Integer actual = SourceEvolutionKpiReader.readNumericValue(ledgerText, spec.id());
            int expected = inventory.expectedValue(spec.id());
            if (actual == null || actual != expected) {
                return true;
            }
        }
        for (Map.Entry<String, Integer> run : inventory.runFileCounts().entrySet()) {
            Integer actual = SourceEvolutionKpiReader.readNumericValue(ledgerText, run.getKey());
            if (actual == null || actual != run.getValue()) {
                return true;
            }
        }
        return false;
    }

    private static void requireCloseoutInfoThresholds(Path ledger,
                                                      SourceEvolutionInventory inventory,
                                                      int publishedScore,
                                                      McpPackageValidationResult result) {
        if (inventory.expectedValue("final_report_mcp_link_completeness") < 100) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.final-report-links-incomplete",
                    "final_report_mcp_link_completeness must be 100 for package close-out; observed "
                            + inventory.expectedValue("final_report_mcp_link_completeness"),
                    ledger));
        }
        if (inventory.expectedValue("closeout_profile_purity") < 100) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.closeout-profile-not-primary",
                    "closeout_profile_purity must be 100: final MCP evidence must use package-closeout or package-audit profile",
                    ledger));
        }
        if (inventory.expectedValue("mcp_extent_freshness") < 100) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.mcp-extent-provenance-incomplete",
                    "mcp_extent_freshness must be 100: ExtentReport.html, ExtentReport.sha256 and extent-origin.json must exist together",
                    ledger));
        }
        if ((inventory.expectedValue("direct_ui_engine_leakage_count") > 0
                || inventory.expectedValue("layering_violation_count") > 0) && publishedScore > 90) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.ui-layering-cap-required",
                    "Direct UI/API engine leakage requires score cap <= 90 or refactoring through support/page/service layers",
                    ledger));
        }
        if ((inventory.expectedValue("duplicate_action_line_ratio") > 25
                || inventory.expectedValue("duplicate_assertion_line_ratio") > 25) && publishedScore > 92) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.duplication-cap-required",
                    "Duplicate action/assertion ratio above 25 requires score cap <= 92 or support/validation refactor",
                    ledger));
        }
    }
}
