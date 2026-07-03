<!-- Generated from ../html_EN/mcp/generated-test-code-review-reference.html. Keep source of truth in html_EN. -->
<!-- Source stylesheet: [shared-report-reference.css](../assets/shared-report-reference.css) -->

# Generated Test Code Review for classes created by agents

Skill analysis-only for code quality review on generated tests. Receive a folder of tests, reads the source code and the layers used, then produces an HTML report that explains whether the tests are coherent, business-grouped, architecturally clean and easy to reorganize.

`CODE REVIEW` `GENERATED TESTS` `REFACTOR PLAN` `READ ONLY` `UI/API ARCHITECTURE`

<details>
<summary>Overview</summary>

- The skill reviews the code of the generated tests, it does not validate the MCP package.
- The tests are evaluated as design and maintenance, not as final business proof.
- Existing atlases can guide business grouping, but they are no substitute for reading the code.
- The output is an HTML review report, not an automatic patch.

| Category | Scope |
| --- | --- |
| Input | `tests folder` `Java package` `touched layers` |
| Output | `generated-test-code-review.html` `score cards` `plan refactor` |
| Focus | `class cohesion` `DUB` `layering` `naming` |
| Do not | `do not modify tests` `does not generate tests` `not decide html-ex` |
| Do not confuse | `run count` `MCP pass` `business atlas` `code quality` |
| It can be used | `before the refactor` `after an MCP run` `before promoting html-ex` |

<!-- /table -->
</details>

<details>
<summary>1. Contract</summary>

<details>
<summary>1.1 Contract in 10 seconds</summary>

| Question | Mandatory answer | Anti-drift |
| --- | --- | --- |
| What does it receive? | `INPUT_TEST_SOURCE_ROOT`, the generated tests folder. | it does not start from the final report or XML |
| What are you reading? | test classes and invoked Java layers: action, component, page, service, validation, model, constants. | it doesn't stop at file names |
| What do you judge? | test code quality: cohesion, accountability, duplication, naming, layering, refactor safety. | do not judge the product as a complete business |
| What does it produce? | `generated-test-code-review.html` with reorganization recommendations. | does not produce automatic patch |

<!-- /table -->

<!-- diagram-readable-table -->

```text
Generated tests -> input source root -> Trace Architecture -> test -> layers -> Quality review -> cohesion / duplication -> Map refactor -> safe / risky / blocked
```
</details>

<details>
<summary>1.2 Canonical parameters</summary>

| Parameter | Example | Role |
| --- | --- | --- |
| `INPUT_TEST_SOURCE_ROOT` | `src/test/java/com/ai/ui/tests` or `src/test/java/com/ai/api/tests` | the root of the analyzed tests |
| `INPUT_TEST_PACKAGE` | `com.ai.ui.tests` / `com.ai.api.tests` | namespace for mapping |
| `INPUT_MAIN_SUPPORT_ROOTS` | `src/main/java/com/ai/ui/support` / `src/main/java/com/ai/api/support` | support allowed for generated tests |
| `PROTECTED_FRAMEWORK_ROOTS` | `core`, `mcp`, `atlas`, `ui/core`, `ui/engine`, `api/core` | surfaces that do not change in the generated package |
| `LIVING_ARCHITECTURE_REFERENCE` | `docs/Living_Architecture_UI_API_doc_v1_0.html` | the UI/API layering law |
| `OPTIONAL_BUSINESS_ATLAS` | `docs/atlas/<ATF_NAME>_business-test-atlas.html` | the business map is used as a guide, not as a single source |
| `OUTPUT_CODE_REVIEW_HTML` | `generated-test-code-review.html` | the ratio produced by the skill |
| `MUST_NOT_MODIFY_ANALYZED_FILES` | `true` | the analysis is read-only |

<!-- /table -->
</details>

<details>
<summary>1.3 Boundaries</summary>

| He is allowed | He is not allowed |
| --- | --- |
| to propose reorganization of packages and classes | to move files automatically |
| to indicate duplications and missing helpers | to rewrite the tests without approval |
| to use Atlas as a business map | to copy conclusions without reading the source |
| to mark risks of refactoring | to declare MCP pass/fail |

<!-- /table -->
</details>
</details>

<details>
<summary>2. Analysis model</summary>

<details>
<summary>2.1 Axes of review</summary>

| Axis | Cold question | What catches |
| --- | --- | --- |
| Class cohesion | Does the class test a single coherent flow/object? | "massive" classes |
| Business grouping | Are the tests grouped by business object, not by artificial run? | run classes difficult to reuse |
| Layer disciplines | Does the test orchestrate, and are the business assertions in validation? | assert direct, engine direct, selectors direct |
| Duplication | Are there steps copied between classes that require helper/action? | drift between runs |
| Naming | Does the name say actor, intention, object, result? | procedural or generic names |
| Safety refactor | Does the reorganization keep the proof of business? | dangerous refactor without a trace |

