package com.endava.ai.skills.mcp.tests.core;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpGeneratedPackageValidator;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpViolationSeverity;
import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpProjectLocator;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.fs.DiffFileExtractor;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-project-locator-contract-test")
public class McpProjectLocatorContractTest {
    private static final String EXAMPLE_UI_TEST_SOURCE = AtfSourcePaths.unixPath(
            AtfSourcePaths.TEST_UI_TESTS_ROOT.resolve("ExampleTest.java"));
    private static final String MISSING_UI_TEST_SOURCE = AtfSourcePaths.unixPath(
            AtfSourcePaths.TEST_UI_TESTS_ROOT.resolve("MissingUiTests.java"));

    @AfterMethod(alwaysRun = true)
    public void restorePackageRootProperties() {
        System.clearProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY);
        System.clearProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY);
        System.clearProperty(McpProjectLocator.REQUIRE_EXPLICIT_PACKAGE_ROOT_PROPERTY);
        System.clearProperty(McpPackageAuditAssumptions.ALLOW_LATEST_FALLBACK_PROPERTY);
    }

    @Test
    @McpCheckRef("mcp.package-root.local-latest-fallback")
    public void packageRootAllowsLatestFallbackForLocalDeveloperConvenience() throws IOException {
        Path projectRoot = temporaryProjectWithRuns("2026-06-25_09.35.15", "2026-06-26_10.00.00");

        Path packageRoot = McpProjectLocator.packageRoot(projectRoot);

        Assert.assertTrue(packageRoot.endsWith(Path.of("docs/out/2026-06-26_10.00.00")),
                "Latest fallback should select the newest timestamp root for local developer runs.");
    }

    @Test
    @McpCheckRef("diff.extractor.quoted-windows-paths")
    public void quotedWindowsGitPathsAreExtractedAndNormalized() {
        String diff = "diff --git \"a/src\\\\test\\\\java\\\\com\\\\endava\\\\ai\\\\ui\\\\tests\\\\ExampleTest.java\" "
                + "\"b/src\\\\test\\\\java\\\\com\\\\endava\\\\ai\\\\ui\\\\tests\\\\ExampleTest.java\"\n"
                + "--- /dev/null\n"
                + "+++ \"b/src\\\\test\\\\java\\\\com\\\\endava\\\\ai\\\\ui\\\\tests\\\\ExampleTest.java\"\n";

        Set<String> files = new DiffFileExtractor().referencedFiles(diff);

        Assert.assertTrue(files.contains(EXAMPLE_UI_TEST_SOURCE),
                "Expected normalized Java path from quoted Windows git diff, actual=" + files);
        Assert.assertFalse(files.contains("/dev/null"), "Diff parser must ignore /dev/null.");
    }

    @Test
    @McpCheckRef("mcp.package-root.strict-explicit-required")
    public void packageRootRejectsLatestFallbackWhenStrictModeIsEnabled() throws IOException {
        Path projectRoot = temporaryProjectWithRuns("2026-06-25_09.35.15");
        System.setProperty(McpProjectLocator.REQUIRE_EXPLICIT_PACKAGE_ROOT_PROPERTY, "true");

        IllegalStateException error = Assert.expectThrows(
                IllegalStateException.class,
                () -> McpProjectLocator.packageRoot(projectRoot));

        Assert.assertTrue(error.getMessage().contains("-D" + McpProjectLocator.PACKAGE_ROOT_PROPERTY),
                "Strict mode error must tell the gate how to publish the audited package root.");
    }

    @Test
    @McpCheckRef("mcp.package-root.strict-explicit-accepted")
    public void packageRootUsesExplicitRootEvenWhenStrictModeIsEnabled() throws IOException {
        Path projectRoot = temporaryProjectWithRuns("2026-06-25_09.35.15", "2026-06-26_10.00.00");
        System.setProperty(McpProjectLocator.REQUIRE_EXPLICIT_PACKAGE_ROOT_PROPERTY, "true");
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, "docs/out/2026-06-25_09.35.15");

        Path packageRoot = McpProjectLocator.packageRoot(projectRoot);

        Assert.assertTrue(packageRoot.endsWith(Path.of("docs/out/2026-06-25_09.35.15")),
                "Explicit package root should win over newest latest directory in strict mode.");
    }

    @Test
    @McpCheckRef("mcp.external-package.source-root-required")
    public void externalPackageWithoutExplicitProjectRootDoesNotPretendSourcePresenceIsDefinitive() throws IOException {
        Path projectRoot = temporaryProjectWithRuns("2026-06-25_09.35.15");
        Path packageRoot = temporaryExternalPackageWithRootDiff(MISSING_UI_TEST_SOURCE);

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .forProfile(projectRoot, McpProfile.UNIVERSAL)
                .validate(packageRoot);

        Assert.assertTrue(hasViolation(result, "root-diff.source-file.requires-source-root", McpViolationSeverity.WARNING),
                "External package audit should request the audited source root instead of emitting definitive source-missing blockers. "
                        + result.violations());
        Assert.assertFalse(hasViolation(result, "root-diff.source-file-missing", McpViolationSeverity.BLOCKER),
                "External package audit without explicit source root must not pretend the validator workspace is the audited project. "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.external-package.explicit-source-root-definitive")
    public void externalPackageWithExplicitProjectRootKeepsSourceMissingAsBlocker() throws IOException {
        Path projectRoot = temporaryProjectWithRuns("2026-06-25_09.35.15");
        System.setProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, projectRoot.toString());
        Path packageRoot = temporaryExternalPackageWithRootDiff(MISSING_UI_TEST_SOURCE);

        McpPackageValidationResult result = McpGeneratedPackageValidator
                .forProfile(projectRoot, McpProfile.UNIVERSAL)
                .validate(packageRoot);

        Assert.assertTrue(hasViolation(result, "root-diff.source-file-missing", McpViolationSeverity.BLOCKER),
                "Explicit audited project root should make missing source files definitive blockers. "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.package-audit.cross-project-preflight")
    public void packageAuditPreflightDetectsExternalPackageWithoutAuditedSourceRoot() throws IOException {
        Path validatorProject = temporaryProjectWithRuns("2026-06-25_09.35.15");
        Path externalPackage = Files.createTempDirectory("mcp-external-package-preflight-");
        System.setProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, validatorProject.toString());
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, externalPackage.toString());

        Assert.assertFalse(McpPackageAuditAssumptions.hasExternalPackageAuditWithoutSourceRoot(),
                "An explicit project root means the external package has an audited source root.");

        System.clearProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY);
        System.setProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, externalPackage.toString());

        Assert.assertTrue(McpPackageAuditAssumptions.hasExternalPackageAuditWithoutSourceRoot(),
                "External package roots without -Dmcp.project.root must stop at package-audit preflight.");
        Assert.assertTrue(
                McpPackageAuditAssumptions.sourceRootRequiredMessage().contains("-D" + McpProjectLocator.PROJECT_ROOT_PROPERTY),
                "Preflight diagnostic must tell the operator how to provide the audited source root.");
    }

    @Test
    @McpCheckRef("mcp.source-audit-scope.rule-guardrail")
    public void sourceDependentRulesDoNotReadLiveSourceFromValidatorProjectRoot() throws IOException {
        Path[] sourceDependentRules = {
                AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.source.RealJavaTestCountabilityRule"),
                AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.ui.RealUiInteractionRule"),
                AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.ui.NoFrameworkReportingInGeneratedUiTestsRule"),
                AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.ui.NoDirectUiInfrastructureRule"),
                AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.rules.business.ClaimSourceActionAlignmentRule")
        };

        for (Path rule : sourceDependentRules) {
            String source = Files.readString(rule);
            Assert.assertFalse(source.contains("context.projectRoot()"),
                    rule + " must not resolve audited source files from the validator project root.");
            Assert.assertTrue(source.contains("sourceAuditScope()"),
                    rule + " must route audited source access through McpSourceAuditScope.");
        }
    }

    private static Path temporaryProjectWithRuns(String... runNames) throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-project-locator-contract-");
        Path outRoot = projectRoot.resolve("docs/out");
        Files.createDirectories(outRoot);
        for (String runName : runNames) {
            Files.createDirectories(outRoot.resolve(runName));
        }
        return projectRoot;
    }

    private static Path temporaryExternalPackageWithRootDiff(String sourcePath) throws IOException {
        Path packageRoot = Files.createTempDirectory("mcp-external-package-contract-");
        Path rootDiff = packageRoot.resolve(McpArtifactPaths.ROOT_DIFF);
        Files.createDirectories(rootDiff.getParent());
        Files.writeString(rootDiff, "diff --git a/" + sourcePath + " b/" + sourcePath + "\n"
                + "--- a/" + sourcePath + "\n"
                + "+++ b/" + sourcePath + "\n"
                + "+// generated source\n");
        return packageRoot;
    }

    private static boolean hasViolation(McpPackageValidationResult result,
                                        String code,
                                        McpViolationSeverity severity) {
        return result.violations().stream()
                .anyMatch(violation -> violation.code().equals(code) && violation.severity() == severity);
    }
}
