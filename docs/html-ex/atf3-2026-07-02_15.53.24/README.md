# README

ATF6 MCP package root: 2026-07-02_15.53.24.

Primary business object: pre-invoice cart line lifecycle record.
Operational target: RUN_EXPECTED_DELTA_QUALITY=95+.
MCP gate score: 95/100.
HTML-ex replacement score: 94/100.
HTML-ex Overall: 94
HTML-ex Cap: 94
MCP gate Overall: 95
MCP gate Cap: 95
Cold HTML-ex replacement score remains: 94/100

| Metric | Value |
| --- | --- |
| MCP gate package score | 95 |
| HTML-ex replacement score | 94 |
| HTML-ex cap | 94 |
| MCP gate cap | 95 |
| Runs completed | 5 |
| Tests passed | 25/25 |

Score honesty note: `MCP gate 95` is preserved as execution-gate evidence for
the owned cart-line lifecycle package. The score that matters inside
`html-ex` is **94/100**, because payment settlement ledger, invoice ownership
and account-bound restore remain the next fresh business frontier and are not
owned by this package.

Evidence: five fresh Playwright UI runs with disjoint method sets, Extent reports, XML, SHA/origin files, command results, run states, source diff, per-run XML parity audit and final report.

Final report: [analysis/langgraph-business-understanding.html](analysis/langgraph-business-understanding.html).

Final MCP close-out artifacts: [validation/mcp/ExtentReport.html](validation/mcp/ExtentReport.html), [validation/mcp/mcp-gate-result.md](validation/mcp/mcp-gate-result.md), [validation/mcp/full-mcp-method-ledger.md](validation/mcp/full-mcp-method-ledger.md), [validation/mcp/TEST-TestSuite.xml](validation/mcp/TEST-TestSuite.xml), [validation/mcp/testng-results.xml](validation/mcp/testng-results.xml), [validation/mcp/mcp-kpi-matrix.md](validation/mcp/mcp-kpi-matrix.md), [validation/mcp/mcp-kpi-matrix.html](validation/mcp/mcp-kpi-matrix.html), [validation/mcp/mcp-kpi-formula-trace.md](validation/mcp/mcp-kpi-formula-trace.md), [validation/mcp/mcp-kpi-repair-board.md](validation/mcp/mcp-kpi-repair-board.md), [validation/mcp/mcp-kpi-repair-board.html](validation/mcp/mcp-kpi-repair-board.html), [validation/mcp/mcp-score-cap-rationale.md](validation/mcp/mcp-score-cap-rationale.md), [review/per-run-xml-parity-audit.md](review/per-run-xml-parity-audit.md), [review/calibrated-score-truth.md](review/calibrated-score-truth.md).

## Post-Feedback Artifact Truth Repair - 2026-07-02

The stale per-run JUnit XML issue called out by the global feedback board has
been repaired. Each `runs/run_##/validation/TEST-TestSuite.xml` now mirrors the
same five UI test methods and zero-failure state as its paired
`testng-results.xml`.

| Run | TestNG | JUnit | Verdict |
|---|---:|---:|---|
| run_01 | 5 passed / 0 failed / 0 skipped | 5 tests / 0 failures / 0 errors / 0 skipped | PASS |
| run_02 | 5 passed / 0 failed / 0 skipped | 5 tests / 0 failures / 0 errors / 0 skipped | PASS |
| run_03 | 5 passed / 0 failed / 0 skipped | 5 tests / 0 failures / 0 errors / 0 skipped | PASS |
| run_04 | 5 passed / 0 failed / 0 skipped | 5 tests / 0 failures / 0 errors / 0 skipped | PASS |
| run_05 | 5 passed / 0 failed / 0 skipped | 5 tests / 0 failures / 0 errors / 0 skipped | PASS |

Audit evidence: [review/per-run-xml-parity-audit.md](review/per-run-xml-parity-audit.md).

## Superseded Codex Deep Feedback - 2026-07-02

Status: superseded by the post-feedback artifact truth repair above. The
stale per-run `TEST-TestSuite.xml` finding is kept below as audit history, but
is no longer the current package state.

Verdict rece: pachetul are cel mai curat KPI matrix dintre ATF1/3/4 si business
focus bun pe cart-line lifecycle, dar nu poate fi inchis onest la 95 cat timp
artefactele per-run `TEST-TestSuite.xml` sunt contaminate. Scorul meu calculat
live: 91/100. Dupa repararea XML-ului per-run poate urca la 93-94; 95 cere fie
artifact truth perfect, fie o trecere reala spre payment settlement ledger.

### Dovezi verificate

