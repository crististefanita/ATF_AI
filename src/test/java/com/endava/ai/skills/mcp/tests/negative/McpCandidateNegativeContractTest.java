package com.endava.ai.skills.mcp.tests.negative;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpProjectLocator;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.rules.candidate.CandidateScoreJustificationRule;
import com.endava.ai.skills.mcp.rules.candidate.CandidateSingleFamilyCapRule;
import com.endava.ai.skills.mcp.rules.candidate.business.CandidateBusinessGraphObjectSpecificityRule;
import com.endava.ai.skills.mcp.rules.candidate.business.CandidateBusinessObjectDepthRule;
import com.endava.ai.skills.mcp.rules.candidate.business.CandidatePositiveOwnershipRule;
import com.endava.ai.skills.mcp.rules.candidate.html.HtmlExampleCandidateEvidenceTraceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.endava.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExamplePackage;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.mutatePackageState;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("html-example-candidate-negative-contracts")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html",
        "runs/run_##/review/run-state.json",
        "runs/run_##/review/thin-no-index.diff"
})
@McpSuiteRef("mcp.suite.mcp-candidate-negative-contract-test")
public class McpCandidateNegativeContractTest {
    @Test
    @ValidatesMcpContract("candidate.business-evidence.structural-proof-row")
    @McpCheckRef("candidate.semantic-proof-row.negative")
    public void rejectsCandidateWhenBusinessWordsAreRepeatedWithoutProofRow() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        String businessTokens = readPackageStateValue(packageRoot,
                "business_object", "owned_business_object", "business_object_owned");
        mutatePackageState(packageRoot, "business_object", businessTokens);
        appendBusinessTokensToRunArtifacts(packageRoot, businessTokens);

        mutateReport(packageRoot, html -> html
                .replace("Business Flow Graph</h2>",
                        "Business Flow Graph</h2><p>Candidate object: " + businessTokens + ".</p>")
                .replace("Run Audit</summary>",
                        "Run Audit</summary><p>Run audit repeats " + businessTokens + ".</p>")
                .replaceAll("(?is)<details\\b[^>]*>\\s*<summary>\\s*<span class=\"summary-title\">Proof Mapping</span>.*?</summary>.*?</details>",
                        "<details><summary><span class=\"summary-title\">Proof Mapping</span>"
                                + "<span class=\"sumhint\">claim to source to diff to state to proof</span></summary>"
                                + "<ul><li>Claim repeats " + businessTokens
                                + " but has no diff, no state file, no execution proof, and no cold verdict.</li></ul>"
                                + "</details>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new HtmlExampleCandidateEvidenceTraceRule());

        logValidatorScenario(
                "Reject candidate business token spam without structural proof row",
                packageRoot,
                "Repeated the package business_object in report prose while removing the Proof Mapping evidence chain.",
                "Expected violation: candidate.business-object.proof-row-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.business-object.proof-row-missing");
    }

    private static String readPackageStateValue(Path packageRoot, String... keys) throws IOException {
        String json = Files.readString(packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE));
        for (String key : keys) {
            Matcher matcher = Pattern.compile("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        throw new AssertionError("Fixture package-state.json must contain one business object key: "
                + String.join(", ", keys));
    }

    private static void appendBusinessTokensToRunArtifacts(Path packageRoot, String businessTokens) throws IOException {
        Path runsRoot = packageRoot.resolve("runs");
        try (var runs = Files.list(runsRoot)) {
            List<Path> runDirectories = runs
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .limit(2)
                    .collect(Collectors.toList());
            for (Path runDirectory : runDirectories) {
                appendIfPresent(runDirectory.resolve("review/run-state.json"), businessTokens);
                appendIfPresent(runDirectory.resolve("review/thin-no-index.diff"), businessTokens);
            }
        }
    }

    private static void appendIfPresent(Path artifact, String businessTokens) throws IOException {
        if (Files.isRegularFile(artifact)) {
            Files.writeString(artifact, Files.readString(artifact)
                    + System.lineSeparator()
                    + "candidate semantic trace: " + businessTokens
                    + System.lineSeparator());
        }
    }

    @Test
    @ValidatesMcpContract("candidate.business-object.depth")
    public void rejectsCandidateWhenOwnedObjectIsOnlyBoundaryState() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "business_object", "guest checkout payment success state");

        McpPackageValidationResult result = validateWithRules(packageRoot, new CandidateBusinessObjectDepthRule());

