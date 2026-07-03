package com.ai.skills.reporting;

public final class ReportSkillSurface {
    public static final String IMPLEMENTATION_PACKAGE = "com.ai.core.reporting";
    public static final String SANITY_SUITE = "src/test/resources/atf/core/reporting/testng-reporting-sanity.xml";
    public static final String INTEGRATION_SUITE = "src/test/resources/atf/core/reporting/testng-reporting-integration.xml";
    public static final String CONSUMER_SMOKE_SUITE =
            "src/test/resources/atf/core/reporting/testng-reporting-consumer-smoke.xml";
    public static final String DEEP_SUITE = "src/test/resources/atf/core/suites/reporting/testng-atf-extent.xml";
    public static final String[] PUBLIC_API = {
            "ReportingManager",
            "ReportLogger",
            "StepLogger",
            "HtmlReportShellValidator",
            "BusinessGraphLayoutAdapter"
    };
    public static final String[] OWNED_RESPONSIBILITIES = {
            "html shell",
            "shared CSS",
            "score card primitives",
            "business graph layout composition",
            "artifact links",
            "Extent/Spark style",
            "runtime audit rendering",
            "console-safe report output"
    };
    public static final String[] FORBIDDEN_OWNERSHIP = {
            "MCP package publishability",
            "Atlas business meaning",
            "Business Understanding semantic scoring",
            "curated example promotion decision"
    };

    private ReportSkillSurface() {
    }
}
