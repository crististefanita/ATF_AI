package com.ai.core.reporting.aggregate.parallel;

import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.model.AggregateSummary;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class AggregateParallelExtentPublisher {
    private AggregateParallelExtentPublisher() {
    }

    public static Path publish(Path report, List<AggregateCollectionResult> results) {
        List<AggregateCollectionResult> orderedResults = List.copyOf(results);
        AggregateReportDescriptor descriptor = AggregateReportDescriptor.builder()
                .testTitle("Parallel Aggregate Collection Close-Out")
                .description("One final Extent report merged from isolated parallel collection runs.")
                .scopeLabel("parallel isolated aggregate collections")
                .fragmentationReason("Collections run concurrently with distinct output roots to avoid shared listener state.")
                .auditShape("Each node represents one isolated collection command and preserves its pass/fail result.")
                .runCommands(orderedResults.stream()
                        .map(result -> result.spec().command())
                        .collect(Collectors.toList()))
                .build();
        new AggregateExtentReportWriter().write(
                report,
                "parallel-collections",
                descriptor,
                AggregateSummary.from(orderedResults),
                orderedResults
        );
        return report.toAbsolutePath().normalize();
    }

    public static Path publishMethodCases(Path report, List<AggregateCollectionTestCase> testCases) {
        List<AggregateCollectionTestCase> orderedCases = List.copyOf(testCases);
        AggregateReportDescriptor descriptor = AggregateReportDescriptor.builder()
                .testTitle("Parallel Aggregate Method Close-Out")
                .description("One final Extent report merged from TestNG method results across isolated collections.")
                .scopeLabel("parallel isolated aggregate method results")
                .fragmentationReason("Collection commands run with separate TestNG result roots, then method results are merged.")
                .auditShape("Each node represents one TestNG test method and preserves its real pass/fail/skip status.")
                .runCommands(orderedCases.stream()
                        .map(testCase -> testCase.description())
                        .distinct()
                        .collect(Collectors.toList()))
                .build();
        new AggregateExtentReportWriter().write(
                report,
                "parallel-method-results",
                descriptor,
                AggregateSummary.from(orderedCases),
                orderedCases
        );
        return report.toAbsolutePath().normalize();
    }
}
