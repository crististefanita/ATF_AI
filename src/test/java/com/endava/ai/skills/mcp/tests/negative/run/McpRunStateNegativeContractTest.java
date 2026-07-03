package com.endava.ai.skills.mcp.tests.negative.run;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.rules.run.RunArtifactSpineRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackage;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.endava.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-state-negative-contracts")
@RequiresArtifacts({
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff",
        "runs/run_##/SUMMARY.md"
})
@McpSuiteRef("mcp.suite.mcp-run-state-negative-contract-test")
public class McpRunStateNegativeContractTest {

    @Test
    @McpCheckRef("run.state.required.negative")
    public void rejectsPackageWhenRunStateIsMissing() throws IOException {
        Path packageRoot = cloneLatestPackage();
        Files.delete(packageRoot.resolve("runs/run_03/review/run-state.json"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new RunArtifactSpineRule());

        logValidatorScenario(
                "Reject missing run-state artifact",
                packageRoot,
                "Deleted runs/run_03/review/run-state.json from an otherwise valid package fixture.",
                "Expected violation: file.missing.",
                "Asserted artifact: runs/run_03/review/run-state.json.",
                result);
        requireCode(result, "file.missing");
    }
}
