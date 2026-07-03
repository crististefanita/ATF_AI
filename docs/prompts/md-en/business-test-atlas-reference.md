<!-- Generated from ../html_EN/atlas/business-test-atlas-reference.html. Keep source of truth in html_EN. -->
<!-- Source stylesheet: [shared-report-reference.css](../assets/shared-report-reference.css) -->

# Business Test Atlas for a test folder

Skill analysis-only: receives the source code of a test package, rebuilds the business already covered and produces `business-test-atlas.html` as a visual understanding atlas, not as an execution plan.

Minimal prompt:

```text
Run the ATF6 Atlas skill.
Input: <test package root>
Output folder: <atlas output folder>
```

The agent resolves detailed requirements from this document and the Atlas contracts.

`INPUT FOLDER` `BUSINESS FLOW ATLAS` `FLOW DIAGRAMS` `ANALYSIS ONLY` `CSS SUCCESSFUL`

<details>
<summary>Overview</summary>

Cold contract: The atlas explains the business already proven by existing tests. Tests are the entrance. The real objects and flows of the application are the subject. Technical layers are traceable evidence. Technical layers are not main nodes.

| Category | Scope |
| --- | --- |
| Owner | `business understanding` `business flows` `visual atlas` |
| Produce | `business-test-atlas.html` `business flow graphs` `layer trace map` `comprehension limits` |
| Use | `test package` `UI layers` `Living Architecture` `shared CSS` |
| Does not produce | `new tests` `patch` `mandatory run plan` `final run report` |
| Do not convert | `classes in flow` `the layers in business` `selectors in objects` |
| Style | `shared-report-reference.css` `collapse` `Minimalist SVG` |

<!-- /table -->
</details>

<details>
<summary>1. Contract</summary>

<details>
<summary>1.1 Contract in 10 seconds</summary>

| Cold question | Mandatory answer | Anti-drift |
| --- | --- | --- |
| What does it receive? | `INPUT_TEST_PACKAGE_ROOT`, meaning the analyzed test package folder. | it does not start from run artifacts or html-ex |
| What are you reading? | the tests as an entry point and the Java layers touched by them. | it doesn't stop at class names |
| What does it rebuild? | real flows of the application: actor -> intent -> visible action -> business object -> observable result. | it does not make a graph of technical layers |
| What does it publish? | `business-test-atlas.html` with the business map, subflows, trace, and understanding limits. | do not publish final run report |

<!-- /table -->

| Category | Scope |
| --- | --- |
| Input | `folder / test package` `read-only source code` |
| Cross | `test` `act` `component/page` `service` `validation` `model/constant` |
| Output | `business-test-atlas.html` `business flows` `layer trace` `comprehension limits` |
| Accept | `product flows` `visible objects` `observable results` |
| Reject | `class inventory` `graph of technical layers` `selector/page flow` `run plan` |
| Do not decide | `package legality` `html-ex readiness` `new tests` |

<!-- /table -->

- **The tests:** entry point, not the subject of the atlas.
- **The layers:** traceable evidence, not master nodes.
- **Nodes:** objects or business actions visible to the user.
- **Subgraphs:** source-specific flows chosen from the real dominant families in the analyzed package.
- **Reject:** any node that can be read as selector, page, helper or technical mechanics.

<!-- diagram-readable-table -->

```text
Test package -> entrypoint, not subject -> Traversal layer -> actions / pages / validation -> Business story -> actor / intent / object -> HTML Atlas -> business flow map
```
</details>

<details>
<summary>1.2 Cold boundary</summary>

| He is allowed | He is not allowed |
| --- | --- |
| to read tests, Java layers and support classes | to modify tests or helpers |
| to publish diagrams and understanding matrices | to declare the package final-ready |
| to write down comprehension limits in the report | to decide the legality of the run or the next mandatory run |

<!-- /table -->
</details>

<details>
<summary>1.3 Canonical parameters</summary>

