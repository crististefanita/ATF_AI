package com.endava.ai.core.reporting.adapters.extent.spark;

import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportPublishPolicy;
import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import com.endava.ai.core.reporting.diagnostic.NonBlockingDiagnostic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExtentSparkReportFinalizer {

    private ExtentSparkReportFinalizer() {
    }

    public static void standardize(Path reportPath, ExtentReportPublishPolicy policy) throws IOException {
        ExtentSparkHtmlPostProcessor.normalizeReport(reportPath);
        validatePublishedOverview(reportPath, policy);
    }

    public static void standardize(Path reportPath, ExtentReportKind kind) throws IOException {
        ExtentSparkHtmlPostProcessor.normalizeReport(reportPath);
        validatePublishedOverview(reportPath, kind);
    }

    public static void standardizePublishedValidation(Path reportPath) throws IOException {
        standardize(reportPath, ExtentReportKind.RUNTIME_VALIDATION);
    }

    public static void validatePublishedOverview(Path reportPath, ExtentReportPublishPolicy policy) throws IOException {
        if (reportPath == null || !Files.isRegularFile(reportPath)) {
            return;
        }
        String html = Files.readString(reportPath, StandardCharsets.UTF_8);
        ExtentPublishedOverviewInspector.inspect(html).requirePublishable(policy);
    }

    public static void validatePublishedOverview(Path reportPath, ExtentReportKind kind) throws IOException {
        if (reportPath == null || !Files.isRegularFile(reportPath)) {
            return;
        }
        String html = Files.readString(reportPath, StandardCharsets.UTF_8);
        ExtentPublishedOverviewInspector.inspect(html).requirePublishable(kind);
    }

    public static void validatePublishedValidation(Path reportPath) throws IOException {
        validatePublishedOverview(reportPath, ExtentReportKind.RUNTIME_VALIDATION);
    }

    public static void standardizeQuietly(Path reportPath, ExtentReportKind kind) {
        try {
            standardize(reportPath, kind);
        } catch (IOException | RuntimeException e) {
            NonBlockingDiagnostic.warn("Extent Spark report finalization", e);
        }
    }
}
