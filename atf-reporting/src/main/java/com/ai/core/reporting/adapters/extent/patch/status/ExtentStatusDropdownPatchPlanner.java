package com.ai.core.reporting.adapters.extent.patch.status;

import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;

import java.util.List;

public final class ExtentStatusDropdownPatchPlanner {
    private ExtentStatusDropdownPatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document) {
        if (document.contains("id=\"" + ExtentDomIds.STATUS_TOGGLE + "\"")
                || document.contains("id='" + ExtentDomIds.STATUS_TOGGLE + "'")) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                "extent.status-dropdown.shell",
                ExtentDomIds.STATUS_TOGGLE,
                ExtentStatusDropdownPatchPlanner::injectStatusDropdown
        )));
    }

    private static String injectStatusDropdown(String html) {
        return injectBeforeBodyEnd(html, statusDropdown());
    }

    private static String injectBeforeBodyEnd(String html, String content) {
        int bodyEnd = html.lastIndexOf("</body>");
        if (bodyEnd < 0) {
            return html + content;
        }
        return html.substring(0, bodyEnd) + content + html.substring(bodyEnd);
    }

    private static String statusDropdown() {
        return "<ul id=\"" + ExtentDomIds.STATUS_TOGGLE + "\" class=\"dropdown-menu dropdown-md p-v-0\">"
                + "<li class=\"atf-status-filter-label\">Status</li>"
                + "<a class=\"dropdown-item\" status=\"pass\" href=\"#\"><span>Pass</span><span class=\"status success\"></span></a>"
                + "<a class=\"dropdown-item\" status=\"fail\" href=\"#\"><span>Fail</span><span class=\"status danger\"></span></a>"
                + "<a class=\"dropdown-item\" status=\"skip\" href=\"#\"><span>Skip</span><span class=\"status warning\"></span></a>"
                + "<a class=\"dropdown-item\" status=\"clear\" href=\"#\"><span>Clear</span><span class=\"pull-right\"><i class=\"fa fa-times\"></i></span></a>"
                + "</ul>";
    }
}
