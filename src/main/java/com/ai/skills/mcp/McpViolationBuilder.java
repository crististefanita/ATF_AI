package com.ai.skills.mcp;

import java.nio.file.Path;

public final class McpViolationBuilder {
    private final McpViolationSeverity severity;
    private final String code;
    private String message;
    private Path path;
    private String failureMeaning;
    private String repairAction;
    private String artifactToRegenerate;

    private McpViolationBuilder(McpViolationSeverity severity, String code) {
        this.severity = severity;
        this.code = code;
    }

    public static McpViolationBuilder blocker(String code) {
        return new McpViolationBuilder(McpViolationSeverity.BLOCKER, code);
    }

    public static McpViolationBuilder warning(String code) {
        return new McpViolationBuilder(McpViolationSeverity.WARNING, code);
    }

    public static McpViolationBuilder recommendation(String code) {
        return new McpViolationBuilder(McpViolationSeverity.RECOMMENDATION, code);
    }

    public McpViolationBuilder message(String message) {
        this.message = message;
        return this;
    }

    public McpViolationBuilder path(Path path) {
        this.path = path;
        return this;
    }

    public McpViolationBuilder meaning(String failureMeaning) {
        this.failureMeaning = failureMeaning;
        return this;
    }

    public McpViolationBuilder repair(String repairAction) {
        this.repairAction = repairAction;
        return this;
    }

    public McpViolationBuilder artifact(String artifactToRegenerate) {
        this.artifactToRegenerate = artifactToRegenerate;
        return this;
    }

    public McpPackageViolation build() {
        return new McpPackageViolation(severity, code, message, path,
                failureMeaning, repairAction, artifactToRegenerate);
    }
}
