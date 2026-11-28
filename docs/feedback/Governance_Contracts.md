# Deeper executable contracts

Baseline: `2f676d9`. This pass adapts useful J0 documentation/source-governance
practices and the archive's ownership principles. It adds no runtime feature,
module, skill registry or agent instruction file. Minimal corrections to existing
defects are permitted; source changes and evidence must be identified explicitly.

## Closure evidence

Implementation checkpoint: `3286731a158dce2885608e8f9f2e4fd684f6b72f`.
The local and aggregate final copies include the T-003 fixture correction.
Every command below ran from its own isolated reactor under
${ATF_WORK_ROOT}/validation. The workspace-normalized common command prefix was
`mvn -B -ntp -Dmaven.repo.local=${ATF_WORK_ROOT}\cache\m2`; append the row's arguments.
Logs are under ${ATF_WORK_ROOT}/runs and module XML results remain in the corresponding
validation copy's target/surefire-reports directories.

| Validation copy and log stem | Command arguments | Observed result |
|---|---|---|
| modules-phase2-local-reliable-final | `-o verify` | 474 passed: 463 atf-tests and 11 native library checks; no failures/errors/skips |
| modules-phase2-aggregate-reliable-final | `-o -Paggregate test` | 474 passed: 463 atf-tests and 11 native library checks; no failures/errors/skips |
| modules-phase2-browser-alone | `-o -Pbrowser test` | 119 passed: 108 browser-suite and 11 native library checks; no failures/errors/skips |

The standalone browser copy predates the final T-003 edit to
WaitUtilsContractTest. The maintained
[browser suite](../../atf-tests/src/test/resources/suites/framework-browser.xml)
does not select that class; selected browser tests and runtime sources were
unchanged. This establishes the selected browser scope, not an identical full
checkout. Counts overlap across runs and must not be summed as unique tests.
These execution receipts do not assert a fresh publication or packaged-JAR audit.

Earlier failed executions remain evidence, not replacement success receipts:

| Historical failed log | Observed failure |
|---|---|
| modules-phase2-browser-final.log | 108 browser-suite invocations, 2 failures: the positive and mismatch network invocations exceeded their 30-second method limits during browser cleanup; 11 native checks passed |
| modules-phase2-aggregate-readme-final.log | 463 atf-tests invocations, 1 failure: the input-value success fixture took 898 ms against its former 600 ms budget; 11 native checks passed |

Independent README example evidence is
${ATF_WORK_ROOT}/tools/readme-example-qa-20261124-01/results.json, compile.log and the
engine execution logs/XML in that directory. All three complete README examples
compiled against the existing public API with Java 11; the Playwright and Selenium
examples each executed one local-page test successfully, with no failures/skips.
They are separate QA executions, not additional reactor cases.

<a id="g-002"></a>
## G-002: Source visibility and execution ownership

- Owner: atf-tests,
  [GovernanceFiles](../../atf-governance/src/main/java/com/ai/atf/governance/GovernanceFiles.java),
  [FrameworkArchitectureRules](../../atf-governance/src/main/java/com/ai/atf/governance/FrameworkArchitectureRules.java)
  and [existing architecture regressions](../../atf-governance/src/test/java/com/ai/atf/governance/FrameworkArchitectureGovernanceTest.java).
- Original observation: global directory-name exclusions could hide legal source packages
  named target or artifacts. Whole-runtime pause, console and interrupt policies
  were documented or checked locally but not governed across modules.
- Expected: owned Java/resource sources remain visible regardless of legal
  package names; actual build output stays excluded. Direct pauses and console
  publication have exact owners. InterruptedException catches structurally
  restore the current thread's flag, backed by separate behavioral tests.
- Acceptance: compilable legal-package fixtures expose forbidden/duplicate
  imports; output fixtures stay excluded. Negative execution fixtures distinguish
  real calls from comments/literals/static imports and wrong-thread restoration.
  Existing API/UI wait tests prove actual interrupt preservation.
- Delivered: source inventories preserve owned legal package paths; exact-owner
  guards reject real pause/console violations and incorrect interrupt restoration.
  Structural checks are complemented by behavioral API/UI interruption cases;
  they do not establish universal control-flow correctness.
- Status: VERIFIED by the final local and aggregate executions above.

