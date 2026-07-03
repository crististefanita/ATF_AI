# MCP KPI Repair Board

Compact action board generated from the full KPI matrix. Fix rows in this order before asking for a higher score.

## Top 3 blockers

- none

## Top 3 INFO risks

- none

## Rerun command

```bash
mvn -q -Dsurefire.suiteXmlFiles=src/test/resources/skills/mcp/testng-mcp-package-closeout.xml -Dmcp.package.root=<package-root> -Dmcp.project.root=<project-root> -Dreporting.engine=extent -Dreports.dir=<package-root>/validation/mcp -Dreports.file.name=ExtentReport.html -Dreports.timestamp.enabled=false test
```

Full audit: `validation/mcp/mcp-kpi-matrix.md`, `validation/mcp/mcp-kpi-matrix.html`, `validation/mcp/mcp-kpi-formula-trace.md`, `validation/mcp/mcp-score-cap-rationale.md`.