<!-- /table -->
</details>

<details>
<summary>2.2 The reading chain</summary>

<!-- diagram-readable-table -->

```text
Test method -> entrypoint -> Action / service -> business action -> Page / component -> surface access -> Validation -> business assertion -> Review -> quality + refactor
```
</details>

<details>
<summary>2.3 Recommended Scores</summary>

| Card | Measure | Ceiling signal |
| --- | --- | --- |
| Class Cohesion | classes with a single purpose and coherent flow | classes with mixed objects |
| Layer Disciplines | test -> action/service -> page/component -> validation | engine/selectors/assert directly in the test |
| Duplication Control | common steps extracted in correct helpers | copying between run classes |
| Business Grouping | organization by object/real flow | organization according to "run_01/run_02" without sense of domain |
| Refactor Safety | recommendations with risk and evidence | proposed moves without mapping to tests/methods |
| Overall | the quality of the review as a reorganization tool | high score without concrete candidate classes |

<!-- /table -->
</details>
</details>

<details>
<summary>3. Expected HTML output</summary>

<details>
<summary>3.1 Structure of the report</summary>

| Section | Default | Mandatory content |
| --- | --- | --- |
| Score Cards | open | Class Cohesion, Layer Discipline, Duplication Control, Business Grouping, Refactor Safety, Overall |
| Executive Context | closed | scope, input, cold verdict, what doesn't change |
| Architecture Review Graph | open | map test package -> layer usage -> review findings |
| Class Cohesion Matrix | closed | good classes, too mixed classes, break/unit classes |
| Duplication Map | closed | repeated steps, recommended helpers, risk of drift |
| Audit Discipline Layer | closed | direct engine, direct selectors, direct assertions, missing validation |
| Refactor Plan | closed | safe, medium-risk, do-not-touch-yet |
| Suggested Package Shape | closed | proposed packages/classes, no automatic patch |

<!-- /table -->
</details>

<details>
<summary>3.2 Mandatory diagrams</summary>

| Chart | What does he explain? | Anti-pattern caught |
| --- | --- | --- |
| Architecture Review Graph | how the tests touch the layers | the tests bypass the framework |
| Class Responsibility Map | the responsibility of each class | bucket classes |
| Duplication Heatmap | where the steps are repeated | copied runs |
| Safety Ladder Refactor | what can be moved safely | blind refactor |

<!-- /table -->
</details>

<details>
<summary>3.3 Visual style</summary>

- The report must reuse `docs/shared-report-reference.css` .
- He is not allowed to invent a completely inline visual system.
- Diagrams must be minimalistic: simple nodes, few colors, collapsible legend.
- Score cards must be short; long descriptions move to collapsed sections.
</details>
</details>

<details>
<summary>4. Contract rules for future tests</summary>

<details>
<summary>4.1 Proposed contract tests</summary>

| Rule id | Severity | What validates |
| --- | --- | --- |
| `code-review.output.exists` | BLOCKER | the HTML review report exists |
| `code-review.read-only.contract` | BLOCKER | the review declares that it does not modify the analyzed files |
| `code-review.score-cards.present` | MAJOR | canonical score cards appear above |
| `code-review.class-cohesion.matrix` | MAJOR | classes are classified by cohesion |
| `code-review.layer-discipline.audit` | BLOCKER | the report directly checks the engine/selectors/assertions in the test layer |
| `code-review.duplication.map` | MAJOR | rehearsals and helper opportunities are visible |
| `code-review.refactor-plan.risk-ranked` | MAJOR | refactor the plan has safe / medium-risk / blocked |
| `code-review.no-business-hardcode` | MAJOR | the tests do not depend on business objects specific to a single application |
| `code-review.architecture-reference.linked` | MAJOR | the report links the review to Living Architecture |

<!-- /table -->
</details>

<details>
<summary>4.2 Standardized diagnosis</summary>

| Block | What should he say? |
| --- | --- |
| Expected contract review | what quality rule is checked |
| Observed source result | concrete classes/methods/files observed |
| Cold verdict | PASS / WARN / FAIL without cosmetic courtesy |
| Repair hint | move, split, helper or check step recommended |

<!-- /table -->
</details>
</details>

<details>
<summary>5. Recommended reorganization for generated tests</summary>

<details>
<summary>5.1 Target shape</summary>

