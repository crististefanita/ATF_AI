<!-- Generated from ../html_EN/mcp/reporting.html. Keep source of truth in html_EN. -->
<!-- Source stylesheet: [shared-report-reference.css](../assets/shared-report-reference.css) -->

# Reporting Standard `OPENING` `GRAPH` `PROOF` `CLOSE-OUT`

- Defines the canonical shape for `analysis/langgraph-business-understanding.html`.
- Publishes the frozen truth: business, proof, accounting, next move.
- Does not reinterpret runtime, launch legality, or production artifacts.



## Overview

| Badge | Read here |
| --- | --- |
| `OPENING` | hero, runtime strip, score cards, Score Overview, compact accounting |
| `GRAPH` | real application business map + package overlay |
| `PROOF` | claim -> run -> diff -> Extent/XML -> verdict |
| `AUDIT` | run audit, Artifact Index, state files, render proof, and link audit |
| `CLOSE-OUT` | countability, runs consumed, same-round, fresh ROOT_RUN, next move |
| `INVALID` | pretty report that raises the score above business substance or hides missing proof |

<!-- /table -->

| Category | Scope |
| --- | --- |
| Owner | `final page` `published shape` |
| Uses | `frozen truth` `proof links` `state files` |
| Does not produce | `runtime` `launch legality` `production accounting` |
| Appendices | `score overview` `business graph rubric` |

<!-- /table -->

Reporting is read-only publication: it takes the closed truth and makes it navigable.

<details>
<summary>Mandatory structure � reference closed by default</summary>

| Zone | Stays visible at the top? | Cold rule |
| --- | --- | --- |
| Visible top | yes | hero verdict, runtime strip, score wall, quick access to `Score Overview` |
| Folded top | only inside fold | Executive Context, extended accounting, jump links, controls, longer explanations |
| Never top | no | run audit, full Artifact Index, long proof tables, detailed close-out, score argumentation |

<!-- /table -->

| Section | What it must contain |
| --- | --- |
| 1. Opening | hero verdict<br>runtime strip<br>score wall + `Score Overview`<br>accounting compact |
| 1a. Score Overview | short collapsed subsection<br>the shape comes from `score-overview-reference.html`<br>do not reinvent locally |
| 2. Executive verdict | winner truth, strongest unresolved truth, same-round truth, fresh ROOT_RUN truth, and historical mutation versus the previous package |
| 4. Business Flow Graph | macro-map application + package overlay<br>the shape comes from `business-flow-graph-rubric.html` |
| 5. Proof and coverage | proof mapping + Artifact Index; each major claim has run, run diff, Extent/XML, verdict MD, and state file |
| 6. Run audit band | a short band per run: claim, delta, kept, diff, Extent/XML, and feedback MD |
| 7. Closure truth | x / ROUND_TARGET_TEST_COUNT<br>runs consumed = n / META_ITERATION_COUNT<br>target met<br>same-round / fresh ROOT_RUN legality<br>open problems and under-spend |
| 8. Render and audit | canonical page + shared CSS<br>render proof + link audit<br>main sections collapsible<br>`4. Business Flow Graph` open default |

<!-- /table -->
</details>

<details open>
<summary>1. Flows and diagrams � form and proof of the final page</summary>

<details>
<summary>1.1 Quick reading map � short orientation</summary>

| If you ask | Open | You learn cold |
| --- | --- | --- |
| what should the first screen look like? | `1.2` | order hero -> runtime -> score cards -> Score Overview -> accounting -> verdict |
| how does reporting read frozen truth? | `1.2.1` | read-only state machine of the final page |
| how should the application, not the package, be drawn? | `1.3` | macro-business map + local overlay |
| how do I link the claim to proof? | `1.4` | claim chain -> run -> diff -> Extent -> verdict |
| what truths must I not mix? | `1.5` | countability, runs consumed, same-package, fresh-root |

<!-- /table -->