| Parameter | Default ATF | Role |
| --- | --- | --- |
| `INPUT_TEST_PACKAGE_ROOT` | `<explicit folder/package>` | analysis root: the source code of the tests and the Java classes touched by them |
| `SHARED_REPORT_REFERENCE_CSS` | `docs/shared-report-reference.css` | canonical style |
| `LIVING_ARCHITECTURE_REFERENCE` | `docs/Living_Architecture_UI_API_doc_v1_0.html` | layer contract |
| `OUTPUT_ATLAS_HTML` | `business-test-atlas.html` or `<ATF_NAME>_business-test-atlas.html` | product report; the generic name is mental template, the scoped name is real artifact per analysis |
| `DECLARED_ATLAS_OUTPUT` | `docs/atlas/<name>_business-test-atlas.html` | the explicit atlas artifact of the current analysis |
| `ATLAS_TEST_EXTENT` | `docs/atlas/validation/atlas-tests-extent-report.html` | auditable artifact for atlas tests |
| `MUST_PRODUCE` | `business-test-atlas.html` | mandatory output of the skill |
| `MUST_NOT_MODIFY_ANALYZED_FILES` | `true` | the analyzed files remain read-only |

<!-- /table -->

- `INPUT_TEST_PACKAGE_ROOT` -> read-only investigation of the test source code.
- The required output is a declared HTML atlas.
- `business-test-atlas.html` is the generic name / mental template.
- `<ATF_NAME>_business-test-atlas.html` is the accepted form for real versioned artifacts per analysis.
- If the output is missing, the skill contract is violated.
- If the analyzed files are modified, the skill contract is violated.
- The atlas is validated from the source code read and from the HTML produced, not from run artifacts.
</details>
</details>

<details>
<summary>2. Business Flow Extraction</summary>

<details>
<summary>2.1 Main pipeline</summary>

<!-- diagram-readable-table -->

```text
Scan -> tests + layers -> Extract -> actor + intent -> Map -> business object -> state -> owned / limit -> Publish -> atlas + prompt
```
</details>

<details>
<summary>2.2 Business object extraction</summary>

<!-- diagram-readable-table -->

```text
Step test -> visible action -> intent -> business reason -> Object -> row / state / document -> state -> test / page / validation
```
</details>

- `test method` -> entry point.
- `layer path` -> traversed technical evidence.
- `actor / intent` -> business meaning of the action.
- `visible action / business object / result` -> application flow.
- `atlas HTML` -> produced business-first map.
</details>

<details>
<summary>3. HTML Atlas</summary>

<details>
<summary>3.1 Anatomy of HTML output</summary>

<!-- diagram-readable-table -->

```text
Score cards -> overall last -> Context -> collapsed -> Main graph -> business-first -> Subflows -> collapsed 3.x -> Evidence -> tables + traces
```
</details>

<details>
<summary>3.2 Mandatory areas</summary>

| Area | What does it look like? | Invalid if |
| --- | --- | --- |
| Score cards | how well the atlas explains the business deduced from the source | measure package readiness or html-ex readiness |
| Executive Context | scope, rational score, limits of understanding | remains open and loads the initial reading |
| Business Flow Graph | the main map of the product seen through tests | show classes, selectors or layers as main nodes |
| Business Flow Understanding | real 3.x subflows of the application, chosen from the dominant families in the source, full-width, collapsed | contains contract graphs, audit, technical coverage or template flows without root in the source |
| Business proof strips | collapsible subsection after each graph/subgraph, titled `Node Evidence Strip` : nod/flow -> test/method -> layer path -> proven business understanding -> limit | the local link between the graph and the code that supports it is missing or the proof is left only in the tables below |
| Graph zone hygiene | the visual area of ​​the graph contains only the SVG, `Legend` and `Node Evidence Strip` | insert narrative paragraphs, proof sentences or audit text between the graph and the legend/proof strip |
| Business Evidence Tables | coverage, repeated corridors, layer trace and maturity | claims that the tables are business flows |
| Source-to-Business Trace | real methods/classes -> layer path -> business object | it is not backed by source |
| Atlas Test Audit | The extent of the atlas tests, the exact order and the synchronized total before publication; when there is a business atlas + framework atlas, publish it as well `Full Atlas regression command` | publish stale numbers, reuse the Extent of another atlas, or hide the full Maven reproduction command |

<!-- /table -->
</details>

