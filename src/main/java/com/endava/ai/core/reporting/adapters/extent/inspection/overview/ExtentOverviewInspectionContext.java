package com.endava.ai.core.reporting.adapters.extent.inspection.overview;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
public final class ExtentOverviewInspectionContext {
    private final String html;

    public ExtentOverviewInspectionContext(String html) {
        this.html = html == null ? "" : html;
    }

    boolean contains(String token) {
        return html.contains(token);
    }

    boolean containsAny(String... tokens) {
        for (String token : tokens) {
            if (contains(token)) {
                return true;
            }
        }
        return false;
    }

    int count(String token) {
        int matches = 0;
        int index = html.indexOf(token);
        while (index >= 0) {
            matches++;
            index = html.indexOf(token, index + token.length());
        }
        return matches;
    }

    boolean runtimeAudited() {
        return contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL)
                || contains(ExtentDataAttributes.REPORT_MODE + "=\"" + ExtentReportModeType.RUNTIME_AUDITED.value() + "\"")
                || contains("\"mode\":\"" + ExtentReportModeType.RUNTIME_AUDITED.value() + "\"");
    }

    boolean componentAdapter() {
        return contains(ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL)
                || contains(ExtentDataAttributes.REPORT_MODE + "=\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\"")
                || contains("\"mode\":\"" + ExtentReportModeType.COMPONENT_ADAPTER.value() + "\"");
    }
}
