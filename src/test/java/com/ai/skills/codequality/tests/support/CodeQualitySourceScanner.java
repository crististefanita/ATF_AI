package com.ai.skills.codequality.tests.support;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualitySourceScanner {
    private CodeQualitySourceScanner() {
    }

    public static List<CodeQualityPackageClassCount> packageClassCounts(List<Path> sourceRoots)
            throws IOException {
        List<CodeQualityPackageClassCount> counts = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> packages = Files.walk(sourceRoot)) {
                packages.filter(Files::isDirectory)
                        .map(CodeQualitySourceScanner::directClassCount)
                        .filter(count -> count.getClasses() > 0)
                        .forEach(counts::add);
            }
        }
        return counts.stream()
                .sorted(Comparator.comparingInt(CodeQualityPackageClassCount::getClasses).reversed()
                        .thenComparing(CodeQualityPackageClassCount::getPath))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<CodeQualitySourceSize> javaSourceSizes(List<Path> sourceRoots) throws IOException {
        List<CodeQualitySourceSize> sizes = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                files.filter(Files::isRegularFile)
                        .filter(AtfSourcePaths::isJavaSource)
                        .map(CodeQualitySourceScanner::sourceSize)
                        .forEach(sizes::add);
            }
        }
        return sizes.stream()
                .sorted(Comparator.comparingInt(CodeQualitySourceSize::getLines).reversed()
                        .thenComparing(CodeQualitySourceSize::getPath))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<CodeQualityResourceSize> yamlResourceSizes(List<Path> resourceRoots) throws IOException {
        List<CodeQualityResourceSize> sizes = new ArrayList<>();
        for (Path resourceRoot : resourceRoots) {
            try (Stream<Path> files = Files.walk(resourceRoot)) {
                files.filter(Files::isRegularFile)
                        .filter(CodeQualitySourceScanner::isYaml)
                        .map(CodeQualitySourceScanner::resourceSize)
                        .forEach(sizes::add);
            }
        }
        return sizes.stream()
                .sorted(Comparator.comparingLong(CodeQualityResourceSize::getBytes).reversed()
                        .thenComparing(CodeQualityResourceSize::getPath))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> testNgSuites(List<Path> resourceRoots) throws IOException {
        List<String> suites = new ArrayList<>();
        for (Path resourceRoot : resourceRoots) {
            try (Stream<Path> files = Files.walk(resourceRoot)) {
                files.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith("testng-"))
                        .filter(AtfSourcePaths::isXmlFile)
                        .map(AtfSourcePaths::projectRelative)
                        .forEach(suites::add);
            }
        }
        return suites.stream().sorted().collect(Collectors.toUnmodifiableList());
    }

    private static CodeQualityPackageClassCount directClassCount(Path directory) {
        try (Stream<Path> children = Files.list(directory)) {
            long classes = children
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .count();
            return new CodeQualityPackageClassCount(AtfSourcePaths.projectRelative(directory), (int) classes);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect package class count: " + directory, exception);
        }
    }

    private static CodeQualitySourceSize sourceSize(Path source) {
        try (Stream<String> lines = Files.lines(source)) {
            return new CodeQualitySourceSize(AtfSourcePaths.projectRelative(source), (int) lines.count());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read Java source for size trend: " + source, exception);
        }
    }

    private static CodeQualityResourceSize resourceSize(Path source) {
        try {
            return new CodeQualityResourceSize(AtfSourcePaths.projectRelative(source), Files.size(source));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read resource size: " + source, exception);
        }
    }

    private static boolean isYaml(Path source) {
        String name = source.getFileName().toString();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }
}
