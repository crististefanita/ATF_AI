package com.endava.ai.mcp.tests.negative;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.mcp.tests.support.McpNegativeFixture.cloneLatestPackage;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.intRuntimeProperty;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.write;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-negative-contracts")
@RequiresArtifacts({
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff",
        "runs/run_##/SUMMARY.md"
})
public class McpRunNegativeContractTest {
    @Test
    public void rejectsPackageWhenRunStateIsMissing() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        Files.delete(packageRoot.resolve("runs/run_03/review/run-state.json"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject missing run-state artifact",
                packageRoot,
                "Deleted runs/run_03/review/run-state.json from an otherwise valid package fixture.",
                "Expected violation: file.missing.",
                "Asserted artifact: runs/run_03/review/run-state.json.",
                result);
        requireCode(result, "file.missing");
        StepLogger.pass("Validator rejected missing run-state artifact");
    }

    @Test
    @ValidatesMcpContract("run-diff.duplicate-all")
    public void rejectsPackageWhenAllRunDiffsAreIdentical() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        String repeatedDiff = "diff --git a/repeated b/repeated\n+same mutation for every run\n";
        for (int index = 1; index <= intRuntimeProperty("META_ITERATION_COUNT"); index++) {
            write(packageRoot.resolve(String.format("runs/run_%02d/review/thin-no-index.diff", index)), repeatedDiff);
        }

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject duplicate per-run diffs",
                packageRoot,
                "Overwrote every runs/run_##/review/thin-no-index.diff with the same content.",
                "Expected violation: run-diff.duplicate-all.",
                "Asserted artifacts: runs/run_##/review/thin-no-index.diff.",
                result);
        requireCode(result, "run-diff.duplicate-all");
        StepLogger.pass("Validator rejected non-distinct run history diffs");
    }

    @Test
    public void rejectsPackageWhenRunLearningDoesNotProgressFromPriorRun() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        write(packageRoot.resolve("runs/run_03/SUMMARY.md"),
                "Run executed. Tests are green. Generic lesson only.");
        write(packageRoot.resolve("runs/run_03/review/business-understanding.md"),
                "Generic business understanding without prior progression.");

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject run learning without progression",
                packageRoot,
                "Replaced runs/run_03 learning artifacts with a generic lesson that does not reference prior progress.",
                "Expected violation: run-learning.progression-missing.",
                "Asserted artifact: runs/run_03/SUMMARY.md.",
                result);
        requireCode(result, "run-learning.progression-missing");
        StepLogger.pass("Validator rejected non-progressive run learning");
    }
}
