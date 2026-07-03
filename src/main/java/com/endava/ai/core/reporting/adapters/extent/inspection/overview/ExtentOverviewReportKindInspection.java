package com.endava.ai.core.reporting.adapters.extent.inspection.overview;

import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportSectionIds;

import java.util.List;

public final class ExtentOverviewReportKindInspection {
    private ExtentOverviewReportKindInspection() {
    }

    public static void inspect(ExtentOverviewInspectionContext context, ExtentReportKind kind, List<String> violations) {
        boolean aggregateAuditPresent = context.contains("id=\"" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "\"")
                || context.contains("id='" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "'");
        if (kind.aggregateAuditRequired() && !aggregateAuditPresent) {
            violations.add(kind.label() + " must expose the aggregate final audit section");
        }
        if (!kind.aggregateAuditAllowed() && aggregateAuditPresent) {
            violations.add(kind.label() + " must not expose aggregate final audit content");
        }
    }
}
