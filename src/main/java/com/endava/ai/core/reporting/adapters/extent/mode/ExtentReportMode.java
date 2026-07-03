package com.endava.ai.core.reporting.adapters.extent.mode;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

public final class ExtentReportMode {
    @Getter
    @Accessors(fluent = true)
    private final ExtentReportModeType type;

    private ExtentReportMode(ExtentReportModeType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public static ExtentReportMode runtimeAudited() {
        return new ExtentReportMode(ExtentReportModeType.RUNTIME_AUDITED);
    }

    public static ExtentReportMode componentAdapter() {
        return new ExtentReportMode(ExtentReportModeType.COMPONENT_ADAPTER);
    }

    public String value() {
        return type.value();
    }

    public boolean runtimeAudit() {
        return type.runtimeAudit();
    }
}
