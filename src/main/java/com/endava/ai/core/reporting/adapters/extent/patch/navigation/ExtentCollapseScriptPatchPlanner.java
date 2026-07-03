package com.endava.ai.core.reporting.adapters.extent.patch.navigation;

import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.spark.style.ExtentSparkCollapsePolicy;

import java.util.List;

public final class ExtentCollapseScriptPatchPlanner {
    private static final String STATUS_MENU_NORMALIZER = "ensureStatusItem('fail','Fail','danger'";

    private ExtentCollapseScriptPatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document) {
        if (document.contains(ExtentHtmlStructure.COLLAPSE_FUNCTION) && document.contains(STATUS_MENU_NORMALIZER)) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        String id = document.contains(ExtentHtmlStructure.COLLAPSE_FUNCTION)
                ? "extent.collapse-script.upgrade"
                : "extent.collapse-script.fallback";
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                id,
                ExtentMarkers.COLLAPSE_SCRIPT,
                ExtentCollapseScriptPatchPlanner::replaceOrInjectCollapseScript
        )));
    }

    private static String replaceOrInjectCollapseScript(String html) {
        int markerIndex = html.indexOf(ExtentMarkers.COLLAPSE_SCRIPT);
        if (markerIndex < 0) {
            markerIndex = html.indexOf(ExtentHtmlStructure.COLLAPSE_FUNCTION);
        }
        if (markerIndex >= 0) {
            int scriptStart = html.lastIndexOf("<script", markerIndex);
            int scriptEnd = html.indexOf("</script>", markerIndex);
            if (scriptStart >= 0 && scriptEnd > markerIndex) {
                return html.substring(0, scriptStart)
                        + collapseScript()
                        + html.substring(scriptEnd + "</script>".length());
            }
        }
        return ExtentNavigationPatchPlanner.injectBeforeBodyEnd(html,
                collapseScript());
    }

    private static String collapseScript() {
        return "<script id=\"" + ExtentMarkers.COLLAPSE_SCRIPT + "\">"
                + ExtentSparkCollapsePolicy.defaultCollapseScript()
                + "</script>";
    }
}
