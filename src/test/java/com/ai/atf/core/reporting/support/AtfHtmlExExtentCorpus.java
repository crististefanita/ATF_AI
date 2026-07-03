package com.ai.atf.core.reporting.support;

import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlPolicySpec;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public final class AtfHtmlExExtentCorpus {
    public static final String CURRENT_STANDARD_CORPUS_KIND = "html-ex-current-standard-corpus";

    private AtfHtmlExExtentCorpus() {
    }

    public static List<HtmlExExtentReport> currentStandardReports() {
        return extentReportCandidates().stream()
                .filter(HtmlExExtentReport::looksLikeCurrentStandard)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<HtmlExExtentReport> extentReportCandidates() {
        Path root = workspaceRoot();
        ArrayList<HtmlExExtentReport> reports = new ArrayList<>();
        for (AtfCoreYamlPolicySpec spec : currentStandardCorpusSpecs()) {
            Path corpusRoot = root.resolve(spec.source()).normalize();
            if (!Files.isDirectory(corpusRoot)) {
                continue;
            }
            reports.addAll(discoverReports(corpusRoot, spec.token()));
        }
        reports.sort(Comparator.comparing(report -> report.path().toString()));
        return List.copyOf(reports);
    }

    public static List<AtfCoreYamlPolicySpec> currentStandardCorpusSpecs() {
        return AtfCoreYamlContractCatalog.reportingShellContracts().stream()
                .filter(spec -> CURRENT_STANDARD_CORPUS_KIND.equals(spec.kind()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<HtmlExExtentReport> discoverReports(Path corpusRoot, String relativeReportPath) {
        ArrayList<HtmlExExtentReport> reports = new ArrayList<>();
        try (var directories = Files.list(corpusRoot)) {
            directories
                    .filter(Files::isDirectory)
                    .map(directory -> directory.resolve(relativeReportPath).normalize())
                    .filter(Files::isRegularFile)
                    .forEach(report -> reports.add(HtmlExExtentReport.read(report)));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not scan html-ex Extent corpus: " + corpusRoot, exception);
        }
        return reports;
    }

    private static Path workspaceRoot() {
        return Path.of(System.getProperty("user.dir"));
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HtmlExExtentReport {
        private final Path path;
        private final String html;

        public static HtmlExExtentReport read(Path path) {
            try {
                return new HtmlExExtentReport(path.toAbsolutePath().normalize(), Files.readString(path));
            } catch (IOException exception) {
                throw new IllegalStateException("Could not read html-ex Extent report: " + path, exception);
            }
        }

        public boolean looksLikeCurrentStandard() {
            return html.contains(ExtentDashboardLabels.DURATION)
                    && html.contains(ExtentDashboardLabels.TEST_CASES)
                    && html.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL)
                    && html.contains(ExtentMarkers.REPORT_MODE)
                    && !html.contains(ExtentDashboardLabels.TESTNG_INVOCATIONS)
                    && !html.contains(cardTitle(ExtentDashboardLabels.STEPS));
        }

        private static String cardTitle(String label) {
            return ">" + label + "<";
        }
    }
}