        logValidatorScenario(
                "Reject candidate business object that is only a boundary/screen/state",
                packageRoot,
                "Changed business_object to a non-durable success state.",
                "Expected violation: candidate.business-object.not-durable.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.business-object.not-durable");
    }

    @Test
    @ValidatesMcpContract("candidate.positive-ownership")
    public void rejectsCandidateWhenAbsenceIsSoldAsOwnedObject() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "business_object", "receipt identity absence");

        McpPackageValidationResult result = validateWithRules(packageRoot, new CandidatePositiveOwnershipRule());

        logValidatorScenario(
                "Reject candidate absence sold as positive ownership",
                packageRoot,
                "Changed business_object to an absence claim instead of a durable owned object.",
                "Expected violation: candidate.positive-ownership.absence-owned.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.positive-ownership.absence-owned");
    }

    @Test
    @ValidatesMcpContract("candidate.business-graph.object-specificity")
    public void rejectsCandidateWhenGraphOmitsNextClosureObject() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutatePackageState(packageRoot, "primary_next_business_object", "registered account invoice row");
        mutateReport(packageRoot, html -> html.replaceAll("(?is)<details\\b[^>]*>\\s*<summary>\\s*<span class=\"summary-title\">Business Flow Graph.*?</details>",
                "<details open><summary><span class=\"summary-title\">Business Flow Graph</span></summary>"
                        + "<p>Application map shows a generic next closure without the registered account invoice row.</p></details>")
                .replace("registered account invoice row", "future durable object"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CandidateBusinessGraphObjectSpecificityRule());

        logValidatorScenario(
                "Reject candidate graph that hides the next closure object",
                packageRoot,
                "Removed the primary next business object tokens from the Business Flow Graph.",
                "Expected violation: candidate.business-graph.next-closure-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.business-graph.next-closure-missing");
    }

    @Test
    @ValidatesMcpContract("candidate.score.justification")
    public void rejectsCandidateWhenScoreRowsAreGeneric() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();

        mutateReport(packageRoot, html -> html.replaceAll("(?is)<details\\b[^>]*>\\s*<summary>\\s*<span class=\"summary-title\">Score Overview.*?</details>",
                "<details><summary><span class=\"summary-title\">Score Overview</span></summary>"
                        + "<table><tr><td>Business Substance</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Business Graph</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Proof Mapping</td><td>93</td><td>good</td></tr>"
                        + "<tr><td>Overall</td><td>93</td><td>good</td></tr></table></details>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CandidateScoreJustificationRule());

        logValidatorScenario(
                "Reject candidate score cards with generic justification",
                packageRoot,
                "Replaced Score Overview rows with repeated generic text.",
                "Expected violation: candidate.score.justification-generic.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "candidate.score.justification-generic");
    }

    @Test
    @ValidatesMcpContract("candidate.single-family.cap")
    public void rejectsCandidateWhenSingleFamilyScoreExceedsRuntimeThresholdWithoutDepthEscape() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        int teacherThreshold = McpProjectLocator.intRuntimeProperty("RUN_EXPECTED_DELTA_QUALITY");

        mutatePackageState(packageRoot, "business_object", "registered account invoice row");
        mutatePackageState(packageRoot, "breadth_verdict", "partial");
        mutatePackageStateNumber(packageRoot, "overall", teacherThreshold + 1);
        mutatePackageStateNumber(packageRoot, "cap", teacherThreshold + 1);
        mutateReport(packageRoot, html -> html
                .replaceAll("(?is)detail route|details drawer|detail drawer|line item|line-item|second durable|secondary business object|another durable|two durable|multi-family",
                        "same family"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CandidateSingleFamilyCapRule());

        logValidatorScenario(
                "Reject candidate single-family package scored above runtime threshold without depth escape",
                packageRoot,
                "Raised Overall/cap above RUN_EXPECTED_DELTA_QUALITY while keeping breadth partial and no second durable object.",
                "Expected violation: candidate.single-family.cap-too-high.",
                "Asserted artifact: review/package-state.json.",
                result);
        requireCode(result, "candidate.single-family.cap-too-high");
    }

    private static void mutatePackageStateNumber(Path packageRoot, String key, int value) throws IOException {
        Path packageState = packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE);
        String json = Files.readString(packageState)
                .replaceAll("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\\d+",
                        "\"" + key + "\":" + value);
        Files.writeString(packageState, json);
    }
}