<details>
<summary>3.3 Minimal HTML structure</summary>

| Section | Minimal content | Default |
| --- | --- | --- |
| Score Cards | Business Understanding, Flow Reconstruction, Layer Traceability, Business Object Clarity, Evidence Fidelity, Overall | visible top; Overall last/right |
| Executive Context | folder, atlas score, comprehension limits | closed / subdetails |
| Business Flow Graph | the business map from the perspective of the tests | open |
| Business Flow Understanding | 3.x subgraphs with real and source-specific flows of the application | closed / full-width / legends closed |
| Business proof strips | after each graph and each subgraph 3.x there is a collapsible subsection `Node Evidence Strip` : node, test/method, layer path, proven business understanding, limit | closed by default; the named methods/classes exist in sourceRoot/UI layers |
| Business Evidence Tables | coverage, repeated corridors, layer trace, maturity and limits | closed / supporting evidence |
| Source-to-Business Trace | class/method -> actor -> object -> proven business understanding | closed |
| Atlas Test Audit | link to the Extent of atlas tests + order + cold result; the declared total must be identical to the linked Extent; for joint publication includes `Full Atlas regression command` : `mvn -q "-Dtest=com.ai.skills.atlas.tests.*Test,com.ai.atf.frameworkatlas.*Test" test` | closed / audit-only / verified before publication |

<!-- /table -->

- Atlas reuses `shared-report-reference.css` .
- Local overrides are noted as deviations.
- The visual form must remain reusable for other domains.
</details>

<details>
<summary>3.3.1 Canonical score cards</summary>

- Canonical formula: **business goal -> flow question -> source/layer evidence -> atlas score** .
- The score is not decorative.
- Each note answers a comprehension question.
- Each note can be reopened through the inspected source and layers.

| Field per axis | What should he answer? | Invalid if |
| --- | --- | --- |
| business goal | what business understanding does the atlas want to measure for the axis | the axis has only name and score |
| flow question | what a cold question the atlas asks about the flow of the application | there is no question, only verdict |
| source/layer evidence | which test/method/layer supports the answer | the proof is only prose |
| atlas score | the calculated/capped grade for that axis | the note cannot be reopened |

<!-- /table -->

| Card | What measure | The cold rule |
| --- | --- | --- |
| Business understanding | how deeply it explains the business deduced from the existing tests | it does not reward readiness or planning |
| Flow reconstruction | actor -> intention -> visible action -> object -> observable result | does not accept the list of classes as flow |
| Traceability Layers | test -> action/component/page/service/validation/model/constants | without a trace, the claim remains weak |
| Business Object Clarity | how clearly the real business objects are separated | page/action/selectors do not become root nodes |
| Fidelity Proof | how faithful the atlas remains to the test code and layers | do not invent business unsupported by code |
| Overall | comprehension score, not package score or html-ex readiness | the last card, visually on the right |

<!-- /table -->

- Each card appears at the top, visible.
- Each card reappears in **Score motivation** .
- The motivation includes the axis, numerical score and short verdict.
- If the score cannot be explained, the card is decorative and invalid.

| Score rule | Cold contract | Invalid if |
| --- | --- | --- |
| Canonical axes | the same 5 axes + Overall in cards and in motivation | the report uses local synonyms or shorthands |
| Card readability | the card description remains short, under 88 characters | the text is cut or crowded in the score-card |
| Audit pointer | Motivation score explicitly indicates 4.5, 4.6 and 5.1 | the reader does not know where to reopen the score calculation |
| Score support | each card has a support row with the same note and proof surface | the card has a number, but it does not have an anchor in the tables/traces |
| Overall formula | 4.6 repeat the axes, notes, weights and contributions | Overall cannot be recalculated cold |
| Computable overall | Overall derives from the 5 axes of business understanding | Overall cannot be recalculated or jumps over all axes |
| Evidence per axis | each line explains what business/layer/source supports the note | it only has a number, without a verifiable reason |
| Boundary | the score measures atlas comprehension | it is sold as package readiness or html-ex readiness |
| GQM / traceability | each grade has a business goal, flow question, source/layer evidence and atlas score | the note remains a decorative number, without question and re-openable proof |
| Even scores | if all axes have the same grade, the atlas explicitly explains the common ceiling and why there is no differentiation | all the cards have the same grade without reasons of uniformity |

