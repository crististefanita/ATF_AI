package com.ai.skills.mcp.rules.business;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpViolationBuilder;
import com.ai.skills.mcp.context.McpClaimEvidence;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.policy.McpClaimActionContractSpec;

import java.util.List;

final class ClaimActionContractEvaluator {
    private ClaimActionContractEvaluator() {
    }

    static void validate(McpClaimEvidence evidence, McpPackageValidationResult result) {
        ContractCatalog.claimActionContracts().stream()
                .filter(spec -> spec.matchesVerb(evidence.normalizedVerb()))
                .forEach(spec -> require(evidence, result, spec, satisfied(evidence, spec)));
    }

    private static void require(McpClaimEvidence evidence,
                                McpPackageValidationResult result,
                                McpClaimActionContractSpec spec,
                                boolean condition) {
        if (condition) {
            return;
        }
        result.add(McpViolationBuilder.blocker(spec.id())
                .message("Run " + evidence.runId() + " publishes verb '" + evidence.normalizedVerb()
                        + "' without compatible source/proof action. Claim: " + evidence.claimText())
                .path(evidence.runState())
                .meaning(spec.meaning() + ". Evidence chain: " + evidence.chainSummary()
                        + ". Missing/weak: " + evidence.missingEvidenceHint())
                .repair(spec.repair() + ". Keep the published verb no stronger than the cold evidence chain.")
                .artifact("run-state.json, thin-no-index.diff, source snapshot, Extent/XML, final Proof Mapping")
                .build());
    }

    private static boolean satisfied(McpClaimEvidence evidence, McpClaimActionContractSpec spec) {
        for (McpClaimActionContractSpec.EvidenceGroup group : spec.requiredEvidence()) {
            if (!any(corpus(evidence, group.scope()), group.tokens())) {
                return false;
            }
        }
        return true;
    }

    private static String corpus(McpClaimEvidence evidence, String scope) {
        if ("action".equals(scope)) {
            return evidence.actionCorpus();
        }
        if ("proof".equals(scope)) {
            return evidence.proofCorpus();
        }
        return evidence.actionCorpus() + evidence.proofCorpus();
    }

    private static boolean any(String corpus, List<String> tokens) {
        String normalized = ClaimActionEvidenceFactory.normalize(corpus);
        return tokens.stream().anyMatch(normalized::contains);
    }
}
