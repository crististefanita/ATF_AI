# Code governance, readable testing strategy and complete Romanian edition

Baseline: main `3663419b34ec6adc96120b419738b77afe145e9e` and main_RO
`aebbbcfaaf32f91b87e86fdf7d7923e040202728`. J0 is inspected read only at
`96f50e5f277771ca9dfac3b7c959b47188afd0b0`; its two local feedback edits remain
untouched. This review strengthens source governance, existing local tests and
documentation. It adds no product journey, runtime feature or emulator.

<a id="g-017"></a>
## G-017: Existing regressions must remain executable and bounded

- Owner: existing suite AST validator and ConsoleSignatureContractTest.
- Priority: 84; impact/risk/urgency 90/80/75.
- Baseline quality: 76; correctness/verification/clarity 80/65/80.
- Reproducer: a maintained source annotation changed to `@Test(enabled=false)`
  remains counted as executable by the old annotation-type inventory. XML
  selection alone cannot protect that source boundary.
- Independent stability defect: one selected console contract calls unbounded
  `worker.join()`. A blocked logging worker could hang the local gate.
- Repair: inspect TestNG annotation attributes through the existing compiler AST.
  Omitted enabled and literal true are supported; indirection fails explicitly
  instead of pretending parse-only inspection resolves external constants.
- Existing nested/manual fixtures retain their reviewed scope. Comments, strings,
  unrelated annotations and locally shadowed Test types are positive controls.
- Bound the existing worker rendezvous and cleanup, propagate its original
  failure, and preserve the test sink reset in finally.
- Delivered quality: 93; correctness/verification/clarity 94/92/92.
- State: VERIFIED for the observed local selection and bounded worker contract;
  the native local-02 evidence below owns the execution claim.

<a id="d-010"></a>
## D-010: Strategy first, complete translation and source-grounded review

- Owner: documentation rules, readability component tests and edition policy.
- Priority: 88; impact/risk/urgency 90/85/85.
- Baseline quality: 68; correctness/verification/clarity 65/60/80.
- Baseline user-observed defect: main_RO/docs/Testing.md was English. The old policy
  deliberately permits translation only for READMEs; its green parity result
  therefore certifies an incomplete Romanian documentation edition.
- Executed baseline signal: the new local language control identifies that exact
  Testing path and 26 other English documents before translation. The preserved
  baseline receipt is external; it is a reproduced defect, not a failed final run.
- Testing strategy: retain selection, API proof scopes, browser identity,
  independent observations and evidence limits in Testing.md. Move the ten
  unchanged original fenced execution blocks into Testing_Commands.md, inside
  closed disclosures with purposeful summaries. Existing incoming anchors remain.
- Readability: adopt J0's decision-oriented principle, with an explicit role
  distinction. The strategy uses a 3-sentence/100-word logical paragraph bound;
  detailed operational references use 100 words, with a 45-word sentence bound.
  These are review triggers, not quality metrics, diagram quotas or line lengths.
- All operational guides receive the supported prose controls; historical issue
  prose retains its evidence shape. Runnable Maven fences remain subject to
  disclosure governance in both areas. Inline diagnostic commands stay visible.
- Translation: every maintained Markdown and the Living HTML projection receives
  a Romanian counterpart at the identical path, including historical feedback.
  Code, commands, knowledge data, identifiers, status tokens and canonical cited
  policy literals remain exact. Original generated reports retain their language.
- Deterministic checks identify missing Romanian markers, unchanged meaningful
  English blocks, structural/technical drift and stale whole-file review hashes.
  They cannot certify every translated sentence or detect every inverted meaning.
- RAG-assisted review: retain the existing deterministic policy retrieval receipt,
  add the actual edition/policy owners and read each complete EN/RO pair. Review
  conditions, negations, chronology, actors, proof limits and diagram legends.
  The shared XML binds every document to both normalized hashes and declares
  the reviewer/dimensions. Hash renewal without reading is not acceptance.
- Bounded parser limit: structural parity still requires single-line link labels
  and canonical link placement on wrapped bullets. Two translation wrapping fixes
  retain that supported form; no complete Markdown parser is claimed.
- Delivered quality: 92; correctness/verification/clarity 94/90/92.
- State: VERIFIED for the maintained document inventory, complete declared review,
  explicit paired native controls and captured desktop document scope below.

<a id="g-016"></a>
## G-016: Review removal of an existing regression method

