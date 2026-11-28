# Report archives

## Overview

Preserve completed ATF outputs by their specific purpose without changing report
bytes or losing relative assets. The [collector](../../tools/collect-artifacts.mjs)
uses Node 20+ built-ins and has no Maven/runtime-library dependency. Run it after
the producing commands finish, from any checkout that contains this tool.

| Category | Observed producer/output | Interpretation |
|---|---|---|
| test-results | Module target/surefire-reports or target/failsafe-reports | Original test XML, TestNG pages and their assets |
| extent | Normal target/reports trees | Existing report pages and data; no reconstruction |
| allure | Native/module allure-results, target/allure-results or an existing allure-report | Raw results or an already generated site |
| aggregate | target/atf-core-aggregate-report | The complete existing aggregate site and assets |
| browser | Screenshot, trace, video and attachment outputs | Preserved evidence, not proof that a real browser produced every fixture |
| build | JAR/ZIP and Maven build metadata | Existing build artifacts; not an implied release |
| execution | Explicit logs/provenance and process diagnostics | Original execution context and declared source revision |
| unclassified | Other retained output | Visible evidence that needs owner review, never silently discarded |

Evidence paths use ordinal UTF-16 ordering, independent of host collation.
Path/digest pairs remain comparable across collations; this order does not make
timestamps or execution outcomes identical.

The collector visits the supplied root and immediate POM-bearing modules.

- It copies target output except declared compiled/temporary directories and generated Java/class files outside report trees.
- Source attachments inside reports remain evidence.
- Module-level allure-results and .codex-artifacts are included.
- This classification does not change producer configuration or add an E2E selection.
- Aggregate/Allure trees retain their producer category; other media is indexed as browser evidence.
- Classification describes location and format, not a verdict.

Current ATF runs publish Extent. The Allure format in the format table identifies retained or external producer data; it is not an active ATF reporting backend.

## Ownership

The caller supplies the completed validation root, a distinct archive root and a unique portable run ID.

- Output must be outside every collected input root.
- Do not collect while writers are active.
- Paths are resolved at execution time; no author drive, cache directory or fixed account path is embedded in the tool.

Complete collections and failed attempts belong in an external history root.

- The live checkout publishes selected original proof in reports/current, following the [bounded publication contract](../README.md).
- Compilation, caches, other complete report sites and temporary investigations stay outside live.
- Preserve historical bundles intact, including their relative assets and source identity.

```text
external-history/artifacts/reports/runs/<run-id>/
  collection-status.json
  manifest.json
  index.html
  catalog/<category>.json
  files/<original source-relative path>
  evidence/<input-number>/<original filename>
```

The original source-relative tree preserves cross-report links and assets.

- The index and catalogs provide views by report purpose without flattening files.
- The manifest records byte counts, SHA-256, original locations, categories and ignored build paths. collectionStatus describes copying; executionVerdict stays NOT_INFERRED.
- Read the preserved XML/logs for actual test outcomes.
- A missing producer is NOT_FOUND; the collector does not fabricate an Extent/Allure report.

Existing run IDs are rejected without overwriting.

- A copy/link/hash failure leaves collection-status.json marked FAILED, with no complete manifest.
- Preserve that partial evidence and use a new ID for a corrected collection.
- The collector does not delete source files, follow linked report trees or rewrite old evidence.

## Collection

Resolve the work root as described in [Development](../../docs/Development.md).
The inputs below refer to an existing completed run, its log and source manifest:

```powershell
$snapshotRoot = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_SOURCE -ErrorAction Stop).Path
$runLog = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_LOG -ErrorAction Stop).Path
$sourceManifest = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_MANIFEST -ErrorAction Stop).Path
$provenance = Get-Content -LiteralPath $sourceManifest -Raw | ConvertFrom-Json
$historyRoot = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_HISTORY_ROOT -ErrorAction Stop).Path
$archiveRoot = Join-Path $historyRoot 'artifacts/reports/runs'
$runId = $env:ATF_ARTIFACT_RUN_ID
node tools/collect-artifacts.mjs --source "$snapshotRoot" --output "$archiveRoot" --run-id "$runId" --revision "$($provenance.sourceCommit)" --evidence "$runLog" --evidence "$sourceManifest"
```

The revision is caller-declared and the source manifest is retained; collection alone cannot prove which source produced an old report.

