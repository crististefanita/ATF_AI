# Project map template

Copy this template to a new analysis output; do not fill the maintained template
with one project's dated execution results. Keep source paths relative to the
target root and identify its revision. Use UNKNOWN where a fact was not observed.

Publish README.md, project-map.md, architecture.md, testing-and-governance.md,
open-questions.md, source-index.json and analysis.json as auditable inputs in one
new run directory. The reader deliverable is exactly one complete HTML for one
selected project: delivery/atlas.html. No other HTML may appear below delivery/.
Keep intermediate HTML, Markdown, manifests and audit/render outputs outside
that reader directory. The delivery directory contains exactly one ordinary file,
atlas.html; non-HTML sidecars and nested files are also rejected. A multi-page
index is not the final understanding report.
See the [complete report contract](../../atf-atlas/Report_Contract.md).

Bind the intake to its actual source and complete documents:

- analysis.json schemaVersion 1 pins projectId/revision, sourceIndexSha256,
  documents with hashes, claims and evidence. Every declared document must exist.
- The source index schemaVersion 1 identifies the same project/revision and records
  each captured path, SHA-256 and role. Distinguish reviewed files from state-only
  captures; the checker verifies captured paths, not unindexed semantic scope.
- Use UNVERSIONED when Git metadata is absent. Record the actual language, build
  system, missing tests and inspected scope; do not copy another project's defaults.
- The same delivery contract applies to knowledge-assisted intake.
- sourceDocuments lists complete maintained target documents needed for this scope.
  Every entry is target-relative and source-index-bound. Include the actual README,
  architecture, testing and usage/contract owners, without partial excerpts or
  navigation to separate HTML pages.

## Target and proof boundary

| Field | Observed value | Evidence |
|---|---|---|
| Project/revision and worktree state | UNKNOWN | Source identity or preserved manifest |
| Requested scope and exclusions | UNKNOWN | Caller request |
| Languages and build/package tools | UNKNOWN | Actual declarations |
| Entry documentation and public entry points | UNKNOWN | Relative source paths |
| Inspected areas and uninspected areas | UNKNOWN | source-index.json |
| Tests executed for this analysis | NOT EXECUTED unless proven | Exact selection, result and artifacts |

For each claim assign a unique uppercase ID, exact statement and status:
SOURCE_REVIEWED, EXECUTED, NOT_EXECUTED or UNKNOWN. Source references declare path,
startLine, endLine and the exact source quote. Declare governanceStatus MAPPED
with concrete governanceRefs, or NOT_MAPPED with the gap kept visible. Every
claim ID/statement must appear in project-map.md. Do not replace a reference with
a keyword count or a path copied from another project.

Execution evidence declares its projectId, artifact-relative path, SHA-256,
selection, original source identity and status.

- An EXECUTED claim references Surefire XML or flat Node TAP, observed
  tests/failures/errors/skipped counters and a sourceManifest mapping relative paths
  to hashes. sourceIdentity is the SHA-256 of that preserved manifest.
- Implementation and governance references must match executed source bytes.
- Historical HTML/reports remain historical; do not relabel them as a fresh run.
- The EXECUTED verdict is PASSED or FAILED and matches referenced testcase outcomes.
  Counter-only headers do not establish an executed selection.

Default source binding is exact. If publication changes only text line endings,
declare sourceNormalization CRLF_LF, retain each live raw sha256 plus a computed
normalizedSha256, and retain a verified normalizedManifest beside the original
execution manifest. The checker still rejects changed content. Normalization
does not apply to published lib artifacts. Record any derived provenance and
its original source-manifest digest rather than rewriting old run evidence.

## Architecture and ownership

Describe the smallest useful module/component map. Add a diagram whose nodes and
arrows are justified by actual declarations/calls, followed by a legend. Identify
state, configuration, external dependencies and resource/cleanup ownership.
Represent the map as dependencyGraphs with id, title, kind DEPENDENCY, nodes and
edges. Each node has id, label, x, y and kind MODULE, COMPONENT, SERVICE or BOUNDARY.
Each edge has from, to, label and claimId. A single supported component can have
zero edges when the graph limitation explains the absence of declared dependencies.