- `1.2` = opening.
- `1.3` = map application.
- `1.4` = proof.
- `1.5` = separate verdicts.
</details>

<details open>
<summary>1.2 Opening shape � first-screen shape</summary>

<!-- diagram-readable-table -->
| Opening block | Reader learns | Keep it |
| --- | --- | --- |
| Hero | short verdict | business-first |
| Runtime strip | active values | compact |
| Score cards | visible axes | canonical |
| Score Overview | why Overall sits there | separate subsection |
| Accounting compact | target / run truth | integrated, not separate |
| Section 2 | executive verdict | below the opening |
<!-- /table -->

```text
hero section
  -> runtime strip
     -> score cards
        -> Score Overview
           -> accounting compact
              -> executive verdict
```

<details>
<summary>Optional Mermaid view</summary>

```mermaid
flowchart LR
    Hero["Hero section"] --> Runtime["Runtime strip"]
    Runtime --> Cards["Score cards"]
    Cards --> Overall["Score Overview"]
    Overall --> Accounting["Accounting compact"]
    Accounting --> Verdict["Executive verdict"]
```

</details>

<details>
<summary>Legend</summary>

- Score cards and `Score Overview` are not the same block; the overview remains a separate subsection.
- Accounting must be integrated after the score area, not as a weak separate helper.
- Jump links are good only if they do not fill the opening with noise.
</details>

</details>

<details>
<summary>1.2.1 Minimum reporting state machine � truth frozen -> final page</summary>

```text
read_package_state

-> read_frozen_truth

-> build_business_graph

-> map_claims_to_proof

-> assemble_run_audit

-> render_and_link_audit

-> publish_final_html
```

| Step | What you read | What you must not do |
| --- | --- | --- |
| `read_package_state` | state files and close-out carriers | do not invent new values or new legality |
| `build_business_graph` | application macro-map + package overlay | do not turn the graph into a run journal |
| `map_claims_to_proof` | run diff, Extent, XML, decisive markdown artifacts | do not leave claims without owner and without proof |
| `render_and_link_audit` | CSS common, render proof, link audit | do not publish a beautiful but unauditable page |

<!-- /table -->

- Reporting does not rejudge.
- Read the already frozen state and truth.
- Verify whether the final page is navigable and cold.
</details>

<details>
<summary>1.3 Business Flow Graph � application map + package overlay</summary>

- Here reporting validates only shape.
- The canonical LangGraph lives in `business-flow-graph-rubric.html`.
- The rubric, examples, and bad smells live there too.

| Loc | Role |
| --- | --- |
| `1.3` | validates the graph shape and points to the reusable rubric |
| `4` | describes the mandatory minimum content of the final report |

<!-- /table -->

- `1.3` does not duplicate section `4`.
- Shape is calibrated in the rubric.
- Published content remains in the report.
</details>

<details>
<summary>1.4 Claim -> proof -> artifact � minimum publication chain</summary>

<!-- diagram-readable-table -->
| Proof link | Publishes | Must point to |
| --- | --- | --- |
| Business claim | what the report says happened | business object or rupture |
| Run | local owner of the claim | run audit band |
| Diff | source evolution | run diff / root diff |
| Extent | execution evidence | visual/report proof when available |
| Verdict MD | owned / partial / blocked truth | frozen review artifact |
<!-- /table -->

```text
business claim
  -> owner run
     -> run diff
        -> Extent or machine proof
           -> frozen verdict artifact
```

<details>
<summary>Optional Mermaid view</summary>

```mermaid
flowchart LR
    Claim["Business claim"] --> Run["Owner run"]
    Run --> Diff["Run diff"]
    Diff --> Extent["Extent or machine proof"]
    Extent --> Verdict["Frozen verdict artifact"]
```

</details>

<details>
<summary>Legend</summary>

- Every published claim must have an owner and navigable proof.
- If the owner, diff, or proof carrier is missing, the claim is not cold.
- The published claim must sound higher than the test name that supports it.
</details>

</details>

