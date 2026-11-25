# Reporting contract tests

## Overview

These [atf-tests](../../../../../../../../README.md) checks protect the Core
reporting contracts and [reporting implementation](../../../../../../../../../atf-reporting/README.md):
lifecycle, steps, Extent rendering, Allure results, attachments, runtime audit
and aggregate publication.

## Test ownership

| Area | Example |
|---|---|
| Step delegation and state | [StepLoggerContractTest](regression/StepLoggerContractTest.java) |
| Suite/session isolation | [ReportingManagerSessionContractTest](regression/ReportingManagerSessionContractTest.java) |
| Lifecycle failure preservation | [TestListenerFailureContractTest](lifecycle/TestListenerFailureContractTest.java) |
| Vendor publication | [adapters](adapters) |
| Aggregate counts, timing and publication | [aggregate](aggregate) |
| Real-browser attachments | [screenshot](screenshot) |

Keep expected failure fixtures explicit and preserve the original test outcome
when reporting diagnostics fail. A focused regression should protect observable
behavior rather than duplicate implementation tokens.

## Execution

Run from an isolated reactor copy:

<details>
<summary>Run Execution commands</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-reporting.xml" test
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-allure.xml" "-Dreporting.engine=allure" test
mvn -B -ntp -Paggregate test
```

</details>

Browser rendering and screenshot checks use the explicit browser profile after
preparation. [Testing](../../../../../../../../../docs/Testing.md) owns setup and artifact
acceptance. Inspect generated reports as well as TestNG results, and preserve
each run separately.
