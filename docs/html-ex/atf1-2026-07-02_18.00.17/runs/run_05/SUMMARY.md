# run_05 - Reopen consistency

Reopening contact intake preserves Return category, invoice bridge, real contact route and support surface identity.

- suite: `src/test/resources/ui/testng-atf1-refund-support-run-05.xml`
- tests produced: 5 / 5
- status: PASS
- proof: `validation/reports/ExtentReport.html`, `validation/testng-results.xml`, `validation/command-result.md`

UI wrapper handoff: actor = customer; user intent = customer actor reopens contact intake; intent is preserving invoice bridge and Return support category; visible proof = refund support flow evidence; business object = support message / invoice record.

Progression from run_04: moves from one-time confirmation to reopened contact intake consistency for the same refund-support surface.
