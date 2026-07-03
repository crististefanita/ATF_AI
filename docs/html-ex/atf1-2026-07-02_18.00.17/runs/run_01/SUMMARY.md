# run_01 - Invoice bridge

Refund request payload is anchored to a purchased invoice, charged amount, return category and registered customer.

- suite: `src/test/resources/ui/testng-atf1-refund-support-run-01.xml`
- tests produced: 5 / 5
- status: PASS
- proof: `validation/reports/ExtentReport.html`, `validation/testng-results.xml`, `validation/command-result.md`

UI wrapper handoff: actor = customer; user intent = customer actor requests refund support for a purchased invoice; intent is invoice-backed refund ownership; visible proof = refund support flow evidence; business object = support message / invoice record.

Progression from baseline: moves beyond generic invoice visibility by making invoice id, customer and charged amount feed refund support intent.
