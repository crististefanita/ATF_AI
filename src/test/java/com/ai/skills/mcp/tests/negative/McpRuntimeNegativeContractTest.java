package com.ai.skills.mcp.tests.negative;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.packageaudit.PackageAccountingRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExampleReportSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.intRuntimeProperty;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCodes;

@Listeners(TestListener.class)
@ValidatesMcpContract("runtime-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
@McpSuiteRef("mcp.suite.mcp-runtime-negative-contract-test")
public class McpRuntimeNegativeContractTest {
    @Test
    @ValidatesMcpContract("runtime.active-values.published")
    @McpCheckRef("runtime.targets.no-drift.negative")
    public void rejectsPackageWhenRuntimeTargetsDriftFromAgentRuntime() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
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

        McpPackageValidationResult result = validateWithRules(packageRoot, new PackageAccountingRule());

        logValidatorScenario(
                "Reject runtime target drift",
                packageRoot,
                "Changed review/package-state.json to publish values that drift from active runtime constants.",
                "Expected violations: runtime.round-target and runtime.meta-iteration.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCodes(result, "runtime.round-target", "runtime.meta-iteration");
    }
}