<details>
<summary>1.5 Truths that must not be mixed � countability, runs consumed, same-round, fresh-root</summary>

<!-- diagram-readable-table -->
| Truth | Published as | Must not be confused with |
| --- | --- | --- |
| Countability | `x / ROUND_TARGET_TEST_COUNT` | number of run slots |
| Runs consumed | spent slots against `META_ITERATION_COUNT` | number of tests |
| Same-package blocked | no more continuation here | fresh ROOT_RUN legality |
| Fresh ROOT_RUN legal | next package possible | automatic launch readiness |
<!-- /table -->

```text
countability
  -> runs consumed
     -> same-package truth
        -> fresh-root truth
```

<details>
<summary>Optional Mermaid view</summary>

```mermaid
flowchart LR
    Count["Countability"] --> Runs["Runs consumed"]
    Runs --> Same["Same-package truth"]
    Same --> Fresh["Fresh-root truth"]
```

</details>

<details>
<summary>Legend</summary>

- These four verdicts are different even if they appear in the same close-out.
- `same-package blocked` does not automatically mean `fresh ROOT_RUN legal = no`.
- If these truths are mixed, the report sounds safer than it is.
</details>

</details>

</details>

<details>
<summary>2. Opening `OPENING` � hero, score wall, Score Overview, accounting compact</summary>

<details>
<summary>2.1 Opening Contract Card � what remains visible and what moves down</summary>

| Visible top | Collapsed / lower |
| --- | --- |
| hero verdict | long scoring explanations |
| runtime strip | runtime doctrine |
| visible score wall + collapsed Score Overview | self-defense argumentation |
| accounting compact | wide audit tables |
| short executive verdict | test, repair, helper, and selector details |

<!-- /table -->

- The report publishes; it does not rejudge.
- Every important claim has a local hyperlink to proof.
- The final published shape lives here, not in the execution documents.
- All main sections are collapsible.
- The only main section open by default: `4. Business Flow Graph`.
</details>

<details>
<summary>2.2 Mandatory opening elements � hero + score wall visible; heavy context collapsed</summary>

| Area | Required model | Cold rule |
| --- | --- | --- |
| Hero | short title + state badge | state the package truth, not marketing |
| Score wall | visible cards for the canonical axes, including `Overall` | values follow `score-overview-reference.html` |
| Executive Context | collapsed subsection with runtime pills, short context, owned / blocked | do not keep heavy context open by default |
| Read First | mini-card with root diff or main historical artifact | the first link must help cold reopening |
| Compiled State | mini-card with `package-state.json` | the compiled state is not hidden in Artifact Index |
| Execution Gate | mini-card with `execution-gate-card.md` | package completeness must be quickly verifiable |
| Jump links | short links to Score Overview, Business Flow Graph, Proof, Close-Out | navigation, not ornament |
| Collapse | after opening, the main sections are collapsible | in final report, only Business Flow Graph remains open by default |

<!-- /table -->

- Good model: `score wall` visible, heavy context in `Executive Context` collapsed.
- Do not put wide tables, full audit, or long explanations on the first screen.
- Runtime and counters appear as short pills; their doctrine remains in the owner artifacts.
</details>

<details>
<summary>2.3 Accounting Compact � short form published in the opening</summary>

```text
Accounting Compact

  tests produced = x / ROUND_TARGET_TEST_COUNT

  runs consumed = n / META_ITERATION_COUNT

  breadth = broad / partial / narrow

  same-round continuation = yes / no / blocked

  fresh ROOT_RUN = yes / no

  owner = quality-accounting-verdict.md
```
</details>
</details>

<details>
<summary>3. Executive verdict `CLOSE-OUT` � winner, under-spend, next move</summary>

| Block | Minimum content |
| --- | --- |
| Winner truth | what new business became cold, owned, and countable |
| Strongest unresolved truth | which relevant frontier remained partial, blocked, or fresh-round-only |
| Same-round stop truth | why same-package continuation is legal or not |
| Fresh ROOT_RUN truth | whether the next step must move into a new package |
| Historical mutation | in 1-3 lines: what this package changed versus the previous package, with an early link to `root-source-thin-no-index.diff` |

