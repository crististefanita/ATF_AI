package com.ai.skills.atlas.tests.report;

import com.ai.skills.atlas.AtlasContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasPublishedExtentReportInventory {
    static final Path ATLAS = Path.of(AtlasContext.ATLAS_DIRECTORY);
    static final Path VALIDATION = Path.of(AtlasContext.VALIDATION_DIRECTORY);
    private static final String SELENIUM_ALL_SUITE_SUFFIX = "_ui-agentic-selenium-all-extent-report.html";
    private static final Pattern VALIDATION_HTML_LINK = Pattern.compile(
            "href=[\"'](validation/[^\"']+\\.html)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SUITE_CLASS = Pattern.compile("<class\\s+name=[\"']([^\"']+)[\"']");

    private AtlasPublishedExtentReportInventory() {
    }

    static List<Path> publishedAtlasValidatorExtentReports() throws IOException {
        return declaredValidationReports()
                .stream()
                .filter(AtlasPublishedExtentReportInventory::isPublishedAtlasValidatorReport)
                .collect(Collectors.toList());
    }

    static List<Path> publishedUiAgenticRuntimeExtentReports() throws IOException {
        return declaredValidationReports()
                .stream()
                .filter(AtlasPublishedExtentReportInventory::isPublishedUiAgenticRuntimeReport)
                .collect(Collectors.toList());
    }

    static List<Path> publishedAggregateAtlasExtentReports() throws IOException {
        return declaredValidationReports()
                .stream()
                .filter(AtlasPublishedExtentReportInventory::isPublishedAggregateReport)
                .collect(Collectors.toList());
    }

    static List<Path> declaredValidationReportsEndingWith(String suffix) throws IOException {
        return declaredValidationReports()
                .stream()
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .collect(Collectors.toList());
    }

    private static List<Path> declaredValidationReports() throws IOException {
        if (!Files.isDirectory(ATLAS)) {
            return List.of();
        }
        LinkedHashSet<Path> reports = new LinkedHashSet<>();
        for (Path atlasHtml : atlasHtmlDocuments()) {
            Matcher matcher = VALIDATION_HTML_LINK.matcher(Files.readString(atlasHtml));
            while (matcher.find()) {
                Path report = ATLAS.resolve(matcher.group(1)).normalize();
                if (Files.isRegularFile(report)) {
                    reports.add(report);
                }
            }
        }
        return reports.stream().sorted().collect(Collectors.toList());
    }

    private static List<Path> atlasHtmlDocuments() throws IOException {
        try (Stream<Path> paths = Files.list(ATLAS)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(AtlasContext.HTML_SUFFIX))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    static List<SeleniumRuntimeEvidence> seleniumAllSuiteEvidence() throws IOException {
        return declaredValidationReportsEndingWith(SELENIUM_ALL_SUITE_SUFFIX)
                .stream()
                .map(AtlasPublishedExtentReportInventory::runtimeEvidence)
                .collect(Collectors.toList());
    }

    private static boolean isPublishedAtlasValidatorReport(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(AtlasContext.NONSTANDARD_HTML_SUFFIX)) {
            return false;
        }
        if (fileName.toLowerCase(Locale.ROOT).contains("aggregate")) {
            return false;
        }
        return fileName.endsWith(AtlasContext.ATLAS_EXTENT_REPORT_FILE);
    }

    private static boolean isPublishedUiAgenticRuntimeReport(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(AtlasContext.NONSTANDARD_HTML_SUFFIX)) {
            return false;
        }
        if (fileName.toLowerCase(Locale.ROOT).contains("aggregate")) {
            return false;
        }
        return fileName.endsWith(AtlasContext.HTML_SUFFIX) && fileName.contains("_ui-agentic-");
    }

    private static boolean isPublishedAggregateReport(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(AtlasContext.HTML_SUFFIX)
                && fileName.toLowerCase(Locale.ROOT).contains("aggregate")
                && !fileName.endsWith(AtlasContext.NONSTANDARD_HTML_SUFFIX);
    }

    static final class SeleniumRuntimeEvidence {
        final Path report;
        final String expectedTestClass;
        final String expectedStatus;
        final int expectedPassed;
        final int expectedFailed;
        final int expectedSkipped;

        SeleniumRuntimeEvidence(Path report,
                                String expectedTestClass,
                                String expectedStatus,
                                int expectedPassed,
                                int expectedFailed,
                                int expectedSkipped) {
            this.report = report;
            this.expectedTestClass = expectedTestClass;
            this.expectedStatus = expectedStatus;
            this.expectedPassed = expectedPassed;
            this.expectedFailed = expectedFailed;
            this.expectedSkipped = expectedSkipped;
        }
    }

    private static SeleniumRuntimeEvidence runtimeEvidence(Path report) {
        try {
            String artifactBase = report.getFileName().toString().replace(AtlasContext.HTML_SUFFIX, "");
            Path readme = report.resolveSibling(artifactBase + AtlasContext.README_SUFFIX);
            Path suite = report.resolveSibling(artifactBase + AtlasContext.TESTNG_XML_SUFFIX);
            String readmeText = Files.readString(readme);
            String suiteText = Files.readString(suite);
            return new SeleniumRuntimeEvidence(
                    report,
                    firstSuiteClassSimpleName(suiteText, suite),
                    readmeValue(readmeText, "Status", readme),
                    readmeInt(readmeText, "Passed", readme),
                    readmeInt(readmeText, "Failed", readme),
                    readmeInt(readmeText, "Skipped", readme));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read Selenium all-suite evidence for " + report, e);
        }
    }

    private static String firstSuiteClassSimpleName(String suiteText, Path suite) {
        Matcher matcher = SUITE_CLASS.matcher(suiteText);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing <class name=...> in " + suite);
        }
        String className = matcher.group(1);
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static String readmeValue(String readmeText, String key, Path readme) {
        Matcher matcher = Pattern.compile("(?m)^-\\s*" + Pattern.quote(key) + ":\\s*(\\S+)\\s*$")
                .matcher(readmeText);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing README value " + key + " in " + readme);
        }
        return matcher.group(1);
    }

    private static int readmeInt(String readmeText, String key, Path readme) {
        return Integer.parseInt(readmeValue(readmeText, key, readme));
    }
}
