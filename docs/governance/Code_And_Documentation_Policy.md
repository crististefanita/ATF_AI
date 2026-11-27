# Code and documentation policy

## Ownership and change scope

Use the existing [module boundaries](../Architecture.md). Core contracts remain
independent of browser and reporting implementations. Product fixtures and
cross-module checks remain in atf-tests. Documentation-only areas have no POM,
Java source, generated report or runtime provider.

Strengthen existing tests and their useful fixtures before adding another
execution surface. Do not restore historical skill facades, routing catalogs,
MCP/Atlas Java validators or generated-test infrastructure through policy work.

## Review a change at its owner

Use this checklist for the affected contract rather than copying it into each
module README. Review the implementation, POM, existing tests and owner guide
together before interpreting an old TODO or run report.

- [ ] The change has one clear module/document owner and preserves the dependency
  direction; vendor conversions remain in their adapter.
- [ ] The public behavior, input and failure are named clearly, with an independent
  expected result that would reject a plausible wrong implementation.
- [ ] Waits and external operations are bounded, failures retain diagnostics, and
  changed state is restored after success or failure.
- [ ] Existing regressions remain reachable through maintained selections; any
  deliberate exclusion has an owner and a stated proof boundary.
- [ ] Coordinates, configuration, commands, examples and diagram arrows agree
  with the current source, and links route to one canonical contract owner.
- [ ] Relevant checks and artifacts were inspected at their actual scope; feedback
  preserves failures and closes only the demonstrated acceptance criteria.

These are review questions. The executable owners below enforce their supported
rules; a green source guard does not establish every item in this checklist.

## Existing test quality

Requirement GOV-DIAG-001: supported governance findings MUST preserve observed
evidence and provide failure meaning, repair hint, canonical owner and a maintained
recheck selection. Follow [failure diagnostics](Failure_Diagnostics.md) and keep
raw evidence separate from suggested causes. Negative fixtures match the emitted
rule token and requested detail; a related prefix or code mentioned in payload
does not establish the expected failure. Unknown findings remain unclassified.

- State the protected behavior, its observable result and a useful failure case.
  Assertions must distinguish the intended result from a plausible wrong result.
- Prefer observed state and an independent expected value. Matching a copied
  string, counting assertion words or calling the implementation twice does
  not establish behavioral correctness.
- Bound external processes, network waits and browser waits. A timeout must
  identify the failed operation and preserve useful diagnostics.
- Restore changed properties, engine state and listeners in cleanup that also
  runs after failure. Do not convert an unexpected exception into a passing test.
- Maintained TestNG source annotations must omit `enabled` or use literal `true`
  (parentheses are allowed). Disabled or indirect flags fail source governance;
  reviewed nested/manual fixtures retain their explicit boundary.
- Omit `invocationCount` or use a positive integer literal (parentheses are allowed).
  Zero, negative values and indirect expressions/constants fail with
  `SUITE-INVOCATIONS`; parse-only inspection does not certify constant values.
  Repair the annotation while preserving the method and assertions.
- Keep meaningful existing test classes reachable through the maintained suite
  selections. Distinguish deliberate opt-in application tests, fixtures and
  environment probes from accidentally omitted regressions.
- Preserve the sorted executable TestNG method identities in
  [test-method-inventory.txt](../../atf-tests/src/test/resources/governance/test-method-inventory.txt).
  The AST guard records each declaring type, method name and parameter types,
  independently of the suite's class selection.
- `TEST-METHOD-REMOVED` identifies a pinned method missing from source;
  `TEST-METHOD-UNREGISTERED` identifies a source method missing from the reviewed
  inventory. Both findings route to the existing framework governance test and
  include a repair hint.
- For an intentional method addition, rename or retirement, review the signature
  delta and affected proof, then update the sorted inventory and its SHA-256 pin
  in `TestMethodInventoryRules`. This is a source-level identity baseline, not
  proof that the method ran or that a Git change was approved. Review the paired
  inventory and digest changes with the owning test.