| Concern | Owner | Public contract | Source reference |
|---|---|---|---|
| Entry/composition | UNKNOWN | UNKNOWN | UNKNOWN |
| Core behavior and data model | UNKNOWN | UNKNOWN | UNKNOWN |
| External adapters and failures | UNKNOWN | UNKNOWN | UNKNOWN |
| Configuration and outputs | UNKNOWN | UNKNOWN | UNKNOWN |

## Behavior, verification and proof limits

Explain the selected project's existing operations and observed state changes,
not just its package or dependency topology. For a framework, identify the
consumer, request, value/artifact, lifecycle or failure contract and observed
outcome. For an application, use its actual actor, object and supported operation.

| behaviorFlows field | Required meaning |
|---|---|
| id and title | Stable local identity and readable existing behavior |
| actor, intent, object and operation | Actual consumer/actor, purpose, subject and action |
| outcome | Observed or source-supported result, with its proof scope |
| testOracle and oracleStatus | Actual existing oracle, or searched scope and NOT_FOUND gap |
| oracleClaimId | Separate SOURCE_REVIEWED/EXECUTED test claim, or UNKNOWN claim for NOT_FOUND |
| claimIds | Existing reviewed/executed source claims supporting this explanation |
| limits | Unexecuted areas, partial mapping and interpretation boundaries |
| graph | BEHAVIOR kind, nodes with id/label/x/y/kind, edges with from/to/label/claimId |

Every edge and flow claim resolves to a SOURCE_REVIEWED or EXECUTED analysis claim.
An absent test oracle does not create an executable claim. A supported single
behavior state may have no edge when graph.limits states why a transition was
not established. Do not invent a flow or request new product/E2E tests to fill it.

| Behavior or invariant | Implementation | Existing check or review | Proof scope and gap |
|---|---|---|---|
| UNKNOWN | UNKNOWN | UNKNOWN | Keep source inspection separate from actual execution |

Record exact commands only after resolving them against the target's actual
configuration. Link retained outcomes, including failures and unexecuted areas.
Do not derive quality from file counts, assertion keywords or a copied report.

## Declared breadth record

The reader bundle uses renderVersion 5 for a new public capture. Add
analysis.understanding with schemaVersion 1, scope PROJECT or SELECTED,
scopeStatement, inventoryClaimIds and units. PROJECT aligns declared review-unit
and architecture inventories. SELECTED explains omitted owners. Neither scope
means complete semantic coverage. Inventory claims cite the actual declarations.

| Unit field | Required meaning |
|---|---|
| id, label and kind | Actual local owner; MODULE, COMPONENT, SERVICE or BOUNDARY |
| scopePaths | Normalized target-relative source file/directory scopes present in the index |
| claimIds | Reviewed claims with source references inside this owner |
| sharedClaimIds | Explicit reviewed references to another source owner |
| flowIds | Existing behavior flow IDs that link this unit back through unitIds |
| facets | All seven reading facets with statuses, claimIds and explanations |
| limits | What this unit review does not establish |

| Facet | Status | Claim IDs | Explanation and remaining limit |
|---|---|---|---|
| purpose | UNKNOWN | None observed | Responsibility, callers and scope |
| structure | UNKNOWN | None observed | Entries, boundaries and dependencies |
| behavior | UNKNOWN | None observed | Existing operation, object and outcome |
| data | UNKNOWN | None observed | Values, state, schemas and outputs |
| configuration | UNKNOWN | None observed | Actual defaults, properties and selections |
| failure | UNKNOWN | None observed | Errors, limits, interruption and cleanup |
| verification | UNKNOWN | None observed | Existing assertions, review and executed proof |

