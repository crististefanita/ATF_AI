package com.endava.ai.core.reporting.adapters.extent.inspection.overview;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;

import java.util.List;
import java.util.Locale;

public final class ExtentOverviewScoreCardInspection {
    private static final String TEST_CASES_CARD = ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES);
    private static final String SPARK_TESTS_CARD = ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TESTS);

    private ExtentOverviewScoreCardInspection() {
    }

    public static void inspect(ExtentOverviewInspectionContext context, List<String> violations) {
        int testCasesCards = context.count(TEST_CASES_CARD);
        if (testCasesCards != 1) {
            violations.add("expected exactly one Test cases score-card, found " + testCasesCards);
        }
        for (String label : ExtentDashboardLabels.FORBIDDEN_OVERVIEW_SCORE_CARDS) {
            ExtentOverviewInspectionSupport.requireAbsent(
                    context,
                    violations,
                    ExtentHtmlStructure.cardTitle(label),
                    label + " score-card is not allowed"
            );
        }
        if (context.runtimeAudited() && context.contains(SPARK_TESTS_CARD)) {
            violations.add("runtime-audited Overview must not expose Spark's ambiguous Tests score-card");
        }
        if (context.html().toLowerCase(Locale.ROOT)
                .contains(ExtentDashboardLabels.CONFIGURATION_METHODS.toLowerCase(Locale.ROOT))) {
            violations.add("Configuration methods must not be exposed in published Overview output");
        }
    }
}
