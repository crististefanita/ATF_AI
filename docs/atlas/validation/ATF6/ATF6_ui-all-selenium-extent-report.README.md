# ATF_name Selenium UI Runtime Extent Report

- Artifact: `ATF_name_ui-all-selenium-extent-report.html`
- Source copy: `target/reports-ATF_name-ui-current-selenium-parity-final-20260702-source-real-1/ExtentReport.html`
- Suite XML: `src/test/resources/atf/core/suites/ui/testng-atf-ui-base-test-ui-all.xml`
- Copied for: `docs/atlas/ATF_name_business-test-atlas.html`
- Scope: current source-real ATF_name UI suite
- Result shown by report dashboard: `Total: 16; Passed: 16; Failed: 0; Skipped: 0`

Canonical diagnostic command:

```powershell
java -cp "<isolated ATF_name UI parity classpath>" "-Dui.engine=selenium" "-Dreporting.engine=extent" "-Dreports.dir=target/reports-ATF_name-ui-current-selenium-parity-final-20260702-source-real-1" "-Dreports.file.name=ExtentReport.html" "-Dreports.timestamp.enabled=false" org.testng.TestNG src/test/resources/atf/core/suites/ui/testng-atf-ui-base-test-ui-all.xml
```

Interpretation: this is the Selenium runtime proof for the current ATF_name UI source suite. It is numerically identical to the Playwright runtime report: 16 passed, 0 failed, 0 skipped.