<!-- /table -->

- The final report must not become a long journal.
- Still, the opening must have a short historical mutation.
- Without it, diffs remain too low and hard to read pedagogically.

<details>
<summary>3.1 Cold reading discipline � opening shape, good order, cold benchmark</summary>

<!-- diagram-readable-table -->
| Reading order | Contains | Reader should understand |
| --- | --- | --- |
| Opening | hero + cards + accounting | verdict before tests |
| Verdict | winner + under-spend + next move | why the package matters |
| Graph | business story | application map + package overlay |
| Proof mapping | claim -> run -> proof | how to reopen evidence |
| Audit | run strip | what each run changed |
| Close-out | final truth | what continues or stops |
<!-- /table -->

```text
final page shape
  -> verdict
     -> Business Flow Graph
        -> proof and coverage
           -> run audit band
              -> closure truth
```

<details>
<summary>Optional Mermaid view</summary>

```mermaid
flowchart LR
    Opening["Final page shape"] --> Verdict["Verdict"]
    Verdict --> Graph["Business Flow Graph"]
    Graph --> Proof["Proof and coverage"]
    Proof --> Audit["Run audit band"]
    Audit --> CloseOut["Closure truth"]
```

</details>

<details>
<summary>Legend</summary>

- Do not let the opening become a technical index.
- Do not let tests explain the business before the verdict.
- Do not lift the heavy payload above proof mapping.
</details>

| Rule | Cold form |
| --- | --- |
| Opening shape | hero -> runtime strip -> canonical score wall<br>Score Overview -> integrated accounting -> section 3<br>without a separate contradictory block between score and verdict |
| Opening clutter | do not publish accounting, yield, or weak comparator helper as a separate opening block |
| Reading order | the reader must understand the verdict and the business first, not the tests or the repair trail |
| Payload moved down | run ids, test names, repair steps<br>selectors, concrete payload, micro-mutations<br>location: legend, proof mapping, audit strip, artifact index |
| Benchmark cold | for form comparison, reopen the benchmarks selected in pretraining; there is no single rigid teacher |

<!-- /table -->

If the reader must understand the tests first to understand the business winner, the report is wrong.

</details>

</details>

<details>
<summary>4. Business Flow Graph `GRAPH` � the pedagogical center of the report</summary>

- Only the mandatory final-report slot remains here.
- In this requirements document: collapsed by default.
- In the final report: the only section open by default.
- Form and validation come from `business-flow-graph-rubric.html`.

| In the report this must be published here | Minimum accepted |
| --- | --- |
| macro-map application | real product lanes, not a run list |
| package overlay | what became owned, partial, blocked, or fresh-round-only |
| legend collapsed | test details, run ids, method, and repair trail stay below the graph, not inside nodes |
| link to proof | send the reader to proof mapping, run audit, and Artifact Index |

<!-- /table -->

- Application is map.
- The package colors only what it touched.
- Drawing and validation: `business-flow-graph-rubric.html`.
</details>

<details>
<summary>5. Proof and coverage `PROOF` � mapping, diff, Extent, artifact index</summary>

<details>
<summary>5.1 Ownership = truth closed � claim -> run -> proof</summary>

| Column | What must exist |
| --- | --- |
| Business claim | cold claim about a business step or identity, not only about a green test |
| Run owner | link to `runs/run_##/SUMMARY.md` |
| Evolution truth | link to `runs/run_##/review/thin-no-index.diff` for run claim; `root-source-thin-no-index.diff` only for package synthesis |
| UI proof | link to `ExtentReport_*.html` for UI packages |
| Machine proof | link to XML or `command-result.md` |
| Cold verdict | owned / partial / blocked / fresh-round-only |

<!-- /table -->

```text
run diff = small mutation

root diff = compiled mutation

carrier = support only if the diff is not enough
```