- The executable-method seal uses UTF-8 with CRLF normalized to LF. Git checkout
  conversion must preserve the digest; metadata, whitespace and method-content
  changes remain protected. Review the canonical seal with its existing owner.
- A source guard checks architecture or policy. Label that scope separately
  from runtime execution and controlled application acceptance.

## Executable code contracts

### Explicit imports and visible dependencies

Name every imported Java type and static member explicitly. Normal and static
wildcard imports hide dependency ownership and are rejected by the existing
architecture guard. Literal examples and comments are not import declarations.
Keep same-package references local and remove redundant imports when changing
the affected file. Compiler attribution and implementation-tree comparison are
useful for import-only cleanup; a passing text scan alone is not compilation.

Use named static imports when they make ownership clear. Do not move business
logic into a test or renderer to shorten a call site. The module boundary and
vendor-SDK ownership checks continue to apply independently of import style.

### English source and comments

Requirement ENG-001: `authored identifiers, comments/Javadoc and framework/test messages MUST be written in English.`

English documentation is canonical on main. The main_RO documentation edition
translates all maintained documentation prose and explanatory diagram labels, as declared by the
[README edition contract](Readme_Editions.md). That explicit exception does not
change the English source/comment requirement.

Preserve external payloads, proper names and
deliberate localization/negative fixtures as data; do not silently translate an
external contract to satisfy a language check.

Existing source governance inspects lexical code, comment and literal regions
separately, after Java Unicode translation. It rejects known Romanian markers
in authored names/comments/messages. This deterministic policy complements human
English review; a finite vocabulary is not a universal language detector.
The exact language-policy fixture files have documented literal-data
ownership for their literal regions, while their identifiers and comments
remain governed. This is reviewed policy/fixture data, not permission to add
non-English authored diagnostics in those files. Unused data
exceptions fail, and a production file cannot receive a blanket test-data
exemption. New external-language cases need explicit owner review.

The executable owner is
[JavaSourceScannerTest](../../atf-tests/src/test/java/com/ai/atf/governance/JavaSourceScannerTest.java),
including repository inspection and deliberate failing comment/name/message cases.
The documentation-test owners protect this mandatory requirement, README diagrams
and compilable consumer examples. The owners are listed explicitly in the local
and aggregate TestNG suites. From an isolated reactor copy:

<details>
<summary>Run English source and comments commands</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Dtest=JavaSourceScannerTest,DocumentationGovernanceTest,DocumentationRootGovernanceTest,DocumentationEditionGovernanceTest,LivingPrinciplesGovernanceTest,DocumentationStructureGovernanceTest,FeedbackGovernanceTest,DocumentationPublicationGovernanceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

</details>

### Visibility and execution

Inventories must retain legal source-package segments such as `target` and
`artifacts`; directory names only identify build output outside owned source
trees. A guard that silently ignores source is not an ownership check.

Direct fixed pauses belong to ApiSleeper, UiSleeper and AtfTestWaiter; direct
console publication belongs to ConsoleLogger. Other code uses the owning
facade. InterruptedException handling restores the current thread's flag.
Structural inspection of those call shapes complements real interruption
tests; it does not prove every possible control-flow path. An API's intentional
executor/future contract is not an excuse for a blanket text ban or a new
unreviewed exception.

### Reporting scope

Extent is the maintained reporting backend. Runtime reporting.engine accepts extent; aggregate publication accepts extent or an explicit disabled setting. Retired Allure dependencies, providers, active suites and execution recipes must not return through an implicit default.

The dependency/source guards and existing command contracts reject retired integration and unsupported engine values. Generic artifact collectors may preserve historical or external producer formats; that does not reactivate a framework backend. Historical receipts retain their original source, counts and verdicts.

## Maintainability and exceptions

