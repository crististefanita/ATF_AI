# Actionable governance diagnostics and exact negative controls

Baseline: `3edac5a1d502284469a45ef1ce3fbff0ba496a2e`. Scope: existing test
utilities, governance fixtures and documentation. No runtime feature, new Java
file, E2E journey or emulator is requested. The main_RO edition retains the same
code and non-README documents.

## G-008 — constructive governance failures

- Owner: GovernanceFiles and the existing architecture/documentation/source guards.
- Priority: 83, from impact/risk/urgency 90/75/75.
- Baseline quality: 62, from correctness/verification/clarity 75/45/60.
- Observation: the shared formatter only joins raw strings. A failing check does
  not consistently identify why the rule matters, its owning contract, a useful
  correction and a bounded recheck selection. Raw findings must remain intact.
- Acceptance: existing failures carry observed evidence, failure meaning, repair
  hint, canonical owner and maintained recheck command. Preserve input order,
  duplicate findings, file/suite scope and original line numbers; do not invent
  locations or infer a root cause for an unknown rule. Document the correction
  workflow and protect the formatting with positive and negative controls.
- State: VERIFIED on 2026-11-25. Owned diagnostics preserve original evidence,
  order, duplicates and producer scope. Real import/link/language fixtures reject
  bad input and accept a repair; all known owner routes and maintained recheck
  classes resolve. Unknown findings retain explicit uncertainty.
- Delivered quality: 95, from correctness/verification/clarity 95/95/95.

## T-006 — exact rule identity in negative governance fixtures

- Owner: the existing assertFinding helpers and their shared finding matcher.
- Priority: 86, from impact/risk/urgency 90/85/75.
- Baseline quality: 68, from correctness/verification/clarity 75/50/75.
- Observation: substring matching can mistake DOC-EDITION-PARITY for DOC-EDITION,
  or accept a code mentioned only in the observed payload. A negative fixture
  could pass for the wrong rule even though the intended guard disappeared.
- Acceptance: match the actual emitted rule token and retain requested location
  or detail fragments. Reject prefix collisions, payload mentions and formatter
  repair text as evidence. Deliberate failing assertions must expose the expected
  rule and the actual finding; repaired fixture input must pass its own guard.
- State: VERIFIED on 2026-11-25. Both existing assertion helpers match the emitted
  rule token and requested evidence fragment. Independent prefix/payload/location
  controls reject the wrong finding, including an actual failing assertion.
  Existing negative fixtures also require owned guidance for emitted findings.
- Delivered quality: 95, from correctness/verification/clarity 95/96/95.

## Reference scope

ATF_J0 at `96f50e5f277771ca9dfac3b7c959b47188afd0b0` separates finding,
rule and fix in DocumentationGovernanceSupport. Its pending reporting-feedback
changes are unrelated and remain untouched. J0 is inspected, not executed.
The immutable main_mcp archive at `70a7279c2a9560d39e6bd2554cd2f96a88626692`
demonstrates failure meaning/repair hints and explicit negative root-cause
controls. Adopt those diagnostic principles in current test utilities, without
copying its YAML catalogs, skill packages, runtime, report generation or numeric
inventory thresholds. Reference files and hashes remain in external evidence.

## Focused observations

The first prepared-source probe rejected the new import example because its
Java fixture omitted a named package. The actual finding was PACKAGE, so the
exact IMPORT-WILDCARD assertion correctly failed. Correct the fixture package
and its path; keep the package rule and import rule intact. The corrected probe
passed 17 direct invocations, including real import/link/language repairs and
the canonical diagnostic-owner routes. These are not TestNG reactor totals.
Both original focused logs and source manifests are retained under the external
work root as modules-diagnostics-focused-01 and modules-diagnostics-focused-02.
Both complete edition selections subsequently passed; their scope is recorded below.

The first complete selections each executed 550 cases: 549 passed and one
existing architecture fixture failed because its expectation was only the phrase
static ownership inventory. The new assertion contract correctly rejected that
unclassified expectation. Bind the fixture to DEPENDENCY-COORDINATE and require
the actual unresolved artifact in the same observed finding. Preserve both
unsuccessful complete runs; no runtime rule or expected module graph changes.

## Current comparison and ratings

Use the same engineering rubric and weights as the previous comparison. The
scores are review assessments, not coverage, uptime or a runtime benchmark.
J0 remains source-review only at the captured revision; its pending feedback
changes were preserved and its reactor was not run.

| Axis | Weight | ATF_AI previous | ATF_AI current | ATF_J0 source review |
|---|---:|---:|---:|---:|
| Architecture and boundaries | 20 | 90 | 90 | 92 |
| Executable governance | 30 | 94 | 95 | 94 |
| Existing-test quality | 25 | 91 | 92 | 88 |
| Documentation and navigation | 15 | 94 | 95 | 89 |
| Feedback ownership and lifecycle | 10 | 93 | 93 | 85 |
| Weighted overall | 100 | **92** | **93** | **90** |

Unrounded totals are 92.35, 93.05 and 90.45. Owned diagnostics and resolvable
guidance improve executable governance; exact negative controls improve the
existing test oracle; the new failure guide and diagram improve navigation and
understanding. Module/test counts receive no points. The small score difference
does not prove runtime superiority over an unexecuted reference project.

## Executed evidence and boundaries

| Selection | Source checkpoint | Outcome | Evidence name |
|---|---|---|---|
| First focused direct-method probe | Prepared source from 3edac5a | Failed: new import fixture had no named package; expected IMPORT-WILDCARD was correctly rejected | modules-diagnostics-focused-01 |
| Corrected focused probe | Prepared source from the same baseline | 17 direct invocations passed; not a TestNG suite total | modules-diagnostics-focused-02 |
| Complete English local selection with explicit peer | 5324975 | 550 passed; 211 governance and 24 framework API cases included | modules-diagnostics-main-local-02 |
| Complete Romanian local selection with explicit peer | dd3f5b7 | 550 passed; same governance/API scope | modules-diagnostics-ro-local-02 |

Both reactors finished with zero failures, errors or skips. Counts overlap across
editions. Runtime Java, POMs, library-native tests, public E2E sources and the
essential HTML/Markdown architecture pair are unchanged. No Java file, E2E
journey, emulator, Maven module or restored historical runtime was introduced.
Browser/aggregate profiles were not rerun for these test-support/documentation
changes; no new browser or external application acceptance is claimed.

The accepted source class files remain unchanged by the final feedback receipt.
A fresh paired source/documentation check validates those receipt-only edits,
including compiler-checked consumer examples and full edition identity; it is
not another execution of every fixture case. All 21 README paths, hierarchy,
commands, code blocks, links, incoming anchors and topology remain aligned.
Only README prose and the declared edition property differ between branches.

Desktop Chromium rendered 12 document/edition/width records at 1440/520 px,
without emulation or page overflow; the minimum measured label height was 24 px. Root README architecture
and existing Playwright/Selenium examples remain unchanged. The first preview
attempt timed out while loading the old rendered template; that unsuccessful
log remains preserved. The accepted preview uses pinned local resources and an
empty initial page, with external requests blocked.

Review references, original focused failures, source hashes, translated README
reviews and diagram evidence belong under
${ATF_WORK_ROOT}/reviews/20261125-diagnostics. Exact source snapshots,
commands and logs belong under the external validation/runs directories. Current
report bundles and final verification receipts are archived by purpose under
${ATF_WORK_ROOT}/artifacts/reports/runs. The original failed probe is preserved;
collection is not an execution verdict. No authenticated GoRest write is claimed.
