package com.ai.core.reporting.adapters.extent.contract;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum ExtentReportPublishPolicy {
    INTERNAL_COMPONENT_FIXTURE(false, "internal component fixture"),
    RUNTIME_ARTIFACT(true, "runtime artifact"),
    PUBLISHED_VALIDATION(true, "published validation artifact"),
    FROZEN_PACKAGE(true, "frozen package artifact");

    private final boolean runtimeAuditedRequired;
    private final String label;

    ExtentReportPublishPolicy(boolean runtimeAuditedRequired, String label) {
        this.runtimeAuditedRequired = runtimeAuditedRequired;
        this.label = label;
    }

}
