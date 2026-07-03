package com.endava.ai.core.reporting.aggregate.internal;

import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;

public final class ExtentFinalAuditHtml {

    private ExtentFinalAuditHtml() {
    }

    public static String reconcileSparkTopCards(String html, long passed, long failed) {
        return ExtentFinalAuditTopCards.reconcileStatus(html, passed, failed);
    }

    public static String reconcileSparkTimingCards(String html, AggregateTimingSummary timing) {
        return ExtentFinalAuditTopCards.reconcileTiming(html, timing);
    }

    public static String finalAuditSection(int total, long passed, long failed, long skipped, String inventoryLines) {
        return finalAuditSection(AggregateReportDescriptor.defaults(), total, total, passed, failed, skipped, inventoryLines);
    }

    public static String finalAuditSection(
            int suiteTotal,
            int reportableTotal,
            long passed,
            long failed,
            long skipped,
            String inventoryLines
    ) {
        return finalAuditSection(
                AggregateReportDescriptor.defaults(),
                suiteTotal,
                reportableTotal,
                passed,
                failed,
                skipped,
                inventoryLines
        );
    }

    public static String finalAuditSection(
            AggregateReportDescriptor descriptor,
            int suiteTotal,
            int reportableTotal,
            long passed,
            long failed,
            long skipped,
            String inventoryLines
    ) {
        return finalAuditSection(
                descriptor,
                suiteTotal,
                reportableTotal,
                passed,
                failed,
                skipped,
                AggregateTimingSummary.empty(),
                inventoryLines
        );
    }

    public static String finalAuditSection(
            AggregateReportDescriptor descriptor,
            int suiteTotal,
            int reportableTotal,
            long passed,
            long failed,
            long skipped,
            AggregateTimingSummary timing,
            String inventoryLines
    ) {
        return ExtentFinalAuditSectionRenderer.render(
                descriptor,
                suiteTotal,
                reportableTotal,
                passed,
                failed,
                skipped,
                timing,
                inventoryLines);
    }
}
