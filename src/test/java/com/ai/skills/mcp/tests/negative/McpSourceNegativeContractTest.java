package com.ai.skills.mcp.tests.negative;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpProjectLayout;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.rules.source.ProtectedFrameworkMutationRule;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiConsistencyRule;
import com.ai.skills.mcp.rules.source.ledger.SourceEvolutionLedgerRule;
import com.ai.skills.mcp.rules.source.protectedroot.ProtectedGitWorkspaceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.assertGeneratedKpiArtifacts;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.assertProtectedPathRejected;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.deleteRecursivelyBestEffort;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.protectedPatch;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.runGit;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.seedKnownSourceDiffs;
import static com.ai.skills.mcp.tests.negative.support.McpSourceEvolutionNegativeSupport.sourceEvolutionLedger;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageSourceSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("source-negative-contracts")
@RequiresArtifacts({
        "review/root-source-thin-no-index.diff"
})
@McpSuiteRef("mcp.suite.mcp-source-negative-contract-test")
public class McpSourceNegativeContractTest {
    private static final String TEST_LISTENER_PATH =
            McpProjectLayout.CORE_MAIN_PATH + "reporting/listener/TestListener.java";

    @Test
    @ValidatesMcpContract("source.protected-framework.not-mutated")
    @McpCheckRef("source.protected-framework.not-mutated.negative")
    public void rejectsPackageWhenRootDiffMutatesProtectedFrameworkLayers() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Path rootDiff = packageRoot.resolve("review/root-source-thin-no-index.diff");
        String protectedRulePath = McpProjectLayout.MCP_MAIN_PATH + "rules/run/RunCountRule.java";
        Files.writeString(rootDiff, Files.readString(rootDiff)
                + "\ndiff --git a/" + protectedRulePath + " "
                + "b/" + protectedRulePath + "\n"
                + "+++ b/" + protectedRulePath + "\n"
                + "+// generated package tried to change MCP infrastructure\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProtectedFrameworkMutationRule());

        logValidatorScenario(
                "Reject generated package mutation of protected framework layers",
                packageRoot,
                "Injected a protected MCP framework path into review/root-source-thin-no-index.diff.",
                "Expected violation: framework-mutation.protected-path.",
                "Asserted artifact: review/root-source-thin-no-index.diff.",
                result);
        requireCode(result, "framework-mutation.protected-path");
    }

