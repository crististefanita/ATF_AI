# Aggregate reporting

## Overview

Aggregate publication in [atf-reporting](../../../../../../../../README.md)
builds one report from executed case records, counts, timings and a caller-owned
descriptor.

## Entry points

- [AggregateReportDescriptor](AggregateReportDescriptor.java): caller-supplied report identity and metadata.
- [AggregateExtentReportWriter](AggregateExtentReportWriter.java): Extent aggregate entry point.
- [AggregateExtentPublicationPipeline](AggregateExtentPublicationPipeline.java): ordered publication of report sections and evidence.

The library publishes observed records. The
[test aggregate infrastructure](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/aggregate)
collects framework cases through its TestNG listener and supplies the descriptor.
Publication preserves outcome and timing evidence; it does not infer acceptance
from historical reports.

## Verification

Run `mvn -B -ntp -Paggregate test` from an isolated reactor copy. The profile
selects the maintained aggregate suite in atf-tests and publishes both Extent
and Allure. The default aggregate output is
atf-tests/target/atf-core-aggregate-report.

Check the generated counts, statuses, timing and attachments. Full commands,
artifact locations and evidence rules belong to
[Testing](../../../../../../../../../docs/Testing.md).
