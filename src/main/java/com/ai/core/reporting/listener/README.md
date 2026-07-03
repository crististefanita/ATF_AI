# Reporting Listener

## Purpose

The listener layer owns TestNG lifecycle routing into ATF reporting sessions. It observes test execution and delegates report output, runtime audit and step narration to focused reporting collaborators.

## Entry points

- `TestListener`: TestNG listener boundary.
- `internal`: lifecycle, runtime and session collaborators.
- `internal/runtime`: runtime audit collection and publication.

## Change here when

- TestNG lifecycle handling changes.
- Runtime audit publication changes.
- Session ownership or thread-local behavior changes.

## Do not

- Do not put report backend rendering logic directly in the listener.
- Do not add MCP or Atlas domain rules here.
- Do not write direct console output; use the reporting/console gateway.
- Do not bypass reporting session lifecycle contracts.

## Verification

- Run `src/test/resources/atf/core/reporting/testng-reporting-sanity.xml`.
- Run `TestListenerArchitectureContractTest` for lifecycle changes.
- Run `RuntimeAuditPublisherCoordinatorContractTest` for runtime audit publication changes.
