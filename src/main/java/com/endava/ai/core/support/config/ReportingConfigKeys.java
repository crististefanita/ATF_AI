package com.endava.ai.core.support.config;

public final class ReportingConfigKeys {

    public static final String REPORTING_ENGINE          = "reporting.engine";
    public static final String REPORTS_DIR               = "reports.dir";
    public static final String REPORTS_FILE_NAME         = "reports.file.name";
    public static final String REPORTS_TIMESTAMP_ENABLED = "reports.timestamp.enabled";
    public static final String REPORTS_TIMESTAMP_FORMAT  = "reports.timestamp.format";
    public static final String EXTENT_MIRROR_ENABLED     = "atf.extent.mirror.enabled";

    public static final String DEFAULT_REPORTS_DIR       = "target/reports";
    public static final String ENGINE_EXTENT             = "extent";
    public static final String ENGINE_ALLURE             = "allure";
    public static final String TIMESTAMP_ENABLED         = "true";
    public static final String TIMESTAMP_DISABLED        = "false";

    private ReportingConfigKeys() {
    }
}
