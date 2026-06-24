package com.endava.ai.mcp.tests.negative;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.context.McpProjectLayout;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.mcp.tests.support.McpNegativeFixture.cloneLatestPackage;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("source-negative-contracts")
@RequiresArtifacts({
        "review/root-source-thin-no-index.diff"
})
public class McpSourceNegativeContractTest {
    @Test
    @ValidatesMcpContract("source.protected-framework.not-mutated")
    public void rejectsPackageWhenRootDiffMutatesProtectedFrameworkLayers() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        Path rootDiff = packageRoot.resolve("review/root-source-thin-no-index.diff");
        String protectedRulePath = McpProjectLayout.MCP_MAIN_PATH + "rules/run/RunCountRule.java";
        Files.writeString(rootDiff, Files.readString(rootDiff)
                + "\ndiff --git a/" + protectedRulePath + " "
                + "b/" + protectedRulePath + "\n"
                + "+++ b/" + protectedRulePath + "\n"
                + "+// generated package tried to change MCP infrastructure\n");

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject generated package mutation of protected framework layers",
                packageRoot,
                "Injected a protected MCP framework path into review/root-source-thin-no-index.diff.",
                "Expected violation: framework-mutation.protected-path.",
                "Asserted artifact: review/root-source-thin-no-index.diff.",
                result);
        requireCode(result, "framework-mutation.protected-path");
        StepLogger.pass("Validator rejected framework/MCP mutation inside generated package diff");
    }
}