- Every important claim has a link to diff.
- Every claim also has a historical mutation sentence.
- Say what opened, closed, or narrowed.

- Without run diff, the claim has no complete cold history.
- The root diff does not compensate for the missing small diff.
</details>

<details>
<summary>5.2 Artifact Index = where you reopen proof � what must be hyperlinked</summary>

- Formula: `claim major -> run -> run diff -> Extent/XML -> verdict MD -> state file -> status`.
- One row per major claim published in Verdict, Business Flow Graph, or Proof Mapping.
- The raw inventory stays in the frozen inventory.

| family | Mandatory artifact | Role |
| --- | --- | --- |
| Runtime | `agent-runtime.properties` | common truth of thresholds and paths |
| Evolution | `runs/run_##/review/thin-no-index.diff` + `review/root-source-thin-no-index.diff` | run diff for local mutation; root diff for package compilation |
| Proof UI | `ExtentReport_*.html` | coloana principala de proof for UI |
| Proof machine | `testng-results.xml`, `TEST-*.xml`, `command-result.md` | machine-readable execution truth |
| Run audit | `SUMMARY.md`, `artifact-checklist.md`, `score-support.md` | truth kept/countable |
| Governance | `quality-accounting-verdict.md`, `next-run-eligibility-card.md`, `package-close-out.md`, `package-state.json`, `execution-gate-card.md` | accounting, legality, close-out, and compiled state |
| Per-run state files | `runs/run_##/review/run-state.json` | links every major claim to the cold run state that produced it |
| Constructive learning | any `*.md` that influenced the verdict | auditable feedback from the agent |
| Publication | `shared-report-reference.css`, `canonical-render-check.png`, `local-link-check.txt` | render proof and link reopenability proof |

<!-- /table -->

Artifact Index is mandatory per major claim; extra value does not replace it.

| Claim major | Run | Run diff / source | Extent / XML | Verdict MD | State file | Status |
| --- | --- | --- | --- | --- | --- | --- |
| claim published in verdict, graph, or proof mapping | `run_##` or `package` | link to diff/source or `missing` | link to Extent/XML or `n/a` | link to the MD that owns the verdict | link to `run-state.json` or `package-state.json` | `linked / missing / n/a` |

<!-- /table -->

- Artifact Index is not decorative inventory.
- Claim without a complete chain remains partial.

- claim -> run diff
- synthesis -> root diff
- carrier -> only when the claim requires additional interpretation

- State files appear here and in Close-Out.
- `package-state.json` = compiled truth.
- `run-state.json` = heavy claim dependent on one slot.
</details>

<details>
<summary>5.3 Allowed extra value � freedom after the mandatory skeleton</summary>

| You may add | When it is worth it | Cold condition |
| --- | --- | --- |
| extra business subgraphs | when one large graph becomes hard to read | short collapsed legend under each subgraph |
| mini blocker matrix | when blockers must be separated more clearly | each row has a link to proof |
| short local comparator | when a real comparator clarifies the standing | so it does not become a vanity strip |
| learning notes per frontier | when saved feedback materially changes the next run | only with local markdown hyperlinked |
| specialized proof subsections | when a flow needs separate explanation for diff, Extent, XML, or render | do not duplicate the Artifact Index |

<!-- /table -->

- The mandatory skeleton comes first.
- After it, the agent may add useful subsections.
- Condition: it must improve reopenability clarity.

- missing `package-state.json` = report partial.
- missing promised run-state files = partial report.
- Beautiful HTML does not compensate for missing state files.
</details>
</details>

<details>
<summary>6. Run audit band `AUDIT` � one cold band per run</summary>

| Column | Minimum content |
| --- | --- |
| Run | `run_##` + link to `SUMMARY.md` |
| Business claim | what it tried to defend and why that run was worth consuming |
| Expected delta | what kind of novelty or depth was expected |
| Actual delta | what materialized cold and what historical mutation it added versus the previous state |
| Kept? | yes / no |
| Why countable / not countable | short cold reason |
| Diff | link to the relevant small run diff; not only to root diff |
| Extent | link for UI or `n/a` explicit |
| Feedback | link to saved constructive feedback |

