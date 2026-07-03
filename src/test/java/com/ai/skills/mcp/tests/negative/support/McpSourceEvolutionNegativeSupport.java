package com.ai.skills.mcp.tests.negative.support;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpProjectLayout;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

public final class McpSourceEvolutionNegativeSupport {
    private static final String AGENTIC_UI_FLOW = McpProjectLayout.UI_SUPPORT_MCP_MAIN_PATH + "AgenticUiFlow.java";
    private static final String AGENTIC_UI_TEST = McpProjectLayout.UI_TESTS_PATH + "AgenticUiFlowTest.java";

    private McpSourceEvolutionNegativeSupport() {
    }

    public static void assertGeneratedKpiArtifacts(Path packageRoot) throws IOException {
        Path matrix = packageRoot.resolve(McpArtifactPaths.MCP_KPI_MATRIX);
        assertTrue(Files.isRegularFile(matrix), "Expected MCP KPI matrix artifact to be generated: " + matrix);
        assertTrue(Files.readString(matrix).contains("root_diff_files_count"),
                "Expected KPI matrix to name root_diff_files_count.");
        Path matrixHtml = packageRoot.resolve(McpArtifactPaths.MCP_KPI_MATRIX_HTML);
        assertTrue(Files.isRegularFile(matrixHtml),
                "Expected MCP KPI matrix HTML artifact to be generated: " + matrixHtml);

        String matrixText = Files.readString(matrix);
        for (String token : List.of(
                "First blocker",
                "Repair command",
                "Observed artifact",
                "Primary repair artifact",
                "Route",
                "Profile",
                "Category",
                "Why",
                "Quality meaning",
                "Agent action",
                "support_cohesion_score",
                "duplicate_action_line_ratio",
                "duplicate_assertion_line_ratio",
                "closeout_profile_purity")) {
            assertTrue(matrixText.contains(token), "Expected KPI matrix to expose token: " + token);
        }
        assertTrue(matrixText.contains("RISK") || matrixText.contains("INFO"),
                "Expected KPI matrix to distinguish diagnostic risk from pass/fail blockers.");

        assertFormulaTrace(packageRoot);
        assertRepairBoard(packageRoot);
        assertScoreCapRationale(packageRoot);
    }

    public static String protectedPatch(String path) {
        return "diff --git a/" + path + " b/" + path + "\n"
                + "--- a/" + path + "\n"
                + "+++ b/" + path + "\n"
                + "+// generated package tried to mutate protected reusable test framework surface\n";
    }

    public static void seedKnownSourceDiffs(Path packageRoot, boolean includeProtectedPath) throws IOException {
        String rootDiff = ""
                + sourcePatch(AGENTIC_UI_FLOW)
                + sourcePatch(AGENTIC_UI_TEST)
                + (includeProtectedPath ? sourcePatch(McpProjectLayout.MCP_MAIN_PATH + "rules/source/InjectedRule.java") : "");
        Files.writeString(packageRoot.resolve(McpArtifactPaths.ROOT_DIFF), rootDiff);
        Files.writeString(packageRoot.resolve(runDiff("run_01")), sourcePatch(AGENTIC_UI_FLOW));
        Files.writeString(packageRoot.resolve(runDiff("run_02")), sourcePatch(AGENTIC_UI_TEST));
        Files.writeString(packageRoot.resolve(runDiff("run_03")), sourcePatch(AGENTIC_UI_FLOW));
        Files.writeString(packageRoot.resolve(runDiff("run_04")), sourcePatch(AGENTIC_UI_TEST));
        Files.writeString(packageRoot.resolve(runDiff("run_05")), sourcePatch(AGENTIC_UI_FLOW));
    }

    public static String sourceEvolutionLedger(boolean includeNumbers,
                                        boolean includeReportKpis,
                                        boolean includeProtectedPath) {
        String protectedPath = includeProtectedPath
                ? "\n- " + McpProjectLayout.MCP_MAIN_PATH + "rules/source/InjectedRule.java"
                : "";
        String numeric = includeNumbers ? "\n"
                + "root_diff_files_count: " + (includeProtectedPath ? 3 : 2) + "\n"
                + "src_main_files: " + (includeProtectedPath ? 2 : 1) + "\n"
                + "src_test_files: 1\n"
                + "files_added: 0\n"
                + "files_modified: " + (includeProtectedPath ? 3 : 2) + "\n"
                + "files_deleted: 0\n"
                + "test_methods_added: 0\n"
                + "protected_root_dirty_count: " + (includeProtectedPath ? 1 : 0) + "\n"
                + "run_01_files_count: 1\n"
                + "run_02_files_count: 1\n"
                + "run_03_files_count: 1\n"
                + "run_04_files_count: 1\n"
                + "run_05_files_count: 1\n"
                : "";
        String reportKpis = includeReportKpis ? "\n"
                + "final report KPI summary: root_diff_files_count="
                + (includeProtectedPath ? 3 : 2)
                + ", protected_root_dirty_count=" + (includeProtectedPath ? 1 : 0) + "\n"
                : "";
        return "# Source evolution ledger\n"
                + "baseline_before_run_0\n"
                + "git status baseline clean\n"
                + "final_before_closeout\n"
                + "score_delta\n"
                + "business novelty ratio\n"
                + "diff efficiency\n"
                + "test value density\n"
                + "support reuse ratio\n"
                + "duplication burn-down\n"
                + "protected-root purity\n"
                + "artifact truth score\n"
                + "run learning yield\n"
                + "- " + McpArtifactPaths.ROOT_DIFF + "\n"
                + "- " + AGENTIC_UI_FLOW + "\n"
                + "- " + AGENTIC_UI_TEST
                + protectedPath + "\n"
                + runLedgerRows()
                + numeric
                + reportKpis;
    }

