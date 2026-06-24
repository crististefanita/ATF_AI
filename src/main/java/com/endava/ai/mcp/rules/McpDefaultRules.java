package com.endava.ai.mcp.rules;

import com.endava.ai.mcp.contract.McpProfile;
import com.endava.ai.mcp.rules.business.BusinessEvidenceTraceRule;
import com.endava.ai.mcp.rules.business.BusinessNoveltyAgainstExistingSuiteRule;
import com.endava.ai.mcp.rules.business.DurableBusinessObjectRule;
import com.endava.ai.mcp.rules.business.NextFrontierSingularityRule;
import com.endava.ai.mcp.rules.candidate.CandidateBusinessGraphObjectSpecificityRule;
import com.endava.ai.mcp.rules.candidate.CandidateBusinessObjectDepthRule;
import com.endava.ai.mcp.rules.candidate.CandidateDiffPedagogyRule;
import com.endava.ai.mcp.rules.candidate.CandidateHtmlExamplePromotionRule;
import com.endava.ai.mcp.rules.candidate.CandidatePositiveOwnershipRule;
import com.endava.ai.mcp.rules.candidate.CandidateProofTokenCompletenessRule;
import com.endava.ai.mcp.rules.candidate.CandidateReopenabilityProofRule;
import com.endava.ai.mcp.rules.candidate.CandidateRunProgressionRule;
import com.endava.ai.mcp.rules.candidate.CandidateScoreJustificationRule;
import com.endava.ai.mcp.rules.candidate.CandidateSingleFamilyCapRule;
import com.endava.ai.mcp.rules.candidate.HtmlExampleCandidateBusinessClosureRule;
import com.endava.ai.mcp.rules.candidate.HtmlExampleCandidateEvidenceTraceRule;
import com.endava.ai.mcp.rules.candidate.HtmlExampleCandidateScoreRule;
import com.endava.ai.mcp.rules.packageaudit.FullMcpGateRule;
import com.endava.ai.mcp.rules.packageaudit.McpGateEvidenceFreshnessRule;
import com.endava.ai.mcp.rules.packageaudit.PackageAccountingRule;
import com.endava.ai.mcp.rules.packageaudit.PackageArtifactSpineRule;
import com.endava.ai.mcp.rules.packageaudit.PublishedArtifactNoiseRule;
import com.endava.ai.mcp.rules.packageaudit.ReadmeStateSyncRule;
import com.endava.ai.mcp.rules.pretraining.PretrainingIntakeRule;
import com.endava.ai.mcp.rules.report.BusinessGraphReportRule;
import com.endava.ai.mcp.rules.report.CloseOutReportRule;
import com.endava.ai.mcp.rules.report.ProofMappingReportRule;
import com.endava.ai.mcp.rules.report.ReportChromeRule;
import com.endava.ai.mcp.rules.report.LocalLinksRule;
import com.endava.ai.mcp.rules.report.ReportVsStateVocabularySyncRule;
import com.endava.ai.mcp.rules.report.ScoreOverviewReportRule;
import com.endava.ai.mcp.rules.run.LearningTraceQualityRule;
import com.endava.ai.mcp.rules.run.RunArtifactSpineRule;
import com.endava.ai.mcp.rules.run.RunCountRule;
import com.endava.ai.mcp.rules.run.RunExtentProofRule;
import com.endava.ai.mcp.rules.run.RunIndependenceRule;
import com.endava.ai.mcp.rules.run.RunLearningProgressionRule;
import com.endava.ai.mcp.rules.run.RunStateCompletenessRule;
import com.endava.ai.mcp.rules.runtime.RuntimeContractRule;
import com.endava.ai.mcp.rules.score.ScoreCapEvidenceRule;
import com.endava.ai.mcp.rules.source.ProtectedFrameworkMutationRule;
import com.endava.ai.mcp.rules.source.RealJavaTestCountabilityRule;
import com.endava.ai.mcp.rules.source.RootDiffSourceConsistencyRule;
import com.endava.ai.mcp.rules.ui.BaseTestUiRule;
import com.endava.ai.mcp.rules.ui.ClaimedAbsenceAssertionRule;
import com.endava.ai.mcp.rules.ui.NoDirectUiInfrastructureRule;
import com.endava.ai.mcp.rules.ui.RealUiInteractionRule;
import com.endava.ai.mcp.rules.ui.UiWrapperHandoffRule;
import com.endava.ai.mcp.rules.ui.ValidationLayerReportingRule;

