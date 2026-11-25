# Governance and existing-test review

Review baseline: `d35e69d`. Reference: the current atf_j0 source workspace and
archived `main_mcp` at `70a7279`. Ratings and the comparative assessment belong
in the external review under ${ATF_WORK_ROOT}/reviews, rather than stable owner guides.
The permitted change surface is documentation and existing test/governance files;
production Java and the five-module Maven dependency graph remain unchanged.

<a id="g-001"></a>
## G-001: Documentation and feedback contracts

- Owner: atf-tests, existing DocumentationGovernanceTest and
  MarkdownDocumentationRules; owner guides under docs.
- Observed: deleting the complete feedback tree could leave documentation
  validation green. Owner README sections and factual build claims were not
  enforced. Closed findings retained present-tense defects and TODO wording.
- Expected: required owner/index/feedback documents, current POM facts,
  actionable active records and unique IDs. Closed evidence keeps its actual
  historical scope. The essential living architecture remains synchronized
  in Markdown and HTML, with a source hash, section map and checked navigation.
  Project governance stays in owner documentation; AGENTS.md is removed from main
  and its reintroduction is rejected by an existing documentation test.
- Reproducer: remove docs/feedback/README.md; change the documented parent
  coordinate/compiler target; duplicate an active ID; leave a stale HTML copy.
- Acceptance: the existing documentation validator rejects those cases and
  accepts the maintained guides, literal examples and normalized Windows text.
- Status: VERIFIED on 2026-11-24. All 24 documentation cases passed within the
  final 74-case governance set; the paired guide also passed visual review.

<a id="t-001"></a>
## T-001: Observable, bounded existing regressions

- Owner: atf-tests, StepLoggerContractTest, FailureAttachmentRegistryTest,
  ConfigManagerFileContractTest, FinalStateAttachmentRegistryTest,
  AtfCoreAggregatePublishedManifestContractTest, UiCoreConfigOverrideDisciplineTest
  and the existing scanner/framework architecture governance classes.
- Observed: unbounded thread waits could hang acceptance; no-op assertions and
  unchecked nested TestNG outcomes could falsely pass. The manifest test compared
  its output to itself and was absent from every maintained suite.
- Expected: observable ordered effects, exact outcome totals, bounded worker
  ownership and cleanup, exact restoration of prior system properties, explicit
  manifest counts and registration in the maintained local/aggregate gates.
- Acceptance: run the four existing classes, repeat configuration isolation
  with pre-existing property values, and pass suite-catalog negative fixtures.
  Live application and intentional failure fixtures remain explicit exemptions;
  stale exceptions and accidental suite selection must fail governance.
- Status: VERIFIED on 2026-11-24. The complete reactor passed 393 cases;
  configuration isolation also passed four cases with pre-existing values.
  Static restoration markers are labeled as a source convention, not proof that
  cleanup executes. Registry dispatch tests no longer imply backend integration.

<a id="p-001"></a>
## P-001: Recovered policies and documentation Atlas

- Owner: docs/governance and the documentation-only atf-atlas area, verified
  through the existing atf-tests documentation/architecture guards.
- Observed: useful archive rules for code readability, ownership and evidence
  were coupled to the retired skills/Atlas generation stack. The old Atlas
  requires substantial removed Java dependencies and monolithic source paths.
- Decision: recover principles and essential architecture contracts in English;
  maintain a separate source/test/evidence Atlas. Do not restore runtime skills,
  MCP integration, generated historical reports or a sixth Maven runtime module.
- Acceptance: current source and suite links resolve, local/browser/product
  evidence remains distinct, the Atlas contains no Java or POM, and the living
  architecture is present and aligned in both HTML and Markdown.
- Status: VERIFIED on 2026-11-24. The Atlas is documentation-only; the recovered
  pair has 34 matching headings, seven static diagrams and no scripts. Links,
  metadata, source boundaries and actual desktop/mobile rendering passed.

## Verification receipt

Source checkpoint: `9a0e95d8a5d768787fff1f7436a70040c5bc6757`.
Evidence root: ${ATF_WORK_ROOT}/runs. The runs below use separate validation copies;
counts overlap and are not a unique total.

| Selection | Result | Evidence relative to the root |
|---|---|---|
| `mvn -B -ntp verify` | 382 local/governance plus 11 native cases; 393 passed, zero failed/error/skipped | modules-review-local-01.log |
| `mvn -B -ntp -Paggregate test` | 393 passed; published Extent and 383 passed Allure results including the summary | modules-review-aggregate-01.log |
| Existing ConfigManagerFileContractTest with `ui.engine=selenium` and `atf.config.fixture=sentinel` | 4 passed, zero failed/error/skipped | modules-review-config-01.log |
| Published Extent overview inspection | Zero violations | governance-review-aggregate-overview.log |
| Source boundary audit | No added Java, production/resource/POM changes or AGENTS.md; only two whitespace-only HTML lines differed from the tested snapshot | governance-review-source-audit-9a0e95d.json |

The 74 governance cases comprise 32 architecture/suite, 24 documentation and
18 scanner cases. The earlier focused runs are preserved as diagnostics;
the final complete reactor validates the final test sources. This pass does
not claim fresh browser or J0 reactor execution.

The normalized Markdown hash is
`c48007d48728740e8e9b738d99f3183774296b73606671f174cb919802ab96cf`.
Static and actual rendered guide evidence is under
${ATF_WORK_ROOT}/tools/living-guide-preview-20261124, including final-static-audit.json
and final-render/visual-qa.txt with desktop/mobile screenshots. Review scores
and their explicit comparison rubric remain outside stable owner documents.

At this historical checkpoint, the desktop browser matrix still needed separate
verification. The [current register](README.md) records the later F-008 evidence.
