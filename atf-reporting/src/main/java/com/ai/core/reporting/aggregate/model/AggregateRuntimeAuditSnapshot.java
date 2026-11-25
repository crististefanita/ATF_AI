package com.ai.core.reporting.aggregate.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class AggregateRuntimeAuditSnapshot {
    private final int total;
    private final long passed;
    private final long failed;
    private final long skipped;
    @Getter(AccessLevel.NONE)
    private final List<String> lines;

    public AggregateRuntimeAuditSnapshot(int total, long passed, long failed, long skipped, List<String> lines) {
        this.total = nonNegative(total, "total");
        this.passed = nonNegative(passed, "passed");
        this.failed = nonNegative(failed, "failed");
        this.skipped = nonNegative(skipped, "skipped");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    }

    public List<String> lineItems() {
        return lines;
    }

    public String lines() {
        return String.join(System.lineSeparator(), lines);
    }

    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate runtime audit snapshot " + name
                    + " must be non-negative: " + value);
        }
        return value;
    }

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate runtime audit snapshot " + name
                    + " must be non-negative: " + value);
        }
        return value;
    }
}
