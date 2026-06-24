package com.endava.ai.mcp;

import java.nio.file.Path;
import java.util.Objects;

public final class McpPackageViolation {
    private final McpViolationSeverity severity;
    private final String code;
    private final String message;
    private final Path path;

    private McpPackageViolation(McpViolationSeverity severity, String code, String message, Path path) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.path = path;
    }

    public static McpPackageViolation blocker(String code, String message, Path path) {
        return new McpPackageViolation(McpViolationSeverity.BLOCKER, code, message, path);
    }

    public static McpPackageViolation warning(String code, String message, Path path) {
        return new McpPackageViolation(McpViolationSeverity.WARNING, code, message, path);
    }

    public static McpPackageViolation recommendation(String code, String message, Path path) {
        return new McpPackageViolation(McpViolationSeverity.RECOMMENDATION, code, message, path);
    }

    public McpViolationSeverity severity() {
        return severity;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        String location = path == null ? "" : " [" + path + "]";
        return severity + " " + code + ": " + message + location;
    }
}