Review package cohesion, large methods/classes and resource growth where the
change occurs. Measurements are signals for an owner to inspect, not quality
scores or inherited universal limits.

When existing debt needs an exception, name the exact source or inventory,
owner, observed issue, repair criterion and expiry/review trigger in feedback.
Do not increase a threshold merely to accept growth. Remove an exception when
its source disappears or its closure/expiry condition is met; stale or broader
exceptions must be rejected during review.

Java compatibility follows the compiler contract in the [parent POM](../../pom.xml).
Keep dependency and source checks aligned with declared module ownership and
actual compiler behavior; do not infer Java support from a method-name search.

## Documentation and evidence

For concurrency regressions, synchronize the observed event with a bounded
rendezvous rather than using a fixed pause to make overlap likely. Keep failure
results, ordered identities and finally-owned cleanup observable. Deadline tests
retain their own timeout oracles; increasing a budget is not a substitute for
diagnosing a scheduling assumption.

The [artifact area](../../artifacts/README.md) owns evidence preservation and the
reusable project Atlas protocol. Maintained guides/templates remain source even
inside an artifacts directory. Only declared report/Atlas run subtrees are data;
their exclusion must never hide an owned source/resource package with that name.
Maintenance CLI checks run separately from the Java suite and do not introduce
a runtime dependency on their tooling language.

Keep one canonical owner for each contract. Root/module READMEs orient the
reader; package guides explain their local responsibilities; feedback holds
active gaps. Cross-link an existing owner instead of maintaining parallel rules.

### Reader orientation and diagrams

`DOC-ROOT-FOCUS-001: The root README routes project decisions to owner guides; keep translated branch names and URLs in the edition guide, including references in comments and encoded examples.`

The root README explains project purpose, composition and reader decisions.
Keep translated-branch names and routes in the [edition guide](Readme_Editions.md),
including references otherwise placed in comments, examples or encoded links.
The same root-page structure applies to both maintained editions.

The first screen should answer what the area does, who owns it, what result it
produces, how it is verified and where the reader should continue. Use a short
question-to-owner table when several entry points are useful. The root explains
composition; module and package guides explain their own contracts.

The root README is the framework's route map: explain the separation between
contracts, providers, reporting and verification; summarize the five Maven
modules; show how API and UI checks enter the framework; then route the reader
to the owner guide. Keep its module summary to two columns. Put compilable
Playwright/Selenium consumer examples in their adapter READMEs, where provider
configuration and setup are visible beside the example.
Place the dependency map under the design section and the API/UI execution map
under the check-run section; governance verifies both diagram locations.

Requirement DOC-PRESENT-001: use one diagram for one question: dependency,
runtime flow or lifecycle order. Maintained flowcharts, including explicit subgraph directions, use compact `LR` reading;
sequence, state and class diagrams retain their semantic direction. Simplify
labels or split a question before widening a diagram. A proposed vertical map
needs an owned requirement, rationale and revised acceptance criterion.

Requirement DOC-PRESENT-002: immediately after every Mermaid fence, place one
balanced `<details>` without an open attribute. Its descriptive Legend summary
introduces an explanation of arrows, responsibilities, branches and omissions.
Keep essential purpose and preconditions visible before the diagram. Literal
examples and commented diagrams are data; a mere nonempty legend is not proof
that its explanation matches the source. Inspect the actual rendering.
Diagrams explain the POM or implementation; they do not replace either source.

Use semantic Markdown headings, minimal tables, typed code fences and concise
labels. Place secondary detail in its owner guide or a named `<details>` block.
Keep renderer styling out of Markdown rather than embedding local fonts or CSS.

Requirement DOC-TABLE-001: each prose cell in a maintained normative Markdown table MUST
stay within 140 visible characters and 22 words. Keep tables for short mappings
and comparisons; move explanations to nearby prose or a linked owner, and split
a table when its rows answer different questions. The bound is a deterministic
review prompt, not a rendering or readability guarantee. Inspect each changed
table at its actual rendered width. Preserve distinct fields such as Priority
and Quality even when a compact layout requires more deliberate wording.

