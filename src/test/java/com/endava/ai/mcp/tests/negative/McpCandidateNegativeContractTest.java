package com.endava.ai.mcp.tests.negative;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpProjectLocator;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExamplePackage;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.mutatePackageState;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("html-example-candidate-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html",
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff"
})
public class McpCandidateNegativeContractTest {
    @Test
    @ValidatesMcpContract("candidate.business-evidence.structural-proof-row")
    public void rejectsCandidateWhenBusinessWordsAreRepeatedWithoutProofRow() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        String businessObject = "portable warranty renewal ledger";
        String businessTokens = "portable warranty renewal ledger";

        mutatePackageState(packageRoot, "business_object", businessObject);
        mutateReport(packageRoot, html -> html
                .replace("Business Flow Graph</h2>",
                        "Business Flow Graph</h2><p>Candidate object: " + businessTokens + ".</p>")
                .replace("Run Audit</summary>",
                        "Run Audit</summary><p>Run audit repeats " + businessTokens + ".</p>")
                .replaceAll("(?is)<details\\b[^>]*>\\s*<summary>Proof Mapping</summary>.*?</details>",
                        "<details><summary>Proof Mapping</summary>"
                                + "<ul><li>Claim repeats " + businessTokens
                                + " but has no diff, no state file, no execution proof, and no cold verdict.</li></ul>"
                                + "</details>"));

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate business token spam without structural proof row",
                packageRoot,
                "Repeated the package business_object in report prose while removing the Proof Mapping evidence chain.",
                "Expected violation: candidate.business-object.proof-row-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.business-object.proof-row-missing");
        StepLogger.pass("Candidate contract rejected business words without proof-row evidence");
    }

    @Test
    @ValidatesMcpContract("candidate.business-object.depth")
    public void rejectsCandidateWhenOwnedObjectIsOnlyBoundaryState() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "business_object", "guest checkout payment success state");

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate business object that is only a boundary/screen/state",
                packageRoot,
                "Changed business_object to a non-durable success state.",
                "Expected violation: candidate.business-object.not-durable.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.business-object.not-durable");
        StepLogger.pass("Candidate contract rejected non-durable business object");
    }

    @Test
    @ValidatesMcpContract("candidate.positive-ownership")
    public void rejectsCandidateWhenAbsenceIsSoldAsOwnedObject() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "business_object", "receipt identity absence");

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate absence sold as positive ownership",
                packageRoot,
                "Changed business_object to an absence claim instead of a durable owned object.",
                "Expected violation: candidate.positive-ownership.absence-owned.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.positive-ownership.absence-owned");
        StepLogger.pass("Candidate contract rejected absence as positive ownership");
    }

    @Test
    @ValidatesMcpContract("candidate.business-graph.object-specificity")
    public void rejectsCandidateWhenGraphOmitsNextClosureObject() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "primary_next_business_object", "registered account invoice row");
        mutateReport(packageRoot, html -> html.replaceAll("(?is)<details\\b[^>]*>\\s*<summary>\\s*<span class=\"summary-title\">Business Flow Graph.*?</details>",
                "<details open><summary><span class=\"summary-title\">Business Flow Graph</span></summary>"
                        + "<p>Application map shows a generic next closure without the registered account invoice row.</p></details>")
                .replace("registered account invoice row", "future durable object"));

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate graph that hides the next closure object",
                packageRoot,
                "Removed the primary next business object tokens from the Business Flow Graph.",
                "Expected violation: candidate.business-graph.next-closure-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.business-graph.next-closure-missing");
        StepLogger.pass("Candidate contract rejected graph without next closure object");
    }

    @Test
    @ValidatesMcpContract("candidate.score.justification")
    public void rejectsCandidateWhenScoreRowsAreGeneric() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutateReport(packageRoot, html -> html.replaceAll("(?is)<details\\b[^>]*>\\s*<summary>\\s*<span class=\"summary-title\">Score Overview.*?</details>",
                "<details><summary><span class=\"summary-title\">Score Overview</span></summary>"
                        + "<table><tr><td>Business Substance</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Business Graph</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Proof Mapping</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Overall</td><td>93</td><td>good</td></tr></table></details>"));

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate score cards with generic justification",
                packageRoot,
                "Replaced Score Overview rows with repeated generic text.",
                "Expected violation: candidate.score.justification-generic.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.score.justification-generic");
        StepLogger.pass("Candidate contract rejected generic score justifications");
    }

    @Test
    @ValidatesMcpContract("candidate.single-family.cap")
    public void rejectsCandidateWhenSingleFamilyScoreExceedsRuntimeThresholdWithoutDepthEscape() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        int teacherThreshold = McpProjectLocator.intRuntimeProperty("RUN_EXPECTED_DELTA_QUALITY");

        mutatePackageState(packageRoot, "business_object", "registered account invoice row");
        mutatePackageState(packageRoot, "breadth_verdict", "partial");
        mutatePackageStateNumber(packageRoot, "overall", teacherThreshold + 1);
        mutatePackageStateNumber(packageRoot, "cap", teacherThreshold + 1);
        mutateReport(packageRoot, html -> html
                .replaceAll("(?is)detail route|details drawer|detail drawer|line item|line-item|second durable|secondary business object|another durable|two durable|multi-family",
                        "same family"));

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .htmlExampleCandidate(projectRoot)
                .validate(packageRoot);

        logValidatorScenario(
                "Reject candidate single-family package scored above runtime threshold without depth escape",
                packageRoot,
                "Raised Overall/cap above RUN_EXPECTED_DELTA_QUALITY while keeping breadth partial and no second durable object.",
                "Expected violation: candidate.single-family.cap-too-high.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.single-family.cap-too-high");
        StepLogger.pass("Candidate contract rejected inflated single-family score");
    }

    private static void mutatePackageStateNumber(Path packageRoot, String key, int value) throws IOException {
        Path packageState = packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE);
        String json = Files.readString(packageState)
                .replaceAll("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\\d+",
                        "\"" + key + "\":" + value);
        Files.writeString(packageState, json);
    }
}
