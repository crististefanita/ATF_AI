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
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.write;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("business-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json"
})
public class McpBusinessNegativeContractTest {
    @Test
    public void rejectsPackageWhenPrimaryNextBusinessObjectIsMissing() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is),?\\s*\"primary_next_business_object\"\\s*:\\s*\"[^\"]*\"", ""));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject missing primary next business object",
                packageRoot,
                "Removed primary_next_business_object from review/package-state.json.",
                "Expected violation: next-frontier.primary-missing.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "next-frontier.primary-missing");
        StepLogger.pass("Validator rejected broad next-frontier handoff");
    }
}
