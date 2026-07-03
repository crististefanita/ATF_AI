# Reporting Adapters

## Purpose

Adapters translate ATF reporting events into concrete report engines and shared HTML output. This package is infrastructure for Extent, Allure and future report backends; it must stay generic and reusable.

## Entry points

- `extent`: Extent/Spark adapter, post-processing and visual standardization.
- `allure`: Allure result boundary.
- `support`: shared fixtures and adapter support.

## Change here when

- A report backend integration changes.
- Shared Extent/Spark behavior changes for all ATF consumers.
- Adapter behavior needs to move into a focused backend package.

## Do not

- Do not add MCP package gates here.
- Do not add Atlas business meaning here.
- Do not import UI/API product code directly.
- Do not hide domain-specific report checks inside generic adapters.

## Verification

- Run `src/test/resources/atf/core/reporting/testng-reporting-sanity.xml`.
- Run `AtfReportingPackageShapeContractTest` after package or ownership changes.
- Run the focused Extent/Allure profile when changing a concrete backend.
