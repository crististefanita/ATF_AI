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
- State: IN PROGRESS; closure requires native local outcomes below.

<a id="d-010"></a>
## D-010: Strategy first, complete translation and source-grounded review

- Owner: documentation rules, readability component tests and edition policy.
- Priority: 88; impact/risk/urgency 90/85/85.
- Baseline quality: 68; correctness/verification/clarity 65/60/80.
- User-observed defect: main_RO/docs/Testing.md is English. The old policy
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
- State: IN PROGRESS; closure requires complete semantic review, explicit paired
  native governance and inspection of changed diagrams/projections.

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
- Final execution and complete review are recorded here after validation; an
  IN PROGRESS item is not closed by a prepared command or source-only inspection.
