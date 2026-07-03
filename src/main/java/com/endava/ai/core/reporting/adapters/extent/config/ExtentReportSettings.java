package com.endava.ai.core.reporting.adapters.extent.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.config.ReportingConfigKeys.REPORTS_DIR;
import static com.endava.ai.core.support.config.ReportingConfigKeys.REPORTS_FILE_NAME;
import static com.endava.ai.core.support.config.ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED;
import static com.endava.ai.core.support.config.ReportingConfigKeys.REPORTS_TIMESTAMP_FORMAT;
import static com.endava.ai.core.support.config.ConfigManager.get;
import static com.endava.ai.core.support.config.ConfigManager.require;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtentReportSettings {

    public static final String DEFAULT_DOCUMENT_TITLE = "Test Report";
    public static final String DEFAULT_REPORT_NAME    = "Automation Framework";

    private static final Map<String, Path> RUN_SCOPED_TIMESTAMPED_PATHS = new HashMap<>();

    private final Path reportPath;
    private final String documentTitle;
    private final String reportName;

    public static ExtentReportSettings fromRuntime() {
        String reportsDir = require(REPORTS_DIR);
        boolean timestampEnabled = Boolean.parseBoolean(require(REPORTS_TIMESTAMP_ENABLED));
        String timestampFormat = require(REPORTS_TIMESTAMP_FORMAT);
        String explicitFileName = get(REPORTS_FILE_NAME, "");
        Date timestamp = new Date();
        Path reportPath = !explicitFileName.isBlank()
                ? Paths.get(reportsDir, ExtentReportFiles.configuredFileName(
                        explicitFileName,
                        timestampEnabled,
                        timestampFormat,
                        timestamp))
                : timestampEnabled
                ? runScopedTimestampedPath(reportsDir, timestampFormat, timestamp)
                : Paths.get(reportsDir, ExtentReportFiles.STABLE_REPORT_FILE_NAME);

        return new ExtentReportSettings(
                reportPath,
                DEFAULT_DOCUMENT_TITLE,
                DEFAULT_REPORT_NAME
        );
    }

    public static ExtentReportSettings fromRuntime(Date timestamp) {
        String reportsDir = require(REPORTS_DIR);
        boolean timestampEnabled = Boolean.parseBoolean(require(REPORTS_TIMESTAMP_ENABLED));
        String timestampFormat = require(REPORTS_TIMESTAMP_FORMAT);
        String explicitFileName = get(REPORTS_FILE_NAME, "");

        return fromValues(
                reportsDir,
                explicitFileName,
                timestampEnabled,
                timestampFormat,
                timestamp,
                DEFAULT_DOCUMENT_TITLE,
                DEFAULT_REPORT_NAME
        );
    }

    public static ExtentReportSettings fromValues(
            String reportsDir,
            boolean timestampEnabled,
            String timestampFormat,
            Date timestamp,
            String documentTitle,
            String reportName
    ) {
        return fromValues(
                reportsDir,
                "",
                timestampEnabled,
                timestampFormat,
                timestamp,
                documentTitle,
                reportName
        );
    }

    public static ExtentReportSettings fromValues(
            String reportsDir,
            String explicitFileName,
            boolean timestampEnabled,
            String timestampFormat,
            Date timestamp,
            String documentTitle,
            String reportName
    ) {
        String fileName = ExtentReportFiles.configuredFileName(
                explicitFileName,
                timestampEnabled,
                timestampFormat,
                timestamp
        );

        return new ExtentReportSettings(
                Paths.get(reportsDir, fileName),
                documentTitle,
                reportName
        );
    }

    private static synchronized Path runScopedTimestampedPath(
            String reportsDir,
            String timestampFormat,
            Date timestamp
    ) {
        String key = reportsDir + "|" + timestampFormat;
        return RUN_SCOPED_TIMESTAMPED_PATHS.computeIfAbsent(
                key,
                ignored -> Paths.get(
                        reportsDir,
                        ExtentReportFiles.fileName(true, timestampFormat, timestamp)
                )
        );
    }

}
