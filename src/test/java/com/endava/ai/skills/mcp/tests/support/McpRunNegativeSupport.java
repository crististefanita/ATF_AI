package com.endava.ai.skills.mcp.tests.support;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class McpRunNegativeSupport {

    private McpRunNegativeSupport() {
    }

    public static Path firstExtentReport(Path packageRoot) throws IOException {
        Path canonicalReports = packageRoot.resolve(McpRunArtifacts.runArtifact("run_01", McpRunArtifacts.REPORTS_ROOT));
        try (Stream<Path> stream = Files.isDirectory(canonicalReports) ? Files.list(canonicalReports) : Stream.empty()) {
            Path canonical = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .sorted(Comparator.comparing(Path::toString))
                    .findFirst()
                    .orElse(null);
            if (canonical != null) {
                return canonical;
            }
        }
        try (Stream<Path> stream = Files.walk(packageRoot.resolve("runs"))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().contains("extentreport"))
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .sorted(Comparator.comparing(Path::toString))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No Extent report found in fixture package: " + packageRoot));
        }
    }

    public static Path extentSha256(Path extentReport) {
        return extentReport.resolveSibling(McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_SHA256));
    }

    public static Path extentOrigin(Path extentReport) {
        return extentReport.resolveSibling(McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_ORIGIN));
    }

    public static String extentSha256Line(String hash) {
        return hash + "  " + McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT) + "\n";
    }

    public static String sha256(Path path) {
        return McpTestDigests.sha256(path);
    }
}
