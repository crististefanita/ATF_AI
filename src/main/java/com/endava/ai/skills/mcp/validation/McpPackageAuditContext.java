package com.endava.ai.skills.mcp.validation;

import com.endava.ai.skills.mcp.McpGeneratedPackageValidator;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpRuntimeContract;
import com.endava.ai.skills.mcp.context.McpProjectLocator;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.nio.file.Path;

@Getter
@Accessors(fluent = true)
public final class McpPackageAuditContext {
    private static final Object CACHE_LOCK = new Object();
    private static AuditContextLoader loader = McpPackageAuditContext::loadUncached;
    private static CacheKey cachedKey;
    private static McpPackageAuditContext cachedContext;

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
        CacheKey key = new CacheKey(projectRoot, packageRoot);
        synchronized (CACHE_LOCK) {
            if (key.equals(cachedKey) && cachedContext != null) {
                return cachedContext;
            }
            McpPackageAuditContext loaded = loader.load(projectRoot, packageRoot);
            cachedKey = key;
            cachedContext = loaded;
            return loaded;
        }
    }

    public static McpPackageAuditContext loadFresh() throws IOException {
        Path projectRoot = McpProjectLocator.projectRoot();
        Path packageRoot = McpProjectLocator.packageRoot(projectRoot);
        synchronized (CACHE_LOCK) {
            McpPackageAuditContext loaded = loader.load(projectRoot, packageRoot);
            cachedKey = new CacheKey(projectRoot, packageRoot);
            cachedContext = loaded;
            return loaded;
        }
    }

    static void resetCacheForTests() {
        synchronized (CACHE_LOCK) {
            cachedKey = null;
            cachedContext = null;
            loader = McpPackageAuditContext::loadUncached;
        }
    }

    static void setLoaderForTests(AuditContextLoader testLoader) {
        synchronized (CACHE_LOCK) {
            cachedKey = null;
            cachedContext = null;
            loader = testLoader == null ? McpPackageAuditContext::loadUncached : testLoader;
        }
    }

    static McpPackageAuditContext snapshotForTests(Path packageRoot,
                                                   McpPackageValidationResult result,
                                                   McpRuntimeContract runtime) {
        return new McpPackageAuditContext(packageRoot, result, runtime);
    }

    private static McpPackageAuditContext loadUncached(Path projectRoot, Path packageRoot) {
        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);
        McpRuntimeContract runtime = McpRuntimeContract.load(projectRoot, new McpPackageValidationResult());
        return new McpPackageAuditContext(packageRoot, result, runtime);
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

    interface AuditContextLoader {
        McpPackageAuditContext load(Path projectRoot, Path packageRoot) throws IOException;
    }

    private static final class CacheKey {
        private final Path projectRoot;
        private final Path packageRoot;

        private CacheKey(Path projectRoot, Path packageRoot) {
            this.projectRoot = projectRoot.toAbsolutePath().normalize();
            this.packageRoot = packageRoot.toAbsolutePath().normalize();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheKey)) {
                return false;
            }
            CacheKey that = (CacheKey) other;
            return projectRoot.equals(that.projectRoot) && packageRoot.equals(that.packageRoot);
        }

        @Override
        public int hashCode() {
            return 31 * projectRoot.hashCode() + packageRoot.hashCode();
        }
    }
}
