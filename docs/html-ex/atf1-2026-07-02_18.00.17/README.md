# ATF1 MCP Package Close-Out

## HTML-ex Curation Note

Refreshed again on 2026-07-03 from the latest ATF1 out root:
`C:\work\ex\java\demo\atf1\test-framework\docs\out\2026-07-02_18.00.17`.

The source package was copied as the report/artifact truth, then
`validation/mcp/full-mcp-method-ledger.md` was regenerated from
`validation/mcp/testng-results.xml` because the copied ledger text still
contained stale `FAIL` rows while the frozen TestNG XML and MCP gate record
`93/93` PASS. No tests were rerun for this html-ex repair.

- packageRoot: `C:\work\ex\java\demo\atf1\test-framework\docs\out\2026-07-02_18.00.17`
- projectRoot: `C:\work\ex\java\demo\atf1\test-framework`
- entryPoint: `docs/prompts/html_ro/mcp/orchestrate-iterative-runs.html`
- runtimeQualityTarget: 95
- roundTargetTestCount: 5
- metaIterationCount: 5
- runsConsumed: 5 / 5
- testsProduced: 25 / 25
- score: 95 / 100
- cap: 95 / 100
- Cold Overall package score: 95 / 100
- Cold overall remains: 95/100

| Metric | Value |
|---|---:|
| Overall package | 95 |

## Verdict

This package is publishable at **95/100**.

The owned business object is **customer-visible support case history after an
admin reply**. The proof chain starts from a purchased invoice, creates a refund
support request, progresses it through an admin reply over the messages API,
moves the case to `IN_PROGRESS`, and proves that the customer can reopen the
progressed case from account messages.

The package does **not** claim refund approval, denial, payment reversal,
settlement, or refund decision history. Those remain the next business frontier.

## Runtime Truth

| Field | Value | Reading |
|---|---:|---|
| `runtimeQualityTarget` | 95 | Package-level target published by `review/package-state.json`. |
| `overall` | 95 | Final package score. |
| `cap` | 95 | Score is capped by the next unowned business object, not by an MCP blocker. |
| `run_expected_delta_quality` | 92 | Frozen prompt runtime value captured by the package contract. |
| `ROUND_TARGET_TEST_COUNT` | 5 | Expected tests per run. |
| `META_ITERATION_COUNT` | 5 | Expected run slots. |

Important: `docs/prompts/html_ro/mcp/agent-runtime.properties` is a protected
runtime prompt artifact. Raising it inside this closed package is invalid
protected drift. The current package still meets the requested 95+ business
quality through `runtimeQualityTarget=95`, `overall=95`, and `cap=95` in the
frozen package state.

## Run Evidence

| Run | Business slice | Tests | Suite | Status |
|---|---|---:|---|---|
| run_01 | Customer message case identity | 5/5 | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` | PASS |
| run_02 | Admin reply progresses case | 5/5 | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` | PASS |
| run_03 | Customer API list reopens progressed case | 5/5 | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` | PASS |
| run_04 | Account messages page shows progressed status | 5/5 | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` | PASS |
| run_05 | Invoice, subject and status remain one proof chain | 5/5 | `src/test/resources/ui/testng-atf1-refund-support-customer-messages-run.xml` | PASS |

## MCP Close-Out

| Gate | Result |
|---|---|
| `testng-mcp-package-closeout.xml` | PASS, frozen XML 93/93, 0 failures, 0 errors, 0 skipped |
| `testng-mcp-post-sync-smoke.xml` | PASS |
| `testng-mcp-final-report-closeout.xml` | PASS |
| `testng-mcp-langgraph-final-report.xml` | PASS |

Final report:
[analysis/langgraph-business-understanding.html](analysis/langgraph-business-understanding.html)

## Frozen MCP Evidence

- [validation/mcp/ExtentReport.html](validation/mcp/ExtentReport.html)
- [validation/mcp/ExtentReport.sha256](validation/mcp/ExtentReport.sha256)
- [validation/mcp/extent-origin.json](validation/mcp/extent-origin.json)
- [validation/mcp/mcp-gate-result.md](validation/mcp/mcp-gate-result.md)
- [validation/mcp/full-mcp-method-ledger.md](validation/mcp/full-mcp-method-ledger.md)
- [validation/mcp/testng-results.xml](validation/mcp/testng-results.xml)
- [validation/mcp/TEST-TestSuite.xml](validation/mcp/TEST-TestSuite.xml)
- [validation/mcp/mcp-kpi-matrix.md](validation/mcp/mcp-kpi-matrix.md)
- [validation/mcp/mcp-kpi-matrix.html](validation/mcp/mcp-kpi-matrix.html)
- [validation/mcp/mcp-kpi-formula-trace.md](validation/mcp/mcp-kpi-formula-trace.md)
- [validation/mcp/mcp-kpi-repair-board.md](validation/mcp/mcp-kpi-repair-board.md)
- [validation/mcp/mcp-kpi-repair-board.html](validation/mcp/mcp-kpi-repair-board.html)
- [validation/mcp/mcp-score-cap-rationale.md](validation/mcp/mcp-score-cap-rationale.md)

## Score Breakdown

| Axis | Score | Reading |
|---|---:|---|
| Business ownership | 95 | Owns customer-visible support case history after admin reply. |
| Execution evidence | 96 | 25/25 UI run tests plus final MCP frozen XML 93/93 close-out. |
| Artifact truth | 95 | Origin, SHA, XML, gate and final report are synchronized on this root. |
| OOP/source shape | 93 | Good generated support shape; remaining repeated action/assertion pressure prevents 96+. |
| Replacement value | 95 | Stronger than the older ATF1 html-ex reference because the proof is customer-visible, not only admin/status-side. |

