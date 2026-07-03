package com.endava.ai.core.reporting.aggregate.model;

import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;

public final class AggregateCaseTiming {
    private final long startMillis;
    private final long endMillis;

    private AggregateCaseTiming(long startMillis, long endMillis) {
        this.startMillis = Math.max(0L, startMillis);
        this.endMillis = Math.max(this.startMillis + 1L, endMillis);
    }

    public static AggregateCaseTiming from(AggregateExtentReportWriter.CaseRecord testCase) {
        long start = Math.max(0L, testCase.startMillis());
        long duration = Math.max(1L, testCase.durationMillis());
        long end = Math.max(start + duration, testCase.endMillis());
        return new AggregateCaseTiming(start, end);
    }

    public AggregateCaseTiming include(AggregateCaseTiming other) {
        return new AggregateCaseTiming(
                Math.min(startMillis, other.startMillis),
                Math.max(endMillis, other.endMillis)
        );
    }

    public long startMillis() {
        return startMillis;
    }

    public long endMillis() {
        return endMillis;
    }

    public long durationMillis() {
        return Math.max(1L, endMillis - startMillis);
    }
}
