package com.ai.core.reporting.adapters.extent.patch.mode;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportMode;

import java.util.List;

public final class ExtentReportModePatchPlanner {
    private ExtentReportModePatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document, ExtentReportMode mode) {
        if (hasCurrentMode(document, mode)) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                "extent.report-mode.marker",
                document.contains(ExtentMarkers.REPORT_MODE) ? "" : ExtentMarkers.REPORT_MODE,
                html -> injectReportModeMarker(html, mode)
        )));
    }

    private static boolean hasCurrentMode(ExtentHtmlDocument document, ExtentReportMode mode) {
        return document.contains(ExtentMarkers.REPORT_MODE)
                && document.contains(ExtentDataAttributes.REPORT_MODE + "=\"" + mode.value() + "\"")
                && document.contains(ExtentDataAttributes.RUNTIME_AUDIT + "=\"" + mode.runtimeAudit() + "\"")
                && document.contains("\"mode\":\"" + mode.value() + "\"")
                && document.contains("\"runtimeAudit\":" + mode.runtimeAudit());
    }

    private static String injectReportModeMarker(String html, ExtentReportMode mode) {
        String markedHtml = replaceBodyReportModeAttributes(html, mode);
        String marker = "<script id=\"" + ExtentMarkers.REPORT_MODE + "\" type=\"application/json\""
                + " " + ExtentDataAttributes.REPORT_MODE + "=\"" + mode.value() + "\">"
                + "{\"mode\":\"" + mode.value() + "\",\"runtimeAudit\":" + mode.runtimeAudit() + "}"
                + "</script>";
        return replaceExistingMarker(markedHtml, marker);
    }

    private static String replaceBodyReportModeAttributes(String html, ExtentReportMode mode) {
        int bodyStart = html.indexOf("<body");
        if (bodyStart < 0) {
            return html;
        }
        int bodyEnd = html.indexOf(">", bodyStart);
        if (bodyEnd < 0) {
            return html;
        }
        String bodyTag = html.substring(bodyStart, bodyEnd);
        bodyTag = bodyTag
                .replaceAll("\\s+" + ExtentDataAttributes.REPORT_MODE + "=\"[^\"]*\"", "")
                .replaceAll("\\s+" + ExtentDataAttributes.RUNTIME_AUDIT + "=\"[^\"]*\"", "");
        String attributes = " " + ExtentDataAttributes.REPORT_MODE + "=\"" + mode.value() + "\""
                + " " + ExtentDataAttributes.RUNTIME_AUDIT + "=\"" + mode.runtimeAudit() + "\"";
        return html.substring(0, bodyStart) + bodyTag + attributes + html.substring(bodyEnd);
    }

    private static String replaceExistingMarker(String html, String marker) {
        String scriptNeedle = "<script id=\"" + ExtentMarkers.REPORT_MODE + "\"";
        int scriptStart = html.indexOf(scriptNeedle);
        int markerId = scriptStart < 0 ? -1 : scriptStart + scriptNeedle.length();
        if (markerId < 0) {
            return injectBeforeBodyEnd(html, marker);
        }
        int scriptEnd = html.indexOf("</script>", markerId);
        if (scriptStart < 0 || scriptEnd < markerId) {
            return injectBeforeBodyEnd(html, marker);
        }
        int afterScript = scriptEnd + "</script>".length();
        return html.substring(0, scriptStart) + marker + html.substring(afterScript);
    }

    private static String injectBeforeBodyEnd(String html, String content) {
        int bodyEnd = html.lastIndexOf("</body>");
        if (bodyEnd < 0) {
            return html + content;
        }
        return html.substring(0, bodyEnd) + content + html.substring(bodyEnd);
    }
}
