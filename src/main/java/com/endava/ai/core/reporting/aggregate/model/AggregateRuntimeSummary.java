package com.endava.ai.core.reporting.aggregate.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class AggregateRuntimeSummary {
    private final int total;
    private final long passed;
    private final long failed;
    private final long skipped;

    public AggregateRuntimeSummary(int total, long passed, long failed, long skipped) {
        this.total = nonNegative(total, "total");
        this.passed = nonNegative(passed, "passed");
        this.failed = nonNegative(failed, "failed");
        this.skipped = nonNegative(skipped, "skipped");
    }

    public static AggregateRuntimeSummary from(List<AggregateRuntimeCase> cases) {
        long passed = cases.stream().filter(testCase -> testCase.status().passed()).count();
        long failed = cases.stream().filter(testCase -> testCase.status().failed()).count();
        long skipped = cases.stream().filter(testCase -> testCase.status().skipped()).count();
        return new AggregateRuntimeSummary(cases.size(), passed, failed, skipped);
    }

    public static AggregateRuntimeSummary fromAggregate(AggregateSummary summary) {
        return new AggregateRuntimeSummary(summary.total(), summary.passed(), summary.failed(), summary.skipped());
    }

    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate runtime summary " + name + " must be non-negative: " + value);
        }
        return value;
    }

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate runtime summary " + name + " must be non-negative: " + value);
        }
        return value;
    }
}
