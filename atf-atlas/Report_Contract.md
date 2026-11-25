# Complete Atlas report contract

Canonical retrieval excerpts appear as inline technical literals. Keep their
exact English text in every documentation edition; translate the surrounding
explanations while preserving the cited requirement and its proof limits.

## Purpose and ownership

ATLAS-REPORT-001 applies to ordinary and knowledge-assisted Atlas authoring.
`Each invocation publishes exactly one complete understanding HTML for exactly one selected project.`
`The selected project may use any language or build system, or have no tests or Git metadata.`
Its reader delivery directory is `delivery/` under the new analysis run.

- Its only HTML is `delivery/atlas.html`, counting all descendants.
- Combining separate target identities, such as AI and J1, or adding another HTML
  in that directory violates the delivery contract.
- Select a monorepo and its scope explicitly when that monorepo is the target;
  do not import facts from an unrelated project.

The delivery directory contains exactly one ordinary file: atlas.html. This is a
single-file contract, so non-HTML sidecars, nested files and symbolic links are
also rejected. Source, analysis, execution and render-review inputs stay outside
delivery/ in their declared analysis or artifact location.

The document must remain readable offline when copied alone. The canonical CSS is
[atlas.css](assets/atlas.css); the publisher embeds its canonical UTF-8 LF bytes,
normalizing CRLF once without changing content. Markdown,
source indexes and execution artifacts remain auditable inputs, not required
navigation dependencies for the reader and stay outside `delivery/`. The Atlas documentation owner maintains
this contract; the selected project's owners remain authoritative for behavior.

The archived Atlas distinguished a behavior atlas from a class inventory. For a
framework, subjects are requests, observations, contracts and lifecycle outcomes.
For a consumer, subjects are domain objects and observed application operations.
A Maven dependency arrow cannot stand in for a behavior transition. Existing
source and test oracles are the entrance; this work adds no product/E2E scenario.

## Required complete sections

| Section ID | Required information |
|---|---|
| context | Target, revision, dirty state, scope, proof status and review limits |
| behavior | Existing behavior graph, actor, intent, object, outcome and oracle |
| evidence | Every claim, status, mapped check and execution reference |
| trace | Exact source and governance fragments, paths and line ranges |
| architecture | Declared module boundaries and dependency graph, separate from behavior |
| audit | Actual outcomes, counters, commands and executed-source identities |
| knowledge | Retrieved requirements, mandatory policy, provenance and knowledge digest |
| limits | Unknowns, contrary evidence, findings and ownership |
| documents | Complete declared Markdown input documents, without truncation |
| provenance | Full source index, analysis contract and original XML/TAP/provenance payloads |

ATLAS-REPORT-002: the final HTML contains every section above and the complete
declared analysis inputs. A navigation index that opens several HTML pages is
not the final understanding artifact. Intermediate documents and prior reports
remain audit history outside the reader delivery directory.

Sections use native details/summary disclosure and internal fragment navigation.
The overview remains open; readers can expand other sections without JavaScript.
Diagrams are inline SVG generated from declared nodes and edges. No stylesheet,
font, image, script, iframe, fetch, import or navigation sidecar is needed. Raw
external source paths are displayed as text with their identity, not invented
links. `The final document is English; code and comments remain English.`

## Source, evidence and behavior

The [intake](../artifacts/atlas/README.md) and exact source/quote/evidence checks
still apply. The complete report embeds the captured state and explicitly
distinguishes source review from execution. Related governance is not complete
coverage of each clause. Unsupported checks remain NOT_MAPPED. A failed retained
`execution stays FAILED even if consistency verification passes.` Later edits
require a new capture; the report records its original source identity.

The analysis contract retains schemaVersion 1 and supplies behaviorFlows and
dependencyGraphs.

| Declaration | Required fields and meaning |
|---|---|
| Behavior | id, title, actor, intent, object, operation, outcome, testOracle, oracleStatus, oracleClaimId, limits, claimIds and graph |
| Observed oracle | oracleStatus is SOURCE_REVIEWED or EXECUTED; oracleClaimId identifies a separate claim from an actual existing test |
| Missing oracle | oracleStatus is NOT_FOUND with an explicit UNKNOWN oracle claim; testOracle states the searched scope and gap without inventing a test/run |
| Behavior graph | BEHAVIOR kind; nodes with id/label/x/y/kind and edges with from/to/label/claimId |
| Dependency graph | id, title, DEPENDENCY kind, the same declared node/edge fields; node kinds MODULE, COMPONENT, SERVICE or BOUNDARY |

