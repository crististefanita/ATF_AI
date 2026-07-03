package com.ai.core.reporting.aggregate.internal;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry.RuntimeAuditInvocation;
import com.ai.core.reporting.ReportText;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AggregateRuntimeAuditMapper {
    private AggregateRuntimeAuditMapper() {
    }

    public static List<RuntimeAuditInvocation> from(List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        return cases.stream()
                .map(testCase -> RuntimeAuditInvocation.of(
                        testCase.statusName(),
                        testCase.className(),
                        testCase.methodName(),
                        testCase.startMillis(),
                        testCase.stackTrace(),
                        tagsFor(testCase)
                ))
                .collect(Collectors.toList());
    }

    private static List<String> tagsFor(AggregateExtentReportWriter.CaseRecord testCase) {
        List<String> tags = new ArrayList<>();
        tags.add("STATUS:" + testCase.statusName());
        tags.add("SUITE:" + ReportText.printable(testCase.suiteName()));
        addTag(tags, "PROFILE", testCase.suiteProfile());
        addTag(tags, "AREA", testCase.area());
        addTag(tags, "CAPABILITY", testCase.capability());
        return tags;
    }

    private static void addTag(List<String> tags, String name, String value) {
        String printable = ReportText.printable(value);
        if (!printable.isBlank()) {
            tags.add(name + ":" + printable);
        }
    }
}
