package com.ai.skills.mcp.rules.source.inventory;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates calculated source-evolution facts used by KPI rules and diagnostics.
 * The ledger declares values; these inventories are the cold source of truth.
 */
public final class SourceEvolutionInventory {
    private final SourceDiffInventory sourceDiff;
    private final RunDeltaInventory runDelta;
    private final ReportEvidenceInventory reportEvidence;
    private final McpCloseoutEvidenceInventory closeoutEvidence;

    private SourceEvolutionInventory(SourceDiffInventory sourceDiff,
                                     RunDeltaInventory runDelta,
                                     ReportEvidenceInventory reportEvidence,
                                     McpCloseoutEvidenceInventory closeoutEvidence) {
        this.sourceDiff = sourceDiff;
        this.runDelta = runDelta;
        this.reportEvidence = reportEvidence;
        this.closeoutEvidence = closeoutEvidence;
    }

    public static SourceEvolutionInventory load(McpPackageContext context,
                                                String rootDiffText,
                                                McpPackageValidationResult result) {
        return new SourceEvolutionInventory(
                SourceDiffInventory.from(rootDiffText),
                RunDeltaInventory.load(context, result),
                ReportEvidenceInventory.load(context, result),
                McpCloseoutEvidenceInventory.load(context, result));
    }

    public int expectedValue(String id) {
        switch (id) {
            case "root_diff_files_count":
                return sourceDiff.sourceFiles();
            case "src_main_files":
                return sourceDiff.srcMainFiles();
            case "src_test_files":
                return sourceDiff.srcTestFiles();
            case "files_added":
                return sourceDiff.added();
            case "files_modified":
                return sourceDiff.modified();
            case "files_deleted":
                return sourceDiff.deleted();
            case "test_methods_added":
                return sourceDiff.testMethodsAdded();
            case "protected_root_dirty_count":
                return sourceDiff.protectedFiles();
            case "test_classes_added":
                return sourceDiff.testClasses();
            case "support_classes_added":
                return sourceDiff.supportClasses();
            case "support_cohesion_score":
                return sourceDiff.supportCohesionScore();
            case "duplicate_action_line_ratio":
                return sourceDiff.duplicateActionLineRatio();
            case "duplicate_assertion_line_ratio":
                return sourceDiff.duplicateAssertionLineRatio();
            case "test_duplication_signal":
                return sourceDiff.duplicateActionLineRatio();
            case "page_component_classes_touched":
                return sourceDiff.pageComponentClasses();
            case "direct_ui_engine_leakage_count":
                return sourceDiff.directUiEngineLeakage();
            case "layering_violation_count":
                return sourceDiff.layeringViolationCount();
            case "assertion_locality_score":
                return sourceDiff.assertionLocalityScore();
            case "business_claims_owned":
            case "business_object_ownership_count":
                return reportEvidence.businessClaimsOwned();
            case "diff_efficiency":
                return percentage(reportEvidence.businessClaimsOwned(), sourceDiff.sourceFiles());
            case "test_value_density":
                return percentage(reportEvidence.businessClaimsOwned(), sourceDiff.testMethodsAdded());
            case "support_reuse_ratio":
                return percentage(sourceDiff.testMethodsUsingSupport(), sourceDiff.testMethodsAdded());
            case "refactor_burn_down":
                return sourceDiff.refactorBurnDown();
            case "protected_root_purity":
                return sourceDiff.protectedFiles() == 0 ? 100 : 0;
            case "run_delta_uniqueness":
            case "run_learning_yield":
                return percentage(runDelta.runsWithDistinctSourceDelta(), runDelta.totalRunsWithDiff());
            case "artifact_reopenability_score":
            case "final_report_mcp_link_completeness":
                return reportEvidence.finalReportMcpLinkCompleteness();
            case "score_delta_justification_score":
                return reportEvidence.scoreDeltaJustificationScore();
            case "closeout_profile_purity":
                return closeoutEvidence.closeoutProfilePurity();
            case "mcp_extent_freshness":
                return closeoutEvidence.mcpExtentFreshness();
            case "skipped_closeout_count":
                return closeoutEvidence.skippedCloseoutCount();
            default:
                return runDelta.fileCounts().getOrDefault(id, 0);
        }
    }

    public Map<String, Integer> runFileCounts() {
        return new LinkedHashMap<>(runDelta.fileCounts());
    }

    public boolean hasProtectedRootFiles() {
        return sourceDiff.protectedFiles() > 0;
    }

    public int protectedRootFiles() {
        return sourceDiff.protectedFiles();
    }

    private static int percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return numerator == 0 ? 0 : 100;
        }
        return Math.round((numerator * 100.0f) / denominator);
    }
}