Historical feedback tables retain their captured proof shape; the active feedback
index remains governed. Short package guides can use bullets or tables without
an artificial diagram quota. Table limits cannot establish column balance:
inspect wrapping, clipping and proportion at 1440 and 900 CSS px on a desktop
browser; keep legitimate wide reference tables in a locally scrollable area.

Requirement DOC-REVIEW-001: every maintained Markdown document MUST have one H1,
begin its heading hierarchy at H1 and descend by at most one level at a time.
Returning to an earlier level is valid. Every opening code fence declares its
language or `text` for literal output; `java compile` and `properties defaults`
retain their executable meanings. Fenced examples may illustrate invalid
Markdown without becoming document structure.

The existing documentation governance applies disclosure balance, presentation
markup and bounded editorial-artifact checks to all maintained Markdown, not
only READMEs. Unresolved tokens such as `@@CODE_0@@`, `ZXQ005ZQ` and tokenizer
word-boundary debris belong to repaired drafting input, not visible prose or
diagram labels. Literal examples remain data. These finite patterns complement
human editing; they cannot certify every spelling, language or semantic issue.

Every diagram has an adjacent explanation of arrows, responsibilities and
omissions. Human review compares that explanation with code/POM ownership and
actual rendering. Preserve a correct, approved diagram rather than changing
its geometry merely to satisfy a preferred visual style.

The living LD-02/LD-03/LD-04 controls retain UIActions interaction ownership, the Selenium/Playwright wait distinction and WaitUtils verdict limits. Counterexamples swap those roles between rows. Owner names are bounded consistency signals; review the conditions and negations against source.

### Apply the documentation review in practice

| Changed claim | Inspect its real source | Required review and evidence |
|---|---|---|
| A browser or reporting option | Typed option owner and `framework.properties` | Separate source defaults from caller overrides; recheck the documented command |
| A dependency arrow | Owning POMs and public boundary | Explain whether the arrow means dependency, invocation or evidence; render the changed graph |
| A failed validation | Existing assertion and original XML/HTML | Preserve expected/actual, actual outcome, owner and repair hint; a hint is not the observed cause |
| A translated README | Canonical English paragraph, table, code and diagram | Preserve technical literals and quantities; review negations, conditions, scope and proof limits in Romanian |
| A historical success or score | Original source checkpoint and receipt | Retain its date and selection; link current usage to its canonical owner |

Use the actual `ui.engine` option as an example: its name remains identical in
both README editions, while its explanatory sentence is translated. A changed
default must be reconciled with the typed/source owner; a JVM override does not
prove that the packaged default changed. An `EXECUTED` Atlas oracle must cite
its actual source fragments and executed subjects; a matching label is not an
independent assertion witness.

Review from the source outward: claim, implementation/POM, owner guide, protected
counterexample, actual verification, then feedback closure. Run the smallest
relevant owned selection first. Preserve a failed fixture, fix the identified
defect and recheck; do not loosen a rule simply to obtain a green report.

### Portable paths and executable facts

