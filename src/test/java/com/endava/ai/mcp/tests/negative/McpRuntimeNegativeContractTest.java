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
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.write;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCodes;

@Listeners(TestListener.class)
@ValidatesMcpContract("runtime-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
public class McpRuntimeNegativeContractTest {
    @Test
    @ValidatesMcpContract("runtime.active-values.published")
    public void rejectsPackageWhenRuntimeTargetsDriftFromAgentRuntime() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        int roundTarget = intRuntimeProperty("ROUND_TARGET_TEST_COUNT");
        int metaIteration = intRuntimeProperty("META_ITERATION_COUNT");
        int driftRoundTarget = Math.max(1, roundTarget - 1);
        int driftMetaIteration = Math.max(1, metaIteration - 2);
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is)\"ROUND_TARGET_TEST_COUNT\"\\s*:\\s*\"?\\d+\"?",
                                "\"ROUND_TARGET_TEST_COUNT\":" + driftRoundTarget)
                        .replaceAll("(?is)\"META_ITERATION_COUNT\"\\s*:\\s*\"?\\d+\"?",
                                "\"META_ITERATION_COUNT\":" + driftMetaIteration)
                        .replaceAll("(?is)\"runs_expected\"\\s*:\\s*\"?\\d+\"?",
                                "\"runs_expected\":" + driftMetaIteration));
        mutateReport(packageRoot, html -> html
                .replace("ROUND_TARGET_TEST_COUNT = " + roundTarget, "ROUND_TARGET_TEST_COUNT = " + driftRoundTarget)
                .replace("META_ITERATION_COUNT = " + metaIteration, "META_ITERATION_COUNT = " + driftMetaIteration));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject runtime target drift",
                packageRoot,
                "Changed review/package-state.json to publish values that drift from active runtime constants.",
                "Expected violations: runtime.round-target and runtime.meta-iteration.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCodes(result, "runtime.round-target", "runtime.meta-iteration");
        StepLogger.pass("Validator rejected runtime counter drift");
    }
}
