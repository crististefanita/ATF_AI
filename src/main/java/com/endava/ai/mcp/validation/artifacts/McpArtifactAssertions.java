package com.endava.ai.mcp.validation.artifacts;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpArtifactSpineProfile;

public final class McpArtifactAssertions {
    private final McpPackageAuditValidation audit;

    public McpArtifactAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertCompletePackageHasNoBlockers() {
        audit.assertNoBlockers("Complete MCP generated package audit",
                violation -> true,
                audit.details(
                        "Asserts: no BLOCKER violations across the full generated ROOT_RUN package.",
                        "Artifacts: package spine, run spine, runtime constants, final report, local links, Extent/XML proof.",
                        "Relative paths: docs/out/<timestamp>/**; " + audit.context.runtimeSourceRelative(),
                        audit.context.runtimeSummary()));
    }

    public void assertRuntimeValuesPublished() {
        audit.assertNoBlockers("Runtime values from agent-runtime.properties",
                violation -> violation.code().startsWith("runtime.")
                        && !violation.code().startsWith("runtime.reference."),
                audit.details(
                        "Asserts: generated package publishes the active runtime values without drift.",
                        "Artifact: " + audit.context.runtimeSourceRelative(),
                        "Runtime keys: RUN_EXPECTED_DELTA_QUALITY, ROUND_TARGET_TEST_COUNT, META_ITERATION_COUNT.",
                        audit.context.runtimeSummary()));
    }

    public void assertRuntimeReferencedPromptDocumentsExist() {
        audit.assertNoBlockers("Runtime referenced prompt documents",
                violation -> violation.code().startsWith("runtime.reference."),
                audit.details(
                        "Asserts: every *_PATH reference in agent-runtime.properties points to an existing prompt/style document.",
                        "Artifact: " + audit.context.runtimeSourceRelative(),
                        "Relative paths: docs/prompts/iterativ/*.html, docs/shared-report-reference.css, other configured prompt references."));
    }

    public void assertPackageAccountingMatchesRuntimeWindow() {
        audit.assertNoBlockers("Package accounting vs runtime window",
                violation -> violation.code().startsWith("accounting."),
                audit.details(
                        "Asserts: package-state accounting matches runtime window and target values.",
                        "Artifact: review/package-state.json",
                        "Expected: runs_expected/runs_completed = META_ITERATION_COUNT; tests_expected = META_ITERATION_COUNT * ROUND_TARGET_TEST_COUNT.",
                        audit.context.runtimeSummary()));
    }

    public void assertPackageLevelArtifactsExistAndAreNotEmpty() {
        audit.assertNoBlockers("Package-level artifact spine",
                audit::isPackageArtifactProblem,
                audit.details(
                        "Asserts: package-level files exist and are not empty.",
                        "Required package spine: " + String.join(", ", McpArtifactSpineProfile.packageArtifacts()) + "."));
    }

    public void assertReadmeMatchesPackageStateTruth() {
        audit.assertNoBlockers("README vs package-state truth",
                violation -> violation.code().startsWith("readme.package-state."),
                audit.details(
                        "Asserts: README.md does not preserve stale partial-run language after package-state.json closes the package.",
                        "Artifacts: README.md and review/package-state.json.",
                        "Expected: README echoes current run/test accounting and does not contradict closed/partial status."));
    }

    public void assertRootDiffReferencesExistingSourceFiles() {
        audit.assertNoBlockers("Root diff source file existence",
                violation -> violation.code().startsWith("root-diff."),
                audit.details(
                        "Asserts: the frozen root diff does not reference phantom source files.",
                        "Discovery artifact: review/root-source-thin-no-index.diff.",
                        "Source truth: current project src/**, because a diff alone is only a delta, not executable code.",
                        "Expected: every src/** file declared as NEW/UPDATE or +++ b/... resolves under the project root before the package can be trusted."));
    }

    public void assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers() {
        audit.assertNoBlockers("Generated package protected framework mutation gate",
                violation -> violation.code().startsWith("framework-mutation."),
                audit.details(
                        "Asserts: generated ROOT_RUN diffs do not edit framework/MCP/core/service infrastructure.",
                        "Artifacts: review/root-source-thin-no-index.diff and runs/run_##/review/thin-no-index.diff.",
                        "Protected layers: mcp, core, UI engine/factory/reporting/service/utils, API client/core/factory/service/steps/utils, prompt requirements, shared CSS.",
                        "Expected: business work changes tests and allowed business-facing layers, not the validator or framework contract itself."));
    }

    public void assertClaimedCommercialAbsenceHasUiAssertion() {
        audit.assertNoBlockers("Claimed absence assertion",
                violation -> violation.code().startsWith("claimed-absence."),
                audit.details(
                        "Asserts: if the package claims an absent/missing/not-visible object, that absence is asserted in evidence, not only narrated.",
                        "Artifacts: review/root-source-thin-no-index.diff plus project src/main/java and src/test/java.",
                        "Expected: explicit assertion/check for absence of the claimed object."));
    }

