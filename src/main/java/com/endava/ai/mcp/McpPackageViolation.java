package com.endava.ai.mcp;

import java.nio.file.Path;
import java.util.Objects;

public final class McpPackageViolation {
    private final McpViolationSeverity severity;
    private final String code;
    private final String message;
    private final Path path;
    private final String failureMeaning;
    private final String repairAction;
    private final String artifactToRegenerate;

    private McpPackageViolation(McpViolationSeverity severity, String code, String message, Path path) {
        this(severity, code, message, path, null, null, null);
    }

    McpPackageViolation(McpViolationSeverity severity,
                        String code,
                        String message,
                        Path path,
                        String failureMeaning,
                        String repairAction,
                        String artifactToRegenerate) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
        this.path = path;
        this.failureMeaning = failureMeaning;
        this.repairAction = repairAction;
        this.artifactToRegenerate = artifactToRegenerate;
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

    public String failureMeaning() {
        return failureMeaning;
    }

    public String repairAction() {
        return repairAction;
    }

    public String artifactToRegenerate() {
        return artifactToRegenerate;
    }

    @Override
    public String toString() {
        String location = path == null ? "" : " [" + path + "]";
        String meaning = failureMeaning == null ? "" : " Meaning: " + failureMeaning + ".";
        String repair = repairAction == null ? "" : " Repair: " + repairAction + ".";
        String artifact = artifactToRegenerate == null ? "" : " Regenerate: " + artifactToRegenerate + ".";
        return severity + " " + code + ": " + message + location + meaning + repair + artifact;
    }
}
