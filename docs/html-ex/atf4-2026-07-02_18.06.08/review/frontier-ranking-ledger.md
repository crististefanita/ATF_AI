# Frontier Ranking Ledger

Live calibration date: 2026-07-02.

This ledger compares the current ATF4 package with the latest local ATF1,
ATF3 and ATF4 roots plus the active `C:\work\ex\java\MCP\html-ex\README.md`
library. Scores below are reviewer scores, not copied package claims.

| Rank | Candidate | Score | Evidence basis | Cap reason |
|---:|---|---:|---|---|
| 1 | ATF4 latest `2026-07-02_18.06.08` | 95 | Current-root MCP close-out 93/93 PASS, 25/25 UI, registered account payment settlement ledger. | Refund lifecycle, refund settlement and chargeback/dispute history remain unowned. |
| 2 | ATF1 latest `2026-07-02_18.00.17` | 95 | Current-root MCP close-out 93/93 PASS, 25/25 UI, customer-visible support case history after admin reply. | Refund decision and settlement outcome history remain unowned. |
| 3 | `html-ex/atf4-2026-07-02_10.59.07` | 94 | Active saved ATF4 corpus reference, post-checkout cart-to-invoice audit record, MCP 93/93 PASS. | Payment settlement ledger is not owned in that saved exemplar. |
| 4 | `html-ex/atf3-2026-07-02_15.53.24` | 94 | Active saved ATF3 corpus reference with repaired per-run XML parity and MCP 93/93 PASS. | Still a pre-payment cart-line lifecycle reference. |
| 5 | `html-ex/atf1-2026-07-02_08.30.31` | 94 | Active saved ATF1 reference for admin order status lifecycle, MCP 93/93 PASS. | Weaker than latest ATF1 because customer-visible support/refund progression is not owned. |
| 6 | ATF3 latest `2026-07-02_18.49.59` | 91 | Business README is stronger, but frozen package-state, inventory and MCP gate still point to `2026-07-02_15.53.24`. | Timestamped root is not a fully regenerated close-out package. |
| 7 | `html-ex/atf4-2026-07-01_12.33.23` | 90 | Useful historical report-shape exemplar. | Older proof slices and stale current-contract assumptions reduce replacement value. |

Decision: ATF4 latest remains rank 1 at **95/100**, not 96. It is a strong
copy candidate over the weakest retained `html-ex` reference, but the next
maximal frontier must move to refund lifecycle, settlement reversal or
chargeback/dispute history instead of repeating invoice/support/cart surfaces.
