package com.ai.skills.mcp.tests.contracts.support;

import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.policy.McpClaimActionContractSpec;
import org.testng.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class McpYamlClaimActionSchemaAssertions {
    private static final Set<String> CLAIM_ACTION_CODES = Set.of(
            "claim-action.relogin-without-session-boundary",
            "claim-action.persist-without-post-boundary-assertion",
            "claim-action.edit-without-save-and-assertion",
            "claim-action.delete-without-absence-assertion",
            "claim-action.history-without-row-or-identity-proof",
            "claim-action.submit-without-acknowledgement-proof");
    private static final Set<String> CLAIM_ACTION_SCOPES = Set.of("action", "proof", "both");

    private McpYamlClaimActionSchemaAssertions() {
    }

    static void assertClaimActionContracts() {
        List<McpClaimActionContractSpec> specs = ContractCatalog.claimActionContracts();
        Assert.assertFalse(specs.isEmpty(), "claim-action-contracts.yml must declare verb/evidence contracts.");

        Set<String> ids = specs.stream().map(McpClaimActionContractSpec::id).collect(Collectors.toUnmodifiableSet());
        Assert.assertEquals(ids, CLAIM_ACTION_CODES,
                "Claim-action YAML must own the known blocker codes instead of Java switch statements.");

        Set<String> verbs = specs.stream()
                .flatMap(spec -> spec.verbs().stream())
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(verbs.containsAll(List.of("persist", "relogin", "delete", "submit", "send")),
                "Claim-action YAML must publish the high-risk business verbs.");

        for (McpClaimActionContractSpec spec : specs) {
            Assert.assertFalse(spec.verbs().isEmpty(), "Claim-action verbs missing for " + spec.id());
            Assert.assertFalse(spec.requiredEvidence().isEmpty(),
                    "Claim-action required evidence groups missing for " + spec.id());
            Assert.assertFalse(spec.meaning().isBlank(), "Claim-action meaning missing for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Claim-action repair missing for " + spec.id());
            for (McpClaimActionContractSpec.EvidenceGroup group : spec.requiredEvidence()) {
                Assert.assertTrue(CLAIM_ACTION_SCOPES.contains(group.scope()),
                        "Unsupported claim-action evidence scope for " + spec.id() + ": " + group.scope());
                Assert.assertFalse(group.tokens().isEmpty(),
                        "Claim-action evidence group must expose at least one token for " + spec.id());
            }
        }
    }
}
