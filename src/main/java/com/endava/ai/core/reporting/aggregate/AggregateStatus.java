package com.endava.ai.core.reporting.aggregate;

import com.endava.ai.core.reporting.TestStatus;

import java.util.Locale;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum AggregateStatus {
    PASSED(TestStatus.PASS),
    FAILED(TestStatus.FAIL),
    SKIPPED(TestStatus.SKIP);

    private final TestStatus reportStatus;

    AggregateStatus(TestStatus reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String category() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean passed() {
        return this == PASSED;
    }

    public boolean failed() {
        return this == FAILED;
    }

    public boolean skipped() {
        return this == SKIPPED;
    }
}
