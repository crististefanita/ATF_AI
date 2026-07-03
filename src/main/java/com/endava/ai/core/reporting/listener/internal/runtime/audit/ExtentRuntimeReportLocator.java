package com.endava.ai.core.reporting.listener.internal.runtime.audit;

import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.diagnostic.NonBlockingDiagnostic;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditTarget;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExtentRuntimeReportLocator {

    private ExtentRuntimeReportLocator() {
    }

    public static Path currentOrLatestReport(List<String> invocationMarkers) throws IOException {
        return locate(invocationMarkers).report();
    }

    public static RuntimeAuditTarget locate(List<String> invocationMarkers) throws IOException {
        RuntimeAuditTarget current = currentExtentReport();
        if (current.writable()) {
            return current;
        }
        return latestExtentReport(invocationMarkers);
    }

    private static RuntimeAuditTarget currentExtentReport() {
        Path current = ExtentAdapter.getInstance().currentReportPath();
        return current != null && Files.isRegularFile(current)
                ? RuntimeAuditTarget.current(current)
                : RuntimeAuditTarget.noCandidate();
    }

    private static RuntimeAuditTarget latestExtentReport(List<String> invocationMarkers) throws IOException {
        List<Path> candidates = extentReportCandidates();
        Path markerMatch = candidates.stream()
                .filter(path -> reportContainsAnyMarker(path, invocationMarkers))
                .findFirst()
                .orElse(null);
        if (markerMatch != null) {
            return RuntimeAuditTarget.markerMatch(markerMatch);
        }
        if (candidates.isEmpty()) {
            return RuntimeAuditTarget.noCandidate();
        }
        NonBlockingDiagnostic.warn(
                "Extent runtime report locating",
                "No current Extent report or marker-bearing candidate was found; runtime audit update skipped to avoid rewriting stale artifacts."
        );
        return RuntimeAuditTarget.ambiguousCandidates();
    }

    private static List<Path> extentReportCandidates() throws IOException {
        Path reportsDir = Path.of(ConfigManager.get(
                ReportingConfigKeys.REPORTS_DIR,
                ReportingConfigKeys.DEFAULT_REPORTS_DIR
        ));
        if (!Files.isDirectory(reportsDir)) {
            return List.of();
        }

        List<Path> candidates = new ArrayList<>();
        try (var stream = Files.list(reportsDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(ExtentRuntimeReportLocator::isExtentReport)
                    .forEach(candidates::add);
        }
        candidates.sort(Comparator.comparingLong((Path path) -> path.toFile().lastModified()).reversed());
        return candidates;
    }

    private static boolean isExtentReport(Path path) {
        return ExtentReportFiles.isExtentHtmlReport(path.getFileName().toString());
    }

    private static boolean reportContainsAnyMarker(Path report, List<String> invocationMarkers) {
        if (invocationMarkers == null || invocationMarkers.isEmpty()) {
            return true;
        }
        try {
            String html = Files.readString(report, StandardCharsets.UTF_8);
            return invocationMarkers.stream()
                    .filter(marker -> marker != null && !marker.isBlank())
                    .anyMatch(html::contains);
        } catch (IOException e) {
            NonBlockingDiagnostic.warn("Extent report candidate inspection", e);
            return false;
        }
    }
}
