# Core Reporting Contract Tests

## Purpose

These tests protect the neutral Reporting skill: Extent/Spark rendering,
runtime audit, aggregate reports, diagnostic HTML, report shell contracts and
business graph layout primitives.

They do not decide MCP package publishability or Atlas business quality.

## Reading Path

1. Use `testng-reporting-sanity.xml` for the neutral Reporting gate.
2. Use `testng-reporting-integration.xml` when shared shell behavior affects MCP, Atlas or aggregate consumers.
3. Use `testng-reporting-consumer-smoke.xml` only as a tiny cross-skill smoke.
4. For business graph layout, test `BusinessGraphConstruction -> BusinessGraphLayout` shape, classes, legend, proof-boundary placement and accessibility. Leave truth/quality to Business Understanding, MCP and Atlas.

## Do Not

- Do not add MCP close-out checks here.
- Do not add Atlas source-trace or score-quality checks here.
- Do not duplicate generated report audits that already belong in MCP latest-package tests.
- Do not grow large contract tests when a focused assertion object or fixture can own the behavior.

## Verification

- Run `mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/atf/core/reporting/testng-reporting-sanity.xml" "-DforkCount=0" test`.
- Run package-shape tests after adding a new reporting test package or helper.