- For a configured custom reports.dir, external Surefire output or other report destination, repeat --include with category=existing-path.
- Include the common parent of a custom HTML site and its assets.
- Files from explicit inputs retain their subtree below files/_included.
- Extra roots and execution-evidence files must already exist.

Open index.html for category navigation. Some producer sites need a local HTTP
server; serve the whole bundle when required by that format. The collector does
not install a server, regenerate reports or reinterpret failures as successes.

## Current selection and history

Select only the native XML/TAP, executed-source manifests and final verification receipts needed for the current summary.

- Bind them in publication.json.
- The generated reports/current/index.html and README.md state captured revisions, actual selections, verdicts and limits.
- A layout check does not execute tests.
- An older report stays historical and a failed selection stays failed.

Keep full Allure, aggregate, browser and corruption-fixture trees in external history.

- Do not duplicate every original collection in live.
- Identify history by snapshot ID and inventory digest; the caller resolves its external root through ATF_ARTIFACT_HISTORY_ROOT.
- Maintained instructions contain no fixed author-machine path.
- Original resolved paths remain provenance, not checkout requirements.

Relocate a complete history snapshot together.

- Atlas inputs may reference several report runs; moving isolated runs breaks those dependencies.
- Preserve source context and original indexes.
- A source copy without Git metadata is reading context, not a substitute for canonical Git verification.
- Retire an old current selection only after its intact external archive is verified.

## Retained UI report publication

The explicit retained-product selection may publish one original reports/current/e2e/playwright/ExtentReport.html and one original reports/current/e2e/selenium/ExtentReport.html.

- Screenshots remain embedded in their native report.
- Select three additional proof files per engine: original Surefire XML, the original executed-source manifest, and the governed audit JSON.
- Complete runs, logs, prior attempts and other report sites remain in history.

Each selected file declares uiEngine, runId and sourceRevision in publication.json.

- The Extent entry has kind extent and references nativeResultsPath, executedSourcePath and auditPath.
- References must select distinct declared test-results/execution/execution files in the same engine directory, with the same run/source identity.
- The reader also states executionScope.
- No orphan proof, second reader, generic execution HTML or cross-engine reference is valid.

The [audit checker](../../tools/verify-ui-e2e-audit.mjs) reconciles original XML subjects and outcomes with the native Extent method/step nodes, expected/actual validation evidence, screenshot metadata and source/engine identity.

- Its result is bounded audit consistency.
- A native application FAIL or SKIP stays visible; copying a report or passing a layout/audit check cannot turn it into PASS.
- An action PASS cannot substitute for the separate business validation.

The [execution guide](../../docs/Testing.md#retained-product-ui-audit) owns the
same retained methods and the two browser commands. The
[living principles](../../docs/Living_Architecture_UI_API_doc_v1_0.md#retained-principles)
own responsibilities and proof limits, including the demo-data cleanup gap.

## Verification

From the reactor root, with Node 20+ available:

```powershell
$artifactRoot = Join-Path (Get-Location).Path 'artifacts'
node --test tools/collect-artifacts.test.mjs
node --test tools/verify-ui-e2e-audit.test.mjs
node tools/collect-artifacts.mjs --check-layout "$artifactRoot"
```

These filesystem/CLI regressions cover byte/asset fidelity, real failed reports,
empty categories, overwrite refusal, invalid IDs, recursive destinations, linked
trees, mutation during copying, custom roots and space-containing paths. They
are maintenance-tool checks, not product E2E tests. The Java local suite governs
the maintained guides, source visibility and edition parity; it does not replace
this explicit tooling selection.

Publication fixtures also cover malformed inventories, duplicate projects, undeclared HTMLs, orphan files, changed/missing proof, unsafe paths, links and legacy history/build/temp folders.

- Rejected checks preserve the files and their timestamps.
- These controls add no product E2E scenario or runtime feature.
- Retained-UI controls additionally reject mismatched engines, runs, revisions and proof references while preserving original failure/skip verdicts.

<details>
<summary>Extent evidence for Atlas construction</summary>

The selection may include `reports/current/atlas-construction/ExtentReport.html`,
with `id` equal to `atlas-construction-extent` and `kind` equal to `extent`. This
derived report binds the exact reader and three original proof families: Node
components, native rendering and publisher controls. It is not a new MCP or E2E execution.

The `business-atlas-acceptance` receipt retains source, reader, report and proof
identities. The archived MCP report is calibration only; other HTML report sites
remain in the external archive. The new governance case refuses mismatched
links or verdicts and verifies repair against declared bytes.

</details>
