package com.ai.core.reporting.adapters.extent.synthetic;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.ai.core.reporting.adapters.extent.inspection.ExtentReportArtifactLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ExtentSyntheticReportBuilder {
    private final Path reportsDir;
    private final ExtentAdapter adapter;

    private ExtentSyntheticReportBuilder(Path reportsDir, ExtentAdapter adapter) {
        this.reportsDir = reportsDir;
        this.adapter = adapter;
    }

    public static ExtentSyntheticReportBuilder in(Path reportsDir) {
        return new ExtentSyntheticReportBuilder(reportsDir, ExtentAdapter.getInstance());
    }

    public static void clearRuntimeAudit() {
        ExtentRuntimeAuditRegistry.clear();
    }

    public ExtentSyntheticReportBuilder test(String name, String description) {
        adapter.startTest(name, description);
        return this;
    }

    public ExtentSyntheticReportBuilder test(String className, String methodName, String description) {
        adapter.startTest(className, methodName, description);
        return this;
    }

    public ExtentSyntheticReportBuilder step(String name) {
        adapter.startStep(name);
        return this;
    }

    public ExtentSyntheticReportBuilder detail(String detail) {
        adapter.logDetail(detail);
        return this;
    }

    public ExtentSyntheticReportBuilder pass(String message) {
        adapter.pass(message);
        return this;
    }

    public ExtentSyntheticReportBuilder fail(String message, String detail) {
        adapter.fail(message, detail);
        return this;
    }

    public ExtentSyntheticReportBuilder attachScreenshotBase64(String base64, String title) {
        adapter.attachScreenshotBase64(base64, title);
        return this;
    }

    public ExtentSyntheticReportBuilder end(TestStatus status) {
        adapter.endTest(status);
        return this;
    }

    public SyntheticExtentReportResult flushAndFind(String marker) throws IOException {
        adapter.flush();
        Optional<Path> currentReport = currentAdapterReportContaining(marker);
        Optional<Path> report = currentReport.isPresent()
                ? currentReport
                : latestConfiguredReportContaining(marker);
        Path reportPath = report.orElseThrow(() -> new IllegalStateException(
                "Could not find synthetic Extent report containing: " + marker
        ));
        ExtentSparkReportFinalizer.standardize(reportPath, ExtentReportKind.INTERNAL_COMPONENT_FIXTURE);
        return new SyntheticExtentReportResult(reportPath, marker);
    }

    private Optional<Path> currentAdapterReportContaining(String marker) {
        Path report = adapter.currentReportPath();
        if (report == null || !Files.isRegularFile(report)) {
            return Optional.empty();
        }
        try {
            return Files.readString(report).contains(marker) ? Optional.of(report) : Optional.empty();
        } catch (IOException e) {
            throw new IllegalStateException("Could not inspect synthetic Extent report " + report, e);
        }
    }

    private Optional<Path> latestConfiguredReportContaining(String marker) throws IOException {
        return ExtentReportArtifactLocator.latestReportContaining(reportsDir, marker);
    }

    public SyntheticExtentReportResult flushFindAndPublishRuntimeAudit(
            String marker,
            List<SyntheticRuntimeInvocation> invocations
    ) throws IOException {
        SyntheticExtentReportResult result = flushAndFind(marker);
        publishRuntimeAudit(result.report(), invocations);
        return result;
    }

    public static void publishRuntimeAudit(Path report, List<SyntheticRuntimeInvocation> invocations) {
        ExtentRuntimeAuditRegistry.publishOrUpdateReport(
                report,
                invocations.stream()
                        .map(SyntheticRuntimeInvocation::toRuntimeAuditInvocation)
                        .collect(Collectors.toList())
        );
    }
}
