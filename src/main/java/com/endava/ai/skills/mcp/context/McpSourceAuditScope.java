package com.endava.ai.skills.mcp.context;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;

@Getter
@Accessors(fluent = true)
public final class McpSourceAuditScope {
    private final Path sourceRoot;
    private final Path packageRoot;
    private final boolean explicitSourceRoot;

    private McpSourceAuditScope(Path sourceRoot, Path packageRoot, boolean explicitSourceRoot) {
        this.sourceRoot = sourceRoot.toAbsolutePath().normalize();
        this.packageRoot = packageRoot.toAbsolutePath().normalize();
        this.explicitSourceRoot = explicitSourceRoot;
    }

    public static McpSourceAuditScope of(Path sourceRoot, Path packageRoot) {
        return new McpSourceAuditScope(sourceRoot, packageRoot, McpProjectLocator.hasExplicitProjectRoot());
    }

    public boolean canProveSourcePresence() {
        return explicitSourceRoot || packageRoot.startsWith(sourceRoot.resolve("docs/out").normalize());
    }

    public boolean requiresExplicitSourceRoot() {
        return !canProveSourcePresence();
    }

    public String sourceRootDescription() {
        return sourceRoot.toString();
    }

    public String packageRootDescription() {
        return packageRoot.toString();
    }
}
