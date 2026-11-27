# Capability coverage map

## How to read the map

Each row names a capability, its source/support, an existing oracle and the
maintained selection that reaches it. This is a curated reading map, not a
complete inventory or a claim of fresh execution. Consult
[Testing](../docs/Testing.md) for commands and [feedback](../docs/feedback/README.md)
for observed results and unresolved acceptance.

| Capability and observed outcome | Source or support | Existing oracle | Maintained selection and proof scope |
|---|---|---|---|
| Startup chooses classpath defaults or an explicit file; overrides remain deterministic | [ConfigManager](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java) | [ConfigManagerFileContractTest](../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java) | [Local suite](../atf-tests/src/test/resources/suites/framework-local.xml): local configuration contract; no application availability claim |
| UI consumers resolve installed providers through neutral contracts | [UiEngineRegistry](../atf-core/src/main/java/com/ai/ui/core/engine/UiEngineRegistry.java), [Playwright provider](../atf-playwright/src/main/java/com/ai/ui/core/engine/playwright/PlaywrightProvider.java), [Selenium provider](../atf-selenium/src/main/java/com/ai/ui/core/engine/selenium/SeleniumProvider.java) | [Core registry test](../atf-core/src/test/java/com/ai/ui/core/engine/UiEngineRegistryTest.java), [cross-module registry contract](../atf-tests/src/test/java/com/ai/atf/ui/component/registry/UiEngineRegistryContractTest.java) | Native Core checks plus [local suite](../atf-tests/src/test/resources/suites/framework-local.xml): discovery, absent/duplicate providers and delegation; discovery does not start a browser |
| API consumers require write credentials explicitly and retain API/runtime separation | [BaseTestAPI](../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java), [API client](../atf-core/src/main/java/com/ai/api/core/client/ApiClient.java) | [BaseTestAPIContractTest](../atf-tests/src/test/java/com/ai/atf/core/support/base/BaseTestAPIContractTest.java), [API execution architecture](../atf-tests/src/test/java/com/ai/atf/core/support/base/ApiCoreExecutionArchitectureContractTest.java) | [Local suite](../atf-tests/src/test/resources/suites/framework-local.xml): readiness and architecture contracts; does not establish successful writes against an external API |
| Browser consumers receive equivalent locator, zoom, window and screenshot behavior | [UI engine contract](../atf-core/src/main/java/com/ai/ui/core/engine/UIEngine.java) and the two provider implementations above | [Locator parity](../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java), [runtime parity](../atf-tests/src/test/java/com/ai/atf/core/reporting/screenshot/UiEngineConfiguredRuntimeParityContractTest.java) | [Browser suite](../atf-tests/src/test/resources/suites/framework-browser.xml): real browser sessions over controlled local fixtures; not product E2E acceptance or a complete browser/CDP matrix |
| Test outcomes, diagnostics and attachments retain lifecycle meaning | [Core listener facade](../atf-core/src/main/java/com/ai/core/reporting/listener/TestListener.java), [reporting listener](../atf-reporting/src/main/java/com/ai/core/reporting/listener/ReportingTestListener.java) | [Failure lifecycle](../atf-tests/src/test/java/com/ai/atf/core/reporting/lifecycle/TestListenerFailureContractTest.java), [Extent presentation](../atf-tests/src/test/java/com/ai/atf/core/reporting/adapters/ExtentAdapterTest.java) | [Local](../atf-tests/src/test/resources/suites/framework-local.xml), [Reporting](../atf-tests/src/test/resources/suites/framework-reporting.xml) and [browser](../atf-tests/src/test/resources/suites/framework-browser.xml) selections protect their explicit fixture scopes |
| Aggregate publication preserves observed report bytes and accurate manifest fields | [Aggregate writer](../atf-reporting/src/main/java/com/ai/core/reporting/aggregate/AggregateExtentReportWriter.java), [manifest publication fixture](../atf-tests/src/test/java/com/ai/atf/core/reporting/aggregate/publish/AtfCoreAggregatePublishedManifestContractTest.java) | The publication contract checks the explicit output directory, copied bytes and parsed manifest values | [Local](../atf-tests/src/test/resources/suites/framework-local.xml) and [aggregate](../atf-tests/src/test/resources/suites/framework-aggregate.xml) selections; distinguish publication fixtures from inspection of an actual generated aggregate |
| A customer's invoice lookup rejects absent, unrelated and prefix-only evidence | [Invoice actions](../atf-tests/src/test/java/com/ai/ui/support/actions/AccountInvoiceActions.java), [invoice evidence](../atf-tests/src/test/java/com/ai/ui/support/validation/InvoiceEvidence.java) | [InvoiceEvidenceContractTest](../atf-tests/src/test/java/com/ai/atf/ui/component/InvoiceEvidenceContractTest.java), [locator parity](../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java) | [Local](../atf-tests/src/test/resources/suites/framework-local.xml) engine doubles; [browser](../atf-tests/src/test/resources/suites/framework-browser.xml) selector fixtures; no controlled customer/admin acceptance |
| An admin status change must match the persisted value after reopening the order | [Admin actions](../atf-tests/src/test/java/com/ai/ui/support/actions/AdminOrderStatusActions.java), [status validation](../atf-tests/src/test/java/com/ai/ui/support/validation/AdminOrderStatusValidation.java) | [InvoiceEvidenceContractTest](../atf-tests/src/test/java/com/ai/atf/ui/component/InvoiceEvidenceContractTest.java) covers selected value, displayed label and unrelated-order failures | [Local suite](../atf-tests/src/test/resources/suites/framework-local.xml): engine-double state evidence; controlled application persistence still requires independent acceptance |
| Registration submits one identity and preserves the first form failure | [Registration service](../atf-tests/src/test/java/com/ai/ui/support/service/RegistrationService.java), [validation](../atf-tests/src/test/java/com/ai/ui/support/validation/RegistrationValidation.java) | [Submission contract](../atf-tests/src/test/java/com/ai/atf/ui/component/RegistrationSubmissionContractTest.java), [validation contract](../atf-tests/src/test/java/com/ai/atf/ui/component/RegistrationValidationContractTest.java) | [Local suite](../atf-tests/src/test/resources/suites/framework-local.xml): controlled doubles and values; it does not prove server-side account creation |

