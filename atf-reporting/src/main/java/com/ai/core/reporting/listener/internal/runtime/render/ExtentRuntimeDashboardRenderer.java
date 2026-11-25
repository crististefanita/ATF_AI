package com.ai.core.reporting.listener.internal.runtime.render;

import com.ai.core.reporting.listener.internal.runtime.model.DashboardSummary;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeSummary;

import com.ai.core.reporting.listener.internal.runtime.audit.MarkedHtmlSectionRewriter;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.listener.internal.runtime.support.ExtentRuntimePercentages;

import java.util.List;

public final class ExtentRuntimeDashboardRenderer {
    private ExtentRuntimeDashboardRenderer() {
    }

    public static String replaceTestsDashboardRow(String html, ExtentRuntimeSummary summary) {
        int failLabel = ExtentRuntimeDashboardAnchors.testsFailedLabel(html);
        if (failLabel < 0) {
            return html;
        }
        return MarkedHtmlSectionRewriter.replaceBetweenMarkersAfter(
                html,
                failLabel,
                ExtentRuntimeDashboardAnchors.ROW_START_MARKERS,
                ExtentRuntimeDashboardAnchors.TIMELINE_ROW_START_MARKERS,
                dashboardTestsCardRow(summary)
        );
    }

    public static String replaceTimelineRow(String html, List<ExtentRuntimeInvocation> invocations) {
        return MarkedHtmlSectionRewriter.replaceFromPreviousMarkerToEndMarkerAfterAnchor(
                html,
                ExtentRuntimeDashboardAnchors.TIMELINE_CANVAS,
                ExtentRuntimeDashboardAnchors.TIMELINE_ROW_START_MARKERS,
                ExtentRuntimeDashboardAnchors.SCRIPT_END_MARKERS,
                ExtentRuntimeTimelineRenderer.section(invocations)
        );
    }

    public static String replaceTagAuditRow(String html, ExtentRuntimeSummary summary) {
        int existingAuditRowStart = ExtentRuntimeDashboardAnchors.runtimeTagAuditRow(html);
        int existingAuditScriptStart = ExtentRuntimeDashboardAnchors.statusGroupScriptStart(html, existingAuditRowStart);
        if (existingAuditRowStart >= 0 && existingAuditScriptStart > existingAuditRowStart) {
            return MarkedHtmlSectionRewriter.replaceRange(
                    html,
                    existingAuditRowStart,
                    existingAuditScriptStart,
                    ExtentRuntimeTagAuditRenderer.section(summary)
            );
        }

        int tagRowStart = ExtentRuntimeDashboardAnchors.firstTagRowStart(html);
        int statusGroupScriptStart = ExtentRuntimeDashboardAnchors.statusGroupScriptStart(html, tagRowStart);
        if (tagRowStart < 0) {
            tagRowStart = ExtentRuntimeDashboardAnchors.sparkTagsRowStart(html);
            statusGroupScriptStart = ExtentRuntimeDashboardAnchors.statusGroupScriptStart(html, tagRowStart);
        }
        if (tagRowStart < 0 || statusGroupScriptStart <= tagRowStart) {
            int dashboardScriptStart = ExtentRuntimeDashboardAnchors.statusGroupScriptStart(html, 0);
            if (dashboardScriptStart > 0) {
                return MarkedHtmlSectionRewriter.replaceRange(
                        html,
                        dashboardScriptStart,
                        dashboardScriptStart,
                        ExtentRuntimeTagAuditRenderer.section(summary)
                );
            }
            return html;
        }
        return MarkedHtmlSectionRewriter.replaceRange(
                html,
                tagRowStart,
                statusGroupScriptStart,
                ExtentRuntimeTagAuditRenderer.section(summary)
        );
    }

    public static String replaceStatusGroupScript(String html, ExtentRuntimeSummary summary) {
        return MarkedHtmlSectionRewriter.replaceScriptContaining(
                html,
                ExtentRuntimeDashboardAnchors.STATUS_GROUP_TOKEN,
                ExtentRuntimeStatusGroupScript.render(summary)
        );
    }

    private static String dashboardTestsCardRow(ExtentRuntimeSummary summary) {
        DashboardSummary dashboard = DashboardSummary.from(summary);
        return "<div class=\"row atf-runtime-dashboard-tests-row\">"
                + "<div class=\"col-md-4 col-lg-4\">"
                + "<div class=\"card atf-runtime-dashboard-tests-card\">"
                + "<div class=\"card-header\"><h6 class=\"card-title\">"
                + ExtentDashboardLabels.TEST_CASES
                + "</h6></div>"
                + "<div class=\"card-body\"><h3 data-atf-test-cases-total=\"true\">"
                + dashboard.total()
                + "</h3><p>total " + ExtentDashboardLabels.TEST_CASE_NOUN
                + "</p><div><canvas id='parent-analysis' width='115' height='90'></canvas></div></div>"
                + "<div class=\"card-footer\">"
                + dashboardInvocationFooter(dashboard)
                + "</div>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private static String dashboardInvocationFooter(DashboardSummary dashboard) {
        return "<div><small data-tooltip='" + ExtentRuntimePercentages.simple(dashboard.passed(), dashboard.total()) + "%'><b>"
                + dashboard.passed()
                + "</b> " + ExtentDashboardLabels.TEST_CASE_NOUN + " passed</small></div>"
                + System.lineSeparator()
                + "<div>"
                + System.lineSeparator()
                + "<small data-tooltip='" + ExtentRuntimePercentages.simple(dashboard.failed(), dashboard.total()) + "%'><b>"
                + dashboard.failed()
                + "</b> " + ExtentDashboardLabels.TEST_CASE_NOUN + " failed,"
                + System.lineSeparator()
                + "<b>"
                + dashboard.skipped()
                + "</b> skipped, <b data-tooltip='%'>"
                + dashboard.others()
                + "</b> others"
                + System.lineSeparator()
                + "</small>"
                + System.lineSeparator()
                + "</div>";
    }
}
