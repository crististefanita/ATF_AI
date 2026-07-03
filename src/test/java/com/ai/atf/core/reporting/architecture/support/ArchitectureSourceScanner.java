package com.ai.atf.core.reporting.architecture.support;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlSizeBudgetSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public final class ArchitectureSourceScanner {

    private ArchitectureSourceScanner() {
    }

    public static List<String> overloadedPackages(Path root, int maxClasses) throws IOException {
        try (Stream<Path> directories = Files.walk(root)) {
            return directories
                    .filter(Files::isDirectory)
                    .map(ArchitectureSourceScanner::packageClassCount)
                    .filter(count -> count.classes > maxClasses)
                    .sorted(Comparator.comparing(count -> count.packagePath))
                    .map(count -> count.packagePath + " has " + count.classes + " classes")
                    .collect(Collectors.toList());
        }
    }

    public static List<String> packageClassCountViolations(
            Path root,
            int defaultMaxClasses,
            List<AtfCoreYamlSizeBudgetSpec> capOverrides
    ) throws IOException {
        try (Stream<Path> directories = Files.walk(root)) {
            return directories
                    .filter(Files::isDirectory)
                    .map(directory -> packageClassCountViolation(directory, defaultMaxClasses, capOverrides))
                    .filter(violation -> !violation.isBlank())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static void requireMaxLines(List<String> oversized, Path file, int maxLines) throws IOException {
        long lines = lineCount(file);
        if (lines > maxLines) {
            oversized.add(file + " has " + lines + " lines, max " + maxLines);
        }
    }

    public static List<String> filesAboveLimit(Path root, int maxLines) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .map(path -> classSizeViolation(path, maxLines))
                    .filter(violation -> !violation.isBlank())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static List<String> directJavaFilesAboveLimit(Path directory, int maxLines) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .map(path -> classSizeViolation(path, maxLines))
                    .filter(violation -> !violation.isBlank())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static List<String> oversizedTestClassViolations(
            Path root,
            int defaultMaxLines,
            Map<String, Integer> legacyCaps
    ) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> readUnchecked(path).contains("@Test"))
                    .map(path -> testClassSizeViolation(path, defaultMaxLines, legacyCaps))
                    .filter(violation -> !violation.isBlank())
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static List<String> sourceMatches(Path root, Pattern pattern) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> pattern.matcher(readUnchecked(path)).find())
                    .map(Path::toString)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static boolean sourceContains(Path path, String token) throws IOException {
        return read(path).contains(token);
    }

    public static boolean sourceContainsAny(Path path, List<String> tokens) throws IOException {
        String source = read(path);
        for (String token : tokens) {
            if (source.contains(token)) {
                return true;
            }
        }
        return false;
    }

    public static long directJavaFileCount(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .count();
        }
    }

    public static boolean containsReportingLifecycleCall(String source, String methodName) {
        return source.contains("ReportingManager." + methodName + "();")
                || source.contains("ReportingManager::" + methodName);
    }

    public static String operationalReadmeShapeViolation(Path readme, String id) throws IOException {
        String source = read(readme).toLowerCase();
        for (String section : List.of("## purpose", "## entry point", "## change here when", "## do not",
                "## verification")) {
            if (!source.contains(section)) {
                return "Core Reporting README must expose `" + section + "` for " + id + ": " + readme;
            }
        }
        return "";
    }

    public static String read(Path path) throws IOException {
        return Files.readString(path);
    }

    public static String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect source file " + path, e);
        }
    }

    public static long lineCount(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        }
    }

    public static Map<String, Integer> legacyLargeTestCaps() {
        return AtfCoreYamlContractCatalog.sizeBudgets().stream()
                .filter(budget -> "legacy-test-cap".equals(budget.kind()))
                .collect(Collectors.toUnmodifiableMap(
                        AtfCoreYamlSizeBudgetSpec::path,
                        AtfCoreYamlSizeBudgetSpec::max
                ));
    }

    private static PackageClassCount packageClassCount(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            long classes = files
                    .filter(AtfSourcePaths::isJavaSource)
                    .count();
            return new PackageClassCount(directory.toString(), classes);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect package " + directory, e);
        }
    }

    private static String classSizeViolation(Path path, int maxLines) {
        try {
            long lines = lineCount(path);
            return lines > maxLines ? path + " has " + lines + " lines, max " + maxLines : "";
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect class size for " + path, e);
        }
    }

    private static String packageClassCountViolation(
            Path directory,
            int defaultMaxClasses,
            List<AtfCoreYamlSizeBudgetSpec> capOverrides
    ) {
        String normalized = AtfSourcePaths.unixPath(directory);
        Integer overrideCap = capOverrides.stream()
                .filter(budget -> normalized.equals(budget.path()))
                .map(AtfCoreYamlSizeBudgetSpec::max)
                .findFirst()
                .orElse(null);
        int cap = overrideCap == null ? defaultMaxClasses : overrideCap;
        try {
            long classes = directJavaFileCount(directory);
            if (classes <= cap) {
                return "";
            }
            String capType = overrideCap == null ? "max " : "legacy ratchet cap ";
            return normalized + " has " + classes + " classes, " + capType + cap;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect package class count for " + directory, e);
        }
    }

    private static String testClassSizeViolation(Path path, int defaultMaxLines, Map<String, Integer> legacyCaps) {
        try {
            String normalized = AtfSourcePaths.unixPath(path);
            long lines = lineCount(path);
            Integer legacyCap = legacyCaps.get(normalized);

            if (legacyCap != null && lines > legacyCap) {
                return normalized + " has grown to " + lines + " lines, cap " + legacyCap;
            }
            if (legacyCap == null && lines > defaultMaxLines) {
                return normalized + " has " + lines + " lines, max " + defaultMaxLines;
            }
            return "";
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect test class size for " + path, e);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PackageClassCount {
        private final String packagePath;
        private final long classes;
    }
}
