package com.ai.core.reporting.adapters.extent;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.ai.core.reporting.adapters.extent.config.ExtentReportSettings;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterFactory;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReporterSettings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ExtentReportSession {
    private final ExtentReportSettings settings;
    private final ExtentReports reports;

    static ExtentReportSession fromRuntime() {
        ExtentReportSettings settings = ExtentReportSettings.fromRuntime();
        ensureReportDirectory(settings.reportPath());
        ExtentReports reports = new ExtentReports();
        reports.attachReporter(ExtentSparkReporterFactory.darkReporter(
                ExtentSparkReporterSettings.fromReportSettings(settings)
        ));
        return new ExtentReportSession(settings, reports);
    }

    private static void ensureReportDirectory(Path reportPath) {
        Path parent = reportPath.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Extent reports directory: " + parent, exception);
        }
    }

    ExtentTest createTest(String name, String description) {
        return reports.createTest(
                name != null ? name : "Test",
                description != null ? description : ""
        );
    }

    Path reportPath() {
        return settings.reportPath();
    }

    void flushAndStandardize() {
        reports.flush();
        standardizeQuietly();
    }

    private void standardizeQuietly() {
        try {
            ExtentSparkReportFinalizer.standardizeQuietly(
                    settings.reportPath(),
                    ExtentReportKind.INTERNAL_COMPONENT_FIXTURE
            );
        } catch (RuntimeException | LinkageError standardizationFailure) {
            // Standardization is best-effort; raw Extent output is still the primary artifact.
        }
    }
}
