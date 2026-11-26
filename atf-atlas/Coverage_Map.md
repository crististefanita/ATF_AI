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
