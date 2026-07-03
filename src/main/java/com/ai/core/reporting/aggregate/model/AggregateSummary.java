package com.ai.core.reporting.aggregate.model;

import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class AggregateSummary implements AggregateExtentReportWriter.SummaryRecord {
    private final int total;
    private final long passed;
    private final long failed;
    private final long skipped;

    public AggregateSummary(int total, long passed, long failed, long skipped) {
        this.total = nonNegative(total, "total");
        this.passed = nonNegative(passed, "passed");
        this.failed = nonNegative(failed, "failed");
        this.skipped = nonNegative(skipped, "skipped");
    }

    public static AggregateSummary from(List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        long passed = cases.stream().filter(testCase -> testCase.status().passed()).count();
        long failed = cases.stream().filter(testCase -> testCase.status().failed()).count();
        long skipped = cases.stream().filter(testCase -> testCase.status().skipped()).count();
        return new AggregateSummary(cases.size(), passed, failed, skipped);
    }

    private static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate summary " + name + " must be non-negative: " + value);
        }
        return value;
    }

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate summary " + name + " must be non-negative: " + value);
        }
        return value;
    }
}
