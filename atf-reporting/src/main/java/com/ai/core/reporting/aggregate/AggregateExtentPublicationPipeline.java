package com.ai.core.reporting.aggregate;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.aggregate.internal.AggregateFinalAuditAppender;
import com.ai.core.reporting.aggregate.internal.AggregateRuntimeAuditMapper;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.ai.core.reporting.adapters.extent.overview.ExtentOverviewTopCards;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class AggregateExtentPublicationPipeline {

    void publish(Path report,
                 AggregateReportDescriptor descriptor,
                 AggregateExtentReportWriter.SummaryRecord summary,
                 List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        AggregateTimingSummary timing = AggregateTimingSummary.from(cases);
        if (descriptor.reportKind().aggregateAuditRequired()) {
            AggregateFinalAuditAppender.append(report, descriptor, summary, cases, timing);
        }
        try {
            ExtentRuntimeAuditRegistry.publishOrUpdateReportStrict(report, AggregateRuntimeAuditMapper.from(cases));
            ExtentSparkReportFinalizer.standardize(report, descriptor.reportKind());
            reconcileTiming(report, descriptor, timing);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to finalize aggregate Extent report as runtime-audited: " + report,
                    e);
        }
    }

    private static void reconcileTiming(
            Path report,
            AggregateReportDescriptor descriptor,
            AggregateTimingSummary timing
    ) throws IOException {
        if (timing == null || !timing.observed()) {
            return;
        }
        String html = Files.readString(report, StandardCharsets.UTF_8);
        String updated = ExtentOverviewTopCards.reconcileTiming(
                html,
                timing.startedAt(),
                timing.endedAt(),
                timing.overviewDuration()
        );
        updated = AggregateExtentSummaryNodeTimingReconciler.reconcile(updated, descriptor, timing);
        if (!updated.equals(html)) {
            Files.writeString(report, updated, StandardCharsets.UTF_8);
        }
    }
}
