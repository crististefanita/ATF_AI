package com.endava.ai.skills.mcp.tests.core;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.rules.business.BusinessEvidenceTraceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExamplePackage;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.mutatePackageState;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.endava.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("business-semantic-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html",
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff"
})
@McpSuiteRef("mcp.suite.mcp-semantic-package-validator-test")
public class McpSemanticPackageValidatorTest {
    @Test
    @ValidatesMcpContract("business-evidence.business-object.trace")
    @McpCheckRef("business-evidence.object.trace.negative")
    public void rejectsPackageWhenOwnedBusinessObjectLivesOnlyInPackageState() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        mutatePackageState(packageRoot, "business_object", "offline warranty certificate ledger");

        McpPackageValidationResult result = validateWithRules(packageRoot, new BusinessEvidenceTraceRule());

        logValidatorScenario("Reject business object without cross-artifact evidence trace",
                packageRoot,
                "Changed " + McpArtifactPaths.PACKAGE_STATE + " so business_object lives only in package-state.json.",
                "Expected violation: business-evidence.business-object.missing-in-artifact-family.",
                "Asserted artifact: " + McpArtifactPaths.PACKAGE_STATE,
                result);
        requireCode(result, "business-evidence.business-object.missing-in-artifact-family");
    }

    @Test
    @ValidatesMcpContract("business-evidence.new-frontier.trace")
    public void rejectsPackageWhenNewFrontierIsNotBackedByEvidenceChain() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        mutatePackageState(packageRoot, "new_frontier", "offline warranty renewal ledger");

        McpPackageValidationResult result = validateWithRules(packageRoot, new BusinessEvidenceTraceRule());

        logValidatorScenario("Reject new frontier without evidence chain",
                packageRoot,
                "Changed " + McpArtifactPaths.PACKAGE_STATE + " so new_frontier is not backed by report/run/diff/proof evidence.",
                "Expected violation: business-evidence.business-frontier.missing-in-artifact-family.",
                "Asserted artifact: " + McpArtifactPaths.PACKAGE_STATE,
                result);
        requireCode(result, "business-evidence.business-frontier.missing-in-artifact-family");
    }
}