<a id="g-003"></a>
## G-003: Executable documentation facts and lifecycle

- Owner: existing
  [documentation rules](../../atf-governance/src/main/java/com/ai/atf/governance/MarkdownDocumentationRules.java)
  and [focused documentation-test owners](../../docs/governance/README.md),
  stable guides and the canonical Living Architecture Markdown/HTML pair.
- Original observation: fenced Maven commands were not tied to real profile/module/suite/test
  selections, and a changed documented default could remain green after simply
  refreshing the HTML source hash. Stable guides could accumulate scorecards or
  dated success receipts without a lifecycle finding.
- Expected: supported executable commands resolve against their selected POM and
  source inventory. Explicit properties-default blocks agree with current
  framework.properties; alternative examples remain examples. Stable owner guides
  describe current contracts while feedback retains scoped historical receipts.
- Acceptance: meaningful pass/fail fixtures cover unavailable profiles, missing
  suites/classes/POMs, stale defaults and misplaced receipts, including literal
  examples and legitimate feedback exceptions. The maintained guide stays paired.
- Delivered: supported single-line Maven command fences resolve selected POMs,
  reactor/parent profiles, modules, suites and actual test-class filters. Quoted
  arguments and literal/comment examples have explicit fixture coverage.
  `properties defaults` blocks match framework.properties. Stable-guide lifecycle
  checks retain feedback/historical record exceptions and the paired-guide hash.
  This is bounded command analysis, not a general shell or effective-POM solver.
- Status: VERIFIED by the final local and aggregate executions above.

<a id="t-002"></a>
## T-002: Actual API execution and local browser network evidence

- Owner: existing
  [API execution regressions](../../atf-tests/src/test/java/com/ai/atf/core/support/base/ApiCoreExecutionArchitectureContractTest.java),
  [reporting-session regressions](../../atf-tests/src/test/java/com/ai/atf/core/reporting/regression/ReportingManagerSessionContractTest.java)
  and [cross-engine parity fixture](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java).
- Original observation: retry policy/parser checks did not execute ApiRequestExecutor itself.
  No real browser network-response acceptance was recorded; capability, delegation
  and source checks could not establish observed request/response payloads.
- Expected: executor attempts, retry delays and reporter ordering have independent
  observations. Suite cache/generation behavior follows actual ReportingSession
  semantics. Both engines observe a real local request and response; mismatches
  fail within their wait budget and cleanup owns its server/browser state.
- Acceptance: existing API/session regressions cover retry success/exhaustion,
  non-retry and exception paths. Existing browser parity tests assert URL, method,
  status and actual payloads plus URL/method mismatch outcomes against a local
  server. No public application mutation or empty-body fallback counts as proof.
- Delivered: local executor and reporting-session cases passed in the final local
  and aggregate suites. Real browser observations passed for both engines:
  2 positive response probes and 4 URL/method mismatch probes within the complete
  119-check browser/native run. Server and browser cleanup are exercised; the
  earlier cleanup timeouts remain in the failed-run ledger above.
- Status: VERIFIED for these local framework contracts. F-008 has a later,
  independent browser/driver/typed CDP matrix receipt. Local network pages
  establish the stated framework contract. Current scope is recorded in the [register](README.md).

<a id="f-009"></a>
## F-009: Negative API retry budgets skip the request

- Owner: atf-core
  [ApiRuntimeOptions](../../atf-api/src/main/java/com/ai/api/core/client/ApiRuntimeOptions.java)
  and the existing API execution regressions linked in T-002.
- Original reproducer: create options with maxRateLimitRetries=-1 and execute a request
  whose supplier increments a counter. The unchanged compiled baseline returned
  attempts=0 and response=null with supplierCalls=0.
- Evidence: ${ATF_WORK_ROOT}/runs/negative-api-retry-baseline.log and the independent
  runtime-deepen-20261124 reproduction under ${ATF_WORK_ROOT}/tools.
- Expected: reject a negative factory argument or configured retry value before
  execution with an actionable diagnostic. A zero retry budget still executes
  the initial request once; valid retry behavior remains unchanged.
- Scope decision: the user approved minimal corrections to existing defects,
  without new runtime classes/features. Do not approve the old null result with
  a passing test or alter unrelated factory-delay behavior.
