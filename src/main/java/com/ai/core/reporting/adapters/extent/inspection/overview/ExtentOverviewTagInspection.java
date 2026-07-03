package com.ai.core.reporting.adapters.extent.inspection.overview;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;

import java.util.List;

public final class ExtentOverviewTagInspection {
    private static final String RUNTIME_TAG_TABLE_ATTRIBUTE =
            ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE);
    private static final String COMPONENT_TAG_TABLE_ATTRIBUTE =
            ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE);

    private ExtentOverviewTagInspection() {
    }

    public static void inspect(ExtentOverviewInspectionContext context, List<String> violations) {
        boolean runtime = context.runtimeAudited();
        boolean component = context.componentAdapter();
        if (runtime == component) {
            violations.add("expected exactly one tag audit mode: runtime or component");
            return;
        }
        if (runtime) {
            requireTableCount(context, violations, "runtime", RUNTIME_TAG_TABLE_ATTRIBUTE, 3);
            requireTableCount(context, violations, "component", COMPONENT_TAG_TABLE_ATTRIBUTE, 0);
        } else {
            requireTableCount(context, violations, "component", COMPONENT_TAG_TABLE_ATTRIBUTE, 3);
            requireTableCount(context, violations, "runtime", RUNTIME_TAG_TABLE_ATTRIBUTE, 0);
        }
    }

    private static void requireTableCount(ExtentOverviewInspectionContext context,
                                          List<String> violations,
                                          String label,
                                          String token,
                                          int expected) {
        int actual = context.count(token);
        if (actual != expected) {
            violations.add("expected " + expected + " " + label + " tag tables, found " + actual);
        }
    }
}
