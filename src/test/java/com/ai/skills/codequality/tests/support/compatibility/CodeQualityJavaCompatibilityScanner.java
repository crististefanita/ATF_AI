package com.ai.skills.codequality.tests.support.compatibility;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualityJavaCompatibilityScanner {
    private CodeQualityJavaCompatibilityScanner() {
    }

    public static List<CodeQualityJavaCompatibilityViolation> javaCompatibilityViolations(
            List<Path> sourceRoots,
            List<CodeQualityJavaCompatibilityRule> rules) throws IOException {
        List<CodeQualityJavaCompatibilityViolation> violations = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                for (Path source : javaSources(files)) {
                    collectViolations(source, rules, violations);
                }
            }
        }
        return violations.stream()
                .sorted(Comparator.comparing(CodeQualityJavaCompatibilityViolation::getRuleId)
                        .thenComparing(CodeQualityJavaCompatibilityViolation::getSource)
                        .thenComparingInt(CodeQualityJavaCompatibilityViolation::getLine))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Path> javaSources(Stream<Path> files) {
        return files.filter(Files::isRegularFile)
                .filter(AtfSourcePaths::isJavaSource)
                .collect(Collectors.toUnmodifiableList());
    }

    private static void collectViolations(Path source,
                                          List<CodeQualityJavaCompatibilityRule> rules,
                                          List<CodeQualityJavaCompatibilityViolation> violations)
            throws IOException {
        List<String> lines = Files.readAllLines(source);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            for (CodeQualityJavaCompatibilityRule rule : rules) {
                if (usesForbiddenApi(line, rule.getForbiddenApi())) {
                    violations.add(new CodeQualityJavaCompatibilityViolation(
                            rule.getId(),
                            AtfSourcePaths.projectRelative(source),
                            index + 1,
                            line.trim()));
                }
            }
        }
    }

    private static boolean usesForbiddenApi(String line, String forbiddenApi) {
        if ("stream.toList".equals(forbiddenApi)) {
            return line.contains(".toList()") && !line.contains("Collectors.toList()");
        }
        return line.contains(forbiddenApi);
    }
}
