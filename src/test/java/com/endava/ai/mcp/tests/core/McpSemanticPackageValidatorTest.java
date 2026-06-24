package com.endava.ai.mcp.tests.core;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.endava.ai.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExamplePackage;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.mutatePackageState;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("business-semantic-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html",
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff"
})
public class McpSemanticPackageValidatorTest {
    @Test
    @ValidatesMcpContract("business-evidence.business-object.trace")
    public void rejectsPackageWhenOwnedBusinessObjectLivesOnlyInPackageState() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        mutatePackageState(packageRoot, "business_object", "offline warranty certificate ledger");

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

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
        Path projectRoot = projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        mutatePackageState(packageRoot, "new_frontier", "offline warranty renewal ledger");

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario("Reject new frontier without evidence chain",
                packageRoot,
                "Changed " + McpArtifactPaths.PACKAGE_STATE + " so new_frontier is not backed by report/run/diff/proof evidence.",
                "Expected violation: business-evidence.business-frontier.missing-in-artifact-family.",
                "Asserted artifact: " + McpArtifactPaths.PACKAGE_STATE,
                result);
        requireCode(result, "business-evidence.business-frontier.missing-in-artifact-family");
    }
}