<!-- /table -->

- Only root diff = partial history.
- Run audit must lead to the small deltas per run.

- Order good: claim -> run -> run diff -> Extent/XML -> verdict.
- Another order slows historical reading.

- Run with changed source requires a link to `runs/run_##/review/thin-no-index.diff`.
- Without a link, the row is publicly incomplete.

- What did the run prove?
- What changed in package history?
</details>

<details>
<summary>7. Closure truth `CLOSE-OUT` � countability, legality, next move</summary>

<details>
<summary>7.1 Separate publication of closure truths � separates countability, runs consumed, and next move</summary>

| Must stay separate | What you publish |
| --- | --- |
| Countability | `materially turned into tests = x / ROUND_TARGET_TEST_COUNT` |
| Runs consumed | `runs consumed = n / META_ITERATION_COUNT` |
| Accounting target | `accounting target met = yes / no` |
| Business breadth verdict | `broad / partial / narrow` + cold reason |
| Same-round continuation | `same-round continuation legal = yes / no` |
| Fresh ROOT_RUN legality | `fresh ROOT_RUN legal = yes / no` |
| Open problems | the hardest remaining truths and any unresolved under-spend on tests or runs |

<!-- /table -->

- Do not mix into a single sentence:
- countability, breadth, runs consumed, yield, application exhaustion.
- Counters remain separate.

- The report publishes accounting and close-out already decided.
- Do not move target-production theory here.
- Shows clearly what came out and the final verdict.

- Close-Out shows `package-state.json`.
- Close-Out shows `execution-gate-card.md`.
- If the verdict depends on a run, show `run-state.json` too.
</details>
</details>

<details>
<summary>8. Render and audit `AUDIT` � style, screenshot, hyperlinks</summary>

| Check | Must be published |
| --- | --- |
| Canonical page exists | `analysis/langgraph-business-understanding.html` |
| Shared CSS used | local link to `shared-report-reference.css` |
| Render proof | `canonical-render-check.png` |
| Link audit | `local-link-check.txt` |
| Collapsible discipline | large collapsible sections, short legends under subgraphs |

<!-- /table -->

- `canonical-render-check.png` must show the real report.
- `not found`, gray screen, cookie wall, or foreign page = invalid.
- The report fails cold even if the HTML and links exist.

- Without valid render proof: `New Artifacts Contract` remains at most partial.
- The publishing blocker must be linked explicitly.

<details>
<summary>8.1 MCP report contract `MCP` � what the tests verify automatically</summary>

| Report area | MCP rule | Invalid if |
| --- | --- | --- |
| CSS | `analysis/shared-report-reference.css` is the exact copy of canonical CSS | the report overrides styling through inline CSS or altered CSS |
| Collapse policy | main sections are collapsible; only Business Flow Graph remains open by default | Executive Context, Score, or audit stays open by default |
| Proof Mapping | claim -> run diff -> state -> execution proof -> cold verdict | the claim has only nice text, without an evidence row |
| Score Overview | `Overall` respects the cap and canonical axes | reporting raises the score above substance or state |
| MCP status | MCP-green means auditable; it does not automatically mean `html-ex`-ready | the report sells MCP green as teacher-grade score |
| MCP close-out profile | final report is based on `testng-mcp-package-closeout.xml`, publishes total/passed for this subcollection, and has 0 failures, 0 errors, 0 skipped | report mixes publishability with `testng-mcp-candidate.xml`, hides counts, or closes with skipped/failures |
| MCP close-out evidence | Artifact Index links `validation/mcp/ExtentReport.html`, `ExtentReport.sha256`, `extent-origin.json`, gate result, XML, and `full-mcp-method-ledger.md` | the report says MCP PASS but hides the Extent report or final ledger |
| MCP skipped disclosure | if a diagnostic MCP sweep has skipped tests, the page publishes separately total/passed/skipped, reason, `accepted=true`, cap, and ledger link | skipped appear only in the ledger/Extent and the reader cannot tell why they are not close-out |
| External language | the final report does not mention `html-ex`, benchmark, or teacher | external calibration enters the published report |

