package com.ai.core.reporting.adapters.extent.mode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ExtentReportModeType {
    RUNTIME_AUDITED("runtime-audited", true),
    COMPONENT_ADAPTER("component-adapter", false);

    private final String value;
    private final boolean runtimeAudit;
}