Tracked project paths and links are relative to their owning document or the
reactor root. External locations use an explicit caller-supplied override or a
declared placeholder such as `${ATF_WORK_ROOT}/runs/...`, as defined in
[Development](../Development.md#working-roles). Do not publish a workstation drive,
network share, local-file URI or fixed named account root in source, configuration
or documentation. Dynamic home-directory inputs and explicit runtime inputs may
resolve to absolute paths on the caller's machine; derive those values from the
current reactor root or a named override before passing them to forked processes.

Documented coordinates, paths, commands, profiles, defaults and public entry
points must agree with the POM, resources and sources they describe. Maintain
English prose and diagrams whose arrows explain actual responsibilities.
Literal examples are examples, not evidence that a behavior exists.

Executable Maven examples use selections that resolve against their actual
POM/reactor. Configuration blocks explicitly marked `properties defaults`
describe checked source defaults; override examples do not claim to be defaults.
Complete public consumer examples marked `java compile` in the browser adapter
READMEs compile against the current public API with Java 11 and annotation
processing disabled. The root README routes readers to those provider examples;
ordinary partial/literal examples are not executed as tests.

### Contracts, gaps and execution evidence

Keep review scores and execution receipts outside stable owner documentation.
Feedback closure must identify the reproducer, owner, expected outcome and
observable proof. Preserve the source checkpoint and exact run scope. A source
move, successful copy, compile or green local suite does not prove a controlled
application journey. Historical results must retain their original scope.
Promote reusable behavior into its owner guide and link the closure record rather
than copying a dated delivery summary into the README. Preserve failed runs when
they explain the reproducer or limit of a later success.

Feedback assessments declare their criterion, affected scope and supporting
source/evidence reference. Priority and quality assess different questions and
follow the [feedback rubric](../feedback/README.md); a count of passing tests does
not by itself establish coverage or area quality.

## Selective adoption from the archive and J0

The archived references below are Git paths at
`main_mcp` commit `70a7279c2a9560d39e6bd2554cd2f96a88626692`; they are not current
working-tree paths or active skills.

| Archived source | Decision | Retained principle |
|---|---|---|
| Code quality policy | Adapt | Owned debt, repair criteria and stale-exception removal; legacy budgets, source roots and allowances stay archived |
| Code quality README | Adapt | Cohesion, Java compatibility and dependency ownership belong to review and existing governance tests |
| Evidence-aware scoring | Adapt | Review scores need declared axes, evidence and a reproducible calculation; keyword counts are not an oracle |
| Completeness boundary | Adopt principle | Suite success and product coverage are different claims with separate acceptance evidence |
| Assertion-density test | Reject as quality oracle | Assertion-cue density cannot replace inspection of assertions and meaningful negative cases |
| Retired runtime packages | Do not restore | Reuse useful rules as documentation/governance without the retired registry, facades or runtime stack |

<details>
<summary>Archived source paths and exact rule provenance</summary>

Code quality policy:

```text
src/main/resources/skills/codequality/contracts/code-quality.yml
```

Code quality README:

```text
src/main/java/com/ai/skills/codequality/README.md
```

Evidence-aware scoring:

```text
src/main/java/com/ai/skills/atlas/rules/scoring/AtlasScoreEvidenceTraceRule.java
AtlasScoreOverallCalculationRule.java
```

Completeness boundary:

```text
src/main/java/com/ai/skills/atlas/rules/scoring/limits/AtlasSuiteCompletenessSeparationRule.java
```

Assertion-density test:

```text
src/test/java/com/ai/skills/codequality/tests/AtfCodeQualityAssertionDensityContractTest.java
```

Retired runtime packages:

```text
src/main/java/com/ai/core/skills
src/main/java/com/ai/skills
```

</details>

J0 references inform the same separation: `AtfPackageCohesionGovernanceTest`
uses reviewed inventories and rejects stale exceptions;
`AtfSourceReadabilityGovernanceTest` checks duplicate imports and keeps legal
source-package paths visible. Current
pause/console/interrupt controls adapt the archive's ownership principle to
ATF_AI's documented execution contracts rather than claiming a verbatim old rule.
`DocumentedBuildContracts` checks factual build documentation; and
`DocumentationLifecycleGovernanceTest` keeps delivery history out of stable
contracts. Adapt those principles to this repository's five modules, TestNG
and ownership. J0 workspace catalogs, consumer rules and language aliases are
not ATF_AI contracts.
## Reader-oriented guides

Requirement DOC-READ-001: keep the reader's decision visible before implementation
or execution detail. Testing.md owns strategy and evidence; the linked
[command cookbook](../Testing_Commands.md) owns reproducible recipes.

Requirement DOC-STRATEGY-001: Testing.md uses compact `flowchart LR` diagrams
for its execution paths and review sequence. Keep one question per diagram and
an adjacent legend explaining branches, arrow meaning and omitted responsibilities.
This explicit strategy requirement does not change approved diagrams in other
owners; a grouped architecture map can have a different semantic direction.

Requirement DOC-STRATEGY-002: under the stable test-design-contracts anchor,
compare API, Playwright and Selenium exactly once in a four-column table: lane,
linked design owner, observable oracle and proof limit. Lane identifiers remain
identical in both editions. Keep this comparison outside details. The guard checks this supported table shape and
presence of ownership/observation/limit fields; it cannot certify their meaning.
Review every claim against the executor, providers, selected suites and assertions.

Keep version matrices, cache resolution and configuration precedence in the
command cookbook. The strategy explains why a selection supplies a particular
proof and links to those operations. The readability component cases reject
vertical flows, missing or duplicate lanes and incomplete comparison rows,
then exercise the actual repair with owned diagnostics.

| Reader task | Preferred structure | What must remain explicit |
|---|---|---|
| Choose between selections or options | Comparison table | Preconditions and the proof each choice supplies |
| Follow ordered work or lifecycle | Numbered steps or a diagram | Direction, actors, branches and an adjacent legend |
| Inspect parallel checks or constraints | Bullets | Independent conditions without hiding exceptions |
| Copy execution commands | Closed details with a descriptive summary | Purpose, prerequisites and expected evidence |
| Understand one reason or consequence | Short connected prose | Conditions, negations and limits |

Canonical retrieval excerpts remain quoted English policy literals in both editions;
translate their surrounding explanation. These are choices based on content,
not diagram/table/bullet quotas. A diagram
explains real relationships; its legend states arrow meaning, ownership and
omissions. Do not hide a prerequisite in a disclosure merely to shorten a page.

The source guard measures logical prose blocks across physical wraps. README
orientation and Testing.md permit at most 3 sentences and 100 visible words per
paragraph. Detailed operational references use the 100-word paragraph bound;
all inspected prose uses a 45-word sentence bound. These are review triggers,
not readability scores or proof that shorter text is correct.

Lists, tables, literal code and disclosure content have distinct reading roles.
Historical issue prose retains its evidence form; feedback/README remains an
operational guide. Their source facts, links, code and translation remain
subject to the other guards. Review prose inside disclosures manually as part
of the complete document review.

Requirement DOC-RUN-001: runnable Maven shell fences in maintained documentation
MUST sit inside balanced details without any open attribute. Give the summary
the actual selection or purpose. Keep strategy and proof limits visible; inline
diagnostic rechecks remain directly readable. Literal text/code fixtures are
not runnable shell recipes. The guard inspects real tags outside fences/comments
and recognizes Maven/wrapper invocations, not a keyword in prose.

Feedback reconciliation follows the [lifecycle owner](../feedback/README.md#recording-and-closing-feedback): an empty active register needs an explicit declaration, while closed, external and withdrawn scope retain their actual proof boundaries. A fenced example cannot declare the backlog empty.

Source inventories prune excluded generated directories before inspecting descendants. The same ownership policy must retain output-like names under owned source/resource roots. Resolve the canonical root once per scan, preserve its escape boundary and qualify relative, spaced and Unicode roots. Directory observations test traversal scope without scheduler-dependent timing thresholds.

The current Atlas reader follows the [audience boundary](../../atf-atlas/Report_Contract.md#reader-and-audit-responsibilities):
numbered business explanations, cases and limits in HTML; technical construction
inputs in the adjacent audit. [Reader controls](../../tools/atlas-reader.test.mjs)
complement source verification without automatically certifying prose meaning.
