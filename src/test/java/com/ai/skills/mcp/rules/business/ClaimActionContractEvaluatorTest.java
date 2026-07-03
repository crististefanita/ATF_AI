package com.ai.skills.mcp.rules.business;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpClaimEvidence;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

public class ClaimActionContractEvaluatorTest {
    @Test
    public void evaluatorUsesYamlContractForPersistBoundary() {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpClaimEvidence evidence = evidence(
                "persist billing preference",
                "persist",
                "save billing preference",
                "visible immediately");

        ClaimActionContractEvaluator.validate(evidence, result);

        Assert.assertEquals(result.violations().size(), 1);
        Assert.assertEquals(result.violations().get(0).code(),
                "claim-action.persist-without-post-boundary-assertion");
        Assert.assertTrue(result.violations().get(0).repairAction().contains("refresh/reopen/relogin"));
    }

    @Test
    public void evaluatorPassesWhenYamlEvidenceGroupsAreSatisfied() {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpClaimEvidence evidence = evidence(
                "persist billing preference",
                "persist",
                "save billing preference",
                "refresh page and assert billing preference visible");

        ClaimActionContractEvaluator.validate(evidence, result);

        Assert.assertTrue(result.violations().isEmpty(), "Satisfied YAML evidence groups must not raise blockers.");
    }

    @Test
    public void claimActionVerbsComeFromYamlCatalog() {
        Assert.assertTrue(ClaimActionEvidenceFactory.claimActionVerbs().containsAll(
                List.of("persist", "relogin", "delete", "submit", "send")));
    }

    private static McpClaimEvidence evidence(String claim, String verb, String actionCorpus, String proofCorpus) {
        return new McpClaimEvidence(
                claim,
                verb,
                "billing preference",
                "run_01",
                Path.of("run-state.json"),
                Path.of("thin-no-index.diff"),
                List.of(Path.of("source/Test.java")),
                List.of(Path.of("testng-results.xml")),
                Path.of("ExtentReport.html"),
                "<tr>run_01</tr>",
                "PASS",
                ClaimActionEvidenceFactory.normalize(actionCorpus),
                ClaimActionEvidenceFactory.normalize(proofCorpus));
    }
}
