package com.endava.ai.core.reporting.adapters.extent.inspection;

import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewInspectionContext;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewPublishPolicyInspection;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewReportKindInspection;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewScoreCardInspection;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewShellInspection;
import com.endava.ai.core.reporting.adapters.extent.inspection.overview.ExtentOverviewTagInspection;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure inspector for the ATF-published Extent Overview shell.
 * Runtime callers can still finalize reports best-effort, but publishable
 * artifacts should pass this inspection before they are referenced by Atlas/MCP.
 */
public final class ExtentPublishedOverviewInspector {
    private final ExtentOverviewInspectionContext context;

    private ExtentPublishedOverviewInspector(String html) {
        this.context = new ExtentOverviewInspectionContext(html);
    }

    public static ExtentPublishedOverviewInspector inspect(String html) {
        return new ExtentPublishedOverviewInspector(html);
    }

    public List<String> violations() {
        return violations(ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE);
    }

    public List<String> violations(ExtentReportPublishPolicy policy) {
        ArrayList<String> violations = new ArrayList<>();
        ExtentOverviewShellInspection.inspect(context, violations);
        ExtentOverviewScoreCardInspection.inspect(context, violations);
        ExtentOverviewTagInspection.inspect(context, violations);
        ExtentOverviewPublishPolicyInspection.inspect(context, policy, violations);
        return List.copyOf(violations);
    }

    public List<String> violations(ExtentReportKind kind) {
        ExtentReportKind effective = kind == null ? ExtentReportKind.INTERNAL_COMPONENT_FIXTURE : kind;
        ArrayList<String> violations = new ArrayList<>(violations(effective.publishPolicy()));
        ExtentOverviewReportKindInspection.inspect(context, effective, violations);
        return List.copyOf(violations);
    }

    public void requirePublishable() {
        requirePublishable(ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE);
    }

    public void requirePublishable(ExtentReportPublishPolicy policy) {
        List<String> violations = violations(policy);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Extent Overview is not publishable: " + String.join("; ", violations));
        }
    }

    public void requirePublishable(ExtentReportKind kind) {
        List<String> violations = violations(kind);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Extent Overview is not publishable: " + String.join("; ", violations));
        }
    }
}
