# Core runtime packages

## Overview

These packages belong to [atf-core](../../../../../../README.md).
They own shared configuration/support and the neutral reporting contract,
console steps and session delegation.

## Entry points

- [Core support](support/README.md): configuration, metadata, YAML and HTML primitives.
- [ReportLogger](reporting/ReportLogger.java): report operations independent of Extent.
- [StepLogger](reporting/StepLogger.java): step scopes, console output and report delegation.

Reporting implementations, TestNG listeners and publication live in
[atf-reporting](../../../../../../../atf-reporting/README.md).
Core public signatures remain independent of browser and reporting vendor SDKs.

## Verification

Run native Core checks and the affected cross-module regressions from an
isolated reactor copy. [Testing](../../../../../../../docs/Testing.md) owns the
maintained commands and evidence rules.
