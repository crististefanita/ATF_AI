# Framework testing

## Run location and scope

Run every Maven command below from the reactor root of a disposable validation
copy, following [Development](Development.md). Do not build in the development
or live checkout. Keep Maven/browser caches and each run's evidence outside
those source checkouts.

## Choose the proof boundary

| Change or question | Existing selection | What to inspect |
|---|---|---|
| A library's own contract | Native module checks with its dependencies | Actual selected classes and assertions |
| Cross-module behavior or governance | Default local suite | Observable local outcomes and useful negative fixtures |
| Real browser actions, responses or screenshots | Browser profile | Both selected adapters, cleanup and complete artifacts |
| Extent/Allure publication | Aggregate profile and the native Allure subset | Executed outcomes plus generated report data/rendering |
| An application journey | Explicit concrete test selection | Intended deployment, identities, persisted state and cleanup |

Use the smallest selection that exposes the changed boundary, then run the
relevant complete suite before closure. A source guard proves its supported
inspection rule; local fixtures and browser pages do not establish controlled
application acceptance.

The current maintenance scope retains the existing E2E inventory. Improve
determinism, assertions, documentation and governance of existing checks;
do not add application journeys during this phase. Use desktop browsers for
browser checks. Emulators are outside the requested execution scope.

Select original native test proof and source/verification receipts for the
checkout's ignored artifacts/reports/current tree after external validation.
Its README/index describe actual selections, captured revisions and verdicts.
The explicit retained-UI role can also publish one original audited Extent
reader per browser engine with its native XML, executed-source manifest and
audit proof. Other full report sites remain external.
Complete runs and failures remain in external history. Publish one selected
Atlas HTML per project under artifacts/atlas/current and run the
[dedicated consistency check](../artifacts/atlas/README.md#verification) before
claiming that generated documentation matches captured live source and evidence.
Use the [publication layout check](../artifacts/README.md#verification) before
replacing the current selection. A retained capture is not automatically the
latest HEAD, and publication does not execute its original tests again.

## Local baseline

```powershell
mvn -B -ntp test
```

The root command runs native library checks and the
[framework-local suite](../atf-tests/src/test/resources/suites/framework-local.xml)
in atf-tests, including governance. Local regressions use fixtures and doubles
for configuration, API execution, UI delegation and reporting. They exclude
browser-dependent cases, intentional failure fixtures and live application
journeys. A successful compile or a skipped suite is not execution evidence.

## Selective checks

Use `-pl atf-tests -am` to build the test module and its library dependencies.
Suite paths below are relative to the atf-tests module:

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-ui.xml" test
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-api.xml" test
mvn -B -ntp -pl atf-tests -am "-Dtest=InvoiceEvidenceContractTest,RegistrationSubmissionContractTest,ConfigManagerFileContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

The class-filter flag lets upstream modules continue when the requested class
exists only in atf-tests. Check that the selected downstream tests actually ran.
Use the [API](../atf-tests/src/test/resources/suites/framework-api.xml),
[UI](../atf-tests/src/test/resources/suites/framework-ui.xml) or
[reporting](../atf-tests/src/test/resources/suites/framework-reporting.xml) suite
for a maintained subset. These subsets do not replace the complete default
governance run.

The API subset selects BaseTestAPIContractTest and
ApiCoreExecutionArchitectureContractTest. It checks framework setup, write
readiness, retry budgets, call ordering and failure propagation with local
fixtures and doubles. It does not call the GoRest service. The existing
[PositiveUserTests](../atf-tests/src/test/java/com/ai/api/tests/PositiveUserTests.java)
contains two opt-in GoRest write journeys; both require auth.token. An empty
token causes the shared setup to skip them, which is not passing API application
acceptance. Record these two execution scopes separately.

A library's native checks can be selected with its module and dependencies,
for example `mvn -B -ntp -pl atf-core -am test`. That selection does not run
the cross-module regressions in atf-tests.

## Browser preparation and checks

Use the portable work-root/cache variables from
[Development](Development.md#working-roles), then enter the validation copy's
reactor root. Keep browser acquisition separate from acceptance evidence.

### Playwright runtime

Install the Chromium revision selected by the pinned Playwright Java SDK.
The SDK uses its bundled Node runtime. Prepare local Maven artifacts before
invoking the adapter's CLI:

```powershell
$env:PLAYWRIGHT_BROWSERS_PATH = Join-Path $workRoot 'cache/playwright'
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-playwright -am "-DskipTests" install
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -f atf-playwright/pom.xml org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = '1'
```

The preparation command skips execution and is not acceptance evidence.
The Playwright skip flag affects bootstrap downloads, not browser actions.

### Selenium pinned Manager route

Use the versions selected by the [parent POM](../pom.xml) and browser contract:

| Component | Selected version |
|---|---|
| Selenium Java | 4.54.0 |
| Chrome for Testing | 158.0.8127.88 |
| ChromeDriver | 158.0.8127.88 |
| Required typed CDP | 158 |

The [Selenium release](https://www.selenium.dev/downloads/) supplies the Java SDK;
the [Chrome for Testing manifest](https://googlechromelabs.github.io/chrome-for-testing/158.0.8127.88.json)
supplies platform-specific browser/driver downloads. Obtain the exact Chrome
binary for the host OS and set the caller-provided ATF_CHROME_BINARY to that
existing executable. The following setup resolves that input without assuming
a workstation path or operating-system cache layout.

The existing factory gives an explicit webdriver.chrome.driver or an ATF-cached
driver priority over Selenium Manager. Leave that JVM property unset, including
in inherited JVM options, and use an empty or nonexistent ATF fallback cache.
The separate Selenium Manager cache holds its downloaded assets. Its configuration
must agree with these pins; a conflicting se-config.toml can override environment
settings, as described in [Selenium Manager configuration](https://www.selenium.dev/documentation/selenium_manager/#configuration).

```powershell
$seleniumCache = Join-Path $workRoot 'cache/selenium'
$fallbackCache = Join-Path $workRoot 'cache/selenium-fallback-empty'
if (Test-Path -LiteralPath $fallbackCache) {
    if (@(Get-ChildItem -LiteralPath $fallbackCache -Force).Count -gt 0) {
        throw 'Use an empty ATF fallback cache to exercise Selenium Manager.'
    }
}
$env:SE_BROWSER_PATH = (Resolve-Path -LiteralPath $env:ATF_CHROME_BINARY -ErrorAction Stop).Path
$env:SE_DRIVER_VERSION = '158.0.8127.88'
$env:SE_CACHE_PATH = $seleniumCache
$env:SE_SKIP_DRIVER_IN_PATH = 'true'
$env:SE_SKIP_BROWSER_IN_PATH = 'true'
$env:SE_AVOID_STATS = 'true'
Remove-Item Env:SE_OFFLINE -ErrorAction SilentlyContinue
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-tests -am "-Datf.selenium.cache.root=$fallbackCache" "-Dtest=SeleniumPlaywrightLocatorParityContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

That existing parity class runs both adapters; ui.engine does not reduce its data
providers to one engine. With downloads permitted, the invocation warms the real
Manager route and executes local locator/network contracts. Retain the actual
browser, driver, selected binary and typed CDP evidence. It does not establish
an application journey or replace the complete browser selection.

### Complete browser selection

After Maven dependencies and both browser runtimes are prepared, retain the same
environment and fallback-cache argument for offline acceptance:

```powershell
$env:SE_OFFLINE = 'true'
mvn -B -ntp -o "-Dmaven.repo.local=$mavenCache" "-Datf.selenium.cache.root=$fallbackCache" -Pbrowser test
```

SE_OFFLINE controls Selenium Manager network access; Maven's `-o` controls Maven
artifact resolution. Enable them only after the required assets are cached.
The browser profile selects
[framework-browser.xml](../atf-tests/src/test/resources/suites/framework-browser.xml)
only in atf-tests; native library checks remain part of the reactor run.
The suite uses local pages to cover engine parity, screenshots, window/zoom
behavior and reporting interactions.

Preserve complete screenshots when inspecting browser evidence. A version change
requires a fresh matrix check and browser run, with results recorded in feedback.

## Reporting acceptance

The native Allure subset and aggregate publication are explicit:

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-allure.xml" "-Dreporting.engine=allure" test
mvn -B -ntp -Paggregate test
```

The [Allure suite](../atf-tests/src/test/resources/suites/framework-allure.xml)
checks the native integration. The
[aggregate suite](../atf-tests/src/test/resources/suites/framework-aggregate.xml)
collects executed cases and publishes both Extent and Allure evidence.
Inspect the generated artifacts as well as the TestNG outcome: verify counts,
statuses, timing, attachments and declared output paths.

## Application journeys

Product support and opt-in application classes live in atf-tests. Configure
`base.url` and `base.url.api` for the intended environment. API writes require
an explicit `auth.token`. Select a concrete class with `-Dtest` and the same
upstream no-match flag shown above.

These journeys can create users, orders or invoices. A missing credential,
unavailable service or unexecuted journey does not establish application
acceptance. The active acceptance gaps remain in
[feedback](feedback/README.md).

## Retained product UI audit

The existing registration, authenticated-account and profile checks use the
same test methods for both providers. They are distinct from framework-ui and
framework-browser, which select framework contracts and browser fixtures.
Neither maintained framework suite silently opts into the public application.

| Existing owner | Methods | Observed contract |
|---|---:|---|
| [RegistrationTests](../atf-tests/src/test/java/com/ai/atf/ui/RegistrationTests.java) | 6 | Successful registration and required-field, email, duplicate, password and phone rejection |
| [AccountAuthenticatedAreaTests](../atf-tests/src/test/java/com/ai/ui/tests/AccountAuthenticatedAreaTests.java) | 5 | Authenticated account, profile, favorites, invoices and messages navigation |
| [ProfileContactAddressMemoryTests](../atf-tests/src/test/java/com/ai/ui/tests/ProfileContactAddressMemoryTests.java) | 5 | Registered contact and address values remain observable in the profile |

Use a separate disposable validation copy and output history for each command.
Prepare browser binaries using the sections above. The explicit URL below is
the configured testing demo; replace it with the intended deployment when
appropriate. These retained methods create synthetic customers. They do not
create invoices or change admin orders, and they do not delete their customers.
Browser teardown closes sessions; it is not application-data cleanup.

```powershell
mvn -B -ntp -pl atf-tests -am "-Dtest=RegistrationTests,AccountAuthenticatedAreaTests,ProfileContactAddressMemoryTests" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dui.engine=playwright" "-Dbase.url=https://practicesoftwaretesting.com" "-Dreporting.engine=extent" "-Dreports.timestamp.enabled=false" "-Dui.screenshots.enabled=true" "-Dui.screenshots.on.failure.only=false" "-Dui.screenshots.capture.final.state=true" test
mvn -B -ntp -pl atf-tests -am "-Dtest=RegistrationTests,AccountAuthenticatedAreaTests,ProfileContactAddressMemoryTests" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dui.engine=selenium" "-Dbase.url=https://practicesoftwaretesting.com" "-Dreporting.engine=extent" "-Dreports.timestamp.enabled=false" "-Dui.screenshots.enabled=true" "-Dui.screenshots.on.failure.only=false" "-Dui.screenshots.capture.final.state=true" test
```

For each engine, retain original Surefire XML, source hashes, command, URL,
browser identity and ExtentReport.html. Reconcile the exact class/method subjects
and statuses with the report nodes. An action PASS establishes action completion;
the separate validation step must state expected/actual observations before
business PASS. Preserve FAIL and SKIP outcomes and distinguish missing or
unreached validation from a successful negative scenario.

The [living principles](Living_Architecture_UI_API_doc_v1_0.md#retained-principles)
define the source responsibilities. The
[report publication contract](../artifacts/reports/README.md#retained-ui-report-publication)
defines the per-engine reader and proof boundary. Audit consistency does not
establish a test execution, application-data cleanup or exhaustive coverage.

```powershell
node --test tools/verify-ui-e2e-audit.test.mjs tools/collect-artifacts.test.mjs
```

## Explicit configuration

[ConfigManager](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java)
loads the classpath framework.properties resource by default. An explicit file
replaces that resource; it must provide the settings needed by the selected
consumer. Individual JVM properties still take precedence.

For a consumer-supplied file at config/local.properties in the validation copy,
or an existing file selected through ATF_CONFIG_FILE, resolve the path once at
the reactor root before Maven enters individual modules:

```powershell
$projectRoot = (Get-Location).Path
$configInput = if ($env:ATF_CONFIG_FILE) {
    $env:ATF_CONFIG_FILE
} else {
    Join-Path $projectRoot 'config/local.properties'
}
$configPath = (Resolve-Path -LiteralPath $configInput -ErrorAction Stop).Path
mvn -B -ntp "-Dframework.properties=$configPath" test
```

This file is an explicit consumer prerequisite, not a bundled repository file.
Resolution fails before Maven if it does not exist. Passing the resolved path
avoids relying on each forked test process having the same working directory.

## Output and evidence

The [artifact archive](../artifacts/reports/README.md) owns report classification,
copy integrity and the explicit maintenance-tool checks. Run its Node selection
when changing the collector; Java governance checks do not execute that CLI.

The `atf.build.directory` override relocates Maven compile output for atf-tests
only. `surefire.reportsDirectory` selects that module's execution results.
Upstream library targets and runtime report destinations are configured
separately; these properties do not relocate every artifact in the reactor.

Under the default configuration, paths are relative to the validation copy:

| Output | Location |
|---|---|
| Surefire/TestNG results | Each tested module's target/surefire-reports |
| Cross-module results | atf-tests/target/surefire-reports |
| Extent reports | atf-tests/target/reports |
| Allure raw results | atf-tests/target/allure-results |
| Aggregate publication | atf-tests/target/atf-core-aggregate-report |

The aggregate directory retains its compatibility name. Runtime properties can
redirect report output. Preserve a fresh copy or unique result directories for
each acceptance run so a focused rerun cannot overwrite earlier XML.

Record the source checkpoint, exact command, selected suite, counts and external
artifact paths in feedback. Distinguish historical baseline evidence, newly
executed checks, skipped cases and unexecuted checks. A copied report or a
renamed directory is not a new run.
