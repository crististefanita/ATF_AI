# atf-core

## Overview

Core provides reusable UI and API execution contracts without Selenium,
Playwright, Extent or Allure SDK dependencies. Its coordinates are
`com.ai:atf-core:1.0.0`; it inherits Java 11 compilation from
[atf-parent](../pom.xml).

## Ownership and entry points

| Area | Entry point and responsibility |
|---|---|
| Configuration | [ConfigManager](src/main/java/com/ai/core/support/config/ConfigManager.java), typed options and classpath defaults |
| UI contracts | [UIEngine](src/main/java/com/ai/ui/core/engine/UIEngine.java), capabilities, locators and request/response values |
| UI composition | [UiEngineProvider](src/main/java/com/ai/ui/core/engine/UiEngineProvider.java), registry and factory |
| UI lifecycle | [BaseTestUI](src/main/java/com/ai/ui/core/runtime/BaseTestUI.java), session and runtime metadata |
| API runtime | [BaseTestAPI](src/main/java/com/ai/api/core/BaseTestAPI.java), HTTP execution, retries, polling and validation |
| Reporting contracts | [ReportLogger](src/main/java/com/ai/core/reporting/ReportLogger.java), TestStatus and [ReportingBackend SPI](src/main/java/com/ai/core/reporting/spi/ReportingBackend.java) |
| Steps | [StepLogger](src/main/java/com/ai/core/reporting/StepLogger.java), scopes, console output and neutral report delegation |

[Core support](src/main/java/com/ai/core/support/README.md) owns reusable
configuration, metadata, YAML and HTML primitives. Neutral UIActions remains in
Core; product pages, services, workflows and business assertions belong to
[atf-tests](../atf-tests/README.md).

## Dependencies and extension

Core has no dependency on another ATF module. Consumers add a browser adapter
for UI execution and atf-reporting for Extent/Allure publication.
Java ServiceLoader discovers UiEngineProvider and ReportingBackend
implementations. Public Core signatures remain independent of vendor SDK
types. The neutral [TestListener](src/main/java/com/ai/core/reporting/listener/TestListener.java)
compatibility facade forwards lifecycle events to the selected backend.

See [Architecture](../docs/Architecture.md) for provider composition and the
runtime sequence.

## Configuration

[framework.properties](src/main/resources/framework.properties) supplies defaults.
JVM properties override individual keys; `framework.properties` can select an
explicit file. Important defaults include `ui.engine=playwright`,
`ui.headless=true`, `ui.wait.timeout.seconds=20`,
`api.wait.timeout.seconds=10` and `reporting.engine=extent`.
Browser and report implementations are supplied by their modules.

## Verification

From an isolated reactor copy:

<details>
<summary>Run Verification commands</summary>

```powershell
mvn -B -ntp -pl atf-core -am test
mvn -B -ntp -pl atf-tests -am "-Dtest=ConfigManagerFileContractTest,BaseTestAPIContractTest,BaseTestUIContractTest,StepLoggerContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

</details>

The first command runs native Core checks; the second selects cross-module
regressions. [Testing](../docs/Testing.md) owns complete local, browser and
reporting acceptance.
