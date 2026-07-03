package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AtfSkillCommandEvidenceDiscovery {
    private static final Path SUREFIRE_REPORTS = Path.of("target", "surefire-reports");
    private static final Path TARGET_REPORTS = Path.of("target", "reports");

    private AtfSkillCommandEvidenceDiscovery() {
    }

    static List<String> discover(Path workspaceRoot) {
        Set<Path> artifacts = new LinkedHashSet<>();
        artifacts.addAll(discoverSurefire(workspaceRoot.resolve(SUREFIRE_REPORTS)));
        artifacts.addAll(discoverExtentReports(workspaceRoot.resolve(TARGET_REPORTS)));
        return artifacts.stream()
                .map(path -> relativeTo(workspaceRoot, path))
                .collect(Collectors.toList());
    }

    private static List<Path> discoverSurefire(Path directory) {
        return regularFiles(directory)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return "testng-results.xml".equals(fileName)
                            || "TEST-TestSuite.xml".equals(fileName)
                            || fileName.startsWith("TEST-") && fileName.endsWith(".xml");
                })
                .collect(Collectors.toList());
    }

    private static List<Path> discoverExtentReports(Path directory) {
        return regularFiles(directory)
                .filter(path -> {
                    String fileName = path.getFileName().toString();
                    return "ExtentReport.html".equals(fileName)
                            || fileName.startsWith("ExtentReport_") && fileName.endsWith(".html");
                })
                .collect(Collectors.toList());
    }

    private static Stream<Path> regularFiles(Path directory) {
        if (!Files.isDirectory(directory)) {
            return Stream.empty();
        }
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.toAbsolutePath().normalize())
                    .sorted()
                    .collect(Collectors.toList())
                    .stream();
        } catch (IOException exception) {
            return Stream.empty();
        }
    }

    private static String relativeTo(Path workspaceRoot, Path artifact) {
        Path root = workspaceRoot.toAbsolutePath().normalize();
        Path normalized = artifact.toAbsolutePath().normalize();
        if (normalized.startsWith(root)) {
            return AtfSourcePaths.unixPath(root.relativize(normalized));
        }
        return AtfSourcePaths.unixPath(normalized);
    }
}
