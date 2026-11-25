package com.ai.core.reporting;

import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;

/**
 * Publishes UI runtime metadata without forcing concrete browser engines to know StepLogger details.
 */
public final class UiRuntimeEventPublisher {
    public static final String RESOLVED_ZOOM_PERCENT_KEY = "ui.page.zoom.resolved.percent";
    public static final String ZOOM_PHASE_KEY             = "ui.zoom.phase";

    private UiRuntimeEventPublisher() {
    }

    public static void publishZoomMetadata(
            UiRuntimeOptions runtimeOptions,
            UiEngineType engineType,
            boolean headless,
            int resolvedZoomPercent,
            String phaseLabel
    ) {
        if (!runtimeOptions.isRuntimeMetadataReportingEnabled()) {
            return;
        }

        try {
            StepLogger.logDetail(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY
                    + "=" + runtimeOptions.configuredZoomPercent());
            StepLogger.logDetail(RESOLVED_ZOOM_PERCENT_KEY + "=" + resolvedZoomPercent);
            StepLogger.logDetail(UiRuntimeOptions.UI_ENGINE_KEY + "=" + engineType.configValue());
            StepLogger.logDetail(UiRuntimeOptions.UI_HEADLESS_KEY + "=" + headless);
            StepLogger.logDetail(ZOOM_PHASE_KEY + "=" + phaseLabel);
        } catch (IllegalStateException e) {
            NonBlockingDiagnostic.warn("UI runtime metadata publication", e);
        }
    }
}
