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
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCodes;

@Listeners(TestListener.class)
@ValidatesMcpContract("score-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
public class McpScoreNegativeContractTest {
    @Test
    public void rejectsPackageWhenScoreOverviewOverallDriftsFromPackageState() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is)\"overall\"\\s*:\\s*\"?\\d+\"?", "\"overall\":\"77\""));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject Overall drift between package-state and report",
                packageRoot,
                "Changed review/package-state.json overall while leaving Score Overview unchanged.",
                "Expected violation: report.score.overall-drift.",
                "Asserted artifacts: review/package-state.json and analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.score.overall-drift");
        StepLogger.pass("Validator rejected score overview drift");
    }

    @Test
    public void rejectsPackageWhenScoreCapEvidenceIsMissing() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is),?\\s*\"cap_reason\"\\s*:\\s*\"[^\"]*\"", "")
                        .replaceAll("(?is),?\\s*\"weakest_axis\"\\s*:\\s*\"[^\"]*\"", ""));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject missing score cap evidence",
                packageRoot,
                "Removed cap_reason and weakest_axis from review/package-state.json.",
                "Expected violations: score.cap-evidence.reason-missing and score.cap-evidence.weakest-axis-missing.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCodes(result, "score.cap-evidence.reason-missing", "score.cap-evidence.weakest-axis-missing");
        StepLogger.pass("Validator rejected score cap without evidence");
    }
}
