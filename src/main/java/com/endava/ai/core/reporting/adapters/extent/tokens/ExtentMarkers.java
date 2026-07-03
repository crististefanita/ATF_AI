package com.endava.ai.core.reporting.adapters.extent.tokens;

import java.util.List;

public final class ExtentMarkers {
    public static final String REPORT_MODE = "ATF_EXTENT_REPORT_MODE";
    public static final String NAVIGATION_GUARD = "ATF_EXTENT_NAVIGATION_GUARD";
    public static final String COLLAPSE_SCRIPT = "ATF_EXTENT_COLLAPSE_SCRIPT";
    public static final String LEGACY_COMPONENT_FALLBACK = "ATF_EXTENT_COMPONENT_FALLBACK";
    public static final String RUNTIME_TAG_AUDIT_START = "ATF_RUNTIME_TAG_AUDIT_START";
    public static final String RUNTIME_TAG_AUDIT_END = "ATF_RUNTIME_TAG_AUDIT_END";
    public static final String RUNTIME_FAILURE_GROUPS_START = "ATF_RUNTIME_FAILURE_GROUPS_START";
    public static final String RUNTIME_FAILURE_GROUPS_END = "ATF_RUNTIME_FAILURE_GROUPS_END";
    public static final String CORE_FINAL_AUDIT_START = "ATF_CORE_FINAL_AUDIT_START";
    public static final String CORE_FINAL_AUDIT_END = "ATF_CORE_FINAL_AUDIT_END";
    public static final String COMPONENT_TAG_AUDIT_START = "ATF_COMPONENT_TAG_AUDIT_START";
    public static final String COMPONENT_TAG_AUDIT_END = "ATF_COMPONENT_TAG_AUDIT_END";
    public static final String RUNTIME_TAG_AUDIT_LABEL = "ATF Runtime Tag Audit";
    public static final String RUNTIME_FAILURE_GROUPS_LABEL = "ATF Runtime Failure Groups";
    public static final String COMPONENT_TAG_AUDIT_LABEL = "ATF Component Tag Audit";

    public static final List<String> RUNTIME_AUDIT_TOKENS = List.of(
            RUNTIME_TAG_AUDIT_START,
            RUNTIME_FAILURE_GROUPS_START,
            CORE_FINAL_AUDIT_START,
            RUNTIME_TAG_AUDIT_LABEL,
            RUNTIME_FAILURE_GROUPS_LABEL
    );

    private ExtentMarkers() {
    }

    public static String htmlComment(String marker) {
        return "<!-- " + marker + " -->";
    }

    public static String scriptId(String marker) {
        return "id=\"" + marker + "\"";
    }
}
