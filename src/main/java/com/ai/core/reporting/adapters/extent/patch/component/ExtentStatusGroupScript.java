package com.ai.core.reporting.adapters.extent.patch.component;

final class ExtentStatusGroupScript {
    private ExtentStatusGroupScript() {
    }

    static String render(ExtentComponentStatus status) {
        return "<script>\nvar statusGroup = {\n"
                + line("parentCount", status.classTotal())
                + line("passParent", status.classPassed())
                + line("failParent", status.classFailed())
                + line("warningParent", 0)
                + line("skipParent", status.classSkipped())
                + line("childCount", status.testTotal())
                + line("passChild", status.testPassed())
                + line("failChild", status.testFailed())
                + line("warningChild", 0)
                + line("skipChild", status.testSkipped())
                + line("infoChild", 0)
                + line("grandChildCount", status.testTotal())
                + line("passGrandChild", status.testPassed())
                + line("failGrandChild", status.testFailed())
                + line("warningGrandChild", 0)
                + line("skipGrandChild", status.testSkipped())
                + line("infoGrandChild", 0)
                + line("eventsCount", 0)
                + line("passEvents", 0)
                + line("failEvents", 0)
                + line("warningEvents", 0)
                + line("skipEvents", 0)
                + "infoEvents: 0\n"
                + "};\n</script>";
    }

    private static String line(String key, int value) {
        return key + ": " + value + ",\n";
    }
}
