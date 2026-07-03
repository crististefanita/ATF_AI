# Calibrated Score Truth

| Score surface | Score | Meaning |
|---|---:|---|
| MCP package gate | 95 | The current package passes the MCP close-out contract for the owned pre-invoice cart-line lifecycle record. |
| Calibrated replacement score | 94 | Artifact truth is repaired, but this package still does not own payment settlement ledger, account-bound cart restore, or cart-to-payment-to-invoice continuity. |

## Why 94 Is The Honest Replacement Score

The package is strong: 25/25 UI tests pass, MCP close-out is 93/93, per-run
TestNG/JUnit XML parity is repaired, duplication ratios are clean, and the root
diff is scoped to five cart-line files.

The package is not a 95+ replacement yet because the next business object is
still only named as a frontier. A 95+ replacement needs fresh proof for at least
one of:

1. payment settlement ledger;
2. account-bound cart restore after authentication;
3. cart-to-payment-to-invoice continuity.

## Current Decision

Stop this same-package run after green MCP close-out. Continue only as a fresh
root that owns one of the frontier objects above.
