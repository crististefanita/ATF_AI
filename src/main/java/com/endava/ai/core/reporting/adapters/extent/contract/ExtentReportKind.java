package com.endava.ai.core.reporting.adapters.extent.contract;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Semantic report families that share the same Extent shell but may require
 * different domain-specific audit sections.
 */
@Getter
@Accessors(fluent = true)
public enum ExtentReportKind {
    INTERNAL_COMPONENT_FIXTURE(
            ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE,
            false,
            false,
            "internal component fixture"
    ),
    RUNTIME_VALIDATION(
            ExtentReportPublishPolicy.PUBLISHED_VALIDATION,
            false,
            false,
            "runtime validation report"
    ),
    ATLAS_VALIDATION(
            ExtentReportPublishPolicy.PUBLISHED_VALIDATION,
            false,
            false,
            "Atlas validation report"
    ),
    MCP_CLOSEOUT(
            ExtentReportPublishPolicy.FROZEN_PACKAGE,
            false,
            false,
            "MCP close-out report"
    ),
    AGGREGATE_VALIDATION(
            ExtentReportPublishPolicy.PUBLISHED_VALIDATION,
            true,
            true,
            "aggregate validation report"
    );

    private final ExtentReportPublishPolicy publishPolicy;
    private final boolean aggregateAuditRequired;
    private final boolean aggregateAuditAllowed;
    private final String label;

    ExtentReportKind(
            ExtentReportPublishPolicy publishPolicy,
            boolean aggregateAuditRequired,
            boolean aggregateAuditAllowed,
            String label
    ) {
        this.publishPolicy = publishPolicy;
        this.aggregateAuditRequired = aggregateAuditRequired;
        this.aggregateAuditAllowed = aggregateAuditAllowed;
        this.label = label;
    }

}