- Delivered: existing factory/load validation rejects negative retry budgets
  before request execution. Existing regressions verify negative options and
  configuration, one initial request with zero retries, and valid retry behavior.
  The Living Architecture pair documents this same contract.
- Status: VERIFIED by the final local and aggregate executions above.

<a id="g-004"></a>
## G-004: English authored source, comments and diagnostics

- Owner: atf-tests JavaSourceScanner and its existing
  [language-governance regressions](../../atf-governance/src/test/java/com/ai/atf/governance/JavaSourceScannerTest.java),
  with [ENG-001](../governance/Code_And_Documentation_Policy.md#english-source-and-comments)
  and documentation governance as the public contract owners.
- Requirement: all authored code identifiers, comments/Javadoc and framework/test
  messages MUST be written in English (ENG-001 in the canonical policy).
  External/localized data remains data. The requirement itself and its linked
  test owner are maintained as documentation contracts.
- Expected: executable code, comment and literal regions are checked separately;
  comments cannot hide behind test-data exceptions. Exact policy-fixture owners
  and their reasons are explicit, stale exemptions fail, and production files
  cannot receive blanket test-data exemptions.
- Acceptance: real repository inspection plus negative fixtures for Romanian
  identifiers, comments, Javadoc, diagnostics and Unicode-escaped markers;
  positive literal/comment delimiter cases and narrowly owned localized data.
  README architecture diagrams and concrete Playwright/Selenium consumer examples
  are required; declared complete examples compile against actual public APIs.
- Delivered: repository guards distinguish identifiers, comments/Javadoc and
  literal messages after Java Unicode translation. The three exact policy/data
  fixture exceptions retain reasons and stale-exception checks; their identifiers
  and comments remain governed. The three required README examples also guard
  authored identifiers, comments and literal messages, and compile against the
  current public API. The independent three-example compile and one-test-per-engine
  executions are recorded above; no new tracked Java example class was added.
- Limit: finite known-marker checks complement manual English review; they do not
  establish universal semantic language detection or justify translating external data.
- Status: VERIFIED by final local/aggregate governance and the independent README QA.

<a id="f-010"></a>
## F-010: Repeated configuration changes lose an absent original value

- Owner: existing
  [AtfRuntimeConfigSandbox](../../atf-core/src/main/java/com/ai/core/support/contract/ids/AtfRuntimeConfigSandbox.java),
  [configuration regressions](../../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java)
  and reporting-session regressions linked in T-002.
- Original observation: putIfAbsent treats an existing null snapshot as absent. Starting with
  no property, setting it to A and then B replaces the original null snapshot
  with A; close restores A instead of absence. Both system and framework values
  use this pattern. The new invalid-engine regression exposed the leaked value
  at TestNG suite finish.
- Expected: both snapshot maps preserve their first observed value exactly once,
  including null, across repeated set/clear operations.
- Acceptance: direct configuration regressions and the original reporting-session
  scenario pass without a nested-sandbox workaround or engine fallback change.
- Delivered: containsKey-based capture preserves that first snapshot in both maps.
  Direct absent/present/empty-system-value fixtures and the original invalid-engine
  reporting-session scenario passed without the workaround. ConfigManager's merged
  lookup semantics are unchanged; this does not prove raw source precedence or
  provenance for every system/configuration combination.
- Status: VERIFIED by the final local and aggregate executions above.

<a id="t-003"></a>
## T-003: Input-value success regression depends on scheduler latency

- Owner: existing
  [WaitUtilsContractTest](../../atf-tests/src/test/java/com/ai/atf/ui/component/WaitUtilsContractTest.java);
  production WaitUtils is unchanged.
- Reproducer: modules-phase2-aggregate-readme-final.log records a failure after
  one empty value read. The success fixture requires two observations, polls at
  250 ms and had only a 600 ms total budget; the invocation took 898 ms.
- Expected: a success regression observes the second value and asserts exactly
  two reads. Scheduler delay must not be its success oracle. The independent
  never-matching test still asserts the timeout diagnostic and an observed read.
- Correction: only the success fixture budget and its English explanatory comment
  changed. The five-second bound retains the two-value sequence, matching predicate
  and exact two-read assertion. The separate 10 ms never-match timeout case is unchanged.
- Status: VERIFIED by the final local and aggregate executions above. The failed
  pre-correction aggregate receipt remains in the historical ledger.
