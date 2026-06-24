package com.endava.ai.mcp.validation;

import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpRuntimeContract;
import com.endava.ai.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Path;

public final class McpPackageAuditContext {
    private final Path packageRoot;
    private final McpPackageValidationResult result;
    private final McpRuntimeContract runtime;

    private McpPackageAuditContext(Path packageRoot, McpPackageValidationResult result, McpRuntimeContract runtime) {
        this.packageRoot = packageRoot;
        this.result = result;
        this.runtime = runtime;
    }

    public static McpPackageAuditContext load() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = McpProjectLocator.packageRoot(projectRoot);
        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);
        McpRuntimeContract runtime = McpRuntimeContract.load(projectRoot, new McpPackageValidationResult());
        return new McpPackageAuditContext(packageRoot, result, runtime);
    }

    public Path packageRoot() {
        return packageRoot;
    }

    public McpPackageValidationResult result() {
        return result;
    }

    public McpRuntimeContract runtime() {
        return runtime;
    }

    public String runtimeSummary() {
        return "Runtime values: RUN_EXPECTED_DELTA_QUALITY=" + runtime.expectedDeltaQuality()
                + " | ROUND_TARGET_TEST_COUNT=" + runtime.roundTargetTestCount()
                + " | META_ITERATION_COUNT=" + runtime.metaIterationCount();
    }

    public String runtimeSourceRelative() {
        return relative(runtime.source());
    }

    public String relative(Path path) {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.startsWith(projectRoot)) {
            return projectRoot.relativize(normalized).toString().replace('\\', '/');
        }
        if (normalized.startsWith(packageRoot)) {
            return packageRoot.relativize(normalized).toString().replace('\\', '/');
        }
        return path.toString().replace('\\', '/');
    }
}
