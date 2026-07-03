package com.ai.skills.mcp.validation.artifacts;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpBusinessEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    void assertRootDiffReferencesExistingSourceFiles() {
        audit.assertNoBlockers("Root diff source file existence",
                violation -> violation.code().startsWith("root-diff."),
                audit.details(
                        "Asserts: the frozen root diff does not reference phantom source files.",
                        "Discovery artifact: review/root-source-thin-no-index.diff.",
                        "Source truth: current project src/**, because a diff alone is only a delta, not executable code.",
                        "Expected: every src/** file declared as NEW/UPDATE or +++ b/... resolves under the project root before the package can be trusted."));
    }

    void assertSourceEvolutionLedgerIsAuditable() {
        audit.assertNoBlockers("Source evolution ledger",
                violation -> violation.code().startsWith("source-evolution."),
                audit.details(
                        "Asserts: source evolution is captured as a compact KPI ledger from baseline_before_run_0 to final_before_closeout.",
                        "Artifact: review/source-evolution-ledger.md.",
                        "Expected: baseline, run_##_delta rows, final close-out, score_delta, canonical progress KPIs, source paths and links to run diffs/state/root diff."));
    }

    void assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers() {
        audit.assertNoBlockers("Generated package protected framework mutation gate",
                violation -> violation.code().startsWith("framework-mutation."),
                audit.details(
                        "Asserts: generated ROOT_RUN diffs do not edit reusable framework/MCP/core infrastructure.",
                        "Artifacts: review/root-source-thin-no-index.diff and runs/run_##/review/thin-no-index.diff.",
                        "Protected layers: ATF core, UI core, API core, MCP main/tests/suites, ATF contract tests, prompt requirements, shared CSS.",
                        "Expected: business work may use reusable testing framework surfaces, but must not mutate them inside generated package evidence."));
    }

    void assertClaimedCommercialAbsenceHasUiAssertion() {
        audit.assertNoBlockers("Claimed absence assertion",
                violation -> violation.code().startsWith("claimed-absence."),
                audit.details(
                        "Asserts: if the package claims an absent/missing/not-visible object, that absence is asserted in evidence, not only narrated.",
                        "Artifacts: review/root-source-thin-no-index.diff plus project src/main/java and src/test/java.",
                        "Expected: explicit assertion/check for absence of the claimed object."));
    }

    void assertPretrainingIntakeQuality() {
        audit.assertNoBlockers("Pretraining intake quality",
                violation -> violation.code().startsWith("pretraining-intake."),
                audit.details(
                        "Asserts: pretraining saved a reusable intake brief, not just a prose mention.",
                        "Artifact: review/round-pretraining-brief.md.",
                        "Expected: sources read, extracted lesson, do-not-inherit boundary, chosen frontier, saved artifact."));
    }

    void assertUiWrapperHandoffQuality() {
        audit.assertNoBlockers("UI wrapper handoff quality",
                violation -> violation.code().startsWith("ui-handoff."),
                audit.details(
                        "Asserts: each run handoff carries UI/business context into execution.",
                        "Artifacts: run-state.json, business-understanding.md, SUMMARY.md.",
                        "Expected: actor, intent, visible object/anchor, local result/verdict."));
    }

    void assertClaimToEvidenceChainIsComplete() {
        audit.assertNoBlockers("Claim-to-evidence chain",
                violation -> violation.code().startsWith("claim-evidence."),
                audit.details(
                        "Asserts: every major Proof Mapping claim has cold evidence, not just narrative.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected chain: claim -> diff -> execution proof -> machine result -> state -> verdict."));
    }

    void assertLearningTraceQualityIsAuditable() {
        audit.assertNoBlockers("Run learning trace quality",
                violation -> violation.code().startsWith("learning-trace."),
                audit.details(
                        "Asserts: every run leaves a useful learning trace for the next agent.",
                        "Artifacts: SUMMARY.md, run-state.json, business-understanding.md, lesson-learned.md, frontier-decision.md.",
                        "Expected chain: claim/frontier -> decision/verdict -> evidence/proof -> lesson -> next implication."));
    }

    void assertFinalReportAvoidsUnverifiableLanguage() {
        audit.assertNoBlockers("No unverifiable language",
                violation -> violation.code().startsWith("report.unverifiable-language"),
                audit.details(
                        "Asserts: high-confidence wording is tied to evidence or a partial/capped verdict.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden without evidence: fully proven, fully owned, complete, production ready, ready to replace."));
    }
}
