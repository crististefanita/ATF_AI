package com.endava.ai.skills.mcp.validation.artifacts;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpArtifactSpineProfile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpPackageSpineAssertions {
    private final McpPackageAuditValidation audit;

    void assertRuntimeValuesPublished() {
        audit.assertNoBlockers("Runtime values from agent-runtime.properties",
                violation -> violation.code().startsWith("runtime.")
                        && !violation.code().startsWith("runtime.reference."),
                audit.details(
                        "Asserts: generated package publishes the active runtime values without drift.",
                        "Artifact: " + audit.context.runtimeSourceRelative(),
                        "Runtime keys: RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT.",
                        audit.context.runtimeSummary()));
    }

    void assertRuntimeReferencedPromptDocumentsExist() {
        audit.assertNoBlockers("Runtime referenced prompt documents",
                violation -> violation.code().startsWith("runtime.reference."),
                audit.details(
                        "Asserts: every *_PATH reference in agent-runtime.properties points to an existing prompt/style document.",
                        "Artifact: " + audit.context.runtimeSourceRelative(),
                        "Relative paths: docs/prompts/html_ro/{mcp,atlas,shared}/*.html, docs/shared-report-reference.css, other configured prompt references."));
    }

    void assertPackageAccountingMatchesRuntimeWindow() {
        audit.assertNoBlockers("Package accounting vs runtime window",
                violation -> violation.code().startsWith("accounting."),
                audit.details(
                        "Asserts: package-state accounting matches runtime window and target values.",
                        "Artifact: review/package-state.json",
                        "Expected: runs_expected/runs_completed = META_ITERATION_COUNT; tests_expected = META_ITERATION_COUNT * ROUND_TARGET_TEST_COUNT.",
                        audit.context.runtimeSummary()));
    }

    void assertPackageLevelArtifactsExistAndAreNotEmpty() {
        audit.assertNoBlockers("Package-level artifact spine",
                audit::isPackageArtifactProblem,
                audit.details(
                        "Asserts: package-level files exist and are not empty.",
                        "Required package spine: " + String.join(", ", McpArtifactSpineProfile.packageArtifacts()) + "."));
    }

    void assertReadmeMatchesPackageStateTruth() {
        audit.assertNoBlockers("README vs package-state truth",
                violation -> violation.code().startsWith("readme.package-state."),
                audit.details(
                        "Asserts: README.md does not preserve stale partial-run language after package-state.json closes the package.",
                        "Artifacts: README.md and review/package-state.json.",
                        "Expected: README echoes current run/test accounting and does not contradict closed/partial status."));
    }

    void assertRunWindowAndRunArtifactsExist() {
        audit.assertNoBlockers("Run window and per-run artifact spine",
                violation -> violation.path() != null
                        && audit.containsPathPart(violation, "runs")
                        && (violation.code().equals("file.missing")
                        || violation.code().equals("file.empty")
                        || violation.code().startsWith("runs.")),
                audit.details(
                        "Asserts: each runtime run slot has a complete and non-empty run artifact spine.",
                        "Relative path pattern: runs/run_##/**.",
                        "Required run spine: " + String.join(", ", McpArtifactSpineProfile.runArtifacts()) + ".",
                        "Expected run count: " + audit.context.runtime().metaIterationCount()));
    }

    void assertRunStatesExposeTestsAndBusinessVerdict() {
        audit.assertNoBlockers("Run state countability and test accounting",
                violation -> violation.code().startsWith("run-state."),
                audit.details(
                        "Asserts: each run-state exposes produced tests and a cold business/countability verdict.",
                        "Relative path pattern: runs/run_##/review/run-state.json.",
                        "Expected fields: tests_produced or equivalent; status/verdict/countable/owned/partial/blocked.",
                        "Target per run from runtime: " + audit.context.runtime().roundTargetTestCount()));
    }
}
