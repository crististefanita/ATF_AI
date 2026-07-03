package com.endava.ai.core.reporting.aggregate.artifact;

import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class AggregateReportFiles {
    public static final String EXTENT_ARTIFACT_DIR = "extent";
    public static final String ALLURE_RESULTS_DIR = "allure-results";
    public static final String ALLURE_HTML_REPORT_DIR = "allure-report";
    public static final String ALLURE_HTML_INDEX_FILE = "index.html";
    public static final String EXTENT_REPORT_FILE = "ATFCoreAggregateExtentReport.html";
    public static final String MANIFEST_FILE = "README.md";

    private AggregateReportFiles() {
    }

    public static Path extentReport(Path root) {
        return root.resolve(EXTENT_ARTIFACT_DIR).resolve(EXTENT_REPORT_FILE);
    }

    public static Path allureResults(Path root) {
        return root.resolve(ALLURE_RESULTS_DIR);
    }

    public static Path allureHtmlReport(Path root) {
        return root.resolve(ALLURE_HTML_REPORT_DIR);
    }

    public static String allureHtmlIndexRelativePath() {
        return ALLURE_HTML_REPORT_DIR + "/" + ALLURE_HTML_INDEX_FILE;
    }

    public static String extentReportRelativePath() {
        return EXTENT_ARTIFACT_DIR + "/" + EXTENT_REPORT_FILE;
    }

    public static Path latestExtentReport() {
        Path reportsDir = Path.of(ConfigManager.get(
                        ReportingConfigKeys.REPORTS_DIR,
                        ReportingConfigKeys.DEFAULT_REPORTS_DIR
                ))
                .toAbsolutePath()
                .normalize();
        Path activeReport = ExtentAdapter.getInstance().currentReportPath();
        if (activeReport != null) {
            Path active = activeReport.toAbsolutePath().normalize();
            if (Files.isRegularFile(active) && active.startsWith(reportsDir)) {
                return active;
            }
        }
        if (!Files.isDirectory(reportsDir)) {
            return null;
        }
        try (var stream = Files.list(reportsDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> ExtentReportFiles.isExtentHtmlReport(path.getFileName().toString()))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .orElse(null);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to inspect Extent reports directory: " + reportsDir, e);
        }
    }

    public static void prepareRoot(Path root, boolean clean) {
        if (clean) {
            deleteRecursively(root);
        }
        createDirectory(root);
    }

    public static void createParent(Path file) {
        createDirectory(file.getParent());
    }

    public static void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create aggregate report directory: " + directory, e);
        }
    }

    static void deleteRecursively(Path root) {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Unable to delete aggregate report path: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to clean aggregate report root: " + root, e);
        }
    }
}