<!-- /table -->
</details>

<details>
<summary>3.4 Maturity and limits of understanding</summary>

| Axis | Cold question | Decrease the grade if |
| --- | --- | --- |
| Business durability | does it remain after navigation / refresh / relogin? | it's just screen status |
| UI visibility | does the user see the object? | the proof is only in the service/model |
| Source trace | can you see the route test -> page/action/validation/service? | the object is inferred from the name alone |
| Layer disciplines | does the test delegate to page/actions/validation? | direct assertions in the test layer |
| Repetition pressure | where the existing coverage repeats the same corridor? | the repetition is sold as a new agreement |
| Reportability | can it become node/proof row? | cannot be explained business-first |

<!-- /table -->

- It is not a package overall.
- It does not measure readiness for `html-ex` .
- It measures the understanding of business flows from the read suite.
</details>

<details>
<summary>3.5 Mini-template for atlas row</summary>

| Field | What does it contain? | Short example |
| --- | --- | --- |
| Test / Class | the source class and method | `BusinessFlowTest.reopensVisibleObject()` |
| Business object | visible or blocked object | domain business object / observable state |
| Evidence | the class/method/layer that supports the object | test method + action/component/page/validation/model |
| Maturity | owned / partial / repeated / blocked | owned |
| Comprehension limit | which cannot be understood yet from the read source | unproven lifecycle detail |
| Record notes | the observed limit, without the obligation of the next run | visible state without deeper persistence proof |

<!-- /table -->
</details>
</details>

<details>
<summary>4. Atlas Validation</summary>

<details>
<summary>4.1 What he reads</summary>

| Source | What does it extract? | What does NOT inherit |
| --- | --- | --- |
| `< input-folder >` | the real scope of the analyzed source code | external artifacts or old runtimes |
| `src/test/java/com/ai/ui/tests` | methods, classes, claims, business family | score because the tests are green |
| `src/main/java/com/ai/ui` | page/actions/service/validation layering | business ownership if it's just a helper |
| `docs/Living_Architecture_UI_API_doc_v1_0.html` | UI/API/core/reporting responsibilities and the separation of support vs protected core | the right to overwrite MCP contracts or modify core/MCP/atlas for a single business context |

<!-- /table -->
</details>

<details>
<summary>4.2 Separation from the rest of the system</summary>

| Layer | Role | What should not be mixed |
| --- | --- | --- |
| MCP | validate package contracts and artifacts | not input for the source-based atlas |
| Business Atlas | understand the business flows from the tests | does not become final run report |
| Reporting | publish the current package | it does not replace the atlas of the suite |
| Atlas tests | they will validate the HTML produced by the skill | it does not have to depend on the concrete business of the application |

<!-- /table -->

- Atlas is its own result type.
- The output is a business understanding HTML.
- Atlas validations are separate from MCP.
- The discipline remains similar: contract, evidence, score, audit.
</details>

<details>
<summary>4.3 Atlas test contract</summary>

- Atlas contracts are grouped by families.
- Main families: form, business graphs, source trace, score, audit.
- They are not a list of rules.
- Each group protects a different part of the business-first atlas.

#### Minimal semantic vocabulary

- It does not validate exact sentences.
- It does not validate hardcoded domain objects: invoice, cart, payment, profile, etc.
- It validates the HTML structure through classes, tables, sections and `data-atlas*` .
- It validates the semantics only through the small families below.
- If a word is not in this vocabulary or in the canonical axes, it does not become the test password.

| Family | Terms accepted | Role |
| --- | --- | --- |
| Maturity | owned, held, durable, reopen, relogin, partial, diagnostic, surface, surface-only, surface | classify the maturity of flows without hardcoded business |
| Limit / boundary | limit, limit, limits, missing, lacking, incomplete, unexplained, open, boundary | it marks limits of understanding and unclosed lifecycle |
| Head / threshold | cap, capped, ceiling, ceiling, below 95, below 95, 95+ | link score limits without fixed phrases |
| Story chain | actor, intent, visible action, business object, observable result | makes the flow readable as a product story |
| Evidence surfaces | table/tabel, trace, source/source, layer, object/object, flow/flux, evidence/evidence, proof | link the score and the reopenable evidence graph |
| Business understanding | business understanding, business understanding, business comprehension, comprehension, atlas quality | separate the quality of the atlas from the completeness of the suite |