```text
src/test/java/com/ai/ui/tests
  account | checkout | support | admin | catalog | common
    <BusinessObject><LifecycleOrBoundary>Tests

src/test/java/com/ai/api/tests
  users | posts | comments | auth | common
    <Resource><ContractOrLifecycle>Tests

src/main/java/com/ai/ui/support
  flows        // reusable UI workflow services, no assertions hidden in core
  pages        // UI page/screen surface
  validation   // business-visible UI assertions
  model        // UI data/state records
  factory      // UI test data builders/factories
  utils        // low-level UI helpers only when reusable

src/main/java/com/ai/api/support
  client       // HTTP mechanics outside api.core
  service      // API resource service
  steps        // reusable API flow steps
  validation   // response/schema/business assertions
  model        // request/response records
  factory      // payload builders/factories
  utils        // serialization/wait/data utilities

src/main/java/com/ai/ui/core
src/main/java/com/ai/api/core
  protected framework contracts; generated packages must not modify these roots
```

Names are examples of form, not necessarily business. The rule is: packages are organized according to flows and real objects, not by run IDs or by MCP generation history. Tests stay in `src/test`; reusable support stays in `src/main`; framework core does not become a home for package helpers.
</details>

<details>
<summary>5.1.1 Protected surfaces vs. permitted support</summary>

| Area | Rule | Review verdict |
| --- | --- | --- |
| `com.ai.core` | ATF core protected | FAIL if the generated package modifies it without a separately reported blocker framework |
| `com.ai.skills.mcp` | MCP protected validator | FAIL if the agent adjusts the validator to pass its own packet |
| `com.ai.skills.atlas` | Atlas protected | FAIL if it is changed for a single business context |
| `com.ai.ui.core` / `ui.engine` | Protected runtime UI | FAIL if test workarounds appear in the engine/core |
| `com.ai.api.core` | Protected runtime API | FAIL if local business rules appear in API core |
| `ui/pages\|service\|validation\|model\|factory` | UI support allowed | PASS/WARN after cohesion, naming and reuse |
| `api/client\|service\|steps\|validation\|model\|factory` | API support allowed | PASS/WARN according to contract, payload and clarity scheme |

<!-- /table -->
</details>

<details>
<summary>5.1.2 Surgical examples of split</summary>

| Bad package split | Good package split | The refactor rule |
| --- | --- | --- |
| `ui.tests.Run03SupportAndProfileTests` <br> support ticket, profile edit, notification toggle in the same class | `ui.tests.support.SupportTicketLifecycleTests` <br> `ui.tests.profile.ProfilePreferencesTests` | break by business object; common setup goes in `ui.service` or `ui.factory`, validations in `ui.validation` |
| `ui.tests.AgenticMcpUIGeneratedTests` <br> 5 methods with different flows and the same copied clicks | `ui.tests.checkout.CheckoutBoundaryTests` <br> `ui.tests.catalog.CatalogSearchStateTests` | test class = coherent family; repeated steps become a workflow service, not a helper `ui.core` or `ui.engine` |
| `api.tests.MixedResourceContractTests` <br> auth, users, posts and comments in one file | `api.tests.auth.AuthSessionContractTests` <br> `api.tests.users.UserProfileContractTests` <br> `api.tests.comments.CommentModerationContractTests` | group by resource/contract; payload builders in `api.factory`, schema/assertions in `api.validation`, without local rules in `api.core` |

<!-- /table -->

If the split requires a change in core, mcp, atlas, ui.core, api.core or ui.engine, the review does not propose a local patch. Mark a framework blocker separately and keep the reorganization in the generated zones: src/test plus allowed support from src/main.
</details>

<details>
<summary>5.2 Safety ladder refactor</summary>

| Level | Allowed without high risk | Demands attention |
| --- | --- | --- |
| safe | rename classes/methods, extract constants, helper for data builders | keep TestNG method count and reporting |
| Medium | move classes between packages, split large classes | updates XML, imports, package names and report mapping |
| High | change flow, move assertions, change service/actions | rerun UI proof and MCP after the change |
| Framework blocker | any real need for `core`, `MCP`, `atlas`, `ui.core`, `api.core`, `ui.engine` | does not change locally; it is reported separately as feedback framework |
| Blocked | deletion of evidence without replacement | it cannot be done without a new run/proof |

<!-- /table -->
</details>
</details>

<details>
<summary>6. Good and bad examples</summary>

<details>
<summary>6.1 Good</summary>

- A class has only one central business object.
- The test describes actor, intention, object and result.
- UI actions are delegated to actions/services.
- Business validations are in the validation layer.
- Repetitions are extracted in clear fixtures/helpers.
- New UI/API support resides in the appropriate main/support packages, not in protected core.
</details>

<details>
<summary>6.2 Bad</summary>

- The class combines invoice, support, profile and admin only because they were generated in the same run.
- The test directly uses engine/selectors for business assertions.
- The methods are called `run03Test2` or `validatesScenario` .
- Each run copies the same 40 setup lines.
- Agent modifies MCP, ATF core, atlas, `ui.core`, `api.core` or `ui.engine` to make the package pass.
- The refactor plan says "clean up tests" without concrete classes/methods.
</details>
</details>
