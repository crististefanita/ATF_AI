package com.endava.ai.core.reporting.listener.internal.runtime.audit.model;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuntimeAuditSection {
    public static final RuntimeAuditSection CORE_FINAL_AUDIT = new RuntimeAuditSection(
            ExtentMarkers.htmlComment(ExtentMarkers.CORE_FINAL_AUDIT_START),
            ExtentMarkers.htmlComment(ExtentMarkers.CORE_FINAL_AUDIT_END)
    );
    public static final RuntimeAuditSection TAG_AUDIT = new RuntimeAuditSection(
            ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_START),
            ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_TAG_AUDIT_END)
    );
    public static final RuntimeAuditSection FAILURE_GROUPS = new RuntimeAuditSection(
            ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_FAILURE_GROUPS_START),
            ExtentMarkers.htmlComment(ExtentMarkers.RUNTIME_FAILURE_GROUPS_END)
    );

    private final String startMarker;
    private final String endMarker;

    public String wrap(String html) {
        return "\n" + startMarker + html + endMarker + "\n";
    }
}
