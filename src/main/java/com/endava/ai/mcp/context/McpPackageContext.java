package com.endava.ai.mcp.context;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpRuntimeContract;
import com.endava.ai.mcp.fs.FileTextReader;

import java.nio.file.Path;

public final class McpPackageContext {
    private final Path projectRoot;
    private final Path packageRoot;
    private final McpRuntimeContract runtime;
    private final McpPackagePaths paths;
    private final FileTextReader files;

    private McpPackageContext(Path projectRoot, Path packageRoot, McpRuntimeContract runtime) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.packageRoot = packageRoot.toAbsolutePath().normalize();
        this.runtime = runtime;
        this.paths = new McpPackagePaths(this.packageRoot);
        this.files = new FileTextReader();
    }

    public static McpPackageContext load(Path projectRoot, Path packageRoot, McpPackageValidationResult result) {
        Path normalizedProjectRoot = projectRoot.toAbsolutePath().normalize();
        McpRuntimeContract runtime = McpRuntimeContract.load(normalizedProjectRoot, result);
        return new McpPackageContext(normalizedProjectRoot, packageRoot, runtime);
    }

    public Path projectRoot() {
        return projectRoot;
    }

    public Path packageRoot() {
        return packageRoot;
    }

    public McpRuntimeContract runtime() {
        return runtime;
    }

    public McpPackagePaths paths() {
        return paths;
    }

    public FileTextReader files() {
        return files;
    }
}
