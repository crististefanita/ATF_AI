package com.endava.ai.skills.mcp.support.shape;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpPackageShapeSourceAssertions {
    private static final List<Pattern> DISCIPLINED_LOMBOK_PATTERNS = List.of(
            Pattern.compile("@(?:Data|Setter|SneakyThrows)\\b"),
            Pattern.compile("@AllArgsConstructor\\s*\\(\\s*access\\s*=\\s*AccessLevel\\.PUBLIC\\s*\\)"),
            Pattern.compile("import\\s+lombok\\.(?!(?:AccessLevel|AllArgsConstructor|Builder|Getter|NoArgsConstructor|"
                    + "NonNull|RequiredArgsConstructor|Value|With|experimental\\.Accessors);)\\S+;")
    );

    private McpPackageShapeSourceAssertions() {
    }

    public static void assertConsoleOutputUsesGateway(Path root) throws IOException {
        Pattern directConsole = Pattern.compile("\\bSystem\\s*\\.\\s*(out|err)\\b");
        List<String> violations;
        try (Stream<Path> files = Files.walk(root)) {
            violations = files
                    .filter(McpPackageShapeSourceAssertions::isJavaSource)
                    .filter(path -> directConsole.matcher(readUnchecked(path)).find())
                    .map(McpPackageShapeSourceAssertions::normalize)
                    .collect(Collectors.toList());
        }

        Assert.assertTrue(violations.isEmpty(),
                "MCP main code must use ConsoleLogger instead of direct System.out/System.err: " + violations);
    }

    public static void assertNoDirectImports(Path root, String forbiddenPackage, String message) throws IOException {
        List<String> violations = directImportViolations(root, forbiddenPackage);

        Assert.assertTrue(violations.isEmpty(), message + violations);
    }

    public static void assertMajorPackagesExposeCreditEfficientReadmes(Path mainMcpRoot) {
        List<Path> readmes = List.of(
                mainMcpRoot.resolve("rules").resolve(AtfSourcePaths.README_FILE_NAME),
                mainMcpRoot.resolve("contracts").resolve(AtfSourcePaths.README_FILE_NAME),
                mainMcpRoot.resolve("validation").resolve(AtfSourcePaths.README_FILE_NAME)
        );

        List<String> violations = readmes.stream()
                .filter(path -> !hasCreditEfficientShape(path))
                .map(McpPackageShapeSourceAssertions::normalize)
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(),
                "Major MCP packages need local README maps with Purpose, Entry points, Change here when, "
                        + "Do not and Verification sections: " + violations);
    }

    public static void assertLombokUsageStaysDisciplined(Path mainRoot, Path testRoot) throws IOException {
        List<String> violations = new ArrayList<>();
        collectDisciplinedLombokViolations(mainRoot, violations);
        collectDisciplinedLombokViolations(testRoot, violations);
        Assert.assertTrue(violations.isEmpty(),
                "MCP may use Lombok broadly for immutable specs, rows, snapshots and small result objects. "
                        + "Avoid mutable shortcuts, hidden throws or public Lombok all-args constructors: " + violations);
    }

    private static List<String> directImportViolations(Path root, String forbiddenPackage) throws IOException {
        String forbiddenImport = "import " + forbiddenPackage;
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(McpPackageShapeSourceAssertions::isJavaSource)
                    .filter(path -> readUnchecked(path).contains(forbiddenImport))
                    .map(McpPackageShapeSourceAssertions::normalize)
                    .collect(Collectors.toList());
        }
    }

    private static boolean hasCreditEfficientShape(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String text = readUnchecked(path);
        return text.contains("## Purpose")
                && text.contains("## Entry points")
                && text.contains("## Change here when")
                && text.contains("## Do not")
                && text.contains("## Verification");
    }

    private static void collectDisciplinedLombokViolations(Path root, List<String> violations) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(McpPackageShapeSourceAssertions::isJavaSource)
                    .forEach(path -> collectDisciplinedLombokViolationsIn(path, violations));
        }
    }

    private static void collectDisciplinedLombokViolationsIn(Path path, List<String> violations) {
        String source = readUnchecked(path);
        for (Pattern pattern : DISCIPLINED_LOMBOK_PATTERNS) {
            java.util.regex.Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                violations.add(normalize(path) + ":" + lineNumber(source, matcher.start())
                        + " -> " + matcher.group());
            }
        }
    }

    private static String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect MCP source " + path, e);
        }
    }

    private static boolean isJavaSource(Path path) {
        return path.getFileName().toString().endsWith(AtfSourcePaths.JAVA_EXTENSION);
    }

    private static String normalize(Path path) {
        return AtfSourcePaths.unixPath(path.normalize());
    }

    private static long lineNumber(String source, int offset) {
        return source.substring(0, offset).chars().filter(ch -> ch == '\n').count() + 1;
    }
}
