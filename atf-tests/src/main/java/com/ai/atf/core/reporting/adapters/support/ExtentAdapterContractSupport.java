package com.ai.atf.core.reporting.adapters.support;

import com.aventstack.extentreports.ExtentTest;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.adapters.extent.ExtentTestContext;
import com.ai.core.reporting.adapters.extent.inspection.ExtentReportArtifactLocator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class ExtentAdapterContractSupport {

    private ExtentAdapterContractSupport() {
    }

    public static ExtentTest currentExtentTest(ExtentAdapter adapter) throws ReflectiveOperationException {
        Field field = ExtentAdapter.class.getDeclaredField("testContext");
        field.setAccessible(true);
        ExtentTestContext context = (ExtentTestContext) field.get(adapter);
        return context.get();
    }

    public static Path extentReportContaining(String marker) throws IOException {
        return extentReportContaining(activeReportsDir(), marker);
    }

    public static Path extentReportContaining(Path reportsDir, String marker) throws IOException {
        return ExtentReportArtifactLocator.latestReportContaining(reportsDir, marker)
                .orElseThrow(() -> new AssertionError("No Extent HTML report containing marker " + marker
                        + " generated under " + reportsDir));
    }

    public static long countExtentHtmlReports() throws IOException {
        return countExtentHtmlReports(activeReportsDir());
    }

    public static long countExtentHtmlReports(Path reportsDir) throws IOException {
        return ExtentReportArtifactLocator.countExtentHtmlReports(reportsDir);
    }

    public static String uniqueMarker(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    public static void resetExtentRuntime() {
        ExtentRuntimeAuditRegistry.clear();
        ExtentAdapter.getInstance().endTest(TestStatus.RESET);
        ExtentAdapter.getInstance().resetForTests();
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
    }

    public static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Could not delete " + path, e);
                        }
                    });
        }
    }

    private static Path activeReportsDir() {
        return Path.of(ConfigManager.get(ReportingConfigKeys.REPORTS_DIR, ""));
    }

}