- Owner: test-maintenance policy and the changed test's module owner.
- Priority: 75; impact/risk/urgency 80/70/70.
- Baseline quality: 69; correctness/verification/clarity 75/50/80.
- Delivered quality: 94; correctness/verification/clarity 95/94/94.
- Observation: the suite inventory protected class selection, but not the
  identity of a method removed from a still-selected class.
- J0 reference: TestChangeGovernanceTest and GitTestChanges separate the immutable
  base, exact changed path, reviewer rationale and verification proof. Preserve
  that principle without copying consumer catalogs or inventing approval prompts.
- Correction: the existing framework governance owner now compares an AST-derived,
  sorted inventory of executable TestNG identities (declaring type, method and
  parameter types) with a SHA-256-pinned baseline. `TEST-METHOD-REMOVED` and
  `TEST-METHOD-UNREGISTERED` retain actionable signatures and route to the owner.
- Acceptance: its controlled negative fixture removes one method while the
  class remains present, adds an unregistered replacement, and checks both
  findings and the repair hint. The maintained inventory matches current test
  source. `FrameworkArchitectureGovernanceTest` passed 114/114 in the focused
  reactor selection on 2026-11-26.
- Limit: the baseline detects identity drift but is not a Git change classifier,
  approval gate, proof of method-body quality or evidence that each method ran.
  Intentional changes require explicit review of the signature delta, inventory
  and digest alongside the owning regression.
- State: VERIFIED within this source-governance boundary. The original baseline
  score remains recorded separately in the feedback register.

## Comparable source-review scores

These are 1–100 source-maturity judgments on the same inspected axes. J0 was
not built or executed; no score establishes measured repeatability or coverage.
Existing diagnostics and boundaries are assessed independently of test counts.

| Axis | Weight | AI baseline | AI repaired source | J0 inspected source |
|---|---:|---:|---:|---:|
| Module boundaries and ownership | 25% | 92 | 92 | 94 |
| Executable governance coverage | 25% | 88 | 90 | 92 |
| Negative paths and independent oracles | 20% | 90 | 91 | 94 |
| Bounded waits and cleanup | 20% | 85 | 87 | 92 |
| Actionable diagnostics | 10% | 94 | 94 | 88 |
| Weighted source assessment | 100% | 89.4 | 90.5 | 92.5 |

AI improves executable selection and one bounded concurrency owner. J0 retains
stronger change-control and lifecycle aggregation; AI retains stronger emitted
observations, cause/repair distinction and exact owned recheck routes. F-011
was open at that comparison checkpoint; the current closure follows below. High
scores require stronger evidence, not a wider unsupported claim.

## Acceptance evidence

The external review root is `${ATF_WORK_ROOT}/reviews/20261125-code-and-documentation-governance`.
Native runs and source manifests use `${ATF_WORK_ROOT}/runs/` and separate disposable
reactors. The existing ignored current publication remains at its captured scope.

- Baseline: baseline-translation-language.log records the real untranslated
  Testing document before repair. Readability counterexamples preserve actual
  dense strategy prose, default-open commands and fake tags inside literals.
- Final default local-02 reactor selections: English 690/690
  and Romanian 690/690 passed, with 0 failures,
  0 errors and 0 skipped cases in each edition. Each contains 315 governance cases. All five modules executed;
  the atf-tests selections contain 678 and 678
  cases respectively. The retained original Surefire XML, completed Maven logs,
  source manifests and exit receipts own these counts, not copied report summaries.
- Executed sources: English 1b939695c7a29355bf5e67f50ccbaa0121314c9e and Romanian
  bfa1bcc341cd4540b8f5ef70457b8483695f0d6c. The completed native local-02 summary binds the
  local-02 receipts and actual testcase outcomes. 29 existing
  local API cases and the bounded console worker case passed in each edition;
  these local API observations do not establish GoRest application acceptance.
- Existing Node maintenance controls: 182/182 passed per captured edition at
  a064e9f18f3528c17e109f2119b81808c32ce2e8 and
  669c2baecd3552a71651283c9eb44021e264e3de. Each retained manifest binds 23 files:
  19 tools and 4 contract inputs, unchanged from the final native sources. This preserves the captured
  Node scope; it is not a claim of a new Node execution at the final source commit.
- Complete document review: the shared translation-review.xml binds 51 reviewed
  documents, comprising 50 maintained Markdown paths and the Living HTML pair.
  The actual lexical policy retrieval receipt retains 16 mandatory and 18 selected
  records. Complete source-grounded EN/RO reading covers language, meaning,
  conditions, facts, links, diagrams and disclosures; no semantic recall is claimed.
