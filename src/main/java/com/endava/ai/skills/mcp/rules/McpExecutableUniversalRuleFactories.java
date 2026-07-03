package com.endava.ai.skills.mcp.rules;

import com.endava.ai.skills.mcp.rules.business.BusinessEvidenceTraceRule;
import com.endava.ai.skills.mcp.rules.business.BusinessNoveltyAgainstExistingSuiteRule;
import com.endava.ai.skills.mcp.rules.business.ClaimSourceActionAlignmentRule;
import com.endava.ai.skills.mcp.rules.business.DurableBusinessObjectRule;
import com.endava.ai.skills.mcp.rules.business.NextFrontierSingularityRule;
import com.endava.ai.skills.mcp.rules.packageaudit.closeout.FullMcpGateRule;
import com.endava.ai.skills.mcp.rules.packageaudit.closeout.freshness.McpGateEvidenceFreshnessRule;
import com.endava.ai.skills.mcp.rules.packageaudit.PackageAccountingRule;
import com.endava.ai.skills.mcp.rules.packageaudit.PackageArtifactSpineRule;
import com.endava.ai.skills.mcp.rules.packageaudit.PublishedArtifactNoiseRule;
import com.endava.ai.skills.mcp.rules.packageaudit.ReadmeStateSyncRule;
import com.endava.ai.skills.mcp.rules.packageaudit.gate.McpKpiCloseoutArtifactRule;
import com.endava.ai.skills.mcp.rules.pretraining.PretrainingIntakeRule;
import com.endava.ai.skills.mcp.rules.report.BusinessGraphReportRule;
import com.endava.ai.skills.mcp.rules.report.CloseOutReportRule;
import com.endava.ai.skills.mcp.rules.report.LocalLinksRule;
import com.endava.ai.skills.mcp.rules.report.ProofMappingReportRule;
import com.endava.ai.skills.mcp.rules.report.ReportChromeRule;
import com.endava.ai.skills.mcp.rules.report.ReportVsStateVocabularySyncRule;
import com.endava.ai.skills.mcp.rules.report.ScoreOverviewReportRule;
import com.endava.ai.skills.mcp.rules.run.LearningTraceQualityRule;
import com.endava.ai.skills.mcp.rules.run.RunLearningProgressionRule;
import com.endava.ai.skills.mcp.rules.runtime.RuntimeContractRule;
import com.endava.ai.skills.mcp.rules.score.ScoreCapEvidenceRule;
import com.endava.ai.skills.mcp.rules.source.ProtectedFrameworkMutationRule;
import com.endava.ai.skills.mcp.rules.source.RealJavaTestCountabilityRule;
import com.endava.ai.skills.mcp.rules.source.RootDiffSourceConsistencyRule;
import com.endava.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiConsistencyRule;
import com.endava.ai.skills.mcp.rules.source.ledger.SourceEvolutionLedgerRule;
import com.endava.ai.skills.mcp.rules.source.protectedroot.ProtectedGitWorkspaceRule;

import java.util.Map;
import java.util.function.Supplier;

import static com.endava.ai.skills.mcp.rules.McpExecutableRuleCatalog.*;

final class McpExecutableUniversalRuleFactories {
    private McpExecutableUniversalRuleFactories() {
    }

    static void register(Map<String, Supplier<McpValidationRule>> rules) {
        McpExecutableRuleCatalog.add(rules, REPORT_CHROME, ReportChromeRule::new);
        McpExecutableRuleCatalog.add(rules, BUSINESS_GRAPH, BusinessGraphReportRule::new);
        McpExecutableRuleCatalog.add(rules, SCORE_OVERVIEW, ScoreOverviewReportRule::new);
        McpExecutableRuleCatalog.add(rules, PROOF_MAPPING, ProofMappingReportRule::new);
        McpExecutableRuleCatalog.add(rules, CLOSE_OUT_REPORT, CloseOutReportRule::new);
        McpExecutableRuleCatalog.add(rules, RUNTIME_CONTRACT, RuntimeContractRule::new);
        McpExecutableRuleCatalog.add(rules, PACKAGE_SPINE, PackageArtifactSpineRule::new);
        McpExecutableRuleCatalog.add(rules, PACKAGE_ACCOUNTING, PackageAccountingRule::new);
        McpExecutableRuleCatalog.add(rules, README_SYNC, ReadmeStateSyncRule::new);
        McpExecutableRuleCatalog.add(rules, ROOT_DIFF, RootDiffSourceConsistencyRule::new);
        McpExecutableRuleCatalog.add(rules, SOURCE_LEDGER, SourceEvolutionLedgerRule::new);
        McpExecutableRuleCatalog.add(rules, SOURCE_KPI, SourceEvolutionKpiConsistencyRule::new);
        McpExecutableRuleCatalog.add(rules, PROTECTED_FRAMEWORK, ProtectedFrameworkMutationRule::new);
        McpExecutableRuleCatalog.add(rules, PROTECTED_GIT, ProtectedGitWorkspaceRule::new);
        McpExecutableRuleCatalog.add(rules, BUSINESS_NOVELTY, BusinessNoveltyAgainstExistingSuiteRule::new);
        McpExecutableRuleCatalog.add(rules, DURABLE_OBJECT, DurableBusinessObjectRule::new);
        McpExecutableRuleCatalog.add(rules, BUSINESS_TRACE, BusinessEvidenceTraceRule::new);
        McpExecutableRuleCatalog.add(rules, CLAIM_ACTION, ClaimSourceActionAlignmentRule::new);
        McpExecutableRuleCatalog.add(rules, NEXT_FRONTIER, NextFrontierSingularityRule::new);
        McpExecutableRuleCatalog.add(rules, SCORE_CAP, ScoreCapEvidenceRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_LEARNING, RunLearningProgressionRule::new);
        McpExecutableRuleCatalog.add(rules, REPORT_STATE_SYNC, ReportVsStateVocabularySyncRule::new);
        McpExecutableRuleCatalog.add(rules, PRETRAINING, PretrainingIntakeRule::new);
        McpExecutableRuleCatalog.add(rules, LEARNING_TRACE, LearningTraceQualityRule::new);
        McpExecutableRuleCatalog.add(rules, GATE_FRESHNESS, McpGateEvidenceFreshnessRule::new);
        McpExecutableRuleCatalog.add(rules, PACKAGE_CLOSEOUT, FullMcpGateRule::new);
        McpExecutableRuleCatalog.add(rules, KPI_CLOSEOUT, McpKpiCloseoutArtifactRule::new);
        McpExecutableRuleCatalog.add(rules, NO_TEMPLATE_NOISE, PublishedArtifactNoiseRule::new);
        McpExecutableRuleCatalog.add(rules, TEST_COUNTABILITY, RealJavaTestCountabilityRule::new);
        McpExecutableRuleCatalog.add(rules, LOCAL_LINKS, LocalLinksRule::new);
    }
}
