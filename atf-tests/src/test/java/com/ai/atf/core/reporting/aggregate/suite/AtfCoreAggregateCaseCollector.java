package com.ai.atf.core.reporting.aggregate.suite;

import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.reporting.aggregate.AggregateStatus;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AtfCoreAggregateCaseCollector {
    private AtfCoreAggregateCaseCollector() {
    }

    public static List<AtfCoreAggregateCase> collect(
            List<ISuite> suites,
            List<AtfCoreAggregateCase> runtimeFallbackCases
    ) {
        List<AtfCoreAggregateCase> cases = new ArrayList<>();
        for (ISuite suite : suites) {
            collectSuite(cases, suite);
        }
        if (cases.isEmpty()) {
            synchronized (runtimeFallbackCases) {
                cases.addAll(runtimeFallbackCases);
            }
        }
        cases.sort(Comparator.comparingLong(AtfCoreAggregateCase::startMillis)
                .thenComparing(AtfCoreAggregateCase::fullName));
        return cases;
    }

    private static void collectSuite(List<AtfCoreAggregateCase> cases, ISuite suite) {
        for (ISuiteResult result : suite.getResults().values()) {
            ITestContext context = result.getTestContext();
            context.getPassedTests().getAllResults()
                    .forEach(test -> cases.add(AtfCoreAggregateCase.from(suite, context, test, AggregateStatus.PASSED)));
            context.getFailedTests().getAllResults()
                    .forEach(test -> cases.add(AtfCoreAggregateCase.from(suite, context, test, AggregateStatus.FAILED)));
            context.getSkippedTests().getAllResults()
                    .forEach(test -> cases.add(AtfCoreAggregateCase.from(suite, context, test, AggregateStatus.SKIPPED)));
        }
    }
}