- Final native desktop document review combines visual02 with the bounded
  Architecture repair in visual03: 32 records, 56 SVG observations and 98 selected
  screenshots at 1440/900 widths. Captured source hashes match; geometry findings,
  page/console errors, external requests and document overflow are 0. Wide Living
  figures use contained scrolling. Rendering is document-component evidence,
  not framework/browser application acceptance or universal accessibility proof.
- The first untranslated baseline and the first complete execution remain retained
  below. No new E2E journey, emulator or runtime feature was added. G-016, G-014
  and F-011 were TODO at that checkpoint; synchronization was evidenced by branch,
  remote and live identities after the documentation-only closure check.

## Retained first complete execution

The first complete default reactor selection passes in English. Its atf-tests
module records 678 passed cases. The Romanian run records 677 passed cases and
one failed positive documentation control; no case is skipped. The failing
control searches an English HTML heading before inserting an escaped example,
so it performs no mutation against the fully translated Romanian heading.

The existing test now locates the stable retained-principles section ID, inserts
the same literal example and preserves the same projection assertion. Boolean
precondition assertions avoid dumping the complete HTML when insertion fails.
Both original native runs remain unchanged under their local-01 locators. The
accepted local-02 runs recorded in the acceptance evidence preserve that earlier
failure and establish their own separately captured source and outcomes.


<a id="f-012"></a>
## F-012: Strict UI options and a shared configured wait

- Owner: Core typed UI configuration and wait helpers.
- Priority: 87, from impact/risk/urgency 90/85/80.
- Baseline quality: 71, from correctness/verification/clarity 75/60/75.
- Observation: malformed UI boolean text silently became false; zero, negative
  or millisecond-overflowing waits were accepted. Locale-dependent engine casing
  and duplicated wait parsing weakened the configuration boundary.
- Correction: the existing ConfigManager and UiRuntimeOptions own strict boolean
  and configured-wait readers. Both session options and WaitUtils use them.
  Unknown engines fail before acquisition. Existing defaults, override precedence,
  explicit Duration overloads and supported zoom normalization remain unchanged.
- Existing oracles cover six boolean keys, malformed values, both wait keys,
  canonical precedence, lower/upper bounds and Turkish default locale. Errors
  identify the actual key and value; wait rejection precedes condition polling.
