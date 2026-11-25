package com.ai.core.reporting.adapters.extent.inspection;

import com.ai.core.reporting.adapters.extent.config.ExtentReportFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class ExtentReportArtifactLocator {
    private ExtentReportArtifactLocator() {
    }

    public static Optional<Path> latestReportContaining(Path reportsDir, String marker) throws IOException {
        if (reportsDir == null || !Files.isDirectory(reportsDir)) {
            return Optional.empty();
        }
        try (Stream<Path> files = Files.list(reportsDir)) {
            return files
                    .filter(ExtentReportArtifactLocator::isExtentHtmlReport)
                    .filter(path -> contains(path, marker))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()));
        }
    }

    public static long countExtentHtmlReports(Path reportsDir) throws IOException {
        if (reportsDir == null || !Files.isDirectory(reportsDir)) {
            return 0L;
        }
        try (Stream<Path> files = Files.list(reportsDir)) {
            return files.filter(ExtentReportArtifactLocator::isExtentHtmlReport).count();
        }
    }

    public static boolean isExtentHtmlReport(Path path) {
        return path != null && ExtentReportFiles.isExtentHtmlReport(path.getFileName().toString());
    }

    private static boolean contains(Path path, String marker) {
        try {
            return Files.readString(path).contains(marker);
        } catch (IOException e) {
            throw new IllegalStateException("Could not inspect " + path, e);
        }
    }
}
