package com.endava.ai.skills.mcp.tests.support;

import com.endava.ai.skills.mcp.context.McpProjectLocator;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.profile.html.McpHtmlExampleReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpFixture {
    private final Path root;

    public static McpFixture cloneLatestPackage() throws IOException {
        Path source = McpProjectLocator.packageRoot();
        Path clone = Files.createTempDirectory("mcp-latest-root-run-");
        copyDirectory(source, clone);
        return new McpFixture(clone);
    }

    public static McpFixture cloneLatestPackageFirstRunOnly() throws IOException {
        Path source = McpProjectLocator.packageRoot();
        Path clone = Files.createTempDirectory("mcp-latest-root-run-");
        copyDirectory(source, clone, path -> !isAdditionalRunPath(source, path));
        return new McpFixture(clone);
    }

    public static McpFixture cloneLatestPackageSourceSpine() throws IOException {
        Path source = McpProjectLocator.packageRoot();
        Path clone = Files.createTempDirectory("mcp-latest-source-spine-");
        copyDirectory(source, clone, path -> !isValidationPath(source, path));
        return new McpFixture(clone);
    }

    public static McpFixture clonePrimaryHtmlExamplePackage() throws IOException {
        Path source = primaryReferencePackage();
        Path clone = Files.createTempDirectory("mcp-html-ex-reference-");
        copyDirectory(source, clone);
        return new McpFixture(clone);
    }

    public static McpFixture clonePrimaryHtmlExampleFirstRunOnly() throws IOException {
        Path source = primaryReferencePackage();
        Path clone = Files.createTempDirectory("mcp-html-ex-first-run-");
        copyDirectory(source, clone, path -> !isAdditionalRunPath(source, path));
        return new McpFixture(clone);
    }

    public static McpFixture clonePrimaryHtmlExampleReportSpine() throws IOException {
        Path source = primaryReferencePackage();
        Path clone = Files.createTempDirectory("mcp-html-ex-report-spine-");
        copyDirectory(source, clone, path -> !isRunPath(source, path));
        return new McpFixture(clone);
    }

    public Path resolve(String relativePath) {
        return root.resolve(relativePath);
    }

    public void write(String relativePath, String content) throws IOException {
        write(resolve(relativePath), content);
    }

    public void mutateReport(UnaryOperator<String> mutation) throws IOException {
        mutateReport(root, mutation);
    }

    public static void mutateReport(Path packageRoot, UnaryOperator<String> mutation) throws IOException {
        Path report = packageRoot.resolve(McpArtifactPaths.FINAL_REPORT);
        Files.writeString(report, mutation.apply(Files.readString(report)));
    }

    public static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void copyDirectory(Path source, Path destination) throws IOException {
        copyDirectory(source, destination, path -> true);
    }

    private static void copyDirectory(Path source, Path destination, Predicate<Path> include) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : stream.sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList())) {
                if (!include.test(path)) {
                    continue;
                }
                Path target = destination.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static boolean isAdditionalRunPath(Path source, Path path) {
        String relative = source.relativize(path).toString().replace('\\', '/');
        return relative.startsWith(McpArtifactPaths.RUNS_ROOT + "/run_")
                && !relative.startsWith(McpArtifactPaths.RUNS_ROOT + "/run_01");
    }

    private static boolean isRunPath(Path source, Path path) {
        String relative = source.relativize(path).toString().replace('\\', '/');
        return relative.equals(McpArtifactPaths.RUNS_ROOT)
                || relative.startsWith(McpArtifactPaths.RUNS_ROOT + "/");
    }

    private static boolean isRunValidationPath(Path source, Path path) {
        String relative = source.relativize(path).toString().replace('\\', '/');
        return relative.startsWith(McpArtifactPaths.RUNS_ROOT + "/run_")
                && relative.contains("/" + McpArtifactPaths.VALIDATION_ROOT + "/");
    }

    private static boolean isValidationPath(Path source, Path path) {
        String relative = source.relativize(path).toString().replace('\\', '/');
        return relative.equals(McpArtifactPaths.VALIDATION_ROOT)
                || relative.startsWith(McpArtifactPaths.VALIDATION_ROOT + "/")
                || isRunValidationPath(source, path);
    }

    private static Path primaryReferencePackage() throws IOException {
        return McpHtmlExampleReference.load().root();
    }
}
