package com.endava.ai.skills.mcp.tests.negative;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.step.StepScope;
import com.endava.ai.skills.mcp.rules.report.ScoreOverviewReportRule;
import com.endava.ai.skills.mcp.rules.score.ScoreCapEvidenceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExampleReportSpine;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.mutatePackageState;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.endava.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.hasCode;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCode;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCodes;

@Listeners(TestListener.class)
@ValidatesMcpContract("score-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
@McpSuiteRef("mcp.suite.mcp-score-negative-contract-test")
public class McpScoreNegativeContractTest {
    @Test
    @McpCheckRef("score.overall.state-sync.negative")
    public void rejectsPackageWhenScoreOverviewOverallDriftsFromPackageState() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is)\"overall\"\\s*:\\s*\"?\\d+\"?", "\"overall\":\"77\""));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ScoreOverviewReportRule());

        logValidatorScenario(
                "Reject Overall drift between package-state and report",
                packageRoot,
                "Changed review/package-state.json overall while leaving Score Overview unchanged.",
                "Expected violation: report.score.overall-drift.",
                "Asserted artifacts: review/package-state.json and analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.score.overall-drift");
    }

    @Test
    public void rejectsPackageWhenScoreCapEvidenceIsMissing() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        write(packageRoot.resolve("review/package-state.json"),
                Files.readString(packageRoot.resolve("review/package-state.json"))
                        .replaceAll("(?is),?\\s*\"cap_reason\"\\s*:\\s*\"[^\"]*\"", "")
                        .replaceAll("(?is),?\\s*\"weakest_axis\"\\s*:\\s*\"[^\"]*\"", ""));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ScoreCapEvidenceRule());

        logValidatorScenario(
                "Reject missing score cap evidence",
                packageRoot,
                "Removed cap_reason and weakest_axis from review/package-state.json.",
                "Expected violations: score.cap-evidence.reason-missing and score.cap-evidence.weakest-axis-missing.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCodes(result, "score.cap-evidence.reason-missing", "score.cap-evidence.weakest-axis-missing");
    }

    @Test
    public void acceptsSemanticWeakestAxisPublicationWithoutLiteralWording() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        mutatePackageState(packageRoot, "weakest_axis", "Artifact truth score");
        mutateReport(packageRoot, html -> html.replace("</body>",
                "<section>Cold cap rationale: artifact truth remains capped by linked close-out evidence.</section>"
                        + "</body>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ScoreCapEvidenceRule());

        try (StepScope scope = StepLogger.step("Accept semantic weakest-axis publication")) {
            StepLogger.logDetail("Package: " + packageRoot);
            StepLogger.logDetail("Mutation: weakest_axis=Artifact truth score");
            StepLogger.logDetail("Report wording: artifact truth remains capped by linked close-out evidence");
            StepLogger.logDetail("Expected: no score.cap-evidence.not-published violation");
            scope.pass("Weakest-axis publication accepted semantically");
        }
        Assert.assertFalse(hasCode(result, "score.cap-evidence.not-published"),
                "Weakest-axis publication should be semantic, not fragile literal text: " + result.violations());
    }
}
