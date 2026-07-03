package com.ai.atf.core.reporting.aggregate;

import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.AggregateInventoryLines;
import com.ai.core.reporting.aggregate.internal.ExtentFinalAuditHtml;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeCase;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeSummary;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class AtfCoreAggregateFinalAuditSections {
    private AtfCoreAggregateFinalAuditSections() {
    }

    static String runtimeOrFallback(
            AggregateReportDescriptor descriptor,
            AggregateRuntimeSummary runtimeSummary,
            List<AggregateRuntimeCase> runtimeCases,
            AggregateSummary fallbackSummary,
            List<AtfCoreAggregateCase> fallbackCases
    ) {
        if (!runtimeCases.isEmpty()) {
            return runtime(descriptor, runtimeSummary, runtimeCases, fallbackSummary.total(),
                    AggregateTimingSummary.from(fallbackCases));
        }
        return aggregateCases(descriptor, fallbackSummary, fallbackCases);
    }

    static String runtime(
            AggregateReportDescriptor descriptor,
            AggregateRuntimeSummary summary,
            List<AggregateRuntimeCase> cases
    ) {
        return runtime(descriptor, summary, cases, summary.total(), AggregateTimingSummary.empty());
    }

    static String aggregateCases(
            AggregateReportDescriptor descriptor,
            AggregateSummary summary,
            List<AtfCoreAggregateCase> cases
    ) {
        return ExtentFinalAuditHtml.finalAuditSection(
                descriptor,
                summary.total(),
                summary.total(),
                summary.passed(),
                summary.failed(),
                summary.skipped(),
                AggregateTimingSummary.from(cases),
                AggregateInventoryLines.fromCases(cases)
        );
    }

    private static String runtime(
            AggregateReportDescriptor descriptor,
            AggregateRuntimeSummary summary,
            List<AggregateRuntimeCase> cases,
            int suiteTotal,
            AggregateTimingSummary timing
    ) {
        String lines = cases.stream()
                .sorted(Comparator.comparingLong(AggregateRuntimeCase::startMillis)
                        .thenComparing(AggregateRuntimeCase::fullName))
                .map(AggregateRuntimeCase::line)
                .collect(Collectors.joining(System.lineSeparator()));
        return ExtentFinalAuditHtml.finalAuditSection(
                descriptor,
                suiteTotal,
                summary.total(),
                summary.passed(),
                summary.failed(),
                summary.skipped(),
                timing,
                lines
        );
    }
}
