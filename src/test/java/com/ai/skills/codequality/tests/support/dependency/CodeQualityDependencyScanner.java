package com.ai.skills.codequality.tests.support.dependency;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualityDependencyScanner {
    private CodeQualityDependencyScanner() {
    }

    public static List<CodeQualityImportViolation> forbiddenImportViolations(
            List<CodeQualityDependencyBoundary> boundaries) throws IOException {
        List<CodeQualityImportViolation> violations = new ArrayList<>();
        for (CodeQualityDependencyBoundary boundary : boundaries) {
            Path root = AtfSourcePaths.projectPath(boundary.getRoot());
            try (Stream<Path> files = Files.walk(root)) {
                for (Path source : javaSources(files)) {
                    collectForbiddenImports(source, boundary, violations);
                }
            }
        }
        return violations.stream()
                .sorted(Comparator.comparing(CodeQualityImportViolation::getBoundaryId)
                        .thenComparing(CodeQualityImportViolation::getSource)
                        .thenComparingInt(CodeQualityImportViolation::getLine))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Path> javaSources(Stream<Path> files) {
        return files.filter(Files::isRegularFile)
                .filter(AtfSourcePaths::isJavaSource)
                .collect(Collectors.toUnmodifiableList());
    }

    private static void collectForbiddenImports(Path source,
                                                CodeQualityDependencyBoundary boundary,
                                                List<CodeQualityImportViolation> violations) throws IOException {
        List<String> lines = Files.readAllLines(source);
        for (int index = 0; index < lines.size(); index++) {
            String imported = importedType(lines.get(index));
            if (isForbiddenImport(imported, boundary.getForbiddenPackage())) {
                violations.add(new CodeQualityImportViolation(
                        boundary.getId(),
                        AtfSourcePaths.projectRelative(source),
                        index + 1,
                        imported));
            }
        }
    }

    private static String importedType(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("import static ")) {
            return trimImport(trimmed.substring("import static ".length()));
        }
        if (trimmed.startsWith("import ")) {
            return trimImport(trimmed.substring("import ".length()));
        }
        return "";
    }

    private static String trimImport(String imported) {
        return imported.endsWith(";") ? imported.substring(0, imported.length() - 1) : imported;
    }

    private static boolean isForbiddenImport(String imported, String forbiddenPackage) {
        return imported.equals(forbiddenPackage) || imported.startsWith(forbiddenPackage + ".");
    }
}
