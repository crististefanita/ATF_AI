# ATF Core Aggregate Summary

This folder is the official aggregate reporting artifact for `com.ai.atf TestNG sweep`.
Adapter-local reports may exist elsewhere, but they are support artifacts, not the final audit surface.
Every test node publishes TestNG result plus available @AtfContract / @AtfContractSuite metadata.

| Field | Value |
| --- | --- |
| Engine | `extent` |
| Total test cases | `441` |
| Passed | `441` |
| Failed | `0` |
| Skipped | `0` |
| Wall-clock duration | `590.557 s` |
| Cumulative test duration | `576.920 s` |
| Slowest test | `ExtentSparkStatusDropdownContractTest#extent_spark_status_dropdown_skipped_filter_isolates_skipped_fixture_from_failed_fixtures_in_browser - 27.292 s` |
| Slowest class/group | `ExtentSparkStatusDropdownContractTest - 121.459 s (8 cases)` |
| Extent final artifact | `extent/ATFCoreAggregateExtentReport.html` |

Run with:

```bash
mvn test '-Dsurefire.suiteXmlFiles=src/test/resources/core/testng-atf-core.xml' '-DforkCount=0' '-Datf.core.aggregate.report.engine=extent'
mvn test '-Dsurefire.suiteXmlFiles=src/test/resources/core/testng-atf-core.xml' '-DforkCount=0' '-Datf.core.aggregate.report.engine=allure'
mvn test '-Dsurefire.suiteXmlFiles=src/test/resources/core/testng-atf-core.xml' '-DforkCount=0' '-Datf.core.aggregate.report.engine=both'
mvn test '-Dsurefire.suiteXmlFiles=src/test/resources/core/testng-atf-allure.xml' '-Dreporting.engine=allure'
```
