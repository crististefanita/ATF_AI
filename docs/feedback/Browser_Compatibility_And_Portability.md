# Browser compatibility and portable project guidance

Baseline: `805d58e7670ff1e4fa16d53c56fc0827f7cfd3cb`.
Scores follow the separate priority/quality rubric in the
[feedback register](README.md#assessment-rubric-and-evidence-locations).
Initial component scores and evidence remain there after closure.

## Shared acceptance ledger

Implementation checkpoint: `6a88fc9536625ee019f3010607c9a9b1ba256923`.
Each run's external `${ATF_WORK_ROOT}/runs/<stem>-source.json` preserves exact
arguments, environment inputs, binary hashes and the complete tested file-hash
manifest. Its sourceCommit field records base `805d58e`, before the working
content was committed; it is not the final implementation identity. These three
feedback closure files were updated after execution.

All three accepted copies ran below
`${ATF_WORK_ROOT}/validation/portable workspace/<stem>`. The exact common Maven
argument prefix, with the external root normalized, is
`mvn -B -ntp -Dmaven.repo.local=${ATF_WORK_ROOT}/cache/m2`:

| Run stem | Remaining arguments | Result |
|---|---|---|
| modules-f008-local-accepted | `-o verify` | 514 passed: 502 atf-tests and 12 native checks; no failures/errors/skips |
| modules-f008-aggregate-final | `-o -Paggregate test` | 514 passed: 502 atf-tests and 12 native checks; no failures/errors/skips |
| modules-f008-browser-accepted | `-o -Pbrowser test` | 120 passed: 108 browser-suite and 12 native checks; no failures/errors/skips |

Logs use the same stems under `${ATF_WORK_ROOT}/runs`; XML remains in each
validation copy's module target/surefire-reports directories. Counts overlap and
must not be summed as unique tests. Publication and packaged-JAR audits are
separate checks and are not asserted by this ledger.

The runner appended quoted java.io.tmpdir and atf.selenium.cache.root properties
through JAVA_TOOL_OPTIONS. TEMP/TMP used the run's .tmp directory; the ATF fallback
root was `${ATF_WORK_ROOT}/cache/selenium-f008-fallback-empty`, without a cached
driver. No webdriver.chrome.driver override bypassed Manager. The pinned
environment used:

```text
SE_BROWSER_PATH=${ATF_WORK_ROOT}/cache/selenium/chrome/win64/158.0.8127.88/chrome.exe
SE_DRIVER_VERSION=158.0.8127.88
SE_CACHE_PATH=${ATF_WORK_ROOT}/cache/selenium
SE_OFFLINE=true
SE_SKIP_DRIVER_IN_PATH=true
SE_SKIP_BROWSER_IN_PATH=true
SE_AVOID_STATS=true
PLAYWRIGHT_BROWSERS_PATH=${ATF_WORK_ROOT}/cache/playwright
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
```

Host: Windows 11 x64, NT 10.0.26300; Temurin JDK 21.0.12.1+1 and Maven 3.10.
Library compilation targets Java 11. The Playwright SDK used its bundled Node.
Preparation and skipped execution are not acceptance receipts.

| Preserved failed log | Original observation |
|---|---|
| modules-f008-governance-probe.log | 173 governance cases, 3 failures: scoring-header schema, an XPath false positive and a portability negative-fixture finding |
| modules-f008-local-final.log | 514 checks including 12 native checks, 1 failed UNC portability fixture |
| modules-f008-browser-final.log | 120 checks including 12 native checks, 1 failed network invocation interrupted during DriverService.stop cleanup after 37.17 seconds against its former 30-second method cap |

These failures remain historical evidence and are not accepted runs.

## Closure assessments

Baseline priority/quality and their original components remain in the register.
Priority below retains remediation urgency at that baseline; it is not a new
current priority assigned after closure. Closure quality reassesses only the
documented area and evidence, using the existing rubric.

| ID | Baseline priority / quality | Closure quality | Correctness / verification / clarity | Assessed scope |
|---|---|---:|---|---|
| F-008 | 81 / 72 | 94 | 95 / 95 / 90 | Actual selected Windows matrix and local response contracts; other matrices require fresh proof |
| G-005 | 82 / 57 | 95 | 95 / 95 / 95 | Bounded authored-path rules and relocated Windows execution |
| D-001 | 55 / 80 | 94 | 95 / 90 / 95 | Current owner navigation, public examples and the inspected responsive renderer |
| T-004 | 70 / 72 | 95 | 95 / 95 / 95 | Six observed network bodies and cleanup paths with independent deadlines |

<a id="f-008"></a>
## F-008: Align the maintained browser matrix

- Owner: atf-selenium dependency management and existing native/browser contracts.
- Original observation: Selenium 4.45.0 bundled typed CDP 147–149 while the
  selected Chrome and ChromeDriver were 158.0.8127.88. Real response probes
  passed, but a nearest/missing typed-CDP warning remained.
- Selected matrix: Selenium 4.54.0, Chrome for Testing 158.0.8127.88,
  ChromeDriver 158.0.8127.88 and exact typed CDP v158 on the host recorded above.
- Implementation boundary: change the existing Selenium version property and
  strengthen existing tests. Preserve runtime classes and driver fallback
  behavior. The maintained setup uses existing Selenium Manager environment
  settings and the existing configurable ATF fallback-cache root.
- Expected: the actual WebDriver browser/driver capabilities, selected typed
  provider, CDP product and launched executable agree with this matrix. Real
  positive and URL/method mismatch response probes pass on both adapters.
  No nearest-provider warning is suppressed or accepted as exact matching.
- Acceptance: rerun the scoped native/network tests, full local verification,
  full browser profile and aggregate publication from independent disposable
  copies. Record OS, JDK, Maven, exact binaries, dependency selection and evidence.
  The [canonical preparation](../Testing.md#browser-preparation-and-checks)
  must pin the executable as well as the driver version; an installed Chrome
  that happens to share the version is not equivalent binary-selection proof.
- Delivered: the existing
  [native provider check](../../atf-selenium/src/test/java/com/ai/ui/core/engine/selenium/SeleniumProviderTest.java)
  selects exact v158CdpInfo. The existing
  [browser fixture](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java)
  verifies actual browser/driver capabilities, CDP product, instantiated
  v158Domains and the launched binary against SE_BROWSER_PATH. Both positive
  probes and all four URL/method mismatch probes passed in full browser acceptance.
  No missing/nearest typed-CDP warning appeared and none was suppressed.
- Status: VERIFIED for this pinned Windows matrix by the accepted executions
  above. This pass changes zero production Java files; the dependency correction
  is the existing Selenium version property. Another platform or version requires
  renewed matrix and browser acceptance, not extrapolation from this result.

Primary references supporting the selected matrix and setup:

- [Official Selenium downloads](https://www.selenium.dev/downloads/).
- [Selenium 4.54 Java changelog](https://github.com/SeleniumHQ/selenium/blob/selenium-4.54.0/java/CHANGELOG).
- [Published Selenium 4.54 dependency descriptor](https://repo.maven.apache.org/maven2/org/seleniumhq/selenium/selenium-java/4.54.0/selenium-java-4.54.0.pom).
- [Exact Chrome for Testing release manifest](https://googlechromelabs.github.io/chrome-for-testing/158.0.8127.88.json).
- [Official Selenium Manager configuration](https://www.selenium.dev/documentation/selenium_manager/).

<a id="g-005"></a>
## G-005: Keep project paths portable

- Owner: source/documentation governance and the owning guides.
- Original observation: startup and historical receipts embedded the author's
  workstation layout. One unsupported-upload test used an unrelated absolute
  dummy filename. Runtime cache/temp roots already derived from caller settings.
- Requirement: authored repository instructions, source and configuration use
  project-relative paths, classpath resources or explicit caller/environment
  roots. Resolve an absolute runtime path from those inputs when an API requires
  it. Do not embed workstation drives, account directories or private file URIs.
- Delivered: normalize historical evidence to `${ATF_WORK_ROOT}`;
  retain original execution provenance outside the checkout. Resolve consumer
  configuration once from the reactor root so Maven module forks see the same
  file. Keep JVM temp/user-home APIs and HTTP/API paths valid.
- Acceptance: meaningful positive and negative source/configuration/documentation
  fixtures protect the boundary, including decoded Java literals, escaping,
  Windows/UNC/account-root forms and environment-derived paths. Build/test a
  relocated disposable copy and retain source comparisons.
- Verification: existing
  [path governance](../../atf-tests/src/test/java/com/ai/atf/governance/GovernanceFiles.java)
  and [architecture fixtures](../../atf-tests/src/test/java/com/ai/atf/governance/FrameworkArchitectureGovernanceTest.java)
  check fixed paths in owned text and decoded Java literals. Dynamic home/caller
  roots, JVM temp/user-home resolution, HTTP/API paths and explicit XPath selectors
  remain valid. The accepted local and aggregate copies both ran successfully
  under the relocated portable workspace path.
- Status: VERIFIED for the bounded guard and recorded relocated executions.
  This does not establish all possible shell expansions or acceptance on every OS.

<a id="d-001"></a>
## D-001: Improve README navigation and review practice

- Owner: root/module documentation and the canonical governance policy.
- Original observation: the README had accurate public examples, but reader
  navigation, flow/dependency separation and review guidance could be clearer.
- Expected: compact architecture maps answer specific reader questions. Adjacent
  legends explain arrow meaning and ownership. Detailed implementation notes
  remain in their owning guides. Concrete Playwright/Selenium examples compile
  against the public API. All authored prose, code and comments remain English.
- Adoption from J0: reader-question navigation, one-purpose diagrams, nearby
  legends and one canonical evidence-based review checklist. Do not copy J0's
  module catalog, language aliases or runtime contracts into this framework.
- Acceptance: documentation/source governance passes; links and mandatory
  sections/examples resolve; diagrams are rendered and checked for legibility.
  README content matches the five-module reactor and documentation-only Atlas.
- Original closure evidence: the root and owner indexes routed readers by question; code/docs
  review has one canonical policy. The three complete Java examples are unchanged
  and compile through existing documentation governance. Actual Mermaid 11.4.1
  rendered all five README diagrams at 1440 and 520 px; the minimum rendered label
  height was 17 px, with no clipped labels or horizontal page overflow. Living
  HTML retains 34 headings, 7 static SVGs and zero scripts, with its introduction
  inspected at both widths.
- Evidence: `${ATF_WORK_ROOT}/tools/f008-readme-preview-iteration-03` preserves
  PNGs, SVGs, source/asset hashes, example comparisons and measured results.
  Initial small-label diagrams and iteration-02 remain separate historical
  previews. The final lifecycle alias is Test, avoiding the former Consumer wrap.
- Status: VERIFIED by the accepted governance-containing runs and actual visual
  inspection. The local responsive viewer does not prove every Markdown host's
  rendering; finite language markers still require human English review.

Follow-up review: the root now explains API/UI routes as a framework map,
keeps the five-module summary to two columns, and sends provider examples to the
Playwright/Selenium owner guides. Governance requires the dependency map and
API/UI execution diagram in their respective sections, and negative fixtures
cover missing routes, misplaced examples, missing diagrams, dense table cells
and misaligned rows. Both existing adapter examples remain compile-checked.
Verification on 2026-11-26: the documentation/readability selection passed
176/176 in each edition with explicit peer roots; the reviewed test-method
inventory check passed 1/1 in both. No product E2E, browser profile or emulator
was run. The 140-character/22-word limit is a triage signal; the changed root
tables were not re-rendered, and deterministic checks do not replace review in
the target Markdown host.

<a id="t-004"></a>
## T-004: Bound browser fixture lifetime separately from network matching

- Owner: the two existing network methods in SeleniumPlaywrightLocatorParityContractTest.
- Reproducer: modules-f008-browser-final.log records one timeout among 108
  browser-suite cases, with all 12 native checks passing. The invocation took
  37.17 seconds and was interrupted inside DriverService.stop/ExternalProcess.waitFor
  during finally-owned ChromeDriver cleanup. Earlier historical runs also
  retained cleanup-timeout observations; they are not erased by this correction.
- Expected: the network matcher keeps its two-second setting, and a mismatched
  URL/method keeps its independent 15-second observed upper bound. Setup and
  owned process cleanup have a separate finite whole-fixture lifetime.
- Correction: change only the two existing method caps from 30 to 60 seconds,
  calibrated against the repeated cleanup stack and observed 37-second lifetime.
  Add monotonic setup/body-completion/cleanup receipts. A completion marker is
  emitted only after every body assertion; cleanup failures remain failures.
  Preserve request counts, payloads, version/binary/domain checks, invocation
  threads and production shutdown behavior.
- Acceptance: full local and aggregate suites plus the standalone browser
  profile pass without skips. Inspect all six invocations' phase receipts and the real
  request/response results; a budget change or a partial rerun alone is not proof.
- Delivered: the accepted standalone browser run contains 18 monotonic phase lines
  for all six network invocations: setup complete, body complete and cleanup exit.
  Every cleanup records bodyCompleted=true after the body assertions. Maximum
  cleanup was 458 ms and maximum complete fixture time was 5280 ms. The two-second
  network wait and independent 15-second negative bound are unchanged; no assertion
  or cleanup failure was converted into success.
- Status: VERIFIED by all three accepted full selections above. The initial
  37.17-second cleanup failure remains in the failed-run ledger; these observations
  establish the recorded executions, not a universal guarantee about process timing.
