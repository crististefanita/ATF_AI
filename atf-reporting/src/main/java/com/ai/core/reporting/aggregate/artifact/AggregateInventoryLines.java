package com.ai.core.reporting.aggregate.artifact;

import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AggregateInventoryLines {
    private AggregateInventoryLines() {
    }

    public static String fromCases(List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        Map<String, Integer> invocationByMethod = new LinkedHashMap<>();
        return cases.stream()
                .map(testCase -> testCase.statusName()
                        + " | " + testCase.className()
                        + "#" + surefireDisplayName(testCase, invocationByMethod))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String surefireDisplayName(
            AggregateExtentReportWriter.CaseRecord testCase,
            Map<String, Integer> invocationByMethod
    ) {
        if (testCase.parameters().isBlank()) {
            return testCase.methodName();
        }
        int invocation = invocationByMethod.merge(testCase.methodName(), 1, Integer::sum);
        return testCase.methodName() + testCase.parameters() + "(" + invocation + ")";
    }
}