Replace UNKNOWN only after inspection. REVIEWED and PARTIAL need supported
claimIds; NOT_APPLICABLE needs a reason and cannot dismiss every facet. Reviewed
behavior links an existing flow. Dependency nodes declare unitId. Behavior flows
declare unitIds with matching reverse flowIds. Graph kinds remain BEHAVIOR and
DEPENDENCY; a behavior edge may cite only its own flow's claimIds.

For an EXECUTED behavior/oracle claim, add executionSubjects:

```json
{
  "evidenceId": "actual-retained-run",
  "testClass": "actual.retained.ClassName",
  "testName": "actual retained case name",
  "sourcePath": "relative/existing-oracle.ext",
  "sourceRefIndices": [0]
}
```

Bind exact subjects and selected source fragments:

- Use the retained XML classname/name; an absent classname is the empty string.
  Flat Node TAP uses exact testName and empty/omitted testClass.
- sourcePath must already be quoted in the claim. The example selects zero-based
  entry 0 in its sourceRefs, not a source line or testcase position; replace it
  with the actual matching fragments.
- Every EXECUTED case-oracle subject declares these indices, and retained subjects
  cover every selected oracleRefIndices entry. A file hash or suite total is insufficient.
- Bind every evidence ID. Missing, ambiguous, duplicate, skipped or TODO subjects
  do not establish execution; preserve failures and original provenance.
- Review assertion semantics separately. Testcase identity does not prove every prose clause.

## Behavior case witnesses

Each flow declares caseAnalysis schemaVersion 1, entryNodeId, pathScope,
negativeScope and cases. Every behavior edge has a unique id. All behavior nodes
are reachable from entry and can reach an OUTCOME, each with a case witness.
Dependency topology has a separate purpose and may have disconnected modules.

| Case field | Source-reviewed content |
|---|---|
| id, kind and title | Unique existing POSITIVE/NEGATIVE case and purpose |
| precondition, trigger and expected | Actual setup, operation/input and observation |
| nodeIds and edgeIds | Contiguous directed walk from entry to an OUTCOME |
| claimIds | Reviewed flow-local source claims supporting this case |
| oracleClaimId and oracleStatus | Exact existing local oracle or UNKNOWN/NOT_FOUND gap |
| oracleRefIndices | Selected sourceRefs indices in that oracle claim |
| assertion and limits | Actual comparison/rejection and its proof boundaries |

NOT_FOUND declares oracleGap with searchedClaimIds and explanation. Record actual
search evidence rather than inventing a test or run. negativeScope records
status REVIEWED, PARTIAL, UNKNOWN or NOT_APPLICABLE, nonempty reviewed claimIds
for the inspected scope and explanation. REVIEWED requires a negative witness;
NOT_APPLICABLE cannot contradict one. Existing
negative cases need source and assertion witnesses; gaps remain explicit.

REPRESENTATIVE describes selected case paths. ALL_DECLARED_EDGES requires them
to cover the declared edge set; it does not assert every implementation branch
or loop schedule. Repeated nodes are valid in a finite cycle walk. Positive and
negative cases may share a terminal when their source-supported observations
differ. Do not classify a case from an assertion keyword or relabel source review
as execution.

The entry may be any actual behavior node kind; actor/intent are separate flow
fields, so do not invent an intent node for an actual state/action entry.
Supported behavior self-loops need a finite case witness, an outcome escape and
a clear rendered corridor. Dependency self-loops remain invalid. A bounded
single-OUTCOME entry remains valid with an explicit searched oracle gap.

Selected oracle source ranges must not overlap any implementation range in the
same file; changing a range by one line does not establish independence.
Nonoverlapping source fragments in the same file remain valid. For EXECUTED case
oracles, each executionSubjects entry declares nonempty unique sourceRefIndices
into that oracle's sourceRefs, all matching its sourcePath. Actual subjects must
cover every case-selected oracleRefIndices entry as well as satisfying the
retained XML/TAP identity/outcome gate.

