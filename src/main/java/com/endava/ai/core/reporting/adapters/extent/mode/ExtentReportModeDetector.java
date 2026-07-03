package com.endava.ai.core.reporting.adapters.extent.mode;

import com.endava.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

public final class ExtentReportModeDetector {
    private ExtentReportModeDetector() {
    }

    public static ExtentReportMode detect(ExtentHtmlDocument document) {
        boolean runtimeAuditPresent = ExtentMarkers.RUNTIME_AUDIT_TOKENS.stream().anyMatch(document::contains);
        return runtimeAuditPresent ? ExtentReportMode.runtimeAudited() : ExtentReportMode.componentAdapter();
    }
}
