package com.ai.skills.codequality.tests.support.assertion;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualityAssertionDensityScanner {
    private static final Pattern TEST_ANNOTATION = Pattern.compile("@Test\\b");
    private static final Pattern ASSERTION_CUE = Pattern.compile(
            "Assert\\.|\\bassert[A-Z][A-Za-z0-9_]*\\s*\\(|\\brequire[A-Z][A-Za-z0-9_]*\\s*\\(|\\bfail\\s*\\(");

    private CodeQualityAssertionDensityScanner() {
    }

    public static List<CodeQualityAssertionDensity> testClassDensities(List<Path> sourceRoots) throws IOException {
        List<CodeQualityAssertionDensity> densities = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                for (Path source : testSources(files)) {
                    density(source).ifPresent(densities::add);
                }
            }
        }
        return densities.stream()
                .sorted(Comparator.comparingInt(CodeQualityAssertionDensity::cuePercent)
                        .thenComparing(CodeQualityAssertionDensity::getAssertionCues)
                        .thenComparing(CodeQualityAssertionDensity::getSource))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Path> testSources(Stream<Path> files) {
        return files.filter(Files::isRegularFile)
                .filter(AtfSourcePaths::isJavaSource)
                .filter(CodeQualityAssertionDensityScanner::isTestClass)
                .collect(Collectors.toUnmodifiableList());
    }

    private static java.util.Optional<CodeQualityAssertionDensity> density(Path source) throws IOException {
        String body = Files.readString(source);
        int testMethods = TEST_ANNOTATION.matcher(body).results().collect(Collectors.toList()).size();
        if (testMethods == 0) {
            return java.util.Optional.empty();
        }
        int assertionCues = ASSERTION_CUE.matcher(body).results().collect(Collectors.toList()).size();
        return java.util.Optional.of(new CodeQualityAssertionDensity(
                AtfSourcePaths.projectRelative(source),
                testMethods,
                assertionCues));
    }

    private static boolean isTestClass(Path source) {
        String relative = AtfSourcePaths.projectRelative(source).replace('\\', '/');
        return relative.startsWith("src/test/java/") && source.getFileName().toString().endsWith("Test.java");
    }
}