Layout coordinates are presentation data, not execution observations.

Each edge and flow claim resolves to a SOURCE_REVIEWED or EXECUTED analysis claim;
source and oracle claims include exact fragments. The reviewer verifies semantic
correspondence. Tooling checks declared
`identity and structure; it cannot infer that prose is true from matching words.`

- A single-component dependency map may contain one node and zero edges when
  its limitation explains the absence of declared dependencies.
- A behavior graph may contain one supported state and no transition when
  graph.limits explains the inspected scope.
- Report disclosed gaps instead of inventing flow steps. Missing tests, Git
  metadata or build declarations do not authorize copying another project's defaults.

An optional sourceDocuments list selects complete maintained documents by
target-relative path. Each selected document is bound to the source index and
embedded without truncation. Include the actual entry README, architecture,
testing and relevant contract/usage owners when they belong to the requested
scope. For ATF_AI, the essential living architecture can be included from its
maintained source. Do not substitute another project's documentation or treat
an unreviewed document as proof of execution.

`No score is inferred from passing checks.` If no rubric and assessment are
supplied, display NOT_ASSESSED. Feedback scores retain their own declared scope.

## Knowledge and archived provenance

The [knowledge base](Knowledge_Base.md) adapts useful archived Atlas requirements
into cited, testable records. Main remains free of retired runtime registries and
services. Canonical archive identity is `70a7279c2a9560d39e6bd2554cd2f96a88626692`.
The old shared CSS blob is
`d9a1ec153b4f9b7231fc25c37494e01ded8089788c775450728f843ae405cb1c`.
The source asset preserves its visual rules and adds reviewed document layout.
The old external CSS link becomes canonical inline CSS to satisfy this single-file
delivery contract. The old language default becomes canonical English.

## Verification and constructive failure

The optional [publisher](../tools/atlas-report.mjs) uses Node built-ins. It verifies
source evidence before rendering, escapes authored content and embeds the full
machine bundle. A deterministic re-render detects changed output, missing
sections, external resources and CSS drift. The delivery guard rejects an extra
HTML anywhere below delivery/, and the embedded identity must match the one
selected target. The renderer does not execute product
tests or a language model. The dedicated tests include deliberately corrupted
source, evidence, knowledge, graph and presentation cases.

```powershell
node --test tools/verify-atlas.test.mjs tools/atlas-report.test.mjs tools/atlas-knowledge.test.mjs tools/atlas-bundle.test.mjs tools/atlas-execution.test.mjs tools/atlas-understanding.test.mjs tools/atlas-behavior.test.mjs
```

After the reviewer prepares a source-bound analysis, publish from the reactor
root using the selected source, analysis and artifact roots:

```powershell
$targetRoot = (Resolve-Path -LiteralPath $env:ATLAS_SOURCE_ROOT -ErrorAction Stop).Path
$analysisRoot = (Resolve-Path -LiteralPath $env:ATLAS_ANALYSIS_ROOT -ErrorAction Stop).Path
$artifactRoot = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_ROOT -ErrorAction Stop).Path
node tools/atlas-report.mjs publish --source "$targetRoot" --analysis "$analysisRoot" --artifacts "$artifactRoot"
```

Use a new run directory for a new capture. Publishing is write-once; repairing
the reader output means regenerating from reviewed inputs, not overwriting an
immutable historical report. The output is `delivery/atlas.html` for that target.
The understand command is an alias for publish with the same input and output
contract. It consumes the source-reviewed intake, retrieves mandatory knowledge
and emits the complete document; it does not infer a project model by invoking a
language model. The reviewer prepares the intake against the selected source.

Failures preserve a stable rule ID, observed evidence, meaning, repair hint,
canonical owner and recheck command. Unknown failures remain unclassified.
Hints are investigations, not evidence that a proposed cause is established.
Use an isolated reactor copy for existing Java documentation governance, following
[Development](../docs/Development.md). Render old and new reports in real desktop
Chromium, inspect diagrams, disclosure, overflow, navigation and offline resources.
Preserve failing observations and successful reruns in the selected artifact area.

