package com.ai.core.reporting.adapters.extent.inspection.overview;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;

import java.util.List;

public final class ExtentOverviewShellInspection {
    private ExtentOverviewShellInspection() {
    }

    public static void inspect(ExtentOverviewInspectionContext context, List<String> violations) {
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDomIds.DASHBOARD_VIEW, "missing dashboard/Overview view");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDashboardLabels.TESTS_PASSED, "missing top Tests Passed score-card");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDashboardLabels.TESTS_FAILED, "missing top Tests Failed score-card");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDashboardLabels.DURATION, "missing top Duration score-card");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                "id=\"" + ExtentMarkers.REPORT_MODE + "\"", "missing report-mode marker");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDataAttributes.REPORT_MODE + "=\"", "missing report-mode data attribute");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDataAttributes.RUNTIME_AUDIT + "=\"", "missing runtime-audit data attribute");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentMarkers.NAVIGATION_GUARD, "missing navigation guard");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                "activate(visibleView());", "missing selected-tab navigation behavior");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentHtmlStructure.COLLAPSE_FUNCTION, "missing collapse function");
        ExtentOverviewInspectionSupport.requireContainsAny(context, violations,
                "missing status dropdown toggle",
                ExtentHtmlStructure.idAttribute(ExtentDomIds.STATUS_TOGGLE),
                "id='" + ExtentDomIds.STATUS_TOGGLE + "'");
        ExtentOverviewInspectionSupport.requireContains(context, violations,
                ExtentDomIds.STATUS_FILTER_LABEL_CLASS, "missing status dropdown label");
        ExtentOverviewInspectionSupport.requireAbsent(context, violations,
                ExtentHtmlStructure.BLANK_CARD_TITLE, "blank Spark score-card is still visible");
        ExtentOverviewInspectionSupport.requireAbsent(context, violations,
                ExtentHtmlStructure.NATIVE_TAGS_CARD_HEADER, "native Spark mixed Tags card is still visible");
    }
}
