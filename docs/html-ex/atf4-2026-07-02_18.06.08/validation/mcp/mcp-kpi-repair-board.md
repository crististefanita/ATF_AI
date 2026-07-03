# MCP KPI Repair Board

Compact action board generated from the full KPI matrix. Fix rows in this order before asking for a higher score.

## Top 3 blockers

- `support_cohesion_score` [FAIL] declared 82 but expected 0
  First file: `review/source-evolution-ledger.md`
  Agent action: delete dead helpers or consolidate them behind business-facing flows
  Cap impact: diagnostic only until calibrated across representative ATF_name packages
- `duplicate_action_line_ratio` [FAIL] declared 24 but expected 14
  First file: `review/source-evolution-ledger.md`
  Agent action: extract repeated action lines into reusable support/page/service helpers
  Cap impact: diagnostic only until calibrated across representative ATF_name packages
- `duplicate_assertion_line_ratio` [FAIL] declared 10 but expected 17
  First file: `review/source-evolution-ledger.md`
  Agent action: move repeated assertions into validation/support helpers
  Cap impact: diagnostic only until calibrated across representative ATF_name packages

## Top 3 INFO risks

- none

## Rerun command

```bash
mvn -q -Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-package-closeout.xml -Dmcp.package.root=<package-root> -Dmcp.project.root=<project-root> -Dreporting.engine=extent -Dreports.dir=<package-root>/validation/mcp -Dreports.file.name=ExtentReport.html -Dreports.timestamp.enabled=false test
```

Full audit: `validation/mcp/mcp-kpi-matrix.md`, `validation/mcp/mcp-kpi-matrix.html`, `validation/mcp/mcp-kpi-formula-trace.md`, `validation/mcp/mcp-score-cap-rationale.md`.
