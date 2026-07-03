package com.endava.ai.skills.mcp.fs;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileTextReader {
    private static final Pattern RUN_DIRECTORY_PATTERN = Pattern.compile("(?i)^run[-_]\\d+.*$");

    public String read(Path path, McpPackageValidationResult result) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("file.unreadable", exception.getMessage(), path));
            return "";
        }
    }

    public String readIfRegular(Path path, McpPackageValidationResult result) {
        if (!Files.isRegularFile(path)) {
            return "";
        }
        return read(path, result);
    }

    public void requireDirectory(Path path, String label, McpPackageValidationResult result) {
        if (!Files.isDirectory(path)) {
            result.add(McpPackageViolation.blocker("directory.missing", "Missing " + label, path));
        }
    }

    public void requireFile(Path path, String label, McpPackageValidationResult result) {
        if (!Files.isRegularFile(path)) {
            result.add(McpPackageViolation.blocker("file.missing", "Missing " + label, path));
        }
    }

    public void requireNonEmptyFile(Path path, String label, McpPackageValidationResult result) {
        requireFile(path, label, result);
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            if (Files.size(path) == 0L) {
                result.add(McpPackageViolation.blocker("file.empty", "Empty " + label, path));
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("file.unreadable", exception.getMessage(), path));
        }
    }

    public List<Path> runDirectories(Path runsRoot, McpPackageValidationResult result) {
        try (Stream<Path> stream = Files.list(runsRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> RUN_DIRECTORY_PATTERN.matcher(path.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("runs.unreadable", exception.getMessage(), runsRoot));
            return List.of();
        }
    }

    public List<Path> findFiles(Path directory, String suffix, McpPackageValidationResult result) {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("files.walk-failed", exception.getMessage(), directory));
            return List.of();
        }
    }
}
