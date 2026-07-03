# Aggregate Reporting

## Purpose

Aggregate reporting is the Reporting-owned implementation lane for one
reopenable report over many executed test cases. It turns runtime case records,
summary counts and a descriptor into aggregate Extent evidence plus publication
artifacts.

## Entry points

- `com.endava.ai.skills.reporting.AggregateReportSurface`: small agent-facing
  facade for ownership, public API, forbidden ownership and validation suites.
- `AggregateReportDescriptor`: caller-owned title, scope, audit shape and run
  command metadata.
- `AggregateExtentReportWriter`: implementation writer for aggregate Extent
  output.
- `aggregate/artifact`: manifest, inventory and runtime coverage publication.
- `aggregate/model`: typed runtime summary and timing values.

## Change here when

- Aggregate Extent report structure, final audit or manifest publication changes.
- Runtime case metadata must be displayed differently in aggregate reports.
- Aggregate report output needs the shared Reporting shell without becoming MCP
  or Atlas specific.

## Do not

- Do not decide MCP package publishability here.
- Do not decide Atlas business meaning here.
- Do not encode generated-package promotion rules here.
- Do not make callers read `AggregateExtentReportWriter` internals before they
  can understand aggregate ownership; route through `AggregateReportSurface`
  first.

## Verification

- Run `src/test/resources/atf/core/reporting/testng-reporting-sanity.xml`.
- Run `src/test/resources/atf/core/suites/aggregate/testng-atf-core-aggregate.xml` after changing
  aggregate writer behavior.
- Run `AtfReportingPackageShapeContractTest` after moving aggregate packages or
  README ownership.
- Inspect the generated aggregate Extent report for the shared Overview/status
  shell and compact runtime audit.