    public void assertPretrainingIntakeQuality() {
        audit.assertNoBlockers("Pretraining intake quality",
                violation -> violation.code().startsWith("pretraining-intake."),
                audit.details(
                        "Asserts: pretraining saved a reusable intake brief, not just a prose mention.",
                        "Artifact: review/round-pretraining-brief.md.",
                        "Expected: sources read, extracted lesson, do-not-inherit boundary, chosen frontier, saved artifact."));
    }

    public void assertUiWrapperHandoffQuality() {
        audit.assertNoBlockers("UI wrapper handoff quality",
                violation -> violation.code().startsWith("ui-handoff."),
                audit.details(
                        "Asserts: each run handoff carries UI/business context into execution.",
                        "Artifacts: run-state.json, business-understanding.md, SUMMARY.md.",
                        "Expected: actor, intent, visible object/anchor, local result/verdict."));
    }

    public void assertClaimToEvidenceChainIsComplete() {
        audit.assertNoBlockers("Claim-to-evidence chain",
                violation -> violation.code().startsWith("claim-evidence."),
                audit.details(
                        "Asserts: every major Proof Mapping claim has cold evidence, not just narrative.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected chain: claim -> diff -> execution proof -> machine result -> state -> verdict."));
    }

    public void assertReportStateArtifactsTellSameTruth() {
        audit.assertNoBlockers("Report-state sync",
                violation -> violation.code().startsWith("report-state."),
                audit.details(
                        "Asserts: README, package-state, close-out, and final report publish the same package truth.",
                        "Artifacts: README.md, review/package-state.json, review/package-close-out.md, final HTML.",
                        "Expected: Overall, run count, test count, status/cap do not drift across canonical files."));
    }

    public void assertDecisionTraceIsAuditable() {
        audit.assertNoBlockers("Decision trace",
                violation -> violation.code().startsWith("decision-trace."),
                audit.details(
                        "Asserts: stop/continue/fresh-root decisions are backed by gate/state/close-out artifacts.",
                        "Artifacts: execution-gate-card.md, package-close-out.md, next-run-eligibility-card.md, final Close-Out.",
                        "Expected: no procedural decision is left as free prose."));
    }

    public void assertArtifactFreshnessIsCoherent() {
        audit.assertNoBlockers("Artifact freshness",
                violation -> violation.code().startsWith("artifact.freshness."),
                audit.details(
                        "Asserts: render/link/state artifacts are refreshed after final report edits.",
                        "Artifacts: canonical-render-check.png, local-link-check.txt, package-state.json, final HTML.",
                        "Expected: supporting artifacts are not stale against the report they validate."));
    }

    public void assertFullMcpGateUsesCompleteSuite() {
        audit.assertNoBlockers("Frozen MCP gate uses complete suite",
                violation -> violation.code().startsWith("mcp.gate-result."),
                audit.details(
                        "Asserts: saved MCP gate represents the full MCP contract, not a selective green subset.",
                        "Artifact: validation/mcp/mcp-gate-result.md.",
                        "Expected command marker: -Dtest=Mcp*Test or equivalent full MCP suite wording.",
                        "Forbidden: PASS claim from only McpLatestPackage*Test or a narrow subset."));
    }

    public void assertPublishedArtifactsHaveNoTemplateNoise() {
        audit.assertNoBlockers("Published artifacts contain no template noise",
                violation -> violation.code().startsWith("artifact.template-noise"),
                audit.details(
                        "Asserts: public package artifacts do not leak shell/template leftovers.",
                        "Artifacts: README, final report, review/*.md/json/diff, run review files, MCP gate.",
                        "Forbidden tokens: TODO, FIXME, lorem ipsum, System.Collections, ${...}, $(...), $name, {{...}}, `n, undefined."));
    }

    public void assertLearningTraceQualityIsAuditable() {
        audit.assertNoBlockers("Run learning trace quality",
                violation -> violation.code().startsWith("learning-trace."),
                audit.details(
                        "Asserts: every run leaves a useful learning trace for the next agent.",
                        "Artifacts: SUMMARY.md, run-state.json, business-understanding.md, lesson-learned.md, frontier-decision.md.",
                        "Expected chain: claim/frontier -> decision/verdict -> evidence/proof -> lesson -> next implication."));
    }

    public void assertFinalReportAvoidsUnverifiableLanguage() {
        audit.assertNoBlockers("No unverifiable language",
                violation -> violation.code().startsWith("report.unverifiable-language"),
                audit.details(
                        "Asserts: high-confidence wording is tied to evidence or a partial/capped verdict.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden without evidence: fully proven, fully owned, complete, production ready, ready to replace."));
    }

    public void assertRunWindowAndRunArtifactsExist() {
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

    public void assertRunStatesExposeTestsAndBusinessVerdict() {
        audit.assertNoBlockers("Run state countability and test accounting",
                violation -> violation.code().startsWith("run-state."),
                audit.details(
                        "Asserts: each run-state exposes produced tests and a cold business/countability verdict.",
                        "Relative path pattern: runs/run_##/review/run-state.json.",
                        "Expected fields: tests_produced or equivalent; status/verdict/countable/owned/partial/blocked.",
                        "Target per run from runtime: " + audit.context.runtime().roundTargetTestCount()));
    }

}
