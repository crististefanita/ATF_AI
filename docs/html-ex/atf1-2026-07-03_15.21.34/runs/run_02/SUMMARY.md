# run_02 - Contact form

The real contact form exposes Return intake, message, email ownership and purchased product context before submission.

- suite: `src/test/resources/ui/testng-atf1-refund-support-run-02.xml`
- tests produced: 5 / 5
- status: PASS
- proof: `validation/reports/ExtentReport.html`, `validation/testng-results.xml`, `validation/command-result.md`

UI wrapper handoff: actor = customer; user intent = customer actor fills visible Return contact form; intent is support intake with message and email ownership; visible proof = refund support flow evidence; business object = support message / invoice record.

Progression from run_01: moves from prepared refund payload into the visible Return contact form actor path.
