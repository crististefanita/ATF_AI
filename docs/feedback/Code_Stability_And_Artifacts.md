# Code governance, stable regressions and artifact ownership

Baseline: `fb39f9cf35a815aff5dee7a237b0970b77105ebb`. This phase changes
governance, existing framework regressions, maintenance tooling and documentation.
It adds no E2E journey, emulator or Java runtime feature. The Romanian edition
uses main_RO at the owner's explicit request.

## G-007 — explicit Java import ownership

- Owner: existing architecture governance and the affected source packages.
- Priority: 80, from impact/risk/urgency 85/75/75.
- Baseline quality: 78, from correctness/verification/clarity 85/70/75.
- Observation: wildcard imports hide the concrete types and static members used
  by runtime/test classes; duplicate and module checks do not forbid them.
- Acceptance: explicit imports compile with identical implementation bodies.
  Existing governance rejects normal/static wildcard declarations, including
  whitespace and Unicode variants, while ignoring literal examples/comments.
- State: VERIFIED on 2026-11-25. All 17 import-only implementation trees are
  identical and the full local/aggregate selections compile and pass. Four
  wildcard variants and literal/comment controls are protected by existing
  architecture fixtures. No runtime behavior change was required.
- Delivered quality: 95, from correctness/verification/clarity 96/94/95.

## T-005 — deterministic worker-limit regression

- Owner: AggregateParallelCollectionContractTest in atf-tests.
- Priority: 81, from impact/risk/urgency 85/80/70.
- Baseline quality: 70, from correctness/verification/clarity 80/55/70.
- Observation: the existing concurrency check uses a 75 ms pause to make worker
  overlap likely. A delayed worker can invalidate that assumption.
- Acceptance: use an explicit bounded rendezvous, retain the concurrency ceiling,
  require actual successful results and keep cleanup in finally. Repeat the
  existing selection without adding a product/E2E test.
- State: VERIFIED on 2026-11-25. The existing method uses a bounded two-worker
  rendezvous, requires the exact ceiling, successful worker outcomes and declared
  result order. Twenty consecutive direct invocations and all accepted reactor
  selections passed.
- Delivered quality: 95, from correctness/verification/clarity 95/96/95.

## D-003 — governed artifacts and reusable Atlas intake

- Owner: artifacts documentation, maintenance tooling and existing governance.
- Priority: 85, from impact/risk/urgency 90/85/70.
- Baseline quality: 55, from correctness/verification/clarity 65/40/55.
- Observation: artifacts is globally excluded from the inventory, so maintained
  documentation there would escape links, language and edition checks. Report
  evidence is scattered across module targets and external run directories.
- Acceptance: govern the artifact READMEs/templates; exclude only declared run
  data outside owned source trees. Archive existing reports and assets without
  changing their contents, retaining classification, provenance and SHA-256.
  A collector must reject overwrites and recursive destinations, report partial
  failures and distinguish collection from execution. Atlas intake must describe
  any caller-selected project using observed sources and explicit unknowns,
  without adding a runtime analyzer or restoring retired infrastructure.
- State: VERIFIED on 2026-11-25. Maintained artifact guides/templates are governed;
  exact generated run subtrees remain excluded outside owned source. Eight real
  filesystem/CLI regressions passed; current and historical report bundles were
  archived and rehashed. The documented generic Atlas intake was applied to ATF
  with source references and explicit proof boundaries.
- Delivered quality: 94, from correctness/verification/clarity 94/95/93.
  Generated bundles remain outside tracked source.

Useful J0 practices are explicit imports, owned cleanup, source/output separation,
observable regression outcomes and one canonical documentation owner. J0 remains
a read-only reference; no J0 build or pending change is modified by this work.

## Current comparison and adopted practices

ATF_J0 was inspected again at 96f50e5f277771ca9dfac3b7c959b47188afd0b0.
Its worktree was clean when captured; no J0 build or modification was performed.
The earlier comparison retains its original revision and dirty-state evidence.
This review uses the current code/documentation best-practice guides and source
governance owners as references, adapting their responsibilities to AI's existing
Java 11/TestNG structure. J0's named-static-import guidance informs the explicit
import rule; its contract/gap/evidence distinction informs artifact ownership.

