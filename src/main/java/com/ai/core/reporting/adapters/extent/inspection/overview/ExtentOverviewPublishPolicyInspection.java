package com.ai.core.reporting.adapters.extent.inspection.overview;

import com.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;

import java.util.List;

public final class ExtentOverviewPublishPolicyInspection {
    private static final String RUNTIME_AUDIT_REPAIR =
            "regenerate it through TestListener/runtime audit before linking it as final evidence";

    private ExtentOverviewPublishPolicyInspection() {
    }

    public static void inspect(ExtentOverviewInspectionContext context,
                        ExtentReportPublishPolicy policy,
                        List<String> violations) {
        ExtentReportPublishPolicy effective =
                policy == null ? ExtentReportPublishPolicy.INTERNAL_COMPONENT_FIXTURE : policy;
        if (effective.runtimeAuditedRequired() && !context.runtimeAudited()) {
            violations.add(effective.label() + " must be runtime-audited; component-adapter is internal-only; "
                    + RUNTIME_AUDIT_REPAIR);
        }
        if (effective.runtimeAuditedRequired() && context.componentAdapter()) {
            violations.add(effective.label()
                    + " must not keep component-adapter tag tables; " + RUNTIME_AUDIT_REPAIR);
        }
    }
}
