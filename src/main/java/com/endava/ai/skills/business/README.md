# Business Understanding Kernel

## Purpose

`com.endava.ai.skills.business` is the shared semantic kernel for business
understanding. It defines the input-agnostic pipeline and vocabulary used by MCP
and Atlas when they reason about business objects, claims, source traces, proof
chains, score truth and understanding limits.

This skill has been surfaced out of Core. Any remaining references to old
Core-hosted business-skill packages are compatibility debt and should be
migrated through contracts and tests, not reintroduced in new code.

The core contract is an input-agnostic semantic pipeline.

Different consumers may start from different evidence:

- MCP may start from git diffs, run state, Extent/XML artifacts and `analysis/langgraph-business-understanding.html`.
- Atlas may start from the current Java source tree, generated Atlas HTML, graph proof strips and source-to-business trace tables.

The semantic path must stay the same: normalize input, map support layers, identify business objects, trace claims, map proof chains, score understanding and publish limits.

## Entry Points

- `BusinessUnderstandingKernel`: canonical facade for the shared semantic pipeline.
- `BusinessUnderstandingInputType`: supported evidence entry shapes.
- `BusinessUnderstandingStage`: ordered semantic steps every consumer should follow.
- `BusinessUnderstandingConcept`: shared vocabulary for MCP and Atlas.
- `BusinessUnderstandingArtifactParser`: neutral facade for source/diff/Extent/final-report/Atlas artifact reduction without deciding MCP or Atlas gates.
- `BusinessHtmlArtifactReducer`: package-private HTML extraction for visible text, sections, links and score values.
- `BusinessConceptDetector`: package-private concept detection backed by `skills/business/contracts/semantic-lexicon.yml`.
- `BusinessUnderstandingArtifactSummary`: shared summary model produced by artifact reduction.
- `BusinessObject`, `BusinessClaim`, `BusinessFlowStory`, `BusinessGraphConstruction`, `BusinessProofChain`, `BusinessProofStripRow`, `BusinessSourceTraceRow`, `BusinessScoreRationale`: small neutral value objects for source/report/proof mapping.
- `BusinessMaturity`: shared proof-depth vocabulary: `owned`, `partial`, `diagnostic`, `surface-only`, `blocked`.

## Class Reading Contract

When an agent reads a test class or report row, it should reduce it to these semantic shapes before writing Atlas or MCP conclusions:

1. `BusinessFlowStory`: actor -> intent -> visible action -> business object -> observable result.
2. `BusinessGraphConstruction`: business anchor -> actor intent -> primary owned object -> labeled relationships -> frontier -> proof boundary.
3. `BusinessSourceTraceRow`: class/method -> business object -> layer path -> maturity -> comprehension limit.
4. `BusinessProofStripRow`: graph node/flow -> test method -> layer path -> business evidence -> limit.
5. `BusinessScoreRationale`: score axis -> business goal -> flow question -> source/layer evidence -> score -> cap rationale.

## Business Graph Construction Contract

The shared rubric is deliberately procedural because this was the difference between a weak run/proof chain and a strong business map:

1. Start from the business object born, changed or protected by the flow.
2. Choose one primary owned object and make it visually dominant.
3. Add supporting domain objects only when they explain actor intent or object lifecycle.
4. Label relationships with business verbs such as owns, anchors, captures, submits, confirms or awaits decision.
5. Mark the not-yet-owned frontier as dashed, capped or blocked.
6. Move run IDs, TestNG, XML, Extent, MCP, diff and class inventory evidence to Proof Mapping, Run Audit, Source Trace or proof strips.

MCP may block broken graph mechanics. Atlas may judge business maturity. Reporting may render visual hierarchy. This kernel owns the semantic recipe.

Reporting now exposes `com.endava.ai.core.reporting.graph.BusinessGraphLayoutAdapter`
as the neutral layout adapter for this recipe. The dependency direction matters:
Reporting may consume `BusinessGraphConstruction`; Business Understanding must
not consume Reporting.

This is the reusable abstraction behind older Atlas reports such as `framework-under-test_business-test-atlas.html`: the report may render graphs and tables, but the shared skill owns the semantic grammar.

## Reading Path

1. Start here to decide whether the change is shared business meaning used by both MCP and Atlas.
2. Check `src/main/resources/skills/business/contracts/semantic-pipeline.yml` before changing stages or vocabulary.
3. Run `src/test/resources/skills/business/testng-business-understanding-sanity.xml` before trusting shared semantic changes.

## Package Promotion Rule

Business Understanding is a real skill, not Core internals. New public semantic
models, parsers or taxonomy types belong in `com.endava.ai.skills.business`.
Keep any old-package compatibility only as a temporary bridge for MCP, Atlas and
Reporting consumers.

## Calibration Rules

`skills/business/contracts/semantic-calibration.yml` captures the lessons from the strongest historical Atlas reports:

- Business objects are domain outcomes or durable observable records; technical classes stay in source/layer evidence.
- Local proof comes before global summary: graph claims need nearby proof-strip rows.
- Maturity gates high scores: 95+ needs mostly owned evidence and explicit limits.
- Business-understanding score is separate from MCP close-out, html-ex promotion and package readiness.
- Repeated corridors count only when repeated source methods prove the same object or lifecycle slice.

`skills/business/contracts/consumer-bridge.yml` declares the two current consumers:

- MCP consumes the kernel for `analysis/langgraph-business-understanding.html` meaning after package artifacts are frozen.
- Atlas consumes the kernel for source-backed graph, proof-strip, trace and score-truth validation.

The bridge is a contract, not an import path: MCP and Atlas must continue to consume shared semantics without depending on each other.

## Change Here When

- MCP and Atlas both need the same business-understanding vocabulary.
- A semantic step is reusable regardless of whether the input is git diff, Extent evidence, source structure or a final report.
- A concept belongs to business meaning, not report rendering and not package publishability.

## Do Not

- Do not decide MCP package publishability here.
- Do not own Atlas generated artifacts or Atlas rule matrices here.
- Do not add reporting shell, CSS, Extent or browser-render rules here.
- Do not import MCP, Atlas or Core Reporting from this package.

## Verification

- Run `src/test/resources/skills/business/testng-business-understanding-sanity.xml`.
- Run `BusinessUnderstandingKernelContractTest` after changing the semantic pipeline or package shape.
- Run `BusinessUnderstandingArtifactParserContractTest` after changing artifact reduction behavior.
- Keep `skills/business/contracts/package-shape.yml` aligned with package size and boundary decisions.
- Keep `skills/business/contracts/semantic-model.yml` aligned with any model added here.
- Keep `skills/business/contracts/semantic-calibration.yml` aligned with the interpretation rules used by Atlas and MCP.
- Keep `skills/business/contracts/semantic-lexicon.yml` aligned with concept detection tokens.
- Keep `skills/business/contracts/consumer-bridge.yml` aligned with the MCP and Atlas consumer tests.
