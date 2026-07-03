package com.endava.ai.skills.reporting;

public final class AggregateReportSurface {
    public static final String IMPLEMENTATION_PACKAGE = "com.endava.ai.core.reporting.aggregate";
    public static final String README = "src/main/java/com/endava/ai/core/reporting/aggregate/README.md";
    public static final String SANITY_SUITE =
            "src/test/resources/atf/core/reporting/testng-reporting-sanity.xml";
    public static final String DEEP_SUITE =
            "src/test/resources/atf/core/suites/aggregate/testng-atf-core-aggregate.xml";
    public static final String[] PUBLIC_API = {
            "AggregateReportDescriptor",
            "AggregateExtentReportWriter",
            "AggregateManifestWriter",
            "RuntimeCoverageAuditWriter"
    };
    public static final String[] OWNED_RESPONSIBILITIES = {
            "aggregate report descriptor",
            "aggregate Extent writing",
            "aggregate final audit",
            "aggregate manifest publication",
            "runtime coverage evidence"
    };
    public static final String[] FORBIDDEN_OWNERSHIP = {
            "MCP package publishability",
            "Atlas business meaning",
            "business-understanding scoring",
            "generated package promotion"
    };

    private AggregateReportSurface() {
    }
}