## Declared understanding breadth

ATLAS-REPORT-003: captures using renderVersion 2 or later retain analysis
schemaVersion 1. They require analysis.understanding with schemaVersion 1. Older
renderVersion 1 artifacts remain historical records of their original contract;
preserve their pinned renderer and original bytes rather than silently upgrading
their meaning or overwriting their delivery.

`A full source inventory is not a declaration of complete semantic understanding.`
The understanding object declares scope PROJECT or SELECTED, a nonempty
scopeStatement, inventoryClaimIds and units. Inventory claims resolve to actual
reviewed build, package, entry or ownership declarations. PROJECT means that the
declared architecture and review-unit inventories agree; it never means that
every indexed line has been understood or tested. SELECTED states the inspected
area and excluded areas plainly. A small selected flow is valid when its scope
is clear. Do not require invented components or a minimum flow count for a simple
project.

Each unit declares id, label, kind MODULE/COMPONENT/SERVICE/BOUNDARY, scopePaths,
claimIds, sharedClaimIds, flowIds, facets and limits. scopePaths are normalized,
target-relative files or directories containing actual indexed source. Owned
claimIds need source references inside that unit. A sharedClaimId is an explicit
reviewed cross-owner reference; it does not transfer source ownership.

Each understanding unit declares all seven facets: purpose, structure, behavior, data, configuration, failure and verification.

| Facet | Reader question |
|---|---|
| purpose | What responsibility does this owner have, and who uses it? |
| structure | Which entry points, boundaries and dependencies implement it? |
| behavior | Which existing operation changes or observes which object? |
| data | Which values, schemas, state and artifacts pass through it? |
| configuration | Which actual defaults, properties and selections control it? |
| failure | Which errors, limits, retries, interrupts and cleanup are supported? |
| verification | Which existing assertions or source reviews support the claim? |

Every facet declares status REVIEWED, PARTIAL, UNKNOWN or NOT_APPLICABLE,
claimIds and an explanation. REVIEWED and PARTIAL require supported claims from
the unit's owned/shared set, including at least one owned claim for each facet
except verification, which may use explicitly shared oracle claims. An empty
owned set cannot promote another unit's evidence to reviewed understanding.
A REVIEWED behavior facet requires an existing
traced flow. Explain partial inspection, an absent observation or inapplicability
without manufacturing a test or dismissing all facets as inapplicable.

Dependency nodes declare unitId. Behavior flows declare unitIds, and each unit's
flowIds declares the reverse association. These links must agree in both
directions. A PROJECT capture represents every declared unit in its architecture
graphs. Graph kinds are exactly BEHAVIOR and DEPENDENCY at their corresponding
owners. Every behavior edge claim belongs to that flow's claimIds; merely finding
another reviewed claim in the report is insufficient. Reviewers still check
whether the exact source supports the stated transition.

## Exact execution subjects

`An executed behavior or oracle claim requires executionSubjects bound to retained testcase identities and outcomes.`

Each execution subject declares evidenceId, testClass, testName and sourcePath.

- evidenceId belongs to the claim's evidenceIds, and sourcePath is already quoted
  by that claim. Every referenced execution evidence ID has at least one subject.
- XML subjects match exact classname/name; missing classname means testClass is
  the empty string. Flat Node TAP matches exact testName with empty/omitted testClass.
- The case occurs exactly once with executed PASSED or FAILED outcome. Duplicate,
  missing, ambiguous, skipped and TODO subjects fail.
- Decode predefined/numeric XML entities locally; prohibit external entity
  resolution and actual XML document type/entity declarations.
- Only actual testcase children on a valid suite path establish XML identities.
  Instructions, comments, CDATA and output-container text cannot invent testcases
  or failures; malformed structure/entities/characters retain the evidence diagnostic.
- An observed HTML doctype inside inert CDATA/output remains valid payload.

The selected subjects' original outcomes determine the claim verdict. A suite
failure outside the selected cases cannot substitute for an oracle failure,
and a passing summary cannot erase a selected failed case. Existing whole-run
counter, target and source-manifest checks still apply. A selected PASS in a
mixed suite stays PASSED while that evidence's whole-suite status stays FAILED.
A source file present
during a passing run does not establish that its oracle executed.

