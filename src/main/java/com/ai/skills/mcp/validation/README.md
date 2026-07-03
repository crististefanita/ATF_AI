# MCP Validation

## Purpose

Validation facades and assertions live here. They should make generated package checks reusable by tests, smoke profiles and candidate promotion without duplicating parsing logic.

## Entry points

- `McpPackageAuditValidation`: package audit facade used by TestNG contracts.
- `artifacts`: close-out, score, report and business evidence assertions.
- `profile/html`: reusable html-ex corpus and report-template consensus.
- `report`: report shell, link, visual and score-card assertions.
- `run/extent`: Maven/Extent provenance, screenshots, XML and native Spark evidence assertions.

## Change here when

- Multiple tests need the same package assertion.
- A parser/model should be shared across latest, smoke and candidate profiles.
- A report expectation must be learned from html-ex rather than copied as prose.
- A validation assertion becomes reusable across package-audit, post-sync-smoke and final-report-closeout profiles.

## Do not

- Do not make validation depend on stale `docs/out` state.
- Do not hardcode dated html-ex folders; use auto-selected reusable examples.
- Do not hide expensive browser/final-report checks inside fast sanity profiles.
- Do not add generic catch-all helpers in the validation root when a focused subpackage exists.
- Do not raise the recursive `mcp/validation` surface cap without splitting assertion/model ownership first.

## Verification

- Run `testng-mcp-sanity.xml` for validator health.
- Run `testng-mcp-final-report-closeout.xml` only after package close-out artifacts are frozen.
- Run `testng-mcp-langgraph-final-report.xml` after final-report close-out when validating `analysis/langgraph-business-understanding.html` semantics, score truth, graph shape and proof mapping.
- Run `testng-mcp-html-ex-corpus.xml` after changing html-ex selection logic.
- Run `McpPackageShapeContractTest` after adding validation classes.