The same 1–100 rubric and weights as the previous maturity review apply. These
are engineering assessments, not coverage, uptime or a runtime benchmark.

| Axis | Weight | ATF_AI previous | ATF_AI current | ATF_J0 source review |
|---|---:|---:|---:|---:|
| Architecture and boundaries | 20 | 90 | 90 | 92 |
| Executable governance | 30 | 93 | 94 | 94 |
| Existing-test quality | 25 | 90 | 91 | 88 |
| Documentation and navigation | 15 | 93 | 94 | 89 |
| Feedback ownership and lifecycle | 10 | 93 | 93 | 85 |
| Weighted overall | 100 | **92** | **92** | **90** |

Unrounded current totals are 92.35 for AI and 90.45 for J0. The rounded AI score
remains 92 because this is a focused improvement within a broader framework
assessment. Governed imports and owned artifact sources improve enforceable
boundaries; bounded rendezvous and checked worker outcomes improve one existing
regression. New artifact guides, category navigation and Atlas intake improve
understanding. No points are awarded for file/test counts or deleting scope.
J0 retains source-review confidence only. Neither score certifies arbitrary
consumer projects, every supported browser or public application persistence.

## Delivered evidence and proof limits

| Selection | Source checkpoint | Observed result | Evidence name |
|---|---|---|---|
| First focused governance/concurrency probe | Prepared source from fb39f9cf | 200 passed, one new portability-fixture failure; preserved as unsuccessful evidence | modules-quality-probe-01 |
| Complete English local selection with explicit peer | 1055ca3 | 533 passed, including 194 governance and 24 framework API cases | modules-quality-main-local-01 |
| Complete Romanian local selection with explicit peer | 222b5ba | 533 passed, including the same governance/API scope | modules-quality-ro-local-01 |
| Complete aggregate profile with explicit peer | 1055ca3 | 533 passed; existing Extent/Allure aggregate output retained | modules-quality-main-aggregate-01 |
| Maintenance collector | Same collector source as the accepted checkpoint | Eight filesystem/CLI checks passed | quality-artifact-tooling-final.log |
| Existing worker-limit method | Same Java source as the accepted checkpoint | 20 consecutive direct invocations passed | quality-concurrency-repeat.json |
| Import-only scope | fb39f9cf compared with the accepted checkpoint | All 17 implementation trees identical | quality-import-audit.json |

The initial negative portability fixture constructed a complete drive path from
adjacent constants, so source governance correctly found it in the test itself.
The corrected fixture uses the existing explicit test-data construction pattern;
the path rule and runtime behavior were not relaxed. Original unsuccessful XML
and logs are retained. Counts overlap across selections.

Report bundles are below `${ATF_WORK_ROOT}/artifacts/reports/runs`, grouped by
source run name. The collector preserves source-relative trees and original
bytes, generates category catalogs/indexes and rechecks every copied SHA-256.
Collection is distinct from execution. The preserved historical browser bundle
modules-f008-browser-accepted keeps its original source revision and outcome;
it is not a fresh browser selection in this phase. No authenticated GoRest write
request or product acceptance is claimed.

The applied Atlas example is below
`${ATF_WORK_ROOT}/artifacts/atlas/runs/atf-ai/quality-01`, with a project map,
source index and open proof boundaries. This demonstrates the maintained intake
protocol on ATF; it does not establish an automatic universal analyzer. The
reusable contract stays at artifacts/atlas/README.md and its English template.

All 21 README pairs retain hierarchy, commands, code blocks, link destinations,
incoming anchors and Mermaid topology. Other source is identical apart from the
declared README language property. Desktop Chromium rendered 36 document/edition/
width combinations at 1440/520 px, with no page overflow and minimum measured
label height of 17 px. No device emulation was used.

Detailed manifests, source hashes, reference copies and visual metrics are below
`${ATF_WORK_ROOT}/reviews/20261124-quality-artifacts`. Final receipt-only changes
receive a fresh paired source/documentation validation using the same compiled
Java as the accepted full selection. That check is not another run of all
fixture cases. The full API/runtime and import-only implementation source remains
the accepted source. Final audit and synchronization receipts remain external.
