package com.endava.ai.mcp.validation.run;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class McpRunDiffAssertions {
    private final McpPackageAuditValidation audit;

    public McpRunDiffAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

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

    public void assertRunIndependenceIsAuditable() {
            audit.assertNoBlockers("Run independence",
                    violation -> violation.code().startsWith("run-independence."),
                    audit.details(
                            "Asserts: runs do not reuse the same diff, state, summary, verdict, and learning trace.",
                            "Artifacts: runs/run_##/SUMMARY.md, review/run-state.json, review/business-understanding.md.",
                            "Expected: at least one cold axis is distinct enough to prove iteration novelty."));
        }
}
