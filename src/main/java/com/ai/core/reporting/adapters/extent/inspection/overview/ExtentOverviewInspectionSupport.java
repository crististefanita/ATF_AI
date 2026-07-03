package com.ai.core.reporting.adapters.extent.inspection.overview;

import java.util.List;

final class ExtentOverviewInspectionSupport {
    private ExtentOverviewInspectionSupport() {
    }

    static void requireContains(ExtentOverviewInspectionContext context,
                                List<String> violations,
                                String token,
                                String message) {
        if (!context.contains(token)) {
            violations.add(message);
        }
    }

    static void requireContainsAny(ExtentOverviewInspectionContext context,
                                   List<String> violations,
                                   String message,
                                   String... tokens) {
        if (!context.containsAny(tokens)) {
            violations.add(message);
        }
    }

    static void requireAbsent(ExtentOverviewInspectionContext context,
                              List<String> violations,
                              String token,
                              String message) {
        if (context.contains(token)) {
            violations.add(message);
        }
    }
}
