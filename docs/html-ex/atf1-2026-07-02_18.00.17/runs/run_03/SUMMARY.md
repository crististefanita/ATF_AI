# run_03 - Messages API

The support request is accepted by the real messages endpoint through POST /messages with a concrete URL and success status.

- suite: `src/test/resources/ui/testng-atf1-refund-support-run-03.xml`
- tests produced: 5 / 5
- status: PASS
- proof: `validation/reports/ExtentReport.html`, `validation/testng-results.xml`, `validation/command-result.md`

UI wrapper handoff: actor = customer; user intent = customer actor submits support message to API; intent is durable case intake through POST messages; visible proof = refund support flow evidence; business object = support message / invoice record.

Progression from run_02: moves from UI intake fields into durable POST /messages API acceptance.
