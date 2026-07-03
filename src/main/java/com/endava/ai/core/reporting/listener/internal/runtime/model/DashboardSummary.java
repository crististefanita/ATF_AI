package com.endava.ai.core.reporting.listener.internal.runtime.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class DashboardSummary {
    private final int total;
    private final long passed;
    private final long failed;
    private final long skipped;
    private final long others;

    private DashboardSummary(int total, long passed, long failed, long skipped) {
        this.total = total;
        this.passed = passed;
        this.failed = failed;
        this.skipped = skipped;
        this.others = Math.max(0, total - passed - failed - skipped);
    }

    public static DashboardSummary from(ExtentRuntimeSummary summary) {
        return new DashboardSummary(summary.total, summary.passed, summary.failed, summary.skipped);
    }
}