A REVIEWED failure facet needs an owned flow with a NEGATIVE witness and
negativeScope REVIEWED or PARTIAL. A negative source claim alone is insufficient.

## Optional evidence-bound assessment

If requested, add analysis.assessment with method EVIDENCE_BOUND_REVIEW, reviewer,
scope, limits and groups. Without an assessment, retain NOT_ASSESSED.

- Each group declares id, label, scope, limits and axes. Keep project-quality and
  atlas-quality groups separate.
- Every axis declares id, label, correctness, verification, clarity, score,
  weight, claimIds, evidenceIds, reason and limits.
- Numeric fields are integers 1–100. score is the rounded 40/30/30 weighted review
  of correctness/verification/clarity. Axis weights sum to 100 per group, whose
  score is the rounded weighted mean.
- Cite reviewed claims and retained evidence IDs. An honest source-only review
  may have empty evidenceIds.

Do not confuse these quality axes with feedback urgency, test pass percentages,
code coverage or automatic semantic completeness. State limitations and explain
every judgment. Do not copy another project's score.

## Complete reader artifact and knowledge

Retrieve cited policy for the requested scope and retain the corpus digest,
returned record IDs and every mandatory requirement. Retrieval supplies context
to a source-grounded reviewer; it is not evidence that a project behavior ran.
Unknown facts stay explicit. The complete HTML includes all required sections,
canonical embedded CSS, inline SVG, exact source/governance fragments, complete
declared Markdown and original XML/TAP/source-manifest payloads. Internal
navigation and native disclosure must work when atlas.html is copied alone.
No score is inferred; an unassessed area remains NOT_ASSESSED.

Retain the entire canonical retrieval receipt and full knowledge-source bytes,
including optional records, ranking, citations and provenance. Checking only
mandatory records or a corpus hash does not authenticate an edited receipt.

## Maintenance and open questions

| Question or reproducible gap | Evidence | Owner | Next verification |
|---|---|---|---|
| UNKNOWN | UNKNOWN | UNKNOWN | The smallest check needed to resolve the fact |

Keep stable usage at its source owner. Keep changing findings in feedback and
generated analysis/evidence in the selected artifact root. Declare confidence and
remaining uncertainty without inventing an overall score or an E2E expansion.

## Business reader review

- What value does the project provide, and which reviewed behavior supports it?
- Which owner units and positive/negative cases are represented in analysis.reader?
- Which construction controls remain audit-only, and why?
- Are unknowns, missing checks and contrary evidence visible as boundaries?
- Do section numbers 01–06 and child titles match navigation and anchors?
- Are horizontal diagrams, adjacent legends and tables readable at 1440/900 CSS px?
- Are complete technical inputs retained in atlas-audit.json without hidden HTML inclusion?

## Artifact calibration

The reader explains business purpose, project architecture and behavior. Architecture views preserve actual relationships using reader capability names; behavior maps stay in closed disclosures with adjacent legends.

- Compare the actual artifact with live implementation owners and project documents.
- Follow delegation to the operation; do not broaden detail masking into a guarantee for all screenshots or messages.
- Keep the observed verdict separate from the expected scenario outcome and expose partial review depth.
- Exercise navigation from closed sections, native screenshots, readability and the identity of selected bytes.

The [contract](../../atf-atlas/Report_Contract.md) defines the schema and limits; the [calibration component](../../tools/atlas-calibration.mjs) explains failures and repair routes. Axis ratings remain reasoned review judgments, not test pass rates.

Semantic qualification uses distinct facet observations, owned source and independent oracles, with visible remaining limits. Atlas identity stays compact; review date belongs with confidence. See the [semantic contract](../../atf-atlas/Report_Contract.md#semantic-review-and-compact-identity).

For observation refinement, retain the named oracle, actual proof level and visible non-claims. Distinguish a component verdict from an observed response status, and captured bytes from complete application acceptance.
