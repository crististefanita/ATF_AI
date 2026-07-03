package com.endava.ai.skills.mcp.validation.run;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpRunDiffAssertions {
    private final McpPackageAuditValidation audit;

    public void assertEachRunDiffAndFeedbackAreSubstantiveEnoughToReopen() throws IOException {
            List<String> weakArtifacts = new ArrayList<>();
            audit.forEachRun(run -> {
                Path diff = audit.runRoot(run).resolve("review/thin-no-index.diff");
                Path summary = audit.runRoot(run).resolve("SUMMARY.md");
                if (Files.size(diff) <= 200) {
                    weakArtifacts.add(run + " diff is too small to reopen source evolution");
                }
                if (Files.size(summary) <= 100) {
                    weakArtifacts.add(run + " SUMMARY.md is too small to explain the run");
                }
            });
            audit.assertNoSyntheticViolations("Per-run diff and feedback reopenability", weakArtifacts,
                    audit.context.packageRoot(), "run-history.too-thin",
                    audit.details(
                            "Asserts: each run leaves enough source/history context to reopen the evolution.",
                            "Artifacts: runs/run_##/review/thin-no-index.diff and runs/run_##/SUMMARY.md.",
                            "Thresholds: diff size > 200 bytes; SUMMARY.md size > 100 bytes."));
        }

    public void assertRunDiffHistoryIsColdDistinct() {
            audit.assertNoBlockers("Per-run diff history distinctness",
                    violation -> violation.code().startsWith("run-diff."),
                    audit.details(
                            "Asserts: per-run thin-no-index.diff files are not all identical.",
                            "Artifacts: runs/run_##/review/thin-no-index.diff.",
                            "Expected: each kept run leaves cold-distinct evolution evidence, not copied package-level diff."));
        }

    public void assertRunDiffTimelineCompilesIntoRootDiff() {
            audit.assertNoBlockers("Diff progression from baseline to final",
                    violation -> violation.code().startsWith("run-diff.progression.")
                            || violation.code().startsWith("root-diff.progression."),
                    audit.details(
                            "Asserts: per-run diffs expose parseable src/** deltas and root-source-thin-no-index.diff compiles those run deltas.",
                            "Artifacts: review/root-source-thin-no-index.diff and runs/run_##/review/thin-no-index.diff.",
                            "Expected: run diff = small mutation, root diff = compiled final mutation, with distinct source sets across counted runs."));
        }

    public void assertRunIndependenceIsAuditable() {
            audit.assertNoBlockers("Run independence",
                    violation -> violation.code().startsWith("run-independence."),
                    audit.details(
                            "Asserts: runs do not reuse the same diff, state, summary, verdict, and learning trace.",
                            "Artifacts: runs/run_##/SUMMARY.md, review/run-state.json, review/business-understanding.md.",
                            "Expected: at least one cold axis is distinct enough to prove iteration novelty."));
        }
}