| Dovada | Observatie | Impact |
|---|---|---|
| `validation/mcp/mcp-gate-result.md` | MCP closeout este verde: 93/93, 0 failures, 0 errors, 0 skipped. | Forma MCP este solida. |
| `validation/mcp/mcp-score-cap-rationale.md` | Recomanda maximum 100/100, fara KPI risks. | KPI engine nu vede risc numeric. |
| `validation/mcp/mcp-kpi-matrix.md` | `duplicate_action_line_ratio=0`, `duplicate_assertion_line_ratio=0`, `protected_root_dirty_count=0`. | Dupa KPI, pachetul arata curat. |
| `runs/run_01..run_05/validation/testng-results.xml` | Fiecare run are `total=5`, `passed=5`, `failed=0`, `skipped=0`. | TestNG XML-ul UI sustine 25/25. |
| `runs/run_01..run_05/validation/TEST-TestSuite.xml` | Fiecare run arata `tests=93`, `failures=22`, desi run-state spune 5/5 PASS. | Artifact truth defect major; pare JUnit XML gresit/copiat/stale. |
| `review/package-state.json` | Publica 95/95 si business frontier `payment settlement ledger`. | Scorul este prea mare pana se repara per-run XML. |
| `validation/mcp/mcp-kpi-matrix.md` | `support_reuse_ratio=16`, `test_value_density=36`. | Curat, dar inca slab la reutilizare si densitate business. |

### Scor calculat

| Axa | Nota | Motiv |
|---|---:|---|
| Business substance | 92 | Cart identity, quantity, price, removal si reopen sunt coerente, dar raman pre-invoice. |
| Execution proof | 84 | `testng-results.xml` este verde, dar `TEST-TestSuite.xml` per-run contrazice run-state. |
| MCP closeout | 96 | Closeout 93/93 si KPI matrix fara risc sunt foarte bune. |
| Artifact truth | 72 | JUnit XML per-run cu 93/22 failures intr-un run declarat 5/5 este o ruptura de incredere. |
| OOP/test organization | 90 | Pachet mic, root diff 5, duplicare 0; totusi support reuse 16 este slab. |
| Overall calculat | 91 | Business bun si KPI curat, dar artifact truth blocheaza 95. |

### Ce este foarte bun

1. Pachetul este mult mai dens decat ATF1: `root_diff_files_count=5` si 25
   metode adaugate cu duplicare 0.
2. Business-ul este coerent: cart line identity, quantity mutation, removal,
   reopen si audit trail sunt toate in acelasi obiect pre-invoice.
3. MCP closeout si KPI matrix sunt curate.

### Ce trebuie reparat inainte de 95+

1. Regenerati sau inlocuiti `runs/run_##/validation/TEST-TestSuite.xml` cu
   JUnit XML-ul real al celor 5 metode UI. Nu pastrati artefacte cu 93 teste si
   22 failures intr-un run UI declarat PASS.
2. Adaugati un contract MCP care compara per-run `testng-results.xml` cu
   `TEST-TestSuite.xml`: total/passed/failed/skipped trebuie sa fie identice
   pentru run-ul UI, nu doar pentru closeout.
3. Cresteti `support_reuse_ratio`: flow-ul de cart trebuie sa fie vizibil ca
   API reutilizabil, nu doar ca metode in test.
4. Nu mai cresteti scorul prin inca cinci metode pe acelasi cart line. Urmatorul
   frontier real este payment settlement ledger sau account-bound cart restore.

### Verdict

ATF3 are potentialul cel mai bun dintre cele trei pentru un html-ex curat, dar
nu in forma curenta. Cu `TEST-TestSuite.xml` per-run reparat, il vad 93-94.
Pentru 95 trebuie artifact truth perfect si o punte mai puternica spre payment
settlement, nu doar cart pre-invoice.

## Codex Final Live Calibration - 2026-07-02

Scor calculat dupa verificarea reparatiei XML: **94/100**. Reparatia per-run
`TEST-TestSuite.xml` este reala: fiecare run are acum `5 tests / 0 failures /
0 errors / 0 skipped`, aliniat cu `testng-results.xml`.

| Axa | Nota | Motiv |
|---|---:|---|
| Business ownership | 93 | Cart-line lifecycle este curat si dens, dar ramane pre-invoice. |
| Execution evidence | 97 | 25/25 UI si MCP close-out 93/93, fara skip/failure. |
| Artifact truth | 96 | Contradictia JUnit/TestNG a fost reparata si auditata. |
| OOP si dif control | 94 | `root_diff_files_count=5`, duplicare `0/0`, protected root curat. |
| Replacement value vs html-ex | 90 | Este foarte bun, dar acopera acelasi tip de frontier ca html-ex ATF3; nu demonstreaza inca payment settlement ledger. |

Feedback ferm: publicarea ca 95 este inca prea optimista. Ca pachet curent este
94/100 si poate deveni candidat de inlocuire doar daca demonstreaza un salt
business peste html-ex ATF3: payment settlement ledger, account-bound cart
restore dupa login sau legatura cart-to-payment-to-invoice, nu inca cinci teste
pe acelasi cart-line pre-invoice.

## HTML-ex Save Calibration - 2026-07-02

Saved as the active ATF3 `html-ex` reference at
`C:\work\ex\java\MCP\html-ex\atf3-2026-07-02_15.53.24`.

Final saved score: **94/100**. This replaces the older
`atf3-2026-07-02_08.57.16` reference because it has fresher artifact truth,
repaired per-run JUnit/TestNG parity, a better owned/frontier business graph
and the same clean cart-line lifecycle coverage. It is not scored 95 because
the payment settlement ledger, invoice ownership and account-bound cart restore
remain explicitly outside this package.
