package com.endava.ai.core.reporting.listener.internal.runtime.render;

import com.endava.ai.core.reporting.listener.internal.runtime.model.*;

public final class ExtentRuntimeStatusGroupScript {
    private ExtentRuntimeStatusGroupScript() {
    }

    public static String render(ExtentRuntimeSummary summary) {
        DashboardSummary dashboard = DashboardSummary.from(summary);
        return "<script>\nvar statusGroup = {\n"
                + line("parentCount", dashboard.total())
                + line("passParent", dashboard.passed())
                + line("failParent", dashboard.failed())
                + line("warningParent", 0)
                + line("skipParent", dashboard.skipped())
                + line("childCount", 0)
                + line("passChild", 0)
                + line("failChild", 0)
                + line("warningChild", 0)
                + line("skipChild", 0)
                + line("infoChild", 0)
                + line("grandChildCount", 0)
                + line("passGrandChild", 0)
                + line("failGrandChild", 0)
                + line("warningGrandChild", 0)
                + line("skipGrandChild", 0)
                + line("infoGrandChild", 0)
                + line("eventsCount", 0)
                + line("passEvents", 0)
                + line("failEvents", 0)
                + line("warningEvents", 0)
                + line("skipEvents", 0)
                + "infoEvents: 0\n"
                + "};\n</script>";
    }

    private static String line(String key, long value) {
        return key + ": " + value + ",\n";
    }
}