    public static void runGit(Path cwd, String... args) throws IOException, InterruptedException {
        String[] command = new String[args.length + 3];
        command[0] = "git";
        command[1] = "-C";
        command[2] = cwd.toString();
        System.arraycopy(args, 0, command, 3, args.length);
        Process process = new ProcessBuilder(command).start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("git command failed: " + String.join(" ", command));
        }
    }

    public static void deleteRecursivelyBestEffort(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                try {
                    path.toFile().setWritable(true);
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Best-effort cleanup only; Windows can keep temporary .git object files locked briefly.
                }
            }
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    public static void assertProtectedPathRejected(McpPackageValidationResult result, String path) {
        assertTrue(result.violations().stream()
                        .anyMatch(violation -> violation.code().equals("framework-mutation.protected-path")
                                && violation.message().contains(path)),
                "Expected protected path to be named in framework-mutation violation: " + path
                        + "\nViolations: " + result.violations());
    }

    private static void assertFormulaTrace(Path packageRoot) throws IOException {
        Path formulaTrace = packageRoot.resolve(McpArtifactPaths.MCP_KPI_FORMULA_TRACE);
        assertTrue(Files.isRegularFile(formulaTrace),
                "Expected MCP KPI formula trace artifact to be generated: " + formulaTrace);
        String formulaTraceText = Files.readString(formulaTrace);
        for (String token : List.of(
                "Formula trace",
                "source-evolution-closeout",
                "Agent action",
                "duplicate_action_line_ratio",
                "duplicate_assertion_line_ratio")) {
            assertTrue(formulaTraceText.contains(token), "Expected formula trace to expose token: " + token);
        }
    }

    private static void assertRepairBoard(Path packageRoot) throws IOException {
        Path repairBoard = packageRoot.resolve(McpArtifactPaths.MCP_KPI_REPAIR_BOARD);
        assertTrue(Files.isRegularFile(repairBoard),
                "Expected MCP KPI repair board artifact to be generated: " + repairBoard);
        String repairBoardText = Files.readString(repairBoard);
        for (String token : List.of("Top 3 blockers", "Top 3 INFO risks", "Agent action",
                McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName())) {
            assertTrue(repairBoardText.contains(token), "Expected repair board to expose token: " + token);
        }

        Path repairBoardHtml = packageRoot.resolve(McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML);
        assertTrue(Files.isRegularFile(repairBoardHtml),
                "Expected MCP KPI repair board HTML artifact to be generated: " + repairBoardHtml);
        String repairBoardHtmlText = Files.readString(repairBoardHtml);
        assertTrue(repairBoardHtmlText.contains("Top 3 blockers"),
                "Expected repair board HTML to summarize top blockers.");
        assertTrue(repairBoardHtmlText.contains("Agent action"),
                "Expected repair board HTML to expose agent action.");
    }

    private static void assertScoreCapRationale(Path packageRoot) throws IOException {
        Path scoreCapRationale = packageRoot.resolve(McpArtifactPaths.MCP_SCORE_CAP_RATIONALE);
        assertTrue(Files.isRegularFile(scoreCapRationale),
                "Expected MCP score cap rationale artifact to be generated: " + scoreCapRationale);
        String scoreCapText = Files.readString(scoreCapRationale);
        assertTrue(scoreCapText.contains("Recommended maximum score"),
                "Expected score cap rationale to publish a recommended maximum score.");
        assertTrue(scoreCapText.contains(McpArtifactPaths.MCP_KPI_REPAIR_BOARD),
                "Expected score cap rationale to link the repair board.");
    }

    private static String sourcePatch(String path) {
        return "diff --git a/" + path + " b/" + path + "\n"
                + "--- a/" + path + "\n"
                + "+++ b/" + path + "\n"
                + "+// source evolution evidence\n";
    }

    private static String runLedgerRows() {
        return ""
                + runLedgerRow("run_01", AGENTIC_UI_FLOW)
                + runLedgerRow("run_02", AGENTIC_UI_TEST)
                + runLedgerRow("run_03", AGENTIC_UI_FLOW)
                + runLedgerRow("run_04", AGENTIC_UI_TEST)
                + runLedgerRow("run_05", AGENTIC_UI_FLOW);
    }

    private static String runLedgerRow(String runId, String sourcePath) {
        return runId + "_delta: " + runDiff(runId) + ", "
                + McpRunArtifacts.runArtifact(runId, McpRunArtifacts.STATE) + ", "
                + sourcePath + "\n";
    }

    private static String runDiff(String runId) {
        return McpRunArtifacts.runArtifact(runId, McpRunArtifacts.DIFF);
    }
}