<!-- /table -->

This vocabulary is intentionally small. If the agent wants another wording, he must keep the structure, the semantic classes and one of the above families, not an exact phrase.

#### Structure / readability

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.html.uses-shared-css | HTML reuses `shared-report-reference.css` | prevents ad hoc styles |
| atlas.sections.collapsible | the main sections are collapsible | keep navigation clear |
| atlas.candidate.semantic-contract-markers | the structural contract uses `data-atlas*` / `data-contract`, not many hidden markers | prevents HTML built for invisible tests |
| atlas.output.exists | there is `business-test-atlas.html` | confirm minimum output |
| atlas.source.read-only | the analyzed files have not been modified | keep the analysis-only contract |
| atlas.no-code-generation-claims | does not claim to have generated tests or fixed code | keep boundary analysis-only |

<!-- /table -->

#### Business graph / flow comprehension

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.business-flow.present | there are real business flows, not class inventory | protect the purpose of the atlas |
| atlas.business-flows.application-only | flows describe the application, not meta-contracts | hold the business-first atlas |
| atlas.business-object-centered | business object nodes and cells are real objects extracted from the source, not page/action/selector/component/helper and not a hardcoded list of domains | prevents graphs of selectors/pages/layers disguised as business and keeps the applicable rule in any application |
| atlas.flow-story.complete | the flow has actor, intention, action, object, result | prevents shallow lanes |
| atlas.graph-node.business-proof-strip | each major graph has a short lane node/flow -> test/method -> layer path -> proven business understanding -> limit | makes the graph auditable without turning the nodes into technical classes |
| atlas.subgraph.business-understanding-strips | each 3.x subgraph has its own local band of proven business understanding | does not oblige the reader to look for the proof only in the tables below |
| atlas.graph-node.proof-strip.collapsed | after the main graph there is a collapsible subsection `Node Evidence Strip` | separate the visual legend from the auditable evidence of the nodes |
| atlas.subgraph.proof-strip.collapsed | after each subgraph 3.x there is a collapsible subsection `Node Evidence Strip` | makes each slice reopenable locally, without dependence on global tables |
| atlas.candidate.subgraphs.source-specific-dominance | the 3.x subgraphs reflect real dominant families from the analyzed package, not reused template flows | prevents the structurally valid but too generic semantic atlas |
| atlas.candidate.local-proof-strip.source-backed | the local proof strip names existing methods/classes in sourceRoot or the traversed UI layers | makes each subgraph re-openable in code, without dependence on wording business fix |
| atlas.subgraphs.business-complexity | 3.x subgraphs have distinct nodes, transitions and business objects/states, without hardcoded business tokens in the test | it forces real flows, but remains universal for any domain |
| atlas.candidate.object-maturity.matrix | each major object has maturity, evidence and the limit of understanding | prevents the atlas that names objects, but does not say if they are owned, partial, diagnostic or surface-only |
| atlas.candidate.end-to-end-closure.ladder | the important flows show start -> visible action -> durable object -> reopen/relogin -> lifecycle unowned | makes the difference between deep flow and simple visual lane |
| atlas.candidate.flow-maturity.distribution | the atlas explains the owned / partial / diagnostic / surface-only distribution for the flows discovered from the source, without hardcoded domain objects | prevents the atlas that only mentions limits, but does not say the maturity of the flows |
| atlas.candidate.partial-flow-not-sold-as-owned | a partial, diagnostic or surface-only flow cannot be sold as strong ownership without an explicit head/limit | prevents contradictions between the business map, tables and score |

<!-- /table -->

