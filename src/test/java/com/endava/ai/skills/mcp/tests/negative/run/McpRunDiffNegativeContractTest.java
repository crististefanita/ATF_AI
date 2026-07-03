package com.endava.ai.skills.mcp.tests.negative.run;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpProjectLayout;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.rules.run.RunDiffProgressionRule;
import com.endava.ai.skills.mcp.rules.run.RunIndependenceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageSourceSpine;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.intRuntimeProperty;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.endava.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.endava.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.endava.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-diff-negative-contracts")
@RequiresArtifacts("runs/run_##/review/thin-no-index.diff")
@McpSuiteRef("mcp.suite.mcp-run-diff-negative-contract-test")
public class McpRunDiffNegativeContractTest {
    private static final String REPEATED_RUN_TEST = McpProjectLayout.UI_TESTS_PATH + "RepeatedRunTest.java";
    private static final String ONLY_ROOT_TEST = McpProjectLayout.UI_TESTS_PATH + "OnlyRootTest.java";

    @Test
    @ValidatesMcpContract("run-diff.duplicate-all")
    @McpCheckRef("run.diff.duplicate-all.negative")
    public void rejectsPackageWhenAllRunDiffsAreIdentical() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        String repeatedDiff = "diff --git a/" + REPEATED_RUN_TEST + " b/" + REPEATED_RUN_TEST + "\n"
                + "--- a/" + REPEATED_RUN_TEST + "\n"
                + "+++ b/" + REPEATED_RUN_TEST + "\n"
                + "+same mutation for every run\n";
        for (int index = 1; index <= intRuntimeProperty("META_ITERATION_COUNT"); index++) {
            write(packageRoot.resolve(McpRunArtifacts.runArtifact(
                    String.format("run_%02d", index), McpRunArtifacts.DIFF)), repeatedDiff);
        }

        McpPackageValidationResult result = validateWithRules(packageRoot, new RunIndependenceRule());

        logValidatorScenario(
                "Reject duplicate per-run diffs",
                packageRoot,
                "Overwrote every runs/run_##/review/thin-no-index.diff with the same content.",
                "Expected violation: run-diff.duplicate-all.",
                "Asserted artifacts: runs/run_##/review/thin-no-index.diff.",
                result);
        requireCode(result, "run-diff.duplicate-all");
    }

    @Test
    @ValidatesMcpContract("run-diff.progression.no-source-files")
    @McpCheckRef("run.diff.progression.no-source-files.negative")
    public void rejectsNarrativeRunDiffWithoutSourceDelta() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Path runDiff = packageRoot.resolve(McpRunArtifacts.runArtifact("run_03", McpRunArtifacts.DIFF));
        write(runDiff, "# focused diff for run_03\n# claim: useful prose, but no source path\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new RunDiffProgressionRule());

        logValidatorScenario(
                "Reject run diff without parseable source delta",
                packageRoot,
                "Replaced runs/run_03/review/thin-no-index.diff with narrative-only content.",
                "Expected violation: run-diff.progression.no-source-files.",
                "Asserted artifact: runs/run_03/review/thin-no-index.diff.",
                result);
        requireCode(result, "run-diff.progression.no-source-files");
    }

    @Test
    @ValidatesMcpContract("run-diff.progression.not-compiled-into-root")
    @McpCheckRef("run.diff.progression.root-compilation.negative")
    public void rejectsRunDeltaMissingFromRootDiff() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        write(packageRoot.resolve(McpArtifactPaths.ROOT_DIFF),
                "diff --git a/" + ONLY_ROOT_TEST + " b/" + ONLY_ROOT_TEST + "\n"
                        + "--- a/" + ONLY_ROOT_TEST + "\n"
                        + "+++ b/" + ONLY_ROOT_TEST + "\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new RunDiffProgressionRule());

        logValidatorScenario(
                "Reject run delta missing from root diff",
                packageRoot,
                "Replaced review/root-source-thin-no-index.diff with a source path that omits kept run deltas.",
                "Expected violation: run-diff.progression.not-compiled-into-root.",
                "Asserted artifacts: review/root-source-thin-no-index.diff and runs/run_##/review/thin-no-index.diff.",
                result);
        requireCode(result, "run-diff.progression.not-compiled-into-root");
    }
}
