# framework-under-test Atlas Contract Report

This folder contains the scoped Atlas validation evidence for `docs/atlas/framework-under-test_business-test-atlas.html`.

| Field | Value |
| --- | --- |
| Engine | `extent` |
| Total invocations | `8` |
| Passed | `8` |
| Failed | `0` |
| Skipped | `0` |
| TestNG methods passed | `71` |
| Extent validation artifact | `framework-under-test_atlas-tests-extent-report.html` |
| Surefire XML artifact | `framework-under-test_atlas-surefire-testsuite.xml` |
| TestNG XML artifact | `framework-under-test_atlas-testng-results.xml` |
| Atlas HTML | `docs/atlas/framework-under-test_business-test-atlas.html` |
| Source root | `src/test/java/com/ai/atf` |
| Traversed main root | `src/main/java/com/ai` |
| Source Java files | `171` |
| Source @Test annotations | `433` |
| Source @AtfContractSuite annotations | `102` |

Run the Atlas validation artifact with:

```bash
mvn -q test "-Dsurefire.suiteXmlFiles=src/test/resources/atlas/testng-atlas.xml" "-Datlas.html=docs/atlas/framework-under-test_business-test-atlas.html" "-Datlas.sourceRoot=src/test/java/com/ai/atf" "-Datlas.uiSourceRoot=src/main/java/com/ai" "-Dreporting.engine=extent" "-Dreports.dir=docs/atlas/validation" "-Dreports.file.name=framework-under-test_atlas-tests-extent-report.html" "-Dreports.timestamp.enabled=false" "-DforkCount=0"
```

This manifest is generated from the green Atlas validation run. The Atlas contract total in the HTML uses the Extent Overview score cards (`71 test cases / 0 failures`); the same report also exposes the method-level TestNG summary (`71 methods passed / 0 failed / 0 skipped`).