#### Source trace / layer traversal

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.row-template.complete | the lines have test, object, evidence, maturity, comprehension limit | makes the atlas verifiable |
| atlas.source-trace.method-level | Source-to-Business Trace contains representative methods and test -> layer -> object chains | prevents family/class level atlas |
| atlas.source-trace.backed-by-source | the methods and classes named in traces exist in the read source | prevents business wording without an anchor in the code |
| atlas.layer-trace.concrete-evidence | Layer Trace Map names concrete classes/methods/layers | prevents generic chains of type test -> page -> validation without re-openable proof |
| atlas.source-trace.application-business-only | Source-to-Business Trace contains only business flows of the application | move seed/baseline/agent/MCP/atlas-test to audit or limits, not to the business map |

<!-- /table -->

#### Score / business understanding

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.candidate.score-rationale.audit | each score-card has a line of motivation with axis, score and verdict | make the score auditable, not decorative |
| atlas.candidate.score-cards.readable | the descriptions in the score cards remain short and visible | prevents cut or cramped text in the opening area |
| atlas.candidate.score-opening.audit-pointer | Motivation score refers to 4.5, 4.6 and 5.1 | makes reopening the score fast |
| atlas.candidate.score-support.trace | each card has support with the same note and proof | prevents scores without an anchor in business evidence |
| atlas.candidate.score-formula.trace | the Overall formula contains axes, grades, weights and contributions | makes Overall recalculable |
| atlas.candidate.score-overall.calculation | Overall is calculable from the 5 axes and does not mean readiness | prevents unexplained global scores |
| atlas.candidate.score-cards.documented | the axes from the atlas are documented in this HTML-req | prevents cards invented ad hoc by the agent |
| atlas.candidate.score-business-understanding.evidence | each axis explains the business understanding proven by flow, source and layers | it prevents generic motivation based on words, not on understood business |
| atlas.candidate.score-gqm-traceability | the score uses the chain business goal -> flow question -> source/layer evidence -> atlas score | prevents decorative notes that do not answer a comprehension question |
| atlas.candidate.score-uniformity.chap | if the axes have the same grade, there is an explanation of common ceiling / uniformity | it prevents flat scores that appear aesthetically calibrated |
| atlas.candidate.end-to-end-limit.score-cap | many major end-to-end limits cap the Overall below the excellent area or require explicit head | prevents Overall 95+ when the atlas declares many partial, diagnostic or incomplete flows |
| atlas.candidate.95-plus.limit-disciplines | Overall 95+ requires few limits or an explicit explanation of maturity/head | it prevents decorative growth above 95 when the atlas itself declares many real limits |
| atlas.candidate.suite-completeness.separation | Atlas Overall is separate from the completeness suite | clarifies that 95+ means atlas almost inevitably, not the complete end-to-end suite |

<!-- /table -->

#### Executable audit

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.test-audit.extent-linked | the atlas publishes the Extent of the atlas tests as a re-openable link | makes skill validation auditable |
| atlas.candidate.test-audit.total-synchronized | the total declared in Atlas Test Audit is identical to the total in the linked Extent, and the atlas does not reuse the report for another purpose | stops the publication of an atlas with stale numbers or with the wrong proof of validation |
| atlas.framework.full-regression-command | the framework atlas publishes `Full Atlas regression command` with the Maven command for business atlas + framework atlas | makes final common validation reproducible, not just local validation of a single HTML |

<!-- /table -->
</details>

<details>
<summary>4.4 Codex skill requirements</summary>

- This HTML remains the detailed law.
- The future Codex skill starts from one `SKILL.md` scurt.
- `SKILL.md` describes the trigger, workflow, and agent freedoms.
- The HTML document becomes `references/business-test-atlas-reference.html` .

| Ability | Must | Must note |
| --- | --- | --- |
| Reading project | it scans Java tests and layers starting from the received root | he does not invent business from the method |
| modeling | group by business flows, families and visible objects | it doesn't just produce the list of classes |
| HTML Atlas | publish business understanding diagrams and matrices | do not publish final run report |
| Feedback | write comprehension limits and understanding limits | does not require fresh run; the atlas analyzes the existing source |

<!-- /table -->

#### Proposed skill package

