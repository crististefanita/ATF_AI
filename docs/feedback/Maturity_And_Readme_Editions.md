# Framework maturity and README editions

## Comparison scope and rating method

ATF_AI baseline: `79fec10bc516ac1b32d19fe8afd99a4496382bd1`.
ATF_J0 reference: `287c7ae8c8906f669ea278c68d51e5f507a44ae7`, including its
inspected local documentation/governance changes. J0 was read only; its pending
changes were preserved, and its reactor was not executed. The reference state
and dirty-file inventory are preserved in the external review evidence.

Scores are 1–100 engineering assessments of comparable framework concerns.
They are not coverage percentages, functional equivalence or measured uptime.
The same weights apply to both projects; module/test counts do not earn points.
Weighted total is rounded to the nearest integer, with halves upward.

| Axis | Weight | ATF_AI baseline | ATF_AI reviewed changes | ATF_J0 source review |
|---|---:|---:|---:|---:|
| Architecture and boundaries | 20 | 90 | 90 | 92 |
| Executable governance | 30 | 91 | 93 | 94 |
| Existing-test quality | 25 | 89 | 90 | 88 |
| Documentation and navigation | 15 | 90 | 93 | 89 |
| Feedback ownership and lifecycle | 10 | 80 | 93 | 85 |
| Weighted overall | 100 | **89** | **92** | **90** |

Unrounded totals are 89.05, 91.65 and 90.45. The delivery column assesses the
reviewed source changes; actual execution evidence is recorded below. J0's
score has source-review confidence only, so a small score difference should
not be interpreted as a runtime benchmark or proof of superior reliability.

## Findings and selective adoption

| Concern | Observed comparison | Delivered boundary |
|---|---|---|
| Architecture | AI has five Maven modules and independent browser providers; J0 has broader library/consumer separation | Keep the existing AI module graph and vendor-neutral Core; no new runtime feature or module |
| README structure | J0 checks semantic Markdown, disclosure blocks and nearby diagram explanations more deeply | Strengthen the existing documentation guard and its deliberate failing fixtures |
| Language editions | J0 checks edition configuration and source/translation structure | main remains English; main_RO translates every README at the same path, preserving examples, links, anchors and topology |
| Existing-test quality | Both projects benefit from independently observed failure paths rather than assertion counts | Existing governance fixtures now reject missing files, changed commands/links/edges, missing aliases and non-README source drift |
| Feedback lifecycle | AI retained old product-flow improvement and withdrawn acceptance records alongside framework work | Remove those entries and references from the current feedback area; preserve their original history outside it |

Inspected J0 owners include DocumentationReadmeStructureGovernanceTest,
DocumentationDiagramGovernanceTest, DocumentationLifecycleGovernanceTest,
DocumentationLanguagePolicy, documentation-editions.mjs,
ConsumerSuiteCoverageGovernanceTest and its documentation/code best-practice
guides. Their useful principles were adapted to AI's existing TestNG/Java 11
contracts. J0-specific module catalogs, numeric style budgets and Java 21 runtime
implementation were not copied.

## G-006 — README governance and Romanian edition

- Owner: atf-tests documentation governance and docs/governance.
- Baseline priority: 84, from impact/risk/urgency 90/80/75.
- Baseline quality: 78, from correctness/verification/clarity 85/65/80.
- Reproducers: an incomplete details block or unexplained diagram could pass;
  no declared Romanian README edition or cross-edition identity check existed.
- Expected: malformed or unsupported edition settings fail closed. Only Romanian
  READMEs receive the language exception; source/comments and other documents
  keep their English policy. Both editions retain exactly the same README paths,
  structure, executable examples, link destinations, incoming anchors and code.
- Status: VERIFIED on 2026-11-24. Both materialized editions passed their complete
  local suite with the explicit peer check active: 526 cases each, including
  187 governance and 24 framework API cases, with zero failures/errors/skips.
- Delivered quality: 94, from correctness/verification/clarity 94/94/94. The
  baseline priority and quality above remain historical assessments of this item.
- All 18 README paths, executable examples, incoming English anchors, link
  destinations and Mermaid topology match. Non-README source is identical apart
  from the single declared edition setting. No new Java class, production/POM
  change, E2E test or emulator was introduced.

The durable process belongs to [README editions](../governance/Readme_Editions.md)
and the [code/documentation policy](../governance/Code_And_Documentation_Policy.md).
The checks establish supported structure and identity, not automatic semantic
translation quality or every possible Markdown/Mermaid grammar. Human review
continues to own meaning and diagram readability. Source-based guards do not
replace runtime regression outcomes.

## Evidence ownership

| Selection | Source checkpoint | Result | External run name |
|---|---|---|---|
| Initial documentation/scanner subset | Prepared source from the recorded baseline | 102 passed | modules-maturity-governance-01 |
| First complete English and Romanian selections | c2463f3 / ba314ee | Each had one new parity failure caused by generated Allure output; retained as unsuccessful evidence | modules-maturity-main-local-01 / modules-maturity-ro-local-01 |
| Accepted complete English selection with peer | c65db3e | 526 passed; 187 governance and 24 API cases included | modules-maturity-main-local-02 |
| Accepted complete Romanian selection with peer | 059d6dc | 526 passed; 187 governance and 24 API cases included | modules-maturity-ro-local-02 |

The initial standalone pair check also exposed CRLF/LF differences in gitignore
and SPI descriptors. Normalization now applies to those maintained text files;
a changed provider declaration still fails. The complete-suite failure exposed
generated Allure output outside src. The corrected shared inventory excludes
known output directories while retaining identically named owned resources.
Existing architecture and edition fixtures prove both sides of that boundary.

Actual Mermaid review used desktop Chromium at 1440 and 520 px, without device
emulation: 28 document/edition/viewport records, nine diagrams per edition,
zero page overflow and a minimum measured label height of 17 px. The first
preview's small secondary diagrams were corrected to vertical layouts in both
editions; the approved root topology and executable examples remain intact.
The accepted previews and metrics are in diagram-review-02 below the review root;
diagram-review-01 preserves the original observation.

The external review root is `${ATF_WORK_ROOT}/reviews/20261124-maturity`.
Fresh snapshots, exact resolved commands, source manifests and logs belong under
the selected external work root's validation/runs directories. The earlier
API receipt keeps its original 24-case scope; no GoRest write execution is
claimed without a supplied token. Final evidence must retain failed launches
and distinguish fixtures from real application execution.

The final source/receipt check uses the existing complete governance selection
in each edition with its peer explicitly selected. The exact commands, final
commits and per-module XML totals are preserved in
`${ATF_WORK_ROOT}/runs/maturity-final-evidence.json`; counts overlap across runs.
