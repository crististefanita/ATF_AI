package com.ai.core.reporting.adapters.extent.tokens;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Typed index for semantic Extent/Spark tokens owned by ATF core reporting.
 *
 * <p>This is a small core guardrail, not an MCP-style rule engine. It keeps
 * HTML ids, data attributes, report modes and runtime-audit labels discoverable
 * from one typed surface while the existing owner classes keep the actual
 * constants used by runtime code.</p>
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ExtentSemanticToken {
    REPORT_MODE_MARKER(
            Kind.HTML_ID,
            ExtentMarkers.REPORT_MODE,
            "ExtentMarkers",
            "Report-mode marker script id",
            "ExtentReportModePatchPlanner"
    ),
    NAVIGATION_GUARD_MARKER(
            Kind.HTML_ID,
            ExtentMarkers.NAVIGATION_GUARD,
            "ExtentMarkers",
            "Navigation guard script id",
            "ExtentNavigationPatchPlanner"
    ),
    COLLAPSE_SCRIPT_MARKER(
            Kind.HTML_ID,
            ExtentMarkers.COLLAPSE_SCRIPT,
            "ExtentMarkers",
            "Fallback collapse script id",
            "ExtentCollapseScriptPatchPlanner"
    ),
    LEGACY_COMPONENT_FALLBACK_MARKER(
            Kind.LEGACY_HTML_ID,
            ExtentMarkers.LEGACY_COMPONENT_FALLBACK,
            "ExtentMarkers",
            "Forbidden legacy component fallback marker",
            "ExtentComponentOverviewPatchPlanner"
    ),
    REPORT_MODE_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.REPORT_MODE,
            "ExtentDataAttributes",
            "Report-mode data attribute",
            "ExtentReportModePatchPlanner"
    ),
    RUNTIME_AUDIT_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_AUDIT,
            "ExtentDataAttributes",
            "Runtime-audit presence data attribute",
            "ExtentReportModePatchPlanner"
    ),
    NAVIGATION_BOUND_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.NAV_BOUND,
            "ExtentDataAttributes",
            "Navigation link binding guard attribute",
            "ExtentNavigationPatchPlanner"
    ),
    NAV_TEST_ID(
            Kind.DOM_ID,
            ExtentDomIds.NAV_TEST,
            "ExtentDomIds",
            "Tests tab navigation id",
            "ExtentNavigationPatchPlanner"
    ),
    NAV_CATEGORY_ID(
            Kind.DOM_ID,
            ExtentDomIds.NAV_CATEGORY,
            "ExtentDomIds",
            "Categories tab navigation id",
            "ExtentNavigationPatchPlanner"
    ),
    NAV_DASHBOARD_ID(
            Kind.DOM_ID,
            ExtentDomIds.NAV_DASHBOARD,
            "ExtentDomIds",
            "Dashboard tab navigation id",
            "ExtentNavigationPatchPlanner"
    ),
    STATUS_TOGGLE_ID(
            Kind.DOM_ID,
            ExtentDomIds.STATUS_TOGGLE,
            "ExtentDomIds",
            "Status filter dropdown id",
            "ExtentStatusFilterPatchPlanner"
    ),
    TEST_VIEW_CLASS(
            Kind.DOM_VIEW_CLASS,
            ExtentDomIds.TEST_VIEW,
            "ExtentDomIds",
            "Tests view CSS class",
            "ExtentHtmlStructure"
    ),
    CATEGORY_VIEW_CLASS(
            Kind.DOM_VIEW_CLASS,
            ExtentDomIds.CATEGORY_VIEW,
            "ExtentDomIds",
            "Category/tag view CSS class",
            "ExtentHtmlStructure"
    ),
    DASHBOARD_VIEW_CLASS(
            Kind.DOM_VIEW_CLASS,
            ExtentDomIds.DASHBOARD_VIEW,
            "ExtentDomIds",
            "Dashboard Overview view CSS class",
            "ExtentHtmlStructure"
    ),
    RUNTIME_TAG_TABLE_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_TAG_TABLE,
            "ExtentDataAttributes",
            "Runtime tag audit table selector",
            "ExtentRuntimeTagAuditRenderer"
    ),
    COMPONENT_TAG_TABLE_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.COMPONENT_TAG_TABLE,
            "ExtentDataAttributes",
            "Component tag audit table selector",
            "ExtentComponentOverviewPatchPlanner"
    ),
    RUNTIME_FAILURE_TABLE_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_FAILURE_TABLE,
            "ExtentDataAttributes",
            "Runtime failure table selector",
            "ExtentRuntimeFailureRenderer"
    ),
    RUNTIME_FAILURE_GROUP_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_FAILURE_GROUP,
            "ExtentDataAttributes",
            "Runtime failure group selector",
            "ExtentRuntimeFailureRenderer"
    ),
    RUNTIME_FAILURE_SIGNATURES_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_FAILURE_SIGNATURES,
            "ExtentDataAttributes",
            "Runtime failure signature selector",
            "ExtentRuntimeFailureRenderer"
    ),
    RUNTIME_METHOD_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_METHOD,
            "ExtentDataAttributes",
            "Runtime method hyperlink selector",
            "ExtentRuntimeFailureRenderer"
    ),
    TEST_CASES_TOTAL_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.TEST_CASES_TOTAL,
            "ExtentDataAttributes",
            "Canonical Overview total-test-cases selector",
            "ExtentRuntimeDashboardRenderer"
    ),
    RUNTIME_TOOLTIP_ATTRIBUTE(
            Kind.DATA_ATTRIBUTE,
            ExtentDataAttributes.RUNTIME_TOOLTIP,
            "ExtentDataAttributes",
            "Runtime timeline tooltip selector",
            "ExtentRuntimeDashboardRenderer"
    ),
    RUNTIME_AUDITED_MODE(
            Kind.REPORT_MODE_VALUE,
            ExtentReportModeType.RUNTIME_AUDITED.value(),
            "ExtentReportModeType",
            "Report mode for Extent files with ATF runtime audit sections",
            "ExtentReportModeDetector"
    ),
    COMPONENT_ADAPTER_MODE(
            Kind.REPORT_MODE_VALUE,
            ExtentReportModeType.COMPONENT_ADAPTER.value(),
            "ExtentReportModeType",
            "Report mode for adapter/component Extent contract reports",
            "ExtentReportModeDetector"
    ),
    RUNTIME_TAG_AUDIT_SECTION(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.RUNTIME_TAG_AUDIT_START,
            "ExtentMarkers",
            "Runtime tag audit section start marker",
            "RuntimeAuditSection"
    ),
    RUNTIME_TAG_AUDIT_SECTION_END(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.RUNTIME_TAG_AUDIT_END,
            "ExtentMarkers",
            "Runtime tag audit section end marker",
            "RuntimeAuditSection"
    ),
    COMPONENT_TAG_AUDIT_SECTION(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.COMPONENT_TAG_AUDIT_START,
            "ExtentMarkers",
            "Component tag audit section start marker",
            "ExtentComponentOverviewPatchPlanner"
    ),
    COMPONENT_TAG_AUDIT_SECTION_END(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.COMPONENT_TAG_AUDIT_END,
            "ExtentMarkers",
            "Component tag audit section end marker",
            "ExtentComponentOverviewPatchPlanner"
    ),
    RUNTIME_FAILURE_GROUPS_SECTION(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.RUNTIME_FAILURE_GROUPS_START,
            "ExtentMarkers",
            "Runtime failure groups section start marker",
            "RuntimeAuditSection"
    ),
    RUNTIME_FAILURE_GROUPS_SECTION_END(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.RUNTIME_FAILURE_GROUPS_END,
            "ExtentMarkers",
            "Runtime failure groups section end marker",
            "RuntimeAuditSection"
    ),
    CORE_FINAL_AUDIT_SECTION(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.CORE_FINAL_AUDIT_START,
            "ExtentMarkers",
            "Core final audit section start marker",
            "RuntimeAuditSection"
    ),
    CORE_FINAL_AUDIT_SECTION_END(
            Kind.RUNTIME_AUDIT_SECTION,
            ExtentMarkers.CORE_FINAL_AUDIT_END,
            "ExtentMarkers",
            "Core final audit section end marker",
            "RuntimeAuditSection"
    ),
    RUNTIME_TAG_AUDIT_LABEL(
            Kind.RUNTIME_AUDIT_LABEL,
            ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL,
            "ExtentMarkers",
            "Runtime tag audit visible section label",
            "ExtentRuntimeTagAuditRenderer"
    ),
    RUNTIME_FAILURE_GROUPS_LABEL(
            Kind.RUNTIME_AUDIT_LABEL,
            ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL,
            "ExtentMarkers",
            "Runtime failure groups visible section label",
            "ExtentRuntimeFailureRenderer"
    ),
    COMPONENT_TAG_AUDIT_LABEL(
            Kind.RUNTIME_AUDIT_LABEL,
            ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL,
            "ExtentMarkers",
            "Component tag audit visible section label",
            "ExtentComponentOverviewPatchPlanner"
    ),
    OVERVIEW_STARTED_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.STARTED,
            "ExtentDashboardLabels",
            "Overview start timestamp score-card label",
            "ExtentOverviewTopCards"
    ),
    OVERVIEW_ENDED_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.ENDED,
            "ExtentDashboardLabels",
            "Overview end timestamp score-card label",
            "ExtentOverviewTopCards"
    ),
    OVERVIEW_DURATION_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.DURATION,
            "ExtentDashboardLabels",
            "Overview wall-clock duration score-card label",
            "ExtentOverviewTopCards"
    ),
    OVERVIEW_TEST_CASES_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.TEST_CASES,
            "ExtentDashboardLabels",
            "Canonical Overview test-case total label",
            "ExtentRuntimeDashboardRenderer"
    ),
    OVERVIEW_TESTS_PASSED_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.TESTS_PASSED,
            "ExtentDashboardLabels",
            "Overview passed-test score-card label",
            "ExtentOverviewTopCards"
    ),
    OVERVIEW_TESTS_FAILED_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.TESTS_FAILED,
            "ExtentDashboardLabels",
            "Overview failed-test score-card label",
            "ExtentOverviewTopCards"
    ),
    OVERVIEW_LEGACY_TAGS_LABEL(
            Kind.DASHBOARD_LABEL,
            ExtentDashboardLabels.LEGACY_TAGS,
            "ExtentDashboardLabels",
            "Native Spark Tags label rejected from published Overview",
            "ExtentPublishedOverviewInspector"
    ),
    OVERVIEW_FORBIDDEN_TEST_CLASSES_LABEL(
            Kind.FORBIDDEN_DASHBOARD_LABEL,
            ExtentDashboardLabels.TEST_CLASSES,
            "ExtentDashboardLabels",
            "Forbidden native Spark class-count score-card label",
            "ExtentPublishedOverviewInspector"
    ),
    OVERVIEW_FORBIDDEN_CONFIGURATION_METHODS_LABEL(
            Kind.FORBIDDEN_DASHBOARD_LABEL,
            ExtentDashboardLabels.CONFIGURATION_METHODS,
            "ExtentDashboardLabels",
            "Forbidden native Spark TestNG configuration-method label",
            "ExtentPublishedOverviewInspector"
    ),
    OVERVIEW_FORBIDDEN_TESTNG_INVOCATIONS_LABEL(
            Kind.FORBIDDEN_DASHBOARD_LABEL,
            ExtentDashboardLabels.TESTNG_INVOCATIONS,
            "ExtentDashboardLabels",
            "Forbidden native Spark invocation-count label",
            "ExtentPublishedOverviewInspector"
    ),
    OVERVIEW_FORBIDDEN_STEPS_LABEL(
            Kind.FORBIDDEN_DASHBOARD_LABEL,
            ExtentDashboardLabels.STEPS,
            "ExtentDashboardLabels",
            "Forbidden native Spark step-count label",
            "ExtentPublishedOverviewInspector"
    );

    private final Kind kind;
    private final String value;
    private final String owner;
    private final String responsibility;
    private final String repairTarget;

    public static List<ExtentSemanticToken> all() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }

    public enum Kind {
        HTML_ID,
        LEGACY_HTML_ID,
        DATA_ATTRIBUTE,
        DOM_ID,
        DOM_VIEW_CLASS,
        REPORT_MODE_VALUE,
        RUNTIME_AUDIT_SECTION,
        RUNTIME_AUDIT_LABEL,
        DASHBOARD_LABEL,
        FORBIDDEN_DASHBOARD_LABEL
    }
}
