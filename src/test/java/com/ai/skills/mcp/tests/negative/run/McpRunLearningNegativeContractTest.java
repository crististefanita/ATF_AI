package com.ai.skills.mcp.tests.negative.run;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.run.RunLearningProgressionRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageSourceSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-learning-negative-contracts")
@RequiresArtifacts({
        "runs/run_##/SUMMARY.md",
        "runs/run_##/review/business-understanding.md"
})
@McpSuiteRef("mcp.suite.mcp-run-learning-negative-contract-test")
public class McpRunLearningNegativeContractTest {

    @Test
    @McpCheckRef("run.learning.progression.negative")
    public void rejectsPackageWhenRunLearningDoesNotProgressFromPriorRun() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        write(packageRoot.resolve("runs/run_03/SUMMARY.md"),
                "Run executed. Tests are green. Generic lesson only.");
        write(packageRoot.resolve("runs/run_03/review/business-understanding.md"),
                "Generic business understanding without prior progression.");

        McpPackageValidationResult result = validateWithRules(packageRoot, new RunLearningProgressionRule());

        logValidatorScenario(
                "Reject run learning without progression",
                packageRoot,
                "Replaced runs/run_03 learning artifacts with a generic lesson that does not reference prior progress.",
                "Expected violation: run-learning.progression-missing.",
                "Asserted artifact: runs/run_03/SUMMARY.md.",
                result);
        requireCode(result, "run-learning.progression-missing");
    }
}
