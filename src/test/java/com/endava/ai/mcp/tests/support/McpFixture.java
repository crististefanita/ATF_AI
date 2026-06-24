package com.endava.ai.mcp.tests.support;

import com.endava.ai.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpFixture {
    private static final Pattern PRIMARY_REFERENCE = Pattern.compile(
            "(?is)Primary structural reference report:\\s*`([^`]+)`");
    private static final Pattern ANY_EXAMPLE_REFERENCE = Pattern.compile("`([^`]+)`");

    private final Path root;

    private McpFixture(Path root) {
        this.root = root;
    }

    public static McpFixture cloneLatestPackage() throws IOException {
        Path source = McpProjectLocator.packageRoot();
        Path clone = Files.createTempDirectory("atf6-mcp-latest-root-run-");
        copyDirectory(source, clone);
        return new McpFixture(clone);
    }

    public static McpFixture clonePrimaryHtmlExamplePackage() throws IOException {
        Path htmlExamples = McpProjectLocator.projectRoot().resolve("docs/html-ex");
        Path source = htmlExamples.resolve(primaryReferenceFolder(htmlExamples));
        Path clone = Files.createTempDirectory("atf6-mcp-html-ex-reference-");
        copyDirectory(source, clone);
        return new McpFixture(clone);
    }

    public Path root() {
        return root;
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
        Path report = packageRoot.resolve("analysis/langgraph-business-understanding.html");
        Files.writeString(report, mutation.apply(Files.readString(report)));
    }

    public static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void copyDirectory(Path source, Path destination) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : stream.sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList())) {
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

    private static String primaryReferenceFolder(Path htmlExamples) throws IOException {
        String readme = Files.readString(htmlExamples.resolve("README.md"));
        Matcher explicit = PRIMARY_REFERENCE.matcher(readme);
        if (explicit.find()) {
            return explicit.group(1);
        }
        Matcher any = ANY_EXAMPLE_REFERENCE.matcher(readme);
        while (any.find()) {
            String candidate = any.group(1);
            if (Files.isRegularFile(htmlExamples.resolve(candidate)
                    .resolve("analysis/langgraph-business-understanding.html"))) {
                return candidate;
            }
        }
        throw new IOException("docs/html-ex/README.md does not expose a primary structural reference package");
    }
}