- Stable contract: [Core configuration and cleanup](../../atf-core/README.md#validated-ui-configuration-and-cleanup).
- State: VERIFIED for the owned configuration component boundary. Delivered quality: 94; correctness/verification/clarity 95/93/94. Complete paired evidence follows below.

<a id="f-013"></a>
## F-013: Roll back partial UI session acquisition

- Owner: existing Playwright and Selenium session/provider classes.
- Priority: 82, from impact/risk/urgency 85/80/75.
- Baseline quality: 65, from correctness/verification/clarity 65/50/80.
- Observation: constructor failure after resource acquisition lacked an owned
  rollback path. Nested Playwright close failures could also replace the first
  failure. This starting observation is source-backed, not a process-leak claim.
- Correction: release acquired resources in reverse ownership order, preserve
  the original RuntimeException or Error and retain distinct suppressed cleanup
  causes. Persistent Playwright contexts own browser shutdown; temporary profiles
  are removed. A Selenium factory failure has no acquired driver to quit.
- Existing provider component classes inject stage failures without launching a
  browser: Playwright has 16 cases and Selenium has 12 cases, including positive
  acquisition, cleanup order, primary identity and self-suppression controls.
- Stable contracts: [Playwright](../../atf-playwright/README.md#acquisition-failure-and-cleanup)
  and [Selenium](../../atf-selenium/README.md#acquisition-failure-and-cleanup).
- State: VERIFIED for the owned acquisition and cleanup component paths. Delivered quality: 93; correctness/verification/clarity 94/92/93. Complete paired evidence follows below.

## Current UI maintenance evidence

The external root is `${ATF_WORK_ROOT}/reviews/20261126-ui-lifecycle-config`.
The baseline source, original failing native XML and focused Maven run remain
separate. The focused selection proves only the selected owned contracts; it
does not replace the complete local reactor. No E2E class, product journey or
emulator is added. Historical acceptance sections above retain their captured
source and counts; current F-011 work supersedes its earlier open-state summary.

The first complete local-01 selection retains 703 atf-tests cases per edition,
with 702 passed and one invoice-fixture failure. That fixture configured zero;
it now uses one valid second. The three existing negative controls also assert
the exact missing-row message, so an earlier configuration/navigation failure
cannot satisfy the oracle. Invoice checks remain controlled components without
a browser or product journey.


## UI repair acceptance and current review scores

Complete default local reactors passed 740/740 in each edition, including
703 atf-tests cases, 37 upstream library cases and 315 governance cases.
The existing 29 local API cases passed in each edition. Each run has zero
failures, errors or skipped cases. This is framework/component acceptance;
GoRest application writes, product E2Es and emulators are outside this execution.

| Evidence | Result | Bound source or retained locator |
|---|---|---|
| English default reactor | 740/740 PASS | 6a90e248e09e3b553f77bd8cb5764a6f9b427614 |
| Romanian default reactor | 740/740 PASS | 10d8974f69530c370932643a8227b73a9999decc |
| Focused UI/configuration/providers | 69/69 PASS | modules-code-documentation-ui-lifecycle-focused-01 |
| Node maintenance | 203/203 PASS per edition | en-node-01 and ro-node-01 |
| Original negative baseline | 7 passed, 18 failed; expected | baseline/native/testng-results.xml |

Full source manifests, native XML, completed logs and native-summary.json remain
in the external review root. Source/POM/resources are frozen independently of
later feedback closure. Final paired documentation and governance checks bind
the closure text. Native desktop document review covers the changed Core/Living
pairs at 1440/900 CSS px with 8 records and 32 diagram observations. Contained
scrolling in wide Living figures is intentional. This is file rendering, not
browser-process or application acceptance.

The following 1–100 judgments use the same axes and weights as the prior live
comparison. Weights sum to 100; counts do not determine grades. The baseline is
the 2026-11-26 source review at 14daf233e8039049cf6d8ba2db7421fb48c4c5be.

| Axis | Weight | Baseline | Current | Evidence and remaining limit |
|---|---:|---:|---:|---|
| Architecture and ownership | 15 | 92 | 92 | Same five modules; no new runtime class or product feature |
| Maintainability | 10 | 88 | 89 | Shared wait reader; the large documentation helper still mixes concerns |
| Configuration | 10 | 82 | 93 | Strict booleans, bounded waits and locale-independent engine validation |
| Failure lifecycle | 10 | 84 | 94 | Exact primary identity, finally cleanup and acquired-resource rollback |
| Code/test governance | 15 | 91 | 94 | Existing class selection plus AST-pinned method identities; controlled removal/addition findings |
| Demonstrated test stability | 15 | 90 | 92 | Focused owner 114/114 plus paired full local selections; no new product execution |
| Documentation and editions | 15 | 94 | 94 | Maintained EN/RO contracts, diagrams and paired Living formats |
| Feedback and provenance | 10 | 93 | 95 | G-016 baseline and delivered quality remain separate; paired evidence and limits recorded |

Weighted maturity increases from 89.75 to 92.90, rounded from 90 to 93.
Neither result is a coverage percentage or a complete-runtime guarantee.

## Same-axis ATF_AI and atf-j1 review

These 1–100 judgments use the same eight axes and weights. ATF_AI reflects this
paired source review. The atf-j1 reference is the committed source at
`4e3cac10061c95ea2b9b32cc18a6d4add1007a1e`; its separate dirty workspace was
left unchanged and excluded from the scored source. J1 was not freshly executed
or regraded in this repair.

| Axis | Weight | ATF_AI | atf-j1 | Difference |
|---|---:|---:|---:|---:|
| Architecture and ownership | 15 | 92 | 94 | -2 |
| Maintainability | 10 | 89 | 90 | -1 |
| Configuration | 10 | 93 | 90 | +3 |
| Failure lifecycle | 10 | 94 | 93 | +1 |
| Code/test governance | 15 | 94 | 89 | +5 |
| Demonstrated test stability | 15 | 92 | 80 | +12 |
| Documentation and editions | 15 | 94 | 91 | +3 |
| Feedback and provenance | 10 | 95 | 87 | +8 |
| **Weighted score** | **100** | **92.90** | **89.10** | **+3.80** |

ATF_AI leads this scoped comparison in six of eight axes after closing G-016.
atf-j1 retains a two-point advantage in architecture and a one-point advantage
in maintainability. The governance and test-stability differences reflect the
captured source evidence and execution receipts, not a claim that the J1 tests
were rerun here.

G-016's method-identity loss control is now verified within its stated source
boundary. The next surgical priority is to reduce responsibility mixing in the
1293-line documentation governance helper while preserving its negative
controls and repair hints. This is a maintainability observation, not a failing
execution. No new product journey is required. G-014 remains an external
J1-owned TODO.
