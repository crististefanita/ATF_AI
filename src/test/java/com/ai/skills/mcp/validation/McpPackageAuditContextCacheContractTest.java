package com.ai.skills.mcp.validation;

import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpRuntimeContract;
import com.ai.skills.mcp.context.McpProjectLocator;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-package-audit-context-cache-contract-test")
public class McpPackageAuditContextCacheContractTest {

    @AfterMethod(alwaysRun = true)
    public void resetContextCache() {
        McpPackageAuditContext.resetCacheForTests();
        System.clearProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY);
        System.clearProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY);
        System.clearProperty(McpProjectLocator.REQUIRE_EXPLICIT_PACKAGE_ROOT_PROPERTY);
    }

    @Test
    @McpCheckRef("mcp.package-audit.context-cache.reuses-snapshot")
    public void repeatedLatestPackageAssertionsReuseOneAuditSnapshot() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-context-cache-project-");
        Path packageRoot = Files.createTempDirectory("mcp-context-cache-package-");
        System.setProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, projectRoot.toString());
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, packageRoot.toString());
        AtomicInteger loads = new AtomicInteger();
        McpPackageAuditContext.setLoaderForTests((project, packagePath) -> {
            loads.incrementAndGet();
            return snapshot(packagePath);
        });

        McpPackageAuditValidation first = McpPackageAuditValidation.latestPackage();
        McpPackageAuditValidation second = McpPackageAuditValidation.latestPackage();
        McpPackageAuditValidation third = McpPackageAuditValidation.latestPackage();

        Assert.assertSame(second.context, first.context,
                "Repeated package-audit assertions in the same Maven close-out must reuse one loaded audit context.");
        Assert.assertSame(third.context, first.context,
                "The cache prevents each @Test method from reparsing and revalidating the full package.");
        Assert.assertEquals(loads.get(), 1,
                "A close-out suite should validate the package once, then run focused assertions over that snapshot.");
    }

    @Test
    @McpCheckRef("mcp.package-audit.context-cache.keyed-by-root")
    public void cacheReloadsWhenTheAuditedPackageRootChanges() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-context-cache-project-");
        Path firstPackage = Files.createTempDirectory("mcp-context-cache-package-a-");
        Path secondPackage = Files.createTempDirectory("mcp-context-cache-package-b-");
        System.setProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, projectRoot.toString());
        AtomicInteger loads = new AtomicInteger();
        McpPackageAuditContext.setLoaderForTests((project, packagePath) -> {
            loads.incrementAndGet();
            return snapshot(packagePath);
        });

        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, firstPackage.toString());
        McpPackageAuditContext first = McpPackageAuditValidation.latestPackage().context;
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, secondPackage.toString());
        McpPackageAuditContext second = McpPackageAuditValidation.latestPackage().context;

        Assert.assertNotSame(second, first,
                "Changing -Dmcp.package.root must force a new audit snapshot, not reuse stale evidence.");
        Assert.assertEquals(second.packageRoot(), secondPackage.toAbsolutePath().normalize());
        Assert.assertEquals(loads.get(), 2,
                "The cache key is project root plus package root, so different packages are independently validated.");
    }

    @Test
    @McpCheckRef("mcp.package-audit.context-cache.reset-after-artifact-sync")
    public void explicitResetForcesReloadAfterArtifactSynchronization() throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-context-cache-project-");
        Path packageRoot = Files.createTempDirectory("mcp-context-cache-package-");
        System.setProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, projectRoot.toString());
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, packageRoot.toString());
        AtomicInteger loads = new AtomicInteger();
        McpPackageAuditContext.setLoaderForTests((project, packagePath) -> {
            loads.incrementAndGet();
            return snapshot(packagePath);
        });

        McpPackageAuditContext first = McpPackageAuditValidation.latestPackage().context;
        McpPackageAuditContext.resetCacheForTests();
        McpPackageAuditContext.setLoaderForTests((project, packagePath) -> {
            loads.incrementAndGet();
            return snapshot(packagePath);
        });
        McpPackageAuditContext second = McpPackageAuditValidation.latestPackage().context;

        Assert.assertNotSame(second, first,
                "After validation/mcp artifacts are rewritten in the same JVM, reset must force a fresh package audit.");
        Assert.assertEquals(loads.get(), 2,
                "The reset hook documents the safe escape hatch for artifact synchronization tools and focused tests.");
    }

    private static McpPackageAuditContext snapshot(Path packageRoot) {
        McpRuntimeContract runtime = McpRuntimeContract.load(Path.of("."), new McpPackageValidationResult());
        return McpPackageAuditContext.snapshotForTests(
                packageRoot.toAbsolutePath().normalize(),
                new McpPackageValidationResult(),
                runtime);
    }
}
