# Reporting adapters

## Overview

Adapters in [atf-reporting](../../../../../../../../README.md)
translate neutral report operations into Extent and Allure while preserving
statuses, step hierarchy and attachments.

## Entry points

| Adapter | Responsibility |
|---|---|
| [ExtentAdapter](ExtentAdapter.java) | Extent HTML report lifecycle and rendering |
| [AllureAdapter](AllureAdapter.java) | Allure result and attachment publication |

Vendor types remain inside the reporting module. Shared operations use Core
ReportLogger and TestStatus. Runtime configuration selects the normal report
engine; aggregate publication has its own explicit selection.

## Verification

[Adapter regressions](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/adapters)
cover status translation, directories, HTML composition and Allure behavior.
[Testing](../../../../../../../../../docs/Testing.md) defines reporting subsets,
real-browser report checks and artifact inspection.
