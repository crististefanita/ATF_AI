# Existing API test verification

## Scope and source

On 2026-11-24 the maintained framework API subset was executed from a fresh,
relocated validation copy. Its source checkpoint was
`4e80e40364dd1f59fe3507832bea7405e387dede`. The accompanying maintenance change
updates documentation and scope records; production code, test sources and
suite selections remain unchanged.

The current phase keeps the existing E2E inventory and prioritizes stability,
governance and clear English documentation. No additional E2E test, emulator
or public application mutation was used in this verification.

## Executed selection and result

Use the cache/work-root preparation in [Development](../Development.md), then
run from the disposable reactor root:

```powershell
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-api.xml" test
```

| Executed owner | Cases | Failures | Errors | Skipped | Proof boundary |
|---|---:|---:|---:|---:|---|
| BaseTestAPIContractTest | 8 | 0 | 0 | 0 | Base lifecycle, explicit write readiness, API/UI separation and wait policy |
| ApiCoreExecutionArchitectureContractTest | 16 | 0 | 0 | 0 | Retry success/exhaustion, non-retry statuses, exceptions, interruption, negative/zero budgets and facade ownership |
| Upstream native library checks | 12 | 0 | 0 | 0 | 7 Core, 1 Playwright provider, 2 Selenium provider and 2 reporting provider checks |
| Complete selected reactor | 36 | 0 | 0 | 0 | 24 framework API checks plus 12 native library checks |

The accepted run ended with process exit 0 and BUILD SUCCESS. Counts were read
from the fresh Surefire XML, including the two selected API classes. The local
executor cases use doubles to observe calls, responses, delays and reporting
order. They do not send requests to GoRest or prove an external deployment.

## Existing GoRest journeys

[PositiveUserTests](../../atf-tests/src/test/java/com/ai/api/tests/PositiveUserTests.java)
retains its two existing write journeys: create with schema validation, and
create/wait/delete. Neither was selected or executed in this verification.
The bundled auth.token is empty, and no token override was supplied.
[BaseTestAPI](../../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java)
raises SkipException when the write token is absent. An unavailable token or
a skipped write journey must not be presented as a passing GoRest result.

Thus the observed verdict is: the existing framework API subset passes;
the two external write journeys have no fresh execution result here.

## External evidence and unsuccessful launch

All locators below use the caller-selected ATF_WORK_ROOT. The source checkout
and live checkout contain no generated run artifacts.

- Accepted copy: `${ATF_WORK_ROOT}/validation/portable workspace/modules-api-focused-02`.
- Accepted log: `${ATF_WORK_ROOT}/runs/modules-api-focused-02.log`.
- Source hashes, resolved arguments and environment:
  `${ATF_WORK_ROOT}/runs/modules-api-focused-02-source.json`.
- Per-module XML totals: `${ATF_WORK_ROOT}/runs/modules-api-focused-02-results.json`.
- Original XML: each tested module's target/surefire-reports in the accepted copy.

The earlier modules-api-focused-01 launch stopped before test execution because
PowerShell split the unquoted suite property. It ran zero tests and is not
acceptance evidence. Its separate log and source manifest were retained. The
accepted launch passed the complete suite property as one quoted argument,
as shown above; no test or runtime workaround was introduced.
