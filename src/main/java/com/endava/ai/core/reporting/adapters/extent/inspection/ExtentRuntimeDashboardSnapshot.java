package com.endava.ai.core.reporting.adapters.extent.inspection;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;

public final class ExtentRuntimeDashboardSnapshot {
    private final ExtentSparkReportInspector inspector;

    private ExtentRuntimeDashboardSnapshot(String html) {
        inspector = ExtentSparkReportInspector.inspect(html);
    }

    public static ExtentRuntimeDashboardSnapshot fromHtml(String html) {
        return new ExtentRuntimeDashboardSnapshot(html);
    }

    public int passed() {
        return Integer.parseInt(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_PASSED));
    }

    public int failed() {
        return Integer.parseInt(inspector.dashboardTopCount(ExtentDashboardLabels.TESTS_FAILED));
    }

    public int testCases() {
        return Integer.parseInt(inspector.dashboardSummaryCount(ExtentDashboardLabels.TEST_CASES));
    }

    public int classDistributionTotal() {
        return inspector.runtimeTagTableCountSum(ExtentDataAttributes.TAG_TABLE_CLASSES);
    }

    public int packageDistributionTotal() {
        return inspector.runtimeTagTableCountSum(ExtentDataAttributes.TAG_TABLE_PACKAGES);
    }
}
