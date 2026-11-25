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
- Quality: 69; correctness/verification/clarity 75/50/80.
- Observation: removing one method from a still-selected class can leave the
  class inventory unchanged. This iteration does not introduce a Git-based
  test-change gate or claim method-deletion protection.
- J0 reference: TestChangeGovernanceTest and GitTestChanges separate the immutable
  base, exact changed path, reviewer rationale and verification proof. Preserve
  that principle without copying consumer catalogs or inventing approval prompts.
- Acceptance: a future bounded control must reproduce a real method deletion,
  distinguish fixtures/refactors from lost proof, cover committed and local
  changes, reject stale/foreign control entries and expose an owned repair hint.
- State: TODO. Source inspection is evidence of the gap, not an executed mutation.

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
remains open. High scores require stronger evidence, not a wider unsupported claim.

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
  and F-011 remain TODO; synchronization is evidenced separately by actual branch,
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
