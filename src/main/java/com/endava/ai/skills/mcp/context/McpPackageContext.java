package com.endava.ai.skills.mcp.context;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpRuntimeContract;
import com.endava.ai.skills.mcp.fs.FileTextReader;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;

@Getter
@Accessors(fluent = true)
public final class McpPackageContext {
    private final Path projectRoot;
    private final Path packageRoot;
    private final McpRuntimeContract runtime;
    private final McpPackagePaths paths;
    private final FileTextReader files;
    private final McpSourceAuditScope sourceAuditScope;

    private McpPackageContext(Path projectRoot, Path packageRoot, McpRuntimeContract runtime) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.packageRoot = packageRoot.toAbsolutePath().normalize();
        this.runtime = runtime;
        this.paths = new McpPackagePaths(this.packageRoot);
        this.files = new FileTextReader();
        this.sourceAuditScope = McpSourceAuditScope.of(this.projectRoot, this.packageRoot);
    }

    public static McpPackageContext load(Path projectRoot, Path packageRoot, McpPackageValidationResult result) {
        Path normalizedProjectRoot = projectRoot.toAbsolutePath().normalize();
        McpRuntimeContract runtime = McpRuntimeContract.load(normalizedProjectRoot, result);
        return new McpPackageContext(normalizedProjectRoot, packageRoot, runtime);
    }

}
