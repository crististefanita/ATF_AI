package com.endava.ai.core.reporting.adapters.extent.spark;

import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportSettings;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtentSparkReporterSettings {

    private final Path reportPath;
    private final String documentTitle;
    private final String reportName;

    public static ExtentSparkReporterSettings fromReportSettings(ExtentReportSettings settings) {
        return new ExtentSparkReporterSettings(
                settings.reportPath(),
                settings.documentTitle(),
                settings.reportName()
        );
    }

    public static ExtentSparkReporterSettings of(Path reportPath, String documentTitle, String reportName) {
        return new ExtentSparkReporterSettings(reportPath, documentTitle, reportName);
    }

}
