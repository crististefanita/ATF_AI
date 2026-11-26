# Reporting implementation packages

## Overview

These packages belong to [atf-reporting](../../../../../../../README.md).
They implement report publication and TestNG integration over Core reporting
contracts.

Core owns ReportLogger, TestStatus, StepLogger and ReportingBackend. This module
supplies the backend through ServiceLoader and owns vendor-specific types and
Extent lifecycle publication.

## Entry points

- [Adapters](adapters/README.md): Extent translation.
- [Listener](listener/README.md): TestNG lifecycle, reporting sessions and outcomes.
- [Aggregate reporting](aggregate/README.md): combined executed-case publication.

Runtime attachment and audit helpers preserve the test identity, status and
observed browser state. Ordinary execution requires no historical reports.

## Verification

The maintained reporting suite exercises local lifecycle and presentation
contracts. Extent aggregate publication and real-browser attachments
have explicit acceptance selections in
[Testing](../../../../../../../../docs/Testing.md).
