# Framework feedback and TODOs

Track reproducible gaps in the UI/API runtime, reporting, configuration and
documentation. Stable usage belongs in the owning README. Builds produce logs
and reports in external staging; selected verified copies can be published in
the caller-selected local archive following the [report guide](../../artifacts/reports/README.md).

## Active TODOs

No accepted TODOs remain open within the current maintenance scope.

Current review: [live source, documentation, Atlas/RAG and framework attributes](Code_And_Documentation_Maturity.md#current-assessment).

| ID | Finding | Owner | Priority (1-100) | Quality (1-100) | Status | Closure evidence |
|---|---|---|---:|---:|---|---|

<!-- feedback:empty -->



The register is reserved for framework governance, stability and documentation.
Product-flow improvement records are excluded from this backlog.

Historical verification rows describe their captured revisions. Current reporting is Extent-only; see [R-002](Code_And_Documentation_Maturity.md#r-002).

<details>
<summary>Retained scope decisions and external observations</summary>

- [G-014](Atlas_Depth_And_Assessment.md#g-014) preserves a captured J1 failure. It is an external observation, with no current J1 verdict or ATF_AI implementation obligation.
- [F-011](Behavior_And_Negative_Cases.md#f-011) is verified; older checkpoints do not reopen its completed component correction.
- [LD-11](../Living_Architecture_UI_API_doc_v1_0.md#retained-principles) still discloses absent application-data deletion. It remains a PARTIAL proof boundary, outside new E2E implementation scope.
- [R-002](Code_And_Documentation_Maturity.md#r-002) retires Allure integration. Historical receipts and generic archive classification retain their original identities.

</details>

## Current maintenance scope

Prioritize maturity, stability, readable architecture, English documentation with diagrams, governance and the quality of existing checks.

- Keep the current E2E inventory; do not add application journeys during this phase.
- Use desktop browsers for retained browser checks and do not use emulators.
- Assessment scores must follow evidence and the rubric below.
- Removing a requirement from scope is not a quality improvement or a test success.

## Assessment rubric and evidence locations

The two scores answer different questions.

- Priority increases with the urgency of remediation; quality increases with the strength of the affected area.
- Both are integers from 1 to 100.
- They are evidence-backed review judgments, not test coverage percentages or probabilities.

- Priority = round(0.50 × impact + 0.30 × exposure/risk + 0.20 × urgency).
- Quality = round(0.40 × correctness + 0.30 × verification + 0.30 × clarity).

Each input uses the same 1–100 scale.

- Use 1–39 for weak/low, 40–69 for partial/ moderate, 70–89 for strong/high, and 90–100 for thoroughly evidenced/critical, according to the relevant axis.
- Round to the nearest integer, with halves rounded upward.
- State the scope, observations and component scores in the linked item.
- A closed item retains its baseline assessment there; historical verified records do not receive invented retrospective scores.

Review scores when evidence or scope changes, not simply when test counts grow.
The existing documentation governance validates the two distinct columns,
integer range and actionable owner/status/evidence fields.

Retained baseline component scores:

<details>
<summary>Historical baseline scores and original gaps</summary>

| ID | Impact / risk / urgency | Correctness / verification / clarity | Evidence scope |
|---|---|---|---|
| D-018 | 90 / 85 / 80 | 90 / 90 / 93 | Ten partial facets need a preserved baseline and additional evidence |
| D-019 | 88 / 85 / 82 | 85 / 78 / 85 | Generic relationship substitution and unmeasured graph geometry |
| D-011 | 90 / 85 / 85 | 65 / 45 / 60 | Generic HTML column widths, literal source blocks and no measured readability gate |
| G-017 | 90 / 80 / 75 | 80 / 65 / 80 | Source-disabled selected tests and one unbounded console worker |
| D-010 | 90 / 85 / 85 | 65 / 60 / 80 | English non-README documentation remains in the declared Romanian edition |
| G-016 | 80 / 70 / 70 | 75 / 50 / 80 | A method removal can preserve class selection; source-only observation |
| G-006 | 90 / 80 / 75 | 85 / 65 / 80 | Missing structural and edition checks in the reviewed baseline |
| F-008 | 90 / 70 / 75 | 70 / 70 / 75 | Real local network probes pass; SDK/browser/typed-CDP selection was misaligned |
| G-005 | 85 / 80 / 75 | 70 / 40 / 55 | Runtime roots are resolved dynamically, but authored instructions contain machine-specific paths |
| D-001 | 65 / 40 / 50 | 90 / 80 / 65 | Existing API examples compile; navigation, diagram legends and review guidance need refinement |
| T-004 | 75 / 60 / 70 | 85 / 65 / 60 | Whole-method timeouts can mask assertions while waiting for browser-process cleanup |
| G-012 | 95 / 90 / 90 | 65 / 55 / 60 | Source hashes accepted an unrelated test; receipt and graph associations were missing |
| G-013 | 90 / 85 / 85 | 65 / 55 / 60 | Indexed files and a narrow flow set lacked a structured owner/facet review declaration |
| D-006 | 90 / 80 / 85 | 65 / 60 / 55 | Existing complete HTML delivery still explained only selected AI/J1 behavior |
| A-001 | 85 / 75 / 80 | 60 / 45 / 50 | Requested project/Atlas axes lacked a shared evidence-bound assessment record |
| G-014 | 85 / 85 / 80 | 65 / 50 / 90 | Dirty J1 lacks eligible controls for two governance test changes; its guard correctly fails |
| G-015 | 95 / 90 / 90 | 65 / 55 / 65 | Valid endpoints admitted reversed outcomes, orphan observations and an implementation-only oracle |
| D-007 | 90 / 80 / 85 | 65 / 55 / 55 | Source-backed negative claims lacked explicit behavior paths and selected assertion witnesses |
| A-002 | 85 / 80 / 85 | 70 / 55 / 70 | Structural checks lacked traversal mutations and publisher proof beyond field presence |
| F-011 | 85 / 80 / 75 | 70 / 40 / 85 | Source order may skip tracker cleanup after engine-quit failure; runtime leak not demonstrated |
| F-012 | 90 / 85 / 80 | 75 / 60 / 75 | Malformed UI booleans and unbounded configured waits; duplicated parsing |
| F-013 | 85 / 80 / 75 | 65 / 50 / 80 | Partial startup lacks rollback and nested close failures may mask the first cause |
| D-008 | 90 / 90 / 90 | 60 / 45 / 70 | Live has 44 report and 14 Atlas captures without a bounded selection or layout guard |
| R-001 | 90 / 90 / 90 | 60 / 40 / 65 | Retained UI methods lack per-engine native proof; registration and sensitive-input audits need controls |
| D-009 | 90 / 80 / 80 | 85 / 65 / 80 | Markdown structure, code/numeric parity and meaning drift lacked owned controls |
| G-018 | 70 / 75 / 60 | 75 / 45 / 60 | One 1297-line documentation test class mixed six review domains and selector/fixture ownership |
| D-012 | 90 / 80 / 75 | 70 / 40 / 75 | Testing strategy used a vertical flow and lacked comparable API/Playwright/Selenium design boundaries |
| G-023 | 90 / 80 / 80 | 93 / 95 / 92 | Duplicate JSON members were admitted at Atlas boundaries |
| G-024 | 75 / 70 / 65 | 91 / 92 / 94 | Host collation changed path order for the same bytes |
| T-008 | 90 / 85 / 80 | 94 / 95 / 93 | TestNG could pass while invocationCount=0 suppressed a method |
| G-021 | 85 / 80 / 75 | 90 / 94 / 89 | Loader/retrieval admitted array IDs and digests before the separate archived-provenance guard |
| G-022 | 85 / 85 / 75 | 88 / 86 / 92 | Generated descendants were traversed before filtering; current Unicode/space-path acceptance was absent |
| T-007 | 75 / 65 / 65 | 93 / 94 / 90 | Bounded source-bound native repeats of retained component oracles were absent |
| G-020 | 75 / 70 / 75 | 70 / 70 / 85 | A raw CRLF inventory digest rejected identical method content after an LF checkout |
| G-019 | 80 / 85 / 70 | 75 / 65 / 75 | An external item and stale closure text confused the active scope; empty-state intent lacked a guard |
| D-013 | 85 / 85 / 75 | 75 / 45 / 75 | Vertical flows, open/missing legend boundaries and dense overview decisions |
| R-002 | 90 / 80 / 75 | 60 / 60 / 60 | Previously supported Allure conflicts with the newly narrowed Extent-only scope |

</details>

Historical evidence locators use `${ATF_WORK_ROOT}/...`, with the external
workspace selected as described in [Development](../Development.md).
These locators are not repository links or additional checkout requirements.
Original resolved execution roots remain in the archived external manifests;
normalizing their presentation does not claim a fresh execution.

## Verified corrections

<details>
<summary>Verified corrections and captured execution evidence</summary>

| ID | Delivered behavior | Verification |
|---|---|---|
| D-015 | Native artifact/live-source audit, architecture and explicit verdicts; quality 95 (95/95/94) | Rejected original artifact, native repair and independent controls; [evidence and ratings](Code_And_Documentation_Maturity.md#d-015) |
| D-014 | Numbered business reader, adjacent audit and mandatory RAG; quality 94 (94/95/94) | Source, independent oracles, negative/repair controls and native rendering; [evidence and limits](Code_And_Documentation_Maturity.md#d-014) |
| G-023 | Unique JSON members across Atlas boundaries, quality 94 (95/95/93) | 209 Node per edition, counterexamples/repairs and identical valid receipt; [evidence](Code_And_Documentation_Maturity.md#g-023) |
| G-024 | Ordinal path ordering, quality 93 (93/92/95) | Identical path/digest pairs under two native collations; [evidence](Code_And_Documentation_Maturity.md#g-024) |
| T-008 | Explicit positive invocation counts, quality 95 (95/95/94) | Real TestNG probe, extended existing controls and annotation repairs; [evidence](Code_And_Documentation_Maturity.md#t-008) |
| G-021 | Strict JSON IDs/digests, quality 93 (93/95/92) | 205 Node per edition, malformed inputs/repairs and identical valid receipt; [evidence](Code_And_Documentation_Maturity.md#g-021) |
| G-022 | Prune before traversal and Windows relocation, quality 92 (91/92/94) | 736 native per edition in Unicode/space LF/CRLF paths and final closure; [limits](Code_And_Documentation_Maturity.md#g-022) |
| T-007 | Bounded repeatability of retained components, quality 94 (94/95/93) | 120/120 native outcomes, no skips/configuration errors; [evidence](Code_And_Documentation_Maturity.md#t-007) |
| G-020 | Canonical LF/CRLF-equivalent inventory seal; quality 94 (95/94/93) | Existing owner-method probe and final native closure; [evidence](Code_And_Documentation_Maturity.md#g-020) |
| G-019 | Reconciled lifecycle and explicit zero-task contract; quality 94 (95/93/94) | Paired native/Node receipts, negative controls and actual repairs; [evidence](Code_And_Documentation_Maturity.md#g-019) |
| D-013 | Shared presentation rules across 50 pairs; quality 93 (94/92/94) | Paired 736/736; 203 Node each; desktop rendering; [evidence and limits](Code_And_Documentation_Maturity.md#d-013) |
| R-002 | Extent-only maintained backend with explicit rejection and recovery; quality 93 (95/92/92) | Paired aggregate readers and 15 desktop components; [evidence](Code_And_Documentation_Maturity.md#r-002) |
| D-012 | Horizontal strategy maps and comparable API/Playwright/Selenium boundaries; quality 93 (94/92/94) | EN/RO 209/209; native desktop diagrams/tables; [evidence and limits](Code_And_Documentation_Maturity.md#d-012) |
| F-011 | Teardown clears interaction state after success, RuntimeException and Error; delivered quality 94 (95/92/94) | Complete EN/RO local reactors, focused 69/69 and preserved negative baseline; [evidence](Behavior_And_Negative_Cases.md#f-011) |
| F-012 | Shared strict UI configuration and wait boundaries; delivered quality 94 (95/93/94) | Positive/negative component contracts and complete paired acceptance; [evidence](Code_And_Documentation_Maturity.md#f-012) |
| F-013 | Partial acquisition rolls back without masking the original cause; delivered quality 93 (94/92/93) | Existing Playwright 16 and Selenium 12 provider cases; [evidence](Code_And_Documentation_Maturity.md#f-013) |
| D-011 | Atlas semantic blocks, adjacent legends, bounded columns and native table balance; delivered quality 93 (94/92/92) | 203/203 Node per edition, EN 315/315 and RO 315/315 existing governance; [evidence and limits](Atlas_Depth_And_Assessment.md#d-011) |
| G-017 | Disabled selected tests and an unbounded console worker are now rejected; quality 93 (94/92/92) | Final local-02 EN/RO 690/690 and 690/690; original failed attempt retained; [evidence](Code_And_Documentation_Maturity.md#g-017) |
| G-018 | Six focused owners are registered explicitly in both maintained suites; quality 92 (94/90/92) | EN/RO docs/source 205/205; suite and method inventory 2/2; 573 methods, 60 owners re-pinned; [evidence](Code_And_Documentation_Maturity.md#g-018) |
| D-010 | Strategy, closed command recipes and all 51 document reviews are governed; delivered quality 92 (94/90/92) | Paired native controls, 182 captured Node cases per edition and 32 desktop records/56 SVG/98 captures; [evidence and limits](Code_And_Documentation_Maturity.md#d-010) |
| D-009 | All Markdown has structural/editorial guards; README facts and Romanian meaning are checked separately | 250 governance cases per edition, 182 Node cases, semantic review of all 21 README pairs and inspected changed diagrams; [evidence](Documentation_And_Translation_Review.md#d-009) |
| R-001 | Living principles, expected/actual checks, credential masking and native proof for the same retained methods | Playwright/Selenium 16/16 each; paired EN/RO runs and 181 Node cases; failed attempts preserved; [evidence](Retained_UI_Auditability.md#r-001) |
| F-002 | Startup uses maintained suites and explicit runtime configuration | 304 local cases; aggregate profile produced Extent and Allure artifacts |
| F-003 | Retired generation/audit code, prompts and historical reports are removed from main | 100 browser cases; source and packaged JAR contain no retired components |
| F-006 | Retained regressions call current helpers and use maintained suite paths | 304 local cases and 11 Allure cases passed |
| M-001 | Five modules, independent runtime providers, test-only product support and enforced architecture/documentation contracts | 372 final reactor checks, 102 browser cases, 11 Allure cases and inspected aggregate artifacts; [evidence](Module_Architecture.md) |
| G-001 | Required owner/feedback guides, factual build claims, paired HTML/Markdown and agent-file exclusion | 24 documentation cases within 74 governance cases passed; [evidence](Governance_Review.md#g-001) |
| T-001 | Existing regressions use bounded waits, observable oracles, exact cleanup and enforced suite coverage | 393 reactor cases and 4 configuration-isolation cases passed; [evidence](Governance_Review.md#t-001) |
| D-018 | Ten facets investigated with preserved baseline and boundaries | 38 REVIEWED / 4 PARTIAL; [evidence and limits](Code_And_Documentation_Maturity.md#d-018) |
| D-019 | Diagrams calibrated against archive and live source | 15 graphs, complete relationships and native review; [evidence](Code_And_Documentation_Maturity.md#d-019) |
| D-016 | Scoped semantic explanations and independent oracles | 32 REVIEWED / 10 PARTIAL facets, 12 existing behaviors; [evidence and limits](Code_And_Documentation_Maturity.md#d-016) |
| D-017 | Compact readable Atlas identity | Native 1440/900 rendering, 58 px header and 16 px top offset; [verification](Code_And_Documentation_Maturity.md#d-017) |
| P-001 | Separate documentation Atlas and recovered policies, with the essential living architecture in both formats | Source/links/hash and desktop/mobile rendering verified; no production/POM/new-Java changes; [evidence](Governance_Review.md#p-001) |
| G-002 | Owned source visibility, pause/console ownership and current-thread interrupt restoration are governed | 474 local and 474 aggregate checks passed; [evidence](Governance_Contracts.md#g-002) |
| G-003 | Supported Maven selections, declared defaults and stable-document lifecycle are checked against current owners | Final local/aggregate checks and paired-guide contracts passed; [evidence](Governance_Contracts.md#g-003) |
| T-002 | Actual API execution/session behavior and local browser request/response outcomes have independent observations | 2 positive and 4 negative network probes within 119 browser/native checks passed; [evidence](Governance_Contracts.md#t-002) |
| F-009 | Negative retry budgets fail before execution; zero still permits one initial request | Final local and aggregate API regressions passed; [evidence](Governance_Contracts.md#f-009) |
| G-004 | Mandatory English source/documentation policy, bounded language guards and compilable README examples are enforced | 3 examples compiled; independent Playwright and Selenium examples each passed; [evidence](Governance_Contracts.md#g-004) |
| F-010 | Both sandbox snapshot maps retain their first observed value, including absence, across repeated changes | Direct snapshot and reporting-session regressions passed in the final local/aggregate suites; [evidence](Governance_Contracts.md#f-010) |
| T-003 | Input-value success regression keeps its exact two-read oracle with a usable scheduling budget | 474 local and 474 aggregate checks passed after the fixture-only correction; [evidence](Governance_Contracts.md#t-003) |
| F-008 | The selected Selenium/Chrome/driver matrix uses exact typed CDP v158 and the configured executable through Manager | 120 browser/native checks passed, including 2 positive and 4 negative real response probes; [evidence](Browser_Compatibility_And_Portability.md#f-008) |
| G-005 | Project guidance and bounded source/configuration guards support relocatable paths and explicit runtime roots | Relocated local and aggregate selections each passed 514 checks; [evidence](Browser_Compatibility_And_Portability.md#g-005) |
| D-001 | Root routes API/UI readers to owner guides; module ownership and diagrams stay distinct | Two root diagram positions, routes and table density are governed; both adapter examples compile; [evidence](Browser_Compatibility_And_Portability.md#d-001) |
| T-004 | Network matching keeps its own deadlines while the fixture separately bounds setup and cleanup | All 6 bodies and cleanups completed in the accepted 120-check browser run; [evidence](Browser_Compatibility_And_Portability.md#t-004) |
| G-006 | README structure, EN/RO edition parity and technical facts are governed | Both editions passed 526 local checks, including 187 governance cases; 18 README pairs and rendered diagrams reviewed; [evidence](Maturity_And_Readme_Editions.md#g-006--readme-governance-and-romanian-edition) |
| G-007 | Explicit Java imports preserve existing implementations | 17 identical implementation trees; normal/static wildcard controls and complete accepted selections; [evidence](Code_Stability_And_Artifacts.md#g-007--explicit-java-import-ownership) |
| T-005 | Existing worker-limit regression uses bounded rendezvous and actual results | 20 consecutive invocations and local/aggregate selections passed; [evidence](Code_Stability_And_Artifacts.md#t-005--deterministic-worker-limit-regression) |
| D-003 | Governed artifact documentation, classified archives and reusable Atlas intake | 8 filesystem/CLI checks; archived and rehashed reports; 21 README translations and applied Atlas example; [evidence](Code_Stability_And_Artifacts.md#d-003--governed-artifacts-and-reusable-atlas-intake) |
| G-008 | Constructive failures retain original evidence, ownership and verifiable repair guidance | 550 passed in each edition, including 211 governance cases; owner routes and recheck selections resolve; [evidence](Actionable_Governance_Diagnostics.md#g-008--constructive-governance-failures) |
| T-006 | Negative controls distinguish emitted rule identity from prefixes and payload mentions | 17 focused invocations and both complete selections passed; [evidence](Actionable_Governance_Diagnostics.md#t-006--exact-rule-identity-in-negative-governance-fixtures) |
| G-016 | Test method identities have reviewed removal/addition diagnostics; quality 94 (95/94/94) | Focused owner 114/114, paired local reactors 742/742, Node 203/203 per edition; [evidence and limits](Code_And_Documentation_Maturity.md#g-016) |
| D-005 | One complete reader HTML per project embeds dedicated CSS, behavior, source and evidence | 66 maintenance checks and isolated offline rendering for AI/J1 at two desktop widths with ten sections; [evidence](Atlas_Complete_Report.md#d-005--one-complete-understanding-artifact-per-target) |
| G-010 | Complete report governance retains identity, outcomes, isolation and constructive repair routes | Deep-bundle, oracle, JSON, library-boundary and deliberate presentation controls passed; [evidence](Atlas_Complete_Report.md#g-010--constructive-governance-for-the-complete-atlas-contract) |
| K-001 | Local cited retrieval preserves every mandatory requirement and the selected target scope | 14 records, 12 mandatory policies, 12 knowledge checks and three verified archived Git blobs; [evidence](Atlas_Complete_Report.md#k-001--cited-knowledge-retrieval-with-mandatory-policy) |
| G-011 | Numeric baseline inputs remain separate from preserved verified history | Intended malformed-baseline controls and both 211-case governance selections passed; [evidence](Atlas_Complete_Report.md#g-011--keep-feedback-baseline-inputs-distinct-from-verified-history) |
| G-012 | Exact behavior subjects, flow-local graph claims and the entire canonical knowledge receipt are governed | 111 maintenance checks, original negative evidence and real separate J1 PASS/FAIL subjects; [evidence](Atlas_Depth_And_Assessment.md#g-012) |
| G-013 | Five actual owners per project declare seven facets and honest source-review gaps | AI 8 flows/48 claims; J1 6 flows/37 claims; owned/shared corruption controls and exact source checks; [evidence](Atlas_Depth_And_Assessment.md#g-013) |
| D-006 | Deeper source explanations retain one complete reader per project | 20/23 source documents, 4 desktop records/32 diagrams, both 211-case edition selections and no new E2E; [evidence](Atlas_Depth_And_Assessment.md#d-006) |
| A-001 | Comparable evidence-bound axes separate project-source quality from Atlas/RAG quality | AI 90/90 and J1 87/89; explicit 40/30/30 pillars, weights, source/evidence references and limits; [evidence](Atlas_Depth_And_Assessment.md#a-001) |
| G-015 | Reachable case paths, independent exact oracle fragments and honest negative scope are governed | 138 maintenance cases, alias/cycle/corruption controls and both Java edition selections passed; [evidence](Behavior_And_Negative_Cases.md#g-015) |
| D-007 | Existing positive and negative behavior is explained in each complete reader | AI: 11 / J1: 10 flows, 53 cases, 4 desktop records/46 diagrams and 21/23 embedded source guides; [evidence](Behavior_And_Negative_Cases.md#d-007) |
| A-002 | Pure traversal, publisher components and API component proof retain independent scopes | 138 Node cases and five added invocations in the existing API contract class passed; retained original XML/TAP; [evidence](Behavior_And_Negative_Cases.md#a-002) |
| D-008 | Current artifact publication is bounded and complete immutable history stays outside live | 26 generated current files, two unchanged Atlas readers, 20 component cases and 211/211 Java governance cases; [evidence](Artifact_Publication_And_History.md#d-008) |

</details>

Desktop browser acceptance criteria and explicitly historical correction receipts are in [framework maintenance](Framework_Maintenance.md).

- Current closure contracts and execution receipts are in [deeper executable contracts](Governance_Contracts.md) and [browser compatibility and portability](Browser_Compatibility_And_Portability.md).
- [The earlier governance review](Governance_Review.md) retains its original scope.
- F-008 is verified for its recorded Windows matrix; another platform or version requires fresh acceptance evidence.
- The [API verification record](API_Verification.md) distinguishes framework API regressions from the existing GoRest write journeys.

## Recording and closing feedback

1. Record the smallest reproducer, expected behavior and actual behavior.
2. Assign an owner and a checkable TODO before changing the contract.
3. Keep failed observations and test counts honest; a copied artifact is not a
   new execution.
4. Close an item only after its stated checks pass. Record the source commit,
   exact command and external evidence location.
5. Move durable behavior to its owner documentation and keep this register
   focused on active gaps and concise closure records.

6. If no accepted work remains, retain the table header and one standalone
   empty declaration in Active TODOs. The marker must be outside fenced examples;
   active rows and an empty declaration cannot coexist. Keep external observations,
   withdrawn scope and verified proof separate from implementation work.

The machine-readable declaration is `<!-- feedback:empty -->`. Empty means
no accepted implementation item is recorded; it does not certify defect-free
code, completed external work or an implemented PARTIAL living principle.
