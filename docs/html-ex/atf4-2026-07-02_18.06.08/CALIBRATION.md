# Calibration

Compared live against the active `C:\work\ex\java\MCP\html-ex\README.md`
library and the latest local ATF1/ATF3/ATF4 roots on 2026-07-02.

## Score Truth

This package remains **95/100** after cleanup because it owns a new registered
account payment settlement ledger over the saved ATF4 `html-ex` cart/invoice
support frontier. The score is not promoted above 95 because refund decision
history, refund settlement, chargeback/dispute state history and full carrier
tracking are not owned.

## Ranking Calibration

| Rank | Candidate | Calculated score | Reason |
|---:|---|---:|---|
| 1 | ATF4 latest `2026-07-02_18.06.08` | 95 | Owns registered account payment settlement ledger; MCP close-out 93/93 PASS; final report now aligns run map, cap and frontier. |
| 2 | ATF1 latest `2026-07-02_18.00.17` | 95 | Owns customer-visible support case history after admin reply; MCP close-out 93/93 PASS; capped by refund decision/settlement history. |
| 3 | `html-ex/atf4-2026-07-02_10.59.07` | 94 | Best saved ATF4 corpus reference; package public cap remains lower because payment settlement was not owned there. |
| 4 | `html-ex/atf3-2026-07-02_15.53.24` | 94 | Clean active ATF3 cart-line lifecycle reference with repaired XML parity, still pre-payment. |
| 5 | `html-ex/atf1-2026-07-02_08.30.31` | 94 | Strong admin order status lifecycle exemplar, but customer-visible refund/support progression is weaker than latest ATF1. |
| 6 | ATF3 latest `2026-07-02_18.49.59` | 91 | README claims 95, but package-state/frozen inventory/gate evidence still point to `2026-07-02_15.53.24`; the timestamped root is not a fully regenerated close-out package. |
| 7 | `html-ex/atf4-2026-07-01_12.33.23` | 90 | Useful historical report-shape reference, but older proof slices and stale contracts make it lower than the active corpus. |

## Report Repair Applied

`analysis/langgraph-business-understanding.html` was repaired to remove stale
cart/favorite/support-first narrative carried from earlier runs. The final
report now maps:

- `run_01` = registered account payment ledger.
- `run_02` = payment settlement handshake.
- `run_03` = account invoice ledger.
- `run_04` = staff support response.
- `run_05` = fulfillment timeline boundary.

The report follows the current MCP shell contract: `Business Flow Graph` is the
only default-open `<details>` section; `details#mcp-ai-task-advisor` remains
collapsed by default.