import java.util.ArrayList;
import java.util.List;

public final class McpDefaultRules {
    private McpDefaultRules() {
    }

    public static List<McpValidationRule> create() {
        return fullContract();
    }

    public static McpRuleSet ruleSet(McpProfile profile) {
        return McpRuleSet.of(profile, rulesFor(profile));
    }

    public static List<McpValidationRule> rulesFor(McpProfile profile) {
        switch (profile) {
            case UNIVERSAL:
                return universalContract();
            case UI_PACKAGE:
                return uiContract();
            case HTML_REPORT:
                return htmlReportContract();
            case HTML_EXAMPLE_CANDIDATE:
                return htmlExampleCandidateContract();
            case FULL_PACKAGE:
                return fullContract();
            case SUITE_ATLAS:
                return suiteAtlasContract();
            default:
                throw new IllegalArgumentException("Unsupported MCP profile: " + profile);
        }
    }

    public static List<McpValidationRule> fullContract() {
        List<McpValidationRule> rules = new ArrayList<>();
        rules.addAll(universalContract());
        rules.addAll(uiContract());
        rules.addAll(htmlReportContract());
        return List.copyOf(rules);
    }

    public static List<McpValidationRule> universalContract() {
        return List.of(
                new RuntimeContractRule(),
                new PackageArtifactSpineRule(),
                new PackageAccountingRule(),
                new ReadmeStateSyncRule(),
                new RootDiffSourceConsistencyRule(),
                new ProtectedFrameworkMutationRule(),
                new BusinessNoveltyAgainstExistingSuiteRule(),
                new DurableBusinessObjectRule(),
                new BusinessEvidenceTraceRule(),
                new NextFrontierSingularityRule(),
                new ScoreCapEvidenceRule(),
                new RunLearningProgressionRule(),
                new ReportVsStateVocabularySyncRule(),
                new PretrainingIntakeRule(),
                new LearningTraceQualityRule(),
                new McpGateEvidenceFreshnessRule(),
                new FullMcpGateRule(),
                new PublishedArtifactNoiseRule(),
                new RealJavaTestCountabilityRule(),
                new LocalLinksRule()
        );
    }

    public static List<McpValidationRule> uiContract() {
        return List.of(
                new ClaimedAbsenceAssertionRule(),
                new UiWrapperHandoffRule(),
                new RunCountRule(),
                new RunArtifactSpineRule(),
                new RunStateCompletenessRule(),
                new RunExtentProofRule(),
                new RunIndependenceRule(),
                new BaseTestUiRule(),
                new NoDirectUiInfrastructureRule(),
                new ValidationLayerReportingRule(),
                new RealUiInteractionRule()
        );
    }

    public static List<McpValidationRule> htmlReportContract() {
        return List.of(
                new ReportChromeRule(),
                new BusinessGraphReportRule(),
                new ScoreOverviewReportRule(),
                new ProofMappingReportRule(),
                new CloseOutReportRule()
        );
    }

    public static List<McpValidationRule> htmlExampleCandidateContract() {
        return List.of(
                new HtmlExampleCandidateScoreRule(),
                new HtmlExampleCandidateBusinessClosureRule(),
                new CandidateBusinessObjectDepthRule(),
                new CandidatePositiveOwnershipRule(),
                new CandidateReopenabilityProofRule(),
                new CandidateProofTokenCompletenessRule(),
                new CandidateRunProgressionRule(),
                new CandidateDiffPedagogyRule(),
                new CandidateSingleFamilyCapRule(),
                new CandidateBusinessGraphObjectSpecificityRule(),
                new CandidateScoreJustificationRule(),
                new CandidateHtmlExamplePromotionRule(),
                new HtmlExampleCandidateEvidenceTraceRule()
        );
    }

    public static List<McpValidationRule> suiteAtlasContract() {
        List<McpValidationRule> rules = new ArrayList<>();
        rules.addAll(universalContract());
        rules.addAll(htmlReportContract());
        return List.copyOf(rules);
    }
}