    @Test
    @ValidatesMcpContract("source.evolution.ledger")
    @McpCheckRef("source.evolution.ledger.missing.negative")
    public void rejectsPackageWithoutSourceEvolutionLedger() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Files.deleteIfExists(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER));

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionLedgerRule());

        logValidatorScenario(
                "Reject missing source evolution ledger",
                packageRoot,
                "Removed review/source-evolution-ledger.md.",
                "Expected violation: source-evolution.ledger.missing.",
                "Asserted artifact: review/source-evolution-ledger.md.",
                result);
        requireCode(result, "source-evolution.ledger.missing");
    }

    @Test
    @ValidatesMcpContract("source.evolution.ledger")
    @McpCheckRef("source.evolution.ledger.kpi.negative")
    public void rejectsSourceEvolutionLedgerWithoutKpisAndRunLinks() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Files.writeString(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER),
                "# Source evolution ledger\nbaseline_before_run_0\nfinal_before_closeout\nscore_delta\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionLedgerRule());

        logValidatorScenario(
                "Reject shallow source evolution ledger",
                packageRoot,
                "Wrote a ledger with the main section names but without KPIs, run deltas or source paths.",
                "Expected violation: source-evolution.ledger.kpi-missing.",
                "Asserted artifact: review/source-evolution-ledger.md.",
                result);
        requireCode(result, "source-evolution.ledger.kpi-missing");
        requireCode(result, "source-evolution.ledger.run-section-missing");
    }

    @Test
    @ValidatesMcpContract("source.evolution.kpi-consistency")
    @McpCheckRef("source.evolution.kpi.numeric-values.negative")
    public void rejectsSourceEvolutionLedgerWithKpiNamesButNoNumbers() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        seedKnownSourceDiffs(packageRoot, false);
        Files.writeString(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER),
                sourceEvolutionLedger(false, false, false));

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionKpiConsistencyRule());

        logValidatorScenario(
                "Reject non-numeric source evolution KPI ledger",
                packageRoot,
                "Wrote canonical KPI names and links, but omitted numeric key/value counters.",
                "Expected violation: source-evolution.kpi.numeric-missing.",
                "Asserted artifact: review/source-evolution-ledger.md.",
                result);
        requireCode(result, "source-evolution.kpi.numeric-missing");
        assertGeneratedKpiArtifacts(packageRoot);
    }

    @Test
    @ValidatesMcpContract("source.evolution.kpi-consistency")
    @McpCheckRef("source.evolution.kpi.value-mismatch.negative")
    public void rejectsSourceEvolutionLedgerWithWrongRootDiffCounts() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        seedKnownSourceDiffs(packageRoot, false);
        Files.writeString(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER),
                sourceEvolutionLedger(true, true, false).replace("root_diff_files_count: 2", "root_diff_files_count: 99"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionKpiConsistencyRule());

        logValidatorScenario(
                "Reject mismatched source evolution KPI counts",
                packageRoot,
                "Declared root_diff_files_count=99 while root diff exposes two source files.",
                "Expected violation: source-evolution.kpi.value-mismatch.",
                "Asserted artifacts: source-evolution-ledger.md and root-source-thin-no-index.diff.",
                result);
        requireCode(result, "source-evolution.kpi.value-mismatch");
    }

    @Test
    @ValidatesMcpContract("source.evolution.kpi-consistency")
    @McpCheckRef("source.evolution.kpi.protected-root-drift.negative")
    public void rejectsProtectedRootPurityLieWhenRootDiffTouchesProtectedPath() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        seedKnownSourceDiffs(packageRoot, true);
        Files.writeString(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER),
                sourceEvolutionLedger(true, true, true).replace("protected_root_dirty_count: 1", "protected_root_dirty_count: 0"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionKpiConsistencyRule());

        logValidatorScenario(
                "Reject protected-root purity lie",
                packageRoot,
                "Injected protected MCP path in root diff while declaring protected_root_dirty_count=0.",
                "Expected violation: source-evolution.kpi.protected-root-drift.",
                "Asserted artifacts: source-evolution-ledger.md and root-source-thin-no-index.diff.",
                result);
        requireCode(result, "source-evolution.kpi.protected-root-drift");
    }

    @Test
    @ValidatesMcpContract("source.evolution.kpi-consistency")
    @McpCheckRef("source.evolution.report-kpi-summary.negative")
    public void rejectsFinalReportThatLinksLedgerWithoutMatchingKpiSummary() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        seedKnownSourceDiffs(packageRoot, false);
        Files.writeString(packageRoot.resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER),
                sourceEvolutionLedger(true, true, false));
        Files.writeString(packageRoot.resolve(McpArtifactPaths.FINAL_REPORT),
                "<html><body><a href=\"../review/source-evolution-ledger.md\">source-evolution-ledger.md</a></body></html>");

        McpPackageValidationResult result = validateWithRules(packageRoot, new SourceEvolutionKpiConsistencyRule());

        logValidatorScenario(
                "Reject final report with ledger link but no KPI summary",
                packageRoot,
                "Linked source-evolution-ledger.md from the final report but omitted matching numeric KPI counters.",
                "Expected violation: source-evolution.report-kpi-summary.missing.",
                "Asserted artifacts: final report and source-evolution-ledger.md.",
                result);
        requireCode(result, "source-evolution.report-kpi-summary.missing");
        requireCode(result, "source-evolution.report-kpi-artifacts.missing");
    }

    @Test
    @ValidatesMcpContract("source.protected-framework.not-mutated")
    @McpCheckRef("source.protected-core-surfaces.not-mutated.negative")
    public void rejectsPackageWhenAnyReusableCoreSurfaceIsMutated() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Path rootDiff = packageRoot.resolve("review/root-source-thin-no-index.diff");
        String diff = String.join("\n",
                Files.readString(rootDiff),
                protectedPatch(TEST_LISTENER_PATH),
                protectedPatch(McpProjectLayout.UI_CORE_MAIN_PATH + "runtime/BaseTestUI.java"),
                protectedPatch(McpProjectLayout.API_CORE_MAIN_PATH + "BaseTestAPI.java"),
                protectedPatch(McpProjectLayout.ATF_TEST_PATH + "core/BaseTestAPIContractTest.java"),
                protectedPatch(McpProjectLayout.MCP_TEST_PATH + "tests/latest/McpRuntimeAndPackageContractTest.java"),
                protectedPatch(McpExecutionProfile.RUN.suiteXml()));
        Files.writeString(rootDiff, diff);

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProtectedFrameworkMutationRule());

        logValidatorScenario(
                "Reject generated package mutation of reusable core surfaces",
                packageRoot,
                "Injected protected ATF core, UI core, API core, ATF contract test, MCP test, and MCP suite paths into the root diff.",
                "Expected violations: framework-mutation.protected-path for every protected surface.",
                "Asserted artifacts: review/root-source-thin-no-index.diff and protected source surface catalog.",
                result);
        requireCode(result, "framework-mutation.protected-path");
        assertProtectedPathRejected(result, TEST_LISTENER_PATH);
        assertProtectedPathRejected(result, McpProjectLayout.UI_CORE_MAIN_PATH + "runtime/BaseTestUI.java");
        assertProtectedPathRejected(result, McpProjectLayout.API_CORE_MAIN_PATH + "BaseTestAPI.java");
        assertProtectedPathRejected(result, McpProjectLayout.ATF_TEST_PATH + "core/BaseTestAPIContractTest.java");
        assertProtectedPathRejected(result, McpProjectLayout.MCP_TEST_PATH + "tests/latest/McpRuntimeAndPackageContractTest.java");
        assertProtectedPathRejected(result, McpExecutionProfile.RUN.suiteXml());
    }

    @Test
    @ValidatesMcpContract("source.protected-framework.not-mutated")
    @McpCheckRef("source.protected-run-diff.not-mutated.negative")
    public void rejectsPackageWhenRunDiffMutatesProtectedCoreLayer() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Path runDiff = packageRoot.resolve("runs/run_03/review/thin-no-index.diff");
        String protectedApiPath = McpProjectLayout.API_CORE_MAIN_PATH + "validation/ResponseValidator.java";
        Files.writeString(runDiff, Files.readString(runDiff)
                + "\n" + protectedPatch(protectedApiPath));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProtectedFrameworkMutationRule());

        logValidatorScenario(
                "Reject protected path hidden in per-run diff",
                packageRoot,
                "Injected an API core path into runs/run_03/review/thin-no-index.diff.",
                "Expected violation: framework-mutation.protected-path.",
                "Asserted artifact: runs/run_03/review/thin-no-index.diff.",
                result);
        requireCode(result, "framework-mutation.protected-path");
        assertProtectedPathRejected(result, protectedApiPath);
    }

    @Test
    @ValidatesMcpContract("source.protected-framework.git-clean")
    @McpCheckRef("source.protected-git-status.not-mutated.negative")
    public void rejectsPackageWhenProtectedGitStatusIsDirtyEvenIfRootDiffIsClean() throws IOException, InterruptedException {
        Path projectRoot = Files.createTempDirectory("mcp-protected-git-status-");
        try {
            Path protectedMcpFile = projectRoot.resolve(McpProjectLayout.MCP_MAIN_PATH)
                    .resolve("rules/packageaudit/closeout/FullMcpGateRule.java");
            Files.createDirectories(protectedMcpFile.getParent());
            Files.writeString(protectedMcpFile, "package com.ai.skills.mcp.rules.packageaudit.closeout;\n");
            runGit(projectRoot, "init");
            runGit(projectRoot, "add", protectedMcpFile.toString());
            Files.writeString(protectedMcpFile,
                    Files.readString(protectedMcpFile) + "// generated package tried to patch MCP\n");

            Path packageRoot = projectRoot.resolve("docs/out/2026-06-29_16.00.00");
            Files.createDirectories(packageRoot.resolve("review"));
            Files.writeString(packageRoot.resolve("review/root-source-thin-no-index.diff"),
                    "diff --git a/src/test/java/com/ai/ui/tests/AllowedUiTest.java "
                            + "b/src/test/java/com/ai/ui/tests/AllowedUiTest.java\n");
            Path runtime = projectRoot.resolve(McpArtifactPaths.RUNTIME_PROPERTIES);
            Files.createDirectories(runtime.getParent());
            Files.writeString(runtime, ""
                    + "RUN_EXPECTED_DELTA_QUALITY=8\n"
                    + "ROUND_TARGET_TEST_COUNT=5\n"
                    + "META_ITERATION_COUNT=3\n");

            McpPackageValidationResult result = validateWithRules(projectRoot, packageRoot, new ProtectedGitWorkspaceRule());

            logValidatorScenario(
                    "Reject protected live git drift omitted from package diff",
                    packageRoot,
                    "Created a dirty MCP file in git status while keeping root-source-thin-no-index.diff business-only.",
                    "Expected violation: framework-mutation.protected-git-status.",
                    "Asserted artifact: git status --porcelain.",
                    result);
            requireCode(result, "framework-mutation.protected-git-status");
        } finally {
            deleteRecursivelyBestEffort(projectRoot);
        }
    }

    @Test
    @ValidatesMcpContract("source.protected-framework.not-mutated")
    @McpCheckRef("source.protected-rename.not-mutated.negative")
    public void rejectsPackageWhenDiffRenamesProtectedCoreFile() throws IOException {
        Path packageRoot = cloneLatestPackageSourceSpine();
        Path rootDiff = packageRoot.resolve("review/root-source-thin-no-index.diff");
        String protectedCorePath = McpProjectLayout.CORE_MAIN_PATH + "reporting/StepLogger.java";
        Files.writeString(rootDiff, Files.readString(rootDiff)
                + "\ndiff --git a/" + protectedCorePath + " b/tmp/StepLogger.java\n"
                + "similarity index 91%\n"
                + "rename from " + protectedCorePath + "\n"
                + "rename to tmp/StepLogger.java\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProtectedFrameworkMutationRule());

        logValidatorScenario(
                "Reject protected source rename",
                packageRoot,
                "Injected rename metadata for a protected ATF core file.",
                "Expected violation: framework-mutation.protected-path.",
                "Asserted artifact: review/root-source-thin-no-index.diff.",
                result);
        requireCode(result, "framework-mutation.protected-path");
        assertProtectedPathRejected(result, protectedCorePath);
    }

}
