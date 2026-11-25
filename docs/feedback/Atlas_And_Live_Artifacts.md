# Atlas evidence and local report publication

Baseline: `e82c9a734c03480be7f7fd14330a370b4bd3d2d3`. The owner now explicitly
selects the live ATF_AI artifacts directory for generated report copies and
Atlas analyses. Builds remain isolated below the external work root. Preserve
existing source changes in J1; generated maps remain outside its maintained
docs/lib perimeter. No E2E journey, emulator or Java runtime feature is added.

## D-004 — discoverable local test evidence

- Owner: artifacts guides, testing documentation and local archive publication.
- Priority: 85, from impact/risk/urgency 90/80/80.
- Baseline quality: 58, from correctness/verification/clarity 70/45/55.
- Observation: completed reports exist in external staging, while the requested
  live artifacts area contains only source guides. Its current wording also
  discourages any generated report copy in the live checkout.
- Acceptance: retain isolated builds, explicitly support caller-selected live
  archive copies, preserve source-relative assets and SHA-256, provide a local
  report index and current testing READMEs. Preserve failed/old evidence and
  distinguish copying from fresh execution.
- Delivered quality: 95, from correctness/verification/clarity 95/94/97.
- State: VERIFIED for selected completed archive publication.
- Evidence: the initial publication copied 19 completed bundles and 3,660 retained files into the
  selected ignored local reports area and rehashed against their original
  manifests. The generated index and testing README distinguish fresh results,
  failed attempts and retained history. Source-relative report assets remain
  together. See `artifacts/reports/runs/index.html` in the selected checkout.
  Later verification receipts have separate bundles; the generated index records
  current totals without rewriting this checkpoint's original counts.

## G-009 — verify generated Atlas against its actual target

- Owner: Atlas intake/template, maintenance verification and existing governance.
- Priority: 89, from impact/risk/urgency 95/85/80.
- Baseline quality: 55, from correctness/verification/clarity 65/35/60.
- Observation: maintained Atlas guides are governed, but generated run data is
  intentionally outside source governance. A formatted project map alone does
  not verify its source identity, test binding, quotes or live drift. J1's binary
  library perimeter must not be replaced by AI's source-module architecture.
- Acceptance: generate distinct target-bound maps, source indexes and proof
  boundaries for AI/J1; validate declared source/document/evidence hashes and
  exact source references. Existing governance results retain their actual
  target/scope. Reject changed source, cross-project identity and unsupported
  execution claims. Preserve pending J1 changes and explicit unknowns.
- Delivered quality: 93, from correctness/verification/clarity 95/92/92.
- State: VERIFIED for declared source/artifact consistency and scoped review.
- Evidence: separate AI/J1 maps bind their own POM module graphs, source roots,
  owner boundaries, exact source/governance fragments and execution evidence.
  Each includes five Markdown/HTML documents and a static module diagram.
  AI owns five source-library/test modules; J1 has five different local modules
  and consumes six versioned binary library pairs. J1 maps remain outside its
  maintained docs/lib tree; existing J1 work was preserved.
- Proof boundary: a related governance mapping is not complete coverage of every
  clause. Unmapped declarations are explicit. The source comparison records
  changes since execution; it does not certify later edits. The checker validates
  declared files, paths, quotes, hashes, XML/TAP outcomes and source bindings,
  while semantic completeness requires review.

## Validation receipt

- AI EN and RO: `modules-atlas-main-governance-02` and
  `modules-atlas-ro-governance-02`, each 211 tests, zero failures/errors/skips,
  from frozen source/peer editions. EN source: `17afeed7af4952d10fd12094a216ea2da5d01f1c`.
  The earlier attempts are retained: changing peer tooling during execution
  correctly triggered cross-edition source-parity findings. No rule was relaxed.
- J1: `j1-atlas-governance-02`, 205 tests, zero failures/errors/skips, from the
  captured dirty source based on `4e3cac10061c95ea2b9b32cc18a6d4add1007a1e`.
  Its earlier attempt retained one Git-discovery isolation failure because TMP
  was inside the cloned repository; the corrected run placed TMP outside Git.
  Later local J1 edits are recorded separately from this passing snapshot.
- Atlas maintenance: `atlas-verifier-06`, 12 tests passed. Negative controls reject
  source/document drift, altered quotes/ranges, target confusion, escaping paths,
  unsupported execution proof, inconsistent counters and changed executed bytes.
  The real mixed self-closing/body-bearing Surefire layout found during collection
  has a regression. Explicit CRLF/LF equivalence cannot normalize published lib
  artifacts or hide a content change.
  Individual flat TAP outcomes also reject a failed test whose summary counters
  were both altered to claim success. The collector's existing eight checks were
  rerun successfully as `atlas-collector-01`.
- Static documentation review rendered ten HTML documents plus both indexes in
  real desktop Chromium without device emulation. Diagrams loaded, pages had no
  horizontal overflow or script errors, and evidence disclosure opened. Visual
  inspection corrected an overlapping direct AI dependency arrow. TAB interactive
  review was unavailable after two service timeouts; no interactive acceptance
  is claimed. A later source snapshot requires revalidation of its own map.
- Retained J1 component HTML is historical: its selected checks and unqualified
  aggregate observation keep their original qualification record. Copying does
  not establish fresh API/network or browser execution. Earlier 550-check AI
  suites and browser qualification bundles are likewise historical in this task.

Atlas remains a documentation/governance protocol. Any added checker is optional
maintenance tooling, not a runtime analyzer or a universal semantic proof.
