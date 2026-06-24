package com.endava.ai.mcp.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class McpProjectLocator {
    private static final Pattern ROOT_RUN_TIMESTAMP = Pattern.compile("\\d{4}-\\d{2}-\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}");

    private McpProjectLocator() {
    }

    public static Path projectRoot() {
        return Path.of(System.getProperty("mcp.project.root", "."))
                .toAbsolutePath()
                .normalize();
    }

    public static Path packageRoot() throws IOException {
        return packageRoot(projectRoot());
    }

    public static Path packageRoot(Path projectRoot) throws IOException {
        String configured = System.getProperty("mcp.package.root", "latest").trim();
        if (!configured.isEmpty() && !"latest".equalsIgnoreCase(configured)) {
            Path path = Path.of(configured);
            return path.isAbsolute() ? path.normalize() : projectRoot.resolve(path).normalize();
        }

        Path outRoot = projectRoot.resolve("docs/out").normalize();
        if (!Files.isDirectory(outRoot)) {
            throw new IllegalStateException("No docs/out directory and no -Dmcp.package.root was provided.");
        }

        try (Stream<Path> stream = Files.list(outRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> ROOT_RUN_TIMESTAMP.matcher(path.getFileName().toString()).matches())
                    .max(Comparator.comparing(path -> path.getFileName().toString()))
                    .orElseThrow(() -> new IllegalStateException("No timestamp ROOT_RUN folder under " + outRoot));
        }
    }

    public static int intRuntimeProperty(String key) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(projectRoot().resolve(McpArtifactPaths.RUNTIME_PROPERTIES))) {
            properties.load(reader);
        }
        return Integer.parseInt(properties.getProperty(key).trim());
    }
}
