# Frontier Ranking Ledger

Selected frontier: registered invoice details record.

Why this frontier:
- The reply thread frontier was probed first and rejected because customer reply text did not reappear in the message detail.
- The invoice details record exposes a durable invoice number, date, total, line item, billing address, payment method, detail route, account reopen and relogin recovery.
- The owned object is countable and visible: INV-nnn on account invoice row and details page.

Rejected frontier:
- Support message reply persistence: reply textarea and submit button exist, but no client-visible reply text could be proven after submit.
