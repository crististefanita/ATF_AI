# Documentation contracts and semantic translation review

Baseline: English `fd5cf47f403bd01d00dbb1a40f17d9a37496e843` and Romanian
`b62d4e9dd3690b25f7ea9de16b84867cb88c561d`. The read-only J0 reference is
`96f50e5f277771ca9dfac3b7c959b47188afd0b0`, including its two observed local
feedback changes. No J0 source, pending work or test result is changed by this
review. Scope: existing governance/component tests, maintained documentation
and the same 21 README paths; no runtime feature or product journey.

<a id="d-009"></a>
## D-009: Protect complete documentation and faithful README editions

- Owner: existing documentation rules, failure diagnostics and README edition
  policy; each factual claim keeps its implementation/POM owner.
- Priority: 85, from impact/risk/urgency 90/80/80.
- Baseline quality: 78, from correctness/verification/clarity 85/65/80.
- Baseline observation: all 47 maintained Markdown files already have typed
  fences, valid heading progression and no prohibited presentation tags, but
  the guard enforces some structural rules only for READMEs. A compliant
  document inventory does not prove the guard rejects a future regression.
- Baseline counterexamples: changing inline `ui.engine` to `unknown.engine`,
  or changing a numeric table row from release 1.0.0 / 16 cases to 9.0.0 / 99
  cases, produces no finding from the accepted pre-review parity checker.
  Shapes and links match while the technical meaning changes.
- Semantic review: all 21 paired READMEs and their 20 ordinary code blocks are
  reviewed. The existing pairs retain their technical inline payloads, paths,
  diagrams and examples. Several Romanian feedback qualifiers nevertheless
  lose meaning: scoped catalog success, dirty source state, wildcard controls,
  optional retrieval receipts and the exact source commit need precise wording.
- Other observed defects: one emitted Node repair-owner fragment does not
  resolve; the Atlas subject example omits a required source-reference index;
  some historical observations use current tense; the living teardown diagram
  omits the known F-011 failure-path qualification.
- Expected: apply supported structure/editorial checks to every maintained
  Markdown document, protect inline code and numeric table facts, retain
  real diagnostic owners/rechecks and exact negative controls, and perform
  explicit semantic review of every Romanian README. Positive fixtures must
  accept literal examples, valid hierarchy returns and equivalent code spans.
- Selective J0 adoption: typed fences, title/hierarchy, disclosure structure,
  bounded editorial markers and claim-to-source review. Retain AI's existing
  factual checks, Java 11 examples, paired living source binding and exact
  diagnostic identity. J0-specific catalogs, style budgets, collapsed legends
  and a blanket diagram direction are not copied.
- State: IN PROGRESS. Close only after the corrected source and both editions
  pass their owned checks and the final tracked trees are reconciled.

## Acceptance and proof boundaries

1. Exercise meaningful negative mutations and demonstrate actual repairs for
   the new documentation and protected-fact boundaries. Preserve the original
   baseline counterexamples and any unsuccessful verification attempt.
2. Validate all maintained Markdown and the essential Living HTML/Markdown
   pair, including source digest, actual owner links and declared limitations.
3. Review all 21 README pairs for complete meaning. Preserve paths, incoming
   English anchors, inline/fenced code, numeric table facts and diagram topology;
   translate explanatory labels and readable prose completely.
4. Resolve actual emitted hint routes and execute their maintained recheck in
   component scope. A source string containing a rule name is not an oracle.
5. Render changed diagrams and inspect their visible labels, arrows and layout.
   Structural validation does not certify visual quality or semantic parity.
6. Preserve code, test inventory and non-README identity across the editions,
   except the one declared edition property. Run checks in isolated snapshots
   under the external work root and synchronize verified commits.

The external review root is
`${ATF_WORK_ROOT}/reviews/20261125-documentation-deep-review`. Its baseline
counterexamples, per-document review scope, source snapshots, selected commands
and actual native results have distinct roles. Current generated E2E/Atlas
readers retain their captured revisions and original bytes; this document
does not relabel them as executions of later documentation changes.

G-014 and F-011 retain their independent TODOs. The LD-11 application-data cleanup
obligation remains PARTIAL. The review adds no E2E flow improvement to feedback.