<!-- /table -->

| Stage | Profile | Role |
| --- | --- | --- |
| MCP self health | `testng-mcp-self.xml` | health of the validator |
| Run preflight | `testng-mcp-run-preflight.xml` | run evidence plus runtime/package accounting before package audit |
| Full sweep | `testng-mcp-full.xml` | broad, secondary diagnosis |
| Package audit | `testng-mcp-package-audit.xml` | pre-closeout |
| Final close-out | `testng-mcp-package-closeout.xml` | basis of the final report |
| Post-sync smoke | `testng-mcp-post-sync-smoke.xml` | fast verification after copying/link updates that the final report and validation/mcp still agree |
| Final report close-out | `testng-mcp-final-report-closeout.xml` | last gate for final report form, hyperlinks, counts and browser-render evidence |
| Candidate/html-ex | `testng-mcp-candidate.xml` | html-ex promotion: teacher-grade plus post-sync and final-report close-out |

<!-- /table -->

- The package is not closed only because `mcp-gate-result.md` is green.
- The package closes only after the final profile `testng-mcp-package-closeout.xml` is fully green and after `testng-mcp-final-report-closeout.xml` is green if the final report has been composed; candidate/html-ex runs separately and becomes required before copying into `html-ex`.
- The final auditable command is `mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-package-closeout.xml" "-Dmcp.package.root=<PACKAGE_ROOT>" "-Dmcp.project.root=<PROJECT_ROOT>" "-DforkCount=0" test`; `validation/mcp/ExtentReport.html`, XML, and `extent-origin.json` must prove exactly this subcollection.
- After any final artifact/link update, run `testng-mcp-post-sync-smoke.xml`; it must be fully green and cannot replace close-out.
- After the final report is composed and the agent has no planned changes, run `testng-mcp-final-report-closeout.xml`; this is the only place for browser/render and final publishable page checks.
- For `html-ex` promotion, then run `testng-mcp-candidate.xml`; it repeats essential final checks so copied packages do not fail after replacement.
- `testng-mcp-full.xml` may be linked only as a separate health sweep; it is not the final package close-out Extent.
- The last readable surface is `analysis/langgraph-business-understanding.html`; it must explain MCP close-out and, separately, any accepted skipped tests from diagnostic sweeps.
- Do not copy the explanation into README as a substitute. README may link, but the final report carries the publishable truth.

```text
Minimum MCP close-out summary in the final report
profile = testng-mcp-package-closeout.xml
command = mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-package-closeout.xml" "-Dmcp.package.root=<PACKAGE_ROOT>" "-Dmcp.project.root=<PROJECT_ROOT>" "-DforkCount=0" test
result = total N, passed N, failures 0, errors 0, skipped 0
primary Extent = validation/mcp/ExtentReport.html
machine XML = validation/mcp/TEST-TestSuite.xml or validation/mcp/testng-results.xml
provenance = validation/mcp/extent-origin.json + validation/mcp/ExtentReport.sha256
ledger = validation/mcp/full-mcp-method-ledger.md
gate = validation/mcp/mcp-gate-result.md
kpi matrix = validation/mcp/mcp-kpi-matrix.md + validation/mcp/mcp-kpi-matrix.html
kpi formula trace = validation/mcp/mcp-kpi-formula-trace.md
kpi repair board = validation/mcp/mcp-kpi-repair-board.md + validation/mcp/mcp-kpi-repair-board.html
kpi score cap rationale = validation/mcp/mcp-score-cap-rationale.md
full sweep = diagnostic only / not used as final close-out
candidate html-ex = separate / n/a
```
</details>
</details>
