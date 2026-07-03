package com.ai.core.reporting.adapters.extent.spark;

import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkCollapsePolicy;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkTheme;

import java.nio.file.Path;

public final class ExtentSparkReporterFactory {

    private ExtentSparkReporterFactory() {
    }

    public static ExtentSparkReporter darkReporter(Path reportPath, String documentTitle, String reportName) {
        return darkReporter(ExtentSparkReporterSettings.of(reportPath, documentTitle, reportName));
    }

    public static ExtentSparkReporter darkReporter(ExtentSparkReporterSettings settings) {
        ExtentSparkReporter spark = new ExtentSparkReporter(settings.reportPath().toString());
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle(settings.documentTitle());
        spark.config().setReportName(settings.reportName());
        spark.config().setCss(ExtentSparkTheme.sharedDarkThemeCss());
        spark.config().setJs(ExtentSparkCollapsePolicy.defaultCollapseScript());
        return spark;
    }
}
