# Reporting listener

## Overview

The listener in [atf-reporting](../../../../../../../../README.md)
routes TestNG lifecycle events into reporting sessions and publishes runtime
outcomes and attachments.

## Entry points

[ReportingTestListener](ReportingTestListener.java) coordinates lifecycle/runtime
collaborators. The compatibility
[TestListener](../../../../../../../../../atf-core/src/main/java/com/ai/core/reporting/listener/TestListener.java)
in Core forwards TestNG events through the reporting SPI. The reporting module
owns the concrete listener and native Allure service registration. Core base
classes do not declare a vendor listener.

The listener must retain the owning test identity through setup, test failure,
teardown and final-state capture. Diagnostic failures must not replace the
original test outcome.

## Verification

[Failure lifecycle checks](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/lifecycle/TestListenerFailureContractTest.java)
and [session checks](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/regression/ReportingManagerSessionContractTest.java)
run in the maintained local suite. Browser attachment checks require the explicit
browser profile. See [Testing](../../../../../../../../../docs/Testing.md).
