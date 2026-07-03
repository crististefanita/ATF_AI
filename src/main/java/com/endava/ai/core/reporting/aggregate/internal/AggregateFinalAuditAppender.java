package com.endava.ai.core.reporting.aggregate.internal;

import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter.CaseRecord;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter.SummaryRecord;
import com.endava.ai.core.reporting.aggregate.artifact.AggregateInventoryLines;
import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AggregateFinalAuditAppender {
    private static final String BODY_END = "</body>";

    private AggregateFinalAuditAppender() {
    }

    public static void append(
            Path report,
            AggregateReportDescriptor descriptor,
            SummaryRecord summary,
            List<? extends CaseRecord> cases
    ) {
        append(report, descriptor, summary, cases, AggregateTimingSummary.from(cases));
    }

    public static void append(
            Path report,
            AggregateReportDescriptor descriptor,
            SummaryRecord summary,
            List<? extends CaseRecord> cases,
            AggregateTimingSummary timing
    ) {
        try {
            String html = Files.readString(report, StandardCharsets.UTF_8);
            String section = ExtentFinalAuditHtml.finalAuditSection(
                    descriptor,
                    summary.total(),
                    summary.total(),
                    summary.passed(),
                    summary.failed(),
                    summary.skipped(),
                    timing,
                    AggregateInventoryLines.fromCases(cases)
            );
            Files.writeString(report, appendBeforeBodyEnd(html, section), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to append aggregate final audit to Extent report: " + report, e);
        }
    }

    private static String appendBeforeBodyEnd(String html, String section) {
        int bodyEnd = html.lastIndexOf(BODY_END);
        if (bodyEnd < 0) {
            return html + section;
        }
        return html.substring(0, bodyEnd) + section + html.substring(bodyEnd);
    }
}
