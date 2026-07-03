package com.ai.skills.mcp.rules;

import com.ai.skills.mcp.rules.candidate.CandidateDiffPedagogyRule;
import com.ai.skills.mcp.rules.candidate.CandidateHtmlExamplePromotionRule;
import com.ai.skills.mcp.rules.candidate.CandidateProofTokenCompletenessRule;
import com.ai.skills.mcp.rules.candidate.CandidateReopenabilityProofRule;
import com.ai.skills.mcp.rules.candidate.CandidateRunProgressionRule;
import com.ai.skills.mcp.rules.candidate.CandidateScoreJustificationRule;
import com.ai.skills.mcp.rules.candidate.CandidateSingleFamilyCapRule;
import com.ai.skills.mcp.rules.candidate.business.BusinessLifecycleDepthRule;
import com.ai.skills.mcp.rules.candidate.business.CandidateBusinessGraphObjectSpecificityRule;
import com.ai.skills.mcp.rules.candidate.business.CandidateBusinessObjectDepthRule;
import com.ai.skills.mcp.rules.candidate.business.CandidatePositiveOwnershipRule;
import com.ai.skills.mcp.rules.candidate.html.HtmlExampleCandidateBusinessClosureRule;
import com.ai.skills.mcp.rules.candidate.html.HtmlExampleCandidateEvidenceTraceRule;
import com.ai.skills.mcp.rules.candidate.html.HtmlExampleCandidateScoreRule;

import java.util.Map;
import java.util.function.Supplier;

import static com.ai.skills.mcp.rules.McpExecutableRuleCatalog.*;

final class McpExecutableCandidateRuleFactories {
    private McpExecutableCandidateRuleFactories() {
    }

    static void register(Map<String, Supplier<McpValidationRule>> rules) {
        McpExecutableRuleCatalog.add(rules, CANDIDATE_SCORE, HtmlExampleCandidateScoreRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_BUSINESS_CLOSURE, HtmlExampleCandidateBusinessClosureRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_OBJECT_DEPTH, CandidateBusinessObjectDepthRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_POSITIVE_OWNERSHIP, CandidatePositiveOwnershipRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_REOPENABILITY, CandidateReopenabilityProofRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_PROOF_TOKEN, CandidateProofTokenCompletenessRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_RUN_PROGRESSION, CandidateRunProgressionRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_DIFF_PEDAGOGY, CandidateDiffPedagogyRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_SINGLE_FAMILY, CandidateSingleFamilyCapRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_GRAPH_OBJECT, CandidateBusinessGraphObjectSpecificityRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_SCORE_JUSTIFICATION, CandidateScoreJustificationRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_PROMOTION, CandidateHtmlExamplePromotionRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_LIFECYCLE, BusinessLifecycleDepthRule::new);
        McpExecutableRuleCatalog.add(rules, CANDIDATE_EVIDENCE_TRACE, HtmlExampleCandidateEvidenceTraceRule::new);
    }
}
