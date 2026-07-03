package com.ai.core.reporting.listener.internal.runtime.audit;

import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditSection;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public final class ExtentHtmlPatchPlan {

    private static final List<MarkedSection> RUNTIME_AUDIT_SECTIONS = List.of(
            MarkedSection.from(RuntimeAuditSection.CORE_FINAL_AUDIT),
            MarkedSection.from(RuntimeAuditSection.TAG_AUDIT),
            MarkedSection.from(RuntimeAuditSection.FAILURE_GROUPS)
    );

    private ExtentHtmlPatchPlan() {
    }

    public static String removeExistingRuntimeAudit(String html) {
        String updated = html == null ? "" : html;
        for (MarkedSection section : RUNTIME_AUDIT_SECTIONS) {
            updated = MarkedHtmlSectionRewriter.removeMarkedBlock(updated, section.startMarker, section.endMarker);
        }
        return updated;
    }

    public static String replaceCategoryRuntimeAudit(String html, String section) {
        return MarkedHtmlSectionRewriter.replaceCategoryViewContent(html, section);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MarkedSection {
        private final String startMarker;
        private final String endMarker;

        private static MarkedSection from(RuntimeAuditSection section) {
            return new MarkedSection(section.startMarker(), section.endMarker());
        }
    }
}
