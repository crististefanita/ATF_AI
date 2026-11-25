# Framework maintenance

F-008 records the verified desktop browser matrix. The remaining sections
retain framework configuration, cleanup and regression evidence at its original
scope. Current execution commands belong to [Testing](../Testing.md).

## F-008 — Selenium browser matrix

- Status: VERIFIED for the selected Windows 11 x64 matrix: Selenium 4.54.0,
  Chrome/ChromeDriver 158.0.8127.88 and exact typed CDP v158. The actual configured
  executable, capabilities and instantiated domain were checked, with no missing
  or nearest typed-CDP warning in the accepted run and no warning suppression.
- Owner: [atf-selenium](../../atf-selenium/README.md), specifically its
  [network implementation](../../atf-selenium/src/main/java/com/ai/ui/core/engine/selenium/network).
  [atf-tests](../../atf-tests/README.md) owns cross-adapter acceptance.
- Original reproducer: the retained browser suite with
  `org.seleniumhq.selenium:selenium-java:4.45.0` and downloaded
  Chrome/ChromeDriver `158.0.8127.88`; run `mvn -B -ntp -Pbrowser test` from an
  isolated reactor copy with the selected binaries.
- Original observation: Selenium reported that no matching typed CDP implementation was bundled.
  Browser parity passed through the adapter's existing behavior while this
  version-alignment warning remained.
- Expected: document and verify an intentional SDK/browser/driver/CDP combination,
  including the actual network-response capability relied on by callers.
- Historical evidence: framework-browser-02.log under
  ${ATF_WORK_ROOT}/runs/20261124-framework. Binaries and drivers remain under
  ${ATF_WORK_ROOT}/cache. Existing
  [capability](../../atf-tests/src/test/java/com/ai/atf/ui/component/UiEngineCapabilityContractTest.java),
  [delegation](../../atf-tests/src/test/java/com/ai/atf/ui/component/UiEngineDelegationContractTest.java)
  and [Selenium source](../../atf-tests/src/test/java/com/ai/atf/ui/component/engine/SeleniumEngineInternalContractTest.java)
  contracts do not replace a real browser network-response probe.
- Current evidence: the existing
  [cross-engine parity fixture](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java)
  passed 2 positive response probes and 4 URL/method mismatch probes against its
  local server. The accepted standalone browser run passed 120 checks: 108 in
  atf-tests and 12 native library checks. The native exact-provider check and
  browser assertions verify the selected matrix through the existing Manager
  route. Source, environment, exact commands and failed historical runs are in
  [F-008 and its shared ledger](Browser_Compatibility_And_Portability.md#f-008).

| Closure probe | Required observation | Current evidence |
|---|---|---|
| Version selection | Record OS, SDK, actual browser/driver versions and typed CDP availability; preserve any warning | Selected Windows matrix, actual executable and exact instantiated v158 domain passed; the old 4.45.0 warning remains historical evidence |
| Actual network response | Trigger one local request; verify method, URL, status and body from the observed response | 2 positive probes passed, one per adapter, including actual request and response bodies |
| Negative network outcome | Missing/mismatched response fails within its declared wait budget | 4 real URL/method mismatch probes passed across both adapters |
| Cross-adapter retention | Existing browser suite remains green with the selected environment and SDK-default Playwright Node | modules-f008-browser-accepted passed all 120 browser/native checks; local and aggregate each passed 514 |

This closure covers the recorded OS and pinned binaries. A different platform,
SDK, browser, driver or typed-CDP selection requires the same fresh observations
and full browser acceptance; [Testing](../Testing.md#browser-preparation-and-checks)
owns the maintained preparation. A version declaration or warning suppression
alone is insufficient. The earlier modules-phase2-browser-final cleanup failures,
the 119-check phase-two receipt and the later modules-f008-browser-final cleanup
failure retain their original scope in the linked records. Local network
acceptance establishes the recorded framework boundary; it does not claim a
controlled application journey.

## F-002 — reproducible startup

- Status: VERIFIED for the historical premodular correction.
- Original reproducer: follow the root startup guide from a fresh checkout.
- Prior observation: absent classes/calibration files were referenced and default
  discovery mixed local checks with public-site tests.
- Delivered correction: maintained local defaults, explicit opt-in browser/application
  selections and an explicit configuration-file override.
- Historical verification: 304 local cases and focused file-override checks passed.
  The current modular commands are owned by [Testing](../Testing.md).

## F-003 — functional framework boundary

- Status: VERIFIED for the historical cleanup; original implementation retained
  on `main_mcp`.
- Original reproducer: inspect production sources and test profiles.
- Prior observation: prompts, generation/promotion infrastructure and historical
  generated artifacts were mixed into the runtime framework.
- Delivered correction: retain UI/API execution, runtime reporting and meaningful
  regressions; remove retired runtime components and dedicated resources/tests.
- Historical verification: source, JAR, local and browser acceptance passed.
  Recovering a reusable review rule does not restore its old runtime machinery.

## F-006 — regression setup

- Status: VERIFIED for the historical regression correction.
- Original reproducer: execute the retained local suite after retiring old profiles.
- Prior observation: two Allure regressions reflected a moved method, a topology
  check read a retired suite, and a polling success case had a 50 ms wall-clock budget.
- Delivered correction: exercise the public result factory, remove the retired
  topology check and preserve exact polling supplier/attempt assertions with a
  separate timeout case and a usable scheduling budget.
- Historical verification: 304 tests passed, with no failures/errors/skips, in the
  framework-final copy; local-acceptance.log and local-results/ retain the receipt.

## Historical correction evidence

The records in this section precede module extraction. They preserve original
counts and provenance; they are not current distribution instructions.

- Baseline: all original production and test sources compiled successfully.
- Initial corrections: 12 local cases passed, 0 failures/errors/skips.
- Historical source and execution evidence remains in Git history and the
  external runs/20261124-framework archive. Product-flow improvement records
  have been removed from this framework maintenance backlog.

Historical code checkpoints are `c590d62` for cleanup and `661d4dc` for the enabled
aggregate profile. The archived original is `70a7279` on main_mcp. The later
[M-001 record](Module_Architecture.md) owns modularization evidence.

Evidence root: ${ATF_WORK_ROOT}/runs/20261124-framework. Each row is a separate run;
counts overlap and must not be summed as unique tests.

| Historical check | Result | Evidence relative to the root |
|---|---|---|
| Default local suite | 304 passed, 0 failed/error/skipped | local-acceptance.log and local-results/ |
| Explicit Allure subset | 11 passed, 0 failed/error/skipped | framework-allure.log |
| Browser suite | 100 passed, 0 failed/error/skipped | framework-browser-02.log; XML in the framework-browser-01 validation copy |
| Aggregate profile | 304 passed; Extent and 305 passed Allure result JSONs including the summary | aggregate-enabled.log and aggregate-enabled-results/ |
| Generated Extent overview | 0 published-overview violations | aggregate-artifact-audit.log |
| Package | JAR built; 0 retired entries | package.log and package-provenance.json |

The historical aggregate artifacts are under
${ATF_WORK_ROOT}/validation/framework-final/target/atf-core-aggregate-report.
The historical package was `com.ai:test-framework:1.0.0`; its original JAR path,
source commit and SHA-256 remain in package-provenance.json. Current consumption
uses the [modular coordinates](../../README.md#consumer-migration). Copying or
renaming that receipt does not imply a new package version or execution.

The historical browser run used SDK-default Node. The selector run set
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 after browser preparation. Its installer-output
warning and the CDP warning remain part of that evidence; neither has been silently
removed by this documentation review.
