package com.endava.ai.skills.mcp.tests.contracts;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.context.McpPackageReadme;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-package-readme-contract-test")
public class McpPackageReadmeContractTest {
    @Test
    @McpCheckRef("mcp.package-readme.live-run-layout")
    public void liveRunLayoutUsesDocsOutReadmeSibling() throws IOException {
        Path docsOut = Files.createTempDirectory("mcp-docs-out-");
        Path packageRoot = docsOut.resolve("2026-07-01_10.00.00");
        Files.createDirectories(packageRoot);
        Path readme = docsOut.resolve("README.md");
        Files.writeString(readme, "Cold Overall package score: 94/100");

        Assert.assertEquals(McpPackageReadme.canonicalFor(packageRoot), readme);
    }

    @Test
    @McpCheckRef("mcp.package-readme.html-ex-layout")
    public void htmlExampleLayoutUsesReadmeInsideCopiedPackage() throws IOException {
        Path packageRoot = Files.createTempDirectory("mcp-html-ex-example-");
        Path packageReadme = packageRoot.resolve("README.md");
        Files.writeString(packageReadme, "Cold Overall remains: 94/100");
        Files.writeString(packageRoot.resolveSibling("README.md"), "# HTML Example Library");

        Assert.assertEquals(McpPackageReadme.canonicalFor(packageRoot), packageReadme);
    }
}