## Explicit application and environment boundaries

The existing [authenticated-area tests](../atf-tests/src/test/java/com/ai/ui/tests/AccountAuthenticatedAreaTests.java)
and [profile-memory tests](../atf-tests/src/test/java/com/ai/ui/tests/ProfileContactAddressMemoryTests.java)
are opt-in application selections. Their presence does not provide a complete
controlled invoice/admin journey or demonstrate that embedded assumptions and
account setup are suitable for a target environment.

The [feedback register](../docs/feedback/README.md) records framework governance
and F-008 verification for its selected desktop browser/driver/CDP matrix.
The current phase improves the framework and its
existing checks; it does not add E2E journeys. Local fixtures do not establish
application acceptance.

## Maintenance rule

When behavior, ownership or suite selection changes, reconcile the affected row
with the live source and oracle. Keep a missing oracle or unavailable environment
visible. Add an execution claim only to its evidence record after the named
selection actually runs; keep this map focused on current contracts and limits.

## Reviewed semantic boundaries

These source observations refine the existing reading map; they do not add a test flow.

| Boundary | Observed behavior | Remaining responsibility |
|---|---|---|
| [Configuration source](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java) | Caller overrides precede loaded settings; an absent explicitly selected source fails | Loaded properties are cached; file edits do not automatically reload them |
| [Write readiness](../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java) | Blank local access makes the write flow unavailable | Nonblank access does not establish remote authorization or a successful write |
| [Bounded waiting](../atf-core/src/main/java/com/ai/ui/support/wait/WaitUtils.java) | Existing checks compare first match, last unmatched observation at expiry and the assertion wrapper | A returned last observation is not acceptance; supplier failures and blocking suppliers remain separate |
| [Final image policy](../atf-core/src/main/java/com/ai/ui/core/reporting/ScreenshotManager.java) | Existing screenshot contracts distinguish success/failure eligibility, absent UI, deduplication and invocation ownership | Capture policy and payloads do not certify pixels; two tasks do not establish every interleaving |
| [Optional reporting](../atf-core/src/main/java/com/ai/core/reporting/spi/ReportingBackendLoader.java) | Existing contracts distinguish Core-only operation, one installed provider and duplicate-before-construction refusal | Discovery fixtures do not prove every deployment; an explicitly requested logger has its own requirement |
| [Browser acquisition](../atf-playwright/src/main/java/com/ai/ui/core/engine/playwright/PlaywrightBrowserSession.java) | The inspected Playwright session selects Chromium; Selenium uses its Chrome factory | Library capability does not establish every browser or operating-system combination |
| [Aggregate values](../atf-reporting/src/main/java/com/ai/core/reporting/aggregate/model/AggregateSummary.java) | Selected constructors reject negative counts; aggregation can derive counts from observed cases | The public summary constructor does not itself enforce all relationships among caller-supplied totals |
| [Reporting compatibility](../atf-reporting/src/main/java/com/ai/core/reporting/ReportingSession.java) | A compatibility mirror switch uses its existing interpretation policy | Do not claim that every reporting switch uses the strict shared boolean parser |