| File / folder | Role | The cold rule |
| --- | --- | --- |
| `SKILL.md` | front matter with `name` and `description`, trigger, and short workflow | under 500 lines; do not copy the entire HTML-req |
| `references/business-test-atlas-reference.html` | detailed contract, diagrams, rules and examples | it is read only when the task asks for details |
| `assets/business-test-atlas-template.html` | reusable HTML skeleton for score cards, main graph, subgraphs, trace and audit | does not contain hardcoded business |
| `scripts/validate-atlas` | runs the atlas contracts and synchronizes the validation report | does not modify the analyzed sources |
| `quick_validate.py` | standard validation of the skill after creation/migration | runs after any change in SKILL.md |

<!-- /table -->

#### Frontmatter minimum for `SKILL.md`

| Field | Recommended value | Why does it matter? |
| --- | --- | --- |
| `name` | `business-test-atlas` | short name, hyphen-case, easy to activate |
| `description` | Use when Codex must analyze an existing test package and produce a read-only business-test-atlas.html that reconstructs application business flows through source/layer evidence. | description is the main trigger of the skill |

<!-- /table -->

#### Short workflow for `SKILL.md`

| Step | Action | Output / check |
| --- | --- | --- |
| 1 | Read `INPUT_TEST_PACKAGE_ROOT` and confirm that the source remains read-only. | explicit folder, not hardcoded path |
| 2 | Scan the test methods and track the layers reached. | test -> action/component/page/service/validation/model/constants |
| 3 | Extract business flows: actor, intent, visible action, object, result. | business flow map, not class inventory |
| 4 | It generates `OUTPUT_ATLAS_HTML` using `assets/business-test-atlas-template.html` and `SHARED_REPORT_REFERENCE_CSS` . | Collapsible HTML, minimalist graphs, auditable score cards |
| 5 | runs `scripts/validate-atlas` or the equivalent atlas contracts. | Reopenable Extent report |
| 6 | It only fixes the atlas/output, not the analyzed sources. | `MUST_NOT_MODIFY_ANALYZED_FILES=true` |

<!-- /table -->

#### Progressive disclosure

| Level | What does the agent charge? | When |
| --- | --- | --- |
| Metadata | `name` + `description` | always, for the trigger |
| `SKILL.md` | short workflow, parameters, output contract | when the skill is activated |
| `references/` | this detailed HTML-req and the matrix of contracts | when the agent builds or repairs the atlas |
| `assets/` | HTML templates and visual primitives | when it produces the output |
| `scripts/` | deterministic validation | after generation or repair |

<!-- /table -->

#### The parameters of the skill

| Parameter | Skill-ready description | Anti-drift |
| --- | --- | --- |
| `INPUT_TEST_PACKAGE_ROOT` | folder / package explicitly received for analysis | it is not assumed `com.ai.ui.tests` decat ca default ATF |
| `OUTPUT_ATLAS_HTML` | the HTML file produced, usually `business-test-atlas.html` | not to be confused with final run report |
| `SHARED_REPORT_REFERENCE_CSS` | Reused common CSS | local overrides are reported |
| `LIVING_ARCHITECTURE_REFERENCE` | layers document for interpreting test/action/page/validation/model | does not become the source of new business |
| `ATLAS_TEST_EXTENT` | the atlas test report after validation | the numbers in the atlas must be synchronized |
| `MUST_PRODUCE` | `business-test-atlas.html` | mandatory output |
| `MUST_NOT_MODIFY_ANALYZED_FILES` | `true` | the skill is analysis-only |

<!-- /table -->

#### Contract tests for skill migration

| Atlas test | What validates | Why does it matter? |
| --- | --- | --- |
| atlas.candidate.skill-migration.readiness | The HTML req documents `SKILL.md`, frontmatter, description, progressive disclosure, `references/`, `assets/`, `scripts/` and validation | makes the migration to the skill almost mechanical |
| atlas.candidate.skill-parameters.generic | input/output/CSS/architecture/audit/read-only are explicit parameters, not paths enclosed in a single `ATF_NAME` | keep the skill applicable on any test package |

<!-- /table -->

The ideal skill is a cartographer: it shows what business flows exist in the already written tests and what limits of understanding remain.
</details>
</details>