An executed maintenance-cohort claim may retain its explicit selection scope
without behavioral subjects when no behavior references it. Source-reviewed
behavior and NOT_FOUND oracle gaps require no invented run. The subject binding
proves retained identity and outcome; source-to-assertion semantics still need a
human review. This is maintenance governance, with no new product/E2E scenario.

For a renderVersion 3 EXECUTED case oracle, sourceRefIndices additionally selects
the exact zero-based entries in that oracle claim's sourceRefs. Each selected
entry must match its subject's sourcePath, and the retained subjects together
must cover every case-selected oracleRefIndices entry. Indices are not line
numbers; the selected references already own their exact startLine, endLine and
quote. See the [copyable subject shape](../artifacts/atlas/Project_Map_Template.md#declared-breadth-record).

## Evidence-bound review axes

`An assessment is a scoped authored review, not a test pass rate or an automatic score.`

analysis.assessment is optional. Without it, the report remains NOT_ASSESSED.
When supplied, method is EVIDENCE_BOUND_REVIEW and reviewer, scope, limits and
groups are explicit. Every group declares a unique id, label, scope, limits and
axes. Keep project quality and Atlas quality in separate groups: a strong
publisher cannot certify the selected project's runtime or its unexecuted tests.

Each axis declares a unique id, label, correctness, verification, clarity, score,
weight, claimIds, evidenceIds, reason and limits.

- Numeric inputs, score and weight are integers from 1 to 100.
- score is the rounded sum of 0.40 × correctness, 0.30 × verification and
  0.30 × clarity.
- Axis weights sum to 100 within each group. Its declared score must match the
  rounded weighted mean.
- Each axis cites existing reviewed claims. Execution references resolve to
  retained evidence and keep its actual outcome and age.
- evidenceIds may be empty when no execution supports the scoped judgment.

Suitable project axes include contract correctness, existing-test quality,
governance, maintenance/ownership, documentation and configuration portability.
Suitable Atlas axes include source breadth, behavior/oracle trace, evidence
integrity, knowledge trace, reader clarity and declared limitations. Explain the
reason for each score and its uncertainty. Scores do not imply statistical
confidence, code coverage or automatic semantic completeness. Feedback priority
and quality remain separate judgments under their own published rubric.

## Complete retrieval receipt

Knowledge-assisted authoring preserves the complete canonical retrieval receipt,
including query parameters, selected IDs, mandatory IDs, optional records,
ranking, citations, corpus identity and source provenance. Recomputing only a
corpus digest or checking mandatory records cannot validate an edited optional
requirement. Retain the full canonical knowledge-source bytes in the complete
bundle. Retrieved policy is authoring context; it cannot establish a fact or an
executed outcome for another target. New negative controls must retain useful
repair hints and prove the intended rule before a feedback item is closed.

## Behavior cases and negative observations

ATLAS-REPORT-004: new public captures use renderVersion 3. Each behavior flow
declares caseAnalysis schemaVersion 1. Earlier renderVersion 1/2 captures retain
their original bytes, pinned renderer and proof scope; they are not certified
retroactively under this case contract.

`Every declared behavior outcome has a source-bound case witness reachable from its entry.`

- caseAnalysis declares entryNodeId, pathScope, negativeScope and cases. Behavior
  edges have stable unique id values plus from, to, label and claimId.
- entryNodeId selects any valid INTENT, ACTION, DECISION, STATE or OUTCOME node.
  Actor and intent belong to the flow; do not invent an intent node when the
  observed operation starts at an action or state.
- A bounded single OUTCOME is valid with an explicit oracle gap. Every behavior
  node is reachable from entry and can reach an OUTCOME.
- Unreachable observations, disconnected operations and trapped cycles fail.
  Dependency graphs remain separate; disconnected modules need no invented common entry.

pathScope is REPRESENTATIVE or ALL_DECLARED_EDGES. REPRESENTATIVE declares
selected existing cases and remaining limits. ALL_DECLARED_EDGES requires their
paths to cover the declared graph edge set. It does not establish every branch
in the implementation, every input combination or every loop schedule. Finite
case walks can revisit nodes, including supported behavior self-loops with a
finite witness and an outcome escape. Such loops have a visible diagram corridor;
dependency self-loops remain invalid. Traversal is bounded by the graph; the validator
does not enumerate all possible schedules.

| Case field | Required meaning |
|---|---|
| id, kind and title | Unique local POSITIVE/NEGATIVE case and its purpose |
| precondition, trigger and expected | Actual starting condition, input/operation and observed result |
| nodeIds and edgeIds | Ordered contiguous directed walk from entry to an OUTCOME |
| claimIds | Reviewed flow-local source claims supporting this case |
| oracleClaimId and oracleStatus | Exact existing local oracle and actual proof scope, or explicit NOT_FOUND |
| oracleRefIndices | Selected sourceRefs indices within that oracle claim |
| assertion | What the selected existing assertion compares or rejects |
| limits | Partial review, unexecuted scope and unsupported conclusions |

Every edgeIds item connects its adjacent nodeIds in the declared direction.
Every OUTCOME has a case witness. Positive and negative cases may share a
terminal state when source and assertions justify their different observations.
Case kind and assertion meaning are reviewed declarations, never inferred from
filenames, labels or assertion keywords.

Reviewed/executed oracle claims are local to the flow. Apply each source and
execution boundary explicitly:

- Source fragments: oracleRefIndices selects exact oracle paths, line ranges and
  quotes. A fragment cannot overlap any selected implementation range in the same
  file, even with smaller or extended slices. Nonoverlapping same-file fragments
  remain valid; an implementation-only alias cannot replace an independent test.
- Content identity: live and portable gates use the verified source index to
  identify equal files across paths. Case variants, hardlinks and identical
  copies cannot turn overlapping ranges into an independent oracle. CRLF/LF
  identity requires the declared policy; published library artifacts stay byte-exact.
- Path scope: paths are never globally lowercased, so case-distinct files with
  different content retain separate identities. Metadata-only analysis reports
  DECLARED_PATHS_ONLY; live/portable publication supplies verified content identities.
- Missing oracle: NOT_FOUND references an UNKNOWN oracle claim and oracleGap with
  searchedClaimIds/explanation. Preserve reviewed search evidence without inventing
  a test or run. Source-reviewed cases retain their actual scope.
- Executed oracle: the retained execution-subject contract also applies. Every
  executionSubjects entry has nonempty unique sourceRefIndices into that oracle's
  sourceRefs with paths matching sourcePath. Actual retained subjects cover every
  case-selected oracleRefIndices entry; independent XML/TAP identity/outcome
  checks still apply alongside source indices.

`Negative observations are explicit reviewed cases or an evidence-backed search gap, never inferred from a happy-path diagram.`

- negativeScope records REVIEWED, PARTIAL, UNKNOWN or NOT_APPLICABLE, nonempty
  reviewed flow-local claimIds and an explanation.
- REVIEWED needs a negative case witness; NOT_APPLICABLE cannot coexist with one.
  Distinguish reviewed cases, searched absence and unreviewed areas.
- A negative claim elsewhere cannot certify a positive-only graph. Explain actual
  rejection, validation, timeout, cleanup or boundary behavior already in source.
- Do not invent application cases for a numerical minimum. The reviewer still
  checks the source meaning of every condition, transition and assertion.

A unit's REVIEWED failure facet needs at least one NEGATIVE case in an owned
flow whose negativeScope is REVIEWED or PARTIAL. A reviewed negative source
fragment without an explicit owned behavior witness cannot certify that facet.

## Algorithm and component proof scope

Behavior governance tests check graph invariants and declared evidence relationships, not automatic prose truth.
Meaningful negative controls reverse or remove a valid outcome path, add an
orphan outcome, substitute a noncontiguous case edge, select an unrelated oracle
fragment or drop declared negative evidence. Each fails for its intended rule
and retains an owned repair route. Consistent identifier renaming, node/edge
reordering and valid layout changes preserve the logical result. A supported
cycle with an outcome escape remains valid; a trapped cycle fails.

Publisher component tests assemble source/index/claim inputs and verify the
complete one-file result or intended refusal. They complement focused traversal
tests, metamorphic mutations and existing Java documentation/architecture
guards. They are maintenance tests, not new product/E2E scenarios. No emulator
is used. Receipt authenticity proves policy provenance; mandatory fallback for
an unrelated query is honest policy context and does not become semantic
project retrieval. Source review supplies the project explanation.