The [semantic profile](Report_Contract.md#semantic-review-and-compact-identity)
requires scope-specific observations and independent existing oracles. A REVIEWED
facet completes its declared scope; named remaining questions still apply.

Successive reviews preserve their original boundaries in the [refinement dossier](Report_Contract.md#partial-facet-refinement). Configuration checks distinguish SDK commands from actual window appearance; aggregation distinguishes calculated totals from manual values. Pixel, network and application-data cleanup unknowns remain visible.

## Successive facet review

The current outcome preserves original boundaries: 40 REVIEWED / 2 PARTIAL.
Completed review concerns its declared scope, not every possible behavior.

| Deepened dimension | Observation and boundary |
|---|---|
| Playwright configuration | Eight combinations inspect profile choice, viewport and cleanup; physical appearance is not certified |
| Selenium configuration | Four combinations inspect options and window commands; complete cache and operating-system states remain separate |
| Reporting information | Derived totals follow outcomes; manually supplied nonnegative fields can be nonconserving |
| Reporting configuration | Compatibility and caching have explicit policies; arbitrary destinations are not inferred as accepted |
| Test selection | The local catalog remains separate from opt-in selections; credentials and application state stay external |
| Atlas configuration | Mandatory policy and explicit inputs are checked; complete semantic recall is not claimed |
| Playwright information | Components preserve observed fields, bounded previews and image bytes; pixel meaning stays separate |
| Selenium information | Request identities and payloads are checked; live Chrome body retrieval and event ordering stay separate |

| Dimension retained PARTIAL | Reason and next evidence boundary |
|---|---|
| Application behavior | Local components explain validation; complete application persistence is not established |
| Application information | Flat resources and randomized values are explained; global uniqueness and data deletion are not guaranteed |

## Local evidence for retained boundaries

Both application facets remain PARTIAL. Existing checks now follow the complete fields from both factories to one rejected submission, generated credential shape and independent data copies. Rejection of stale status and an unrelated invoice checks the expected diagnostic, preventing another error from producing a false positive.

Remaining questions and missing evidence follow the [successive-review contract](Report_Contract.md#successive-application-boundaries). Application persistence, global uniqueness, general format parsing and application-data deletion are not inferred.

## Deeper project-understanding review

The waiting, final-image and optional-reporting rows expose important existing
decisions omitted from the earlier selected reader. Review concrete compared
state under the [current profiles](Report_Contract.md#current-source-bound-authoring-profiles);
do not infer breadth from captured files or increased flow counts.

Failure attachment hooks and final-state hooks have different propagation
policies. The inspected final-state dispatch is direct; healthy-handler tests
do not establish that arbitrary hook or logger failures are non-blocking. Keep
that source-only boundary separate from guarded failure-attachment components.

Cross-project review also distinguishes local implementation from selected
producer contracts. Consumer package checks cannot certify absent producer
source, and selected passing components cannot replace a failing whole-project
gate. The dated comparison and original outcomes belong to feedback and their
external review dossier, not an evergreen coverage claim.

## Reference presentation governance

[Presentation components](../tools/atlas-reference.test.mjs) protect actual object facets and oracles, maturity boundaries and explained scores. [Native calibration](../tools/atlas-calibration.mjs) checks the initial state before interactions and compares the map, local proof and rationale with the original analysis. These controls introduce no product behavior or E2E tests.
