package com.ai.skills.mcp.tests.negative;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.business.ClaimSourceActionAlignmentRule;
import com.ai.skills.mcp.rules.business.DurableBusinessObjectRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageFirstRunOnly;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExampleReportSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("business-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json"
})
@McpSuiteRef("mcp.suite.mcp-business-negative-contract-test")
public class McpBusinessNegativeContractTest {
    @Test
    @McpCheckRef("business.next-object.required.negative")
    public void rejectsPackageWhenPrimaryNextBusinessObjectIsMissing() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is),?\\s*\"primary_next_business_object\"\\s*:\\s*\"[^\"]*\"", ""));

        McpPackageValidationResult result = validateWithRules(packageRoot, new DurableBusinessObjectRule());

        logValidatorScenario(
                "Reject missing primary next business object",
                packageRoot,
                "Removed primary_next_business_object from review/package-state.json.",
                "Expected violation: next-frontier.primary-missing.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "next-frontier.primary-missing");
    }

    @Test
    @ValidatesMcpContract("claim-action.source-proof-alignment")
    public void rejectsReloginClaimWithoutSessionBoundaryProof() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path state = packageRoot.resolve("runs/run_01/review/run-state.json");
        String mutated = Files.readString(state)
                .replaceAll("(?is)\"claim\"\\s*:\\s*\"[^\"]*\"",
                        "\"claim\": \"registered customer relogin proves account ownership without any session boundary\"")
                .replaceAll("(?is)\"progression\"\\s*:\\s*\"[^\"]*\"",
                        "\"progression\": \"claims relogin but keeps the original source and proof unchanged\"");
        write(state, mutated);

        McpPackageValidationResult result = validateWithRules(packageRoot, new ClaimSourceActionAlignmentRule());

        logValidatorScenario(
                "Reject relogin claim without source/proof session boundary",
                packageRoot,
                "Changed run_01 state to publish a relogin claim without changing source, diff or proof.",
                "Expected violation: claim-action.relogin-without-session-boundary.",
                "Asserted artifact: runs/run_01/review/run-state.json.",
                result);
        requireCode(result, "claim-action.relogin-without-session-boundary");
    }
}