Final score stays at **95/100**. Do not promote to 96+ without a fresh package
that owns refund decision history, settlement outcome history, or an equivalent
new customer-visible business object.

## Comparison Snapshot

| Rank | Candidate | Score | Reading |
|---:|---|---:|---|
| 1 | ATF4 latest `2026-07-02_18.06.08` | 95 | Equal score, slightly broader payment-settlement frontier. |
| 2 | ATF1 latest `2026-07-02_18.00.17` | 95 | This package; customer-visible support case history after admin reply. |
| 3 | `html-ex/atf4-2026-07-02_10.59.07` | 94 | Strong older ATF4 reference. |
| 4 | `html-ex/atf3-2026-07-02_15.53.24` | 94 | Strong ATF3 cart-line lifecycle reference. |
| 5 | `html-ex/atf1-2026-07-02_08.30.31` | 93 | Useful historical ATF1 reference, superseded by this package. |
| 6 | ATF3 latest `2026-07-02_18.49.59` | 92 reviewer / 90 copy | Needs regenerated close-out/origin before copy-readiness. |
| 7 | `html-ex/atf4-2026-07-01_12.33.23` | 90 | Historical reference with stale proof under current calibration. |

## Next Frontier

The next package should target **refund decision history**:

1. admin accepts or rejects the refund/support case;
2. the customer-visible case history records the decision;
3. invoice/order state remains linked to the same business identity;
4. settlement or reversal evidence is surfaced only if the application actually owns it.

That is a fresh root-run, not a mutation of this closed package.

## Presentation Recheck

The stricter report-presentation feedback is closed for this package.

| Feedback item | Resolution |
|---|---|
| Business Flow Graph frontier used a non-canonical `capped` node class and inline frontier stroke. | Fixed in the final HTML: the refund-decision frontier now uses the CSS-backed `cap` node class, and its remaining-pressure edge uses the semantic `edge dashed soft` classes. |
| Artifact Index read like a flat link inventory. | Fixed in the final HTML: artifacts are grouped into narrative evidence families with concrete PASS/count/provenance/freshness status, not repeated `linked proof` filler. |

This does not change the business score. The package remains **95/100**:
publishable, honest about the unowned refund-decision frontier, and synchronized
with the frozen MCP gate.

## Final-Report MCP Gate

The stricter MCP final-report validators have been rerun against this exact
package root. A green package close-out alone is not enough after the stricter
graph and Artifact Index contracts, so the report-specific gates are recorded
here as part of close-out evidence.

Required commands from `C:\work\ex\java\demo\atf1\test-framework`:

```powershell
mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-langgraph-final-report.xml" "-Dmcp.package.root=C:\work\ex\java\demo\atf1\test-framework\docs\out\2026-07-02_18.00.17" "-Dmcp.project.root=C:\work\ex\java\demo\atf1\test-framework" "-DforkCount=0" test
```

```powershell
mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-final-report-closeout.xml" "-Dmcp.package.root=C:\work\ex\java\demo\atf1\test-framework\docs\out\2026-07-02_18.00.17" "-Dmcp.project.root=C:\work\ex\java\demo\atf1\test-framework" "-DforkCount=0" test
```

The final-report MCP gate now proves:

1. Business Flow Graph uses CSS-backed `owned`, `cap` or `blocked` semantics.
2. Frontier/next-object edges use CSS-backed visual cues, not inline stroke
   color as hidden semantics.
3. Artifact Index is organized as readable evidence families, either as a
   well-structured table or cards.
4. Status cells say what was proven: PASS counts, XML/Extent provenance,
   SHA/origin/freshness, not repeated `linked proof` filler.

Current result: both final-report validators PASS, and the frozen MCP XML
evidence is PASS 93/93 with 0 failures, 0 errors and 0 skipped tests. The
current package-audit preflight schedules 97 methods, but the public gate,
origin JSON and final report stay synchronized to the frozen XML count.

## Deep Reviewer Feedback - 2026-07-03

Reviewer score: **95/100**. Copy-ready: **YES**.

The package is strong enough for `html-ex`, but it is not a 96+ package yet.
The owned object is correctly scoped as **customer-visible support case history
after admin reply**. That is materially better than admin-only status evidence,
but it still stops before the business object that would unlock the next tier:
refund decision history or settlement outcome history.

| Axis | Score | Feedback |
|---|---:|---|
| Business ownership | 95 | Good: invoice-backed support case becomes customer-visible after admin progress. Missing for 96+: customer-visible accept/reject/refund decision. |
| MCP provenance | 96 | Good: current-root `93/93` close-out, XML/JUnit parity, SHA/origin, no skipped tests. Keep this exact-root discipline. |
| Report graph | 95 | Good: business-only nodes and explicit Refund Decision frontier. Keep run IDs, Extent, TestNG and MCP outside the business graph. |
| Artifact Index | 94 | Improved, but keep tightening: each row should say exact PASS count, provenance, freshness or cap reason, not generic link language. |
| OOP/source shape | 93 | Support abstractions exist, but repeated action/assertion pressure still caps elegance. Extract reusable decision/history assertions before chasing 96+. |
| Score honesty | 95 | Correct cap. Do not raise because MCP is green; raise only when the next business object is owned. |

Concrete next task for ATF1:

1. Start a fresh root.
2. Own **refund decision history**, not only support intake/progress.
3. Prove admin decision state changes are visible to the customer after reopen.
4. Keep invoice/order identity bridged through the same support case.
5. If settlement is not visible in the app, label it as frontier, not owned.

Do not mutate this closed package to chase score. It is already a clean
95/100 reference; the next gain must come from a new business frontier.
