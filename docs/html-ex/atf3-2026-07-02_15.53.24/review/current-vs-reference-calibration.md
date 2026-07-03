# Current Package vs Reference Calibration

Reference read live:
`C:\work\ex\java\MCP\html-ex\atf3-2026-07-02_08.57.16`.

## Verdict

Use `docs/out/2026-07-02_15.53.24` as the active ATF3 package candidate.
Keep `html-ex/atf3-2026-07-02_08.57.16` as the archived 94 calibration baseline.

## Score Truth

| Package | MCP gate | Replacement score | Meaning |
|---|---:|---:|---|
| `html-ex/atf3-2026-07-02_08.57.16` | 93/93 close-out PASS | 94 | Clean archived baseline for pre-checkout cart-line lifecycle. |
| `docs/out/2026-07-02_15.53.24` | 93/93 close-out PASS, published gate 95/cap 95 | 94 | Fresher active evidence package with per-run XML parity, clearer ownership/frontier graph and same honest semantic cap. |

## Why Current Replaces It

- Current graph is more explicit about owned vs frontier objects:
  shopper, catalog product, cart-line record, quantity mutation,
  removal/reopen, checkout boundary and payment ledger frontier.
- Current package separates score truths:
  MCP gate is 95/cap 95, while replacement readiness remains 94.
- Current package adds stronger artifact governance:
  per-run XML parity, calibrated score truth, package-state sync and
  refreshed Extent origin/hash.

## Why It Still Stays Beside It

Both packages stop before payment settlement ledger ownership, invoice ownership
and account-bound restore. That is why the semantic replacement score remains
94 rather than 95.
