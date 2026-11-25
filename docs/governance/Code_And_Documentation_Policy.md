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
- Keep meaningful existing test classes reachable through the maintained suite
  selections. Distinguish deliberate opt-in application tests, fixtures and
  environment probes from accidentally omitted regressions.
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

Requirement ENG-001: authored identifiers, comments/Javadoc and framework/test messages MUST be written in English.

English documentation is canonical on main. The main_RO documentation edition
translates README.md prose and diagram labels only, as declared by the
[README edition contract](Readme_Editions.md). That explicit exception does not
change the source/comment requirement or exempt other documents.

Preserve external payloads, proper names and
deliberate localization/negative fixtures as data; do not silently translate an
external contract to satisfy a language check.

Existing source governance inspects lexical code, comment and literal regions
separately, after Java Unicode translation. It rejects known Romanian markers
in authored names/comments/messages. This deterministic policy complements human
English review; a finite vocabulary is not a universal language detector.
The three exact language-policy fixture files have documented literal-data
ownership for their literal regions, while their identifiers and comments
remain governed. This is reviewed policy/fixture data, not permission to add
non-English authored diagnostics in those files. Unused data
exceptions fail, and a production file cannot receive a blanket test-data
exemption. New external-language cases need explicit owner review.

The executable owner is
[JavaSourceScannerTest](../../atf-tests/src/test/java/com/ai/atf/governance/JavaSourceScannerTest.java),
including repository inspection and deliberate failing comment/name/message cases.
[DocumentationGovernanceTest](../../atf-tests/src/test/java/com/ai/atf/governance/DocumentationGovernanceTest.java)
also protects this mandatory requirement, README diagrams and compilable consumer
examples. From an isolated reactor copy:

```powershell
mvn -B -ntp -pl atf-tests -am "-Dtest=JavaSourceScannerTest,DocumentationGovernanceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

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

The first screen should answer what the area does, who owns it, what result it
produces, how it is verified and where the reader should continue. Use a short
question-to-owner table when several entry points are useful. The root explains
composition; module and package guides explain their own contracts.

Use one diagram for one question: dependency direction, runtime flow or lifecycle
order. Prefer compact left-to-right flowcharts for new relationship maps; switch
to a vertical layout when labels would become unreadable at a narrow viewport.
Use a sequence diagram when ordering matters. Put a short legend immediately after the
diagram that defines arrow meaning, responsibility and any omitted detail.
Diagrams explain the POM or implementation; they do not replace either source.

Use semantic Markdown headings, minimal tables, typed code fences and concise
labels. Place secondary detail in its owner guide or a named `<details>` block.
Keep renderer styling out of Markdown rather than embedding local fonts or CSS.

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
Complete public consumer examples marked `java compile` in the root and browser
adapter READMEs compile against the current public API with Java 11 and annotation
processing disabled. Ordinary partial/literal examples are not executed as tests.

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
| `src/main/resources/skills/codequality/contracts/code-quality.yml` | Adapt | Owned debt, explicit repair criteria and stale-exception removal; do not copy old budgets, source roots or legacy allowances |
| `src/main/java/com/ai/skills/codequality/README.md` | Adapt | Cohesion, Java compatibility and dependency ownership belong to review and existing governance tests |
| `src/main/java/com/ai/skills/atlas/rules/scoring/AtlasScoreEvidenceTraceRule.java` and `AtlasScoreOverallCalculationRule.java` in that package | Adapt | Review scores need declared axes, evidence and a reproducible calculation; keyword counts are not an oracle |
| `src/main/java/com/ai/skills/atlas/rules/scoring/limits/AtlasSuiteCompletenessSeparationRule.java` | Adopt principle | Suite success and product coverage are different claims with separate acceptance evidence |
| `src/test/java/com/ai/skills/codequality/tests/AtfCodeQualityAssertionDensityContractTest.java` | Reject as quality oracle | Assertion-cue density cannot replace inspection of assertions and meaningful negative cases |
| `src/main/java/com/ai/core/skills` and `src/main/java/com/ai/skills` | Do not restore | Reuse useful rules as documentation/governance without the retired registry, facades or runtime stack |

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
