package com.ai.core.reporting.aggregate.internal;

import com.ai.core.reporting.ReportText;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AggregateCaseDisplayNames {
    private final Map<String, Long> titleCounts;

    public static AggregateCaseDisplayNames from(List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (AggregateExtentReportWriter.CaseRecord testCase : cases) {
            counts.merge(baseTitle(testCase), 1L, Long::sum);
        }
        return new AggregateCaseDisplayNames(counts);
    }

    public String titleFor(AggregateExtentReportWriter.CaseRecord testCase) {
        String title = baseTitle(testCase);
        if (titleCounts.getOrDefault(title, 0L) <= 1L) {
            return title;
        }
        String discriminator = ReportText.simpleClassName(testCase.className());
        String parameters = testCase.parameters() == null ? "" : testCase.parameters().trim();
        if (!parameters.isBlank()) {
            discriminator += " " + parameters;
        }
        return title + " [" + discriminator + "]";
    }

    private static String baseTitle(AggregateExtentReportWriter.CaseRecord testCase) {
        String displayName = ReportText.printable(testCase.displayName()).trim();
        if (!displayName.isBlank()) {
            return displayName;
        }
        return ReportText.printable(testCase.methodName()).trim();
    }
}
