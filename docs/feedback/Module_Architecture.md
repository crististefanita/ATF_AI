# Modular architecture feedback

## M-001: Extract independent runtime artifacts

Status: VERIFIED on 2026-11-24. Owner: framework architecture.

The baseline at commit `6f0d5b7` packaged browser SDKs, reporting backends,
product support and framework contracts in one artifact. Consumers could not
select one browser adapter or run Core without those vendor dependencies.

The reactor now contains `atf-core`, `atf-playwright`, `atf-selenium`,
`atf-reporting` and the test-only `atf-tests` module. Java service providers
compose adapters and reporting at runtime. Existing public package names are
retained where practical; source-governance helpers and application scenarios
belong to test sources.

## Acceptance evidence

Final source commit: `457c4daced169a6947e5ff32e78ae690b778340b`.
Commands ran from disposable copies under ${ATF_WORK_ROOT}/validation, with Maven
and browser caches outside the development/live checkouts. Temporary files
were also isolated per run. Every acceptance row has zero failures, errors
and skips; counts across profiles overlap and must not be summed as unique tests.

| Check | Reactor command | Executed cases | Evidence under ${ATF_WORK_ROOT}/runs |
|---|---|---|---|
| Local regressions, governance and packaging | `mvn -B -ntp verify` | 361 verification cases plus 11 native library cases = 372 | modules-acceptance-02.log |
| Selenium and Playwright browser parity | `mvn -B -ntp -Pbrowser test` | 102 browser cases plus 11 native cases = 113 | modules-browser-01.log |
| Native Allure integration | `mvn -B -ntp -Datf.test.suite=src/test/resources/suites/framework-allure.xml -Dreporting.engine=allure test` | 11 integration cases plus 11 native cases = 22 | modules-allure-01.log |
| Actual aggregate publication | `mvn -B -ntp -Paggregate test` | 361 verification cases plus 11 native cases = 372 | modules-aggregate-02.log |

The final local and aggregate copies match all 645 tracked source files at
the recorded commit. Browser and Allure runs used `6723060`; their runtime,
resources, build configuration and selected test sources match the final
version. The only later changes were the root consumer-migration text and two
governance sources, covered by the final local/aggregate reruns. Full differences
and hashes are recorded in modules-source-audit-457c4da.json.

The 57 governance cases include meaningful negative fixtures for Maven profiles,
coordinates and dependency cycles; source ownership and lexical shadow scopes;
service registration; Markdown references, anchors, literal examples and real
diagram fences. The language policy detects known Romanian prose remnants;
the maintained documentation was authored and reviewed in English.

## Artifact inspection

- All five modules package successfully. The four runtime JARs contain their
  owned classes and service descriptors; atf-tests contains zero production
  classes. No retired component or product-support leakage was found. JAR hashes
  are in modules-jar-audit-6723060.json; runtime sources are unchanged afterward.
- The aggregate copy produced
  `atf-tests/target/atf-core-aggregate-report/extent/ATFCoreAggregateExtentReport.html`
  and 362 passed Allure results: 361 cases plus the aggregate summary.
- The published Extent overview inspector found zero violations. Allure result
  timings were valid. Evidence: modules-aggregate-02-artifact-audit.log and
  modules-aggregate-02-allure-audit.json.

An earlier sandbox diagnostic run, modules-local-04, was stopped after restricted
temporary-file replacement and a stalled Java helper. It is explicitly marked
aborted and is not acceptance evidence. The successful runs above used dedicated
temporary directories and normal host execution permissions.

At this historical checkpoint, the desktop browser/CDP matrix still needed
separate verification. The [current register](README.md) records the later
F-008 evidence.
