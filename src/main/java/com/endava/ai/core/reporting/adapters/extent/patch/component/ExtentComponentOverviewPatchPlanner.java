package com.endava.ai.core.reporting.adapters.extent.patch.component;

import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportMode;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;

import java.util.List;
import java.util.regex.Pattern;

public final class ExtentComponentOverviewPatchPlanner {
    private static final String STATUS_GROUP_TOKEN = "var statusGroup";
    private static final Pattern EMPTY_DASHBOARD_CARD = Pattern.compile(
            "(?s)<div class=\"col-md-3\">\\s*<div class=\"card\">\\s*<div class=\"card-header\">\\s*"
                    + "<h6 class=\"card-title\">\\s*</h6>.*?</div>\\s*</div>\\s*</div>"
    );

    private ExtentComponentOverviewPatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document, ExtentReportMode mode) {
        if (mode.type() == ExtentReportModeType.RUNTIME_AUDITED
                && document.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_START)) {
            return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                    "extent.component-overview.remove-stale-runtime",
                    "",
                    ExtentComponentOverviewPatchPlanner::removeStaleComponentAudit
            )));
        }
        if (mode.type() != ExtentReportModeType.COMPONENT_ADAPTER
                || !document.contains(ExtentHtmlStructure.DASHBOARD_VIEW_CLASS)
                || document.contains(ExtentMarkers.COMPONENT_TAG_AUDIT_START)) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                "extent.component-overview.audit",
                ExtentMarkers.COMPONENT_TAG_AUDIT_START,
                ExtentComponentOverviewPatchPlanner::patchOverview
        )));
    }

    private static String patchOverview(String html) {
        String patched = EMPTY_DASHBOARD_CARD.matcher(html).replaceAll("");
        return replaceNativeTagsRow(patched, ExtentComponentOverviewAuditRenderer.render(patched));
    }

    private static String replaceNativeTagsRow(String html, String replacement) {
        int tagsHeader = html.indexOf(ExtentHtmlStructure.NATIVE_TAGS_CARD_HEADER);
        int statusGroupScript = statusGroupScriptStart(html, Math.max(0, tagsHeader));
        if (tagsHeader >= 0) {
            int rowStart = html.lastIndexOf("<div class=\"row\">", tagsHeader);
            if (rowStart >= 0 && statusGroupScript > rowStart) {
                return html.substring(0, rowStart) + replacement + html.substring(statusGroupScript);
            }
        }
        if (statusGroupScript >= 0) {
            return html.substring(0, statusGroupScript) + replacement + html.substring(statusGroupScript);
        }
        return html;
    }

    private static String removeStaleComponentAudit(String html) {
        int auditStart = html.indexOf(ExtentMarkers.COMPONENT_TAG_AUDIT_START);
        if (auditStart < 0) {
            return html;
        }
        int rowStart = html.lastIndexOf(ExtentComponentOverviewAuditRenderer.COMPONENT_TAG_AUDIT_ROW, auditStart);
        if (rowStart < 0) {
            return html;
        }
        int auditEnd = html.indexOf(ExtentMarkers.COMPONENT_TAG_AUDIT_END, auditStart);
        if (auditEnd < 0) {
            return html;
        }
        int rowEnd = html.indexOf("</div></div>", auditEnd + ExtentMarkers.COMPONENT_TAG_AUDIT_END.length());
        if (rowEnd < 0) {
            return html;
        }
        rowEnd += "</div></div>".length();
        return html.substring(0, rowStart) + html.substring(rowEnd);
    }

    private static int statusGroupScriptStart(String html, int fromIndex) {
        int statusGroupStart = html.indexOf(STATUS_GROUP_TOKEN, Math.max(0, fromIndex));
        if (statusGroupStart < 0) {
            return -1;
        }
        return html.lastIndexOf("<script", statusGroupStart);
    }
}
